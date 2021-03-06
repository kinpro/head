package org.mifos.ui.core.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.mifos.core.MifosRuntimeException;
import org.mifos.dto.screen.UploadedFileDto;
import org.mifos.framework.fileupload.service.ClientFileService;
import org.mifos.framework.fileupload.service.FileService;
import org.mifos.framework.fileupload.service.LoanFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes("formBean")
public class ViewUploadedFilesController {
    
    private enum EntityType {
        CLIENT, LOAN
    }

    private static final String REDIRECT_TO_UPLOADED_FILES_LIST = "redirect:/viewUploadedFiles.ftl?";
    private static final String UPLOADED_FILES_LIST_REQUEST_PARAMETERS = "entityId=%s&entityType=%s&backPageUrl=%s";
    
    private FileService fileService;

    @Autowired
    private ClientFileService clientFileService;
    
    @Autowired
    private LoanFileService loanFileService;

    protected ViewUploadedFilesController() {
        // for spring autowiring
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder, HttpSession session) {
        binder.setValidator(new UploadFileFormValidator());
    }

    @RequestMapping(value = "/viewUploadedFiles", method = RequestMethod.GET)
    public ModelAndView showUploadedFiles(HttpServletRequest request, HttpServletResponse response) {
        Integer entityId = Integer.parseInt(request.getParameter("entityId"));
        EntityType entityType = EntityType.valueOf(request.getParameter("entityType"));
        String backPageUrl = request.getParameter("backPageUrl");
        setFileService(entityType);
        
        List<UploadedFileDto> uploadedFiles = null;

        if (StringUtils.isNotBlank(request.getParameter("downloadFileId"))) {
            Long fileId = Long.parseLong(request.getParameter("downloadFileId"));
            UploadedFileDto uploadedFileDto = fileService.read(fileId);
            response.setContentType(uploadedFileDto.getContentType());
            response.setContentLength(uploadedFileDto.getSize());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + uploadedFileDto.getName() + "\"");
            try {
                response.getOutputStream().write(fileService.getData(uploadedFileDto));
            } catch (IOException e) {
                throw new MifosRuntimeException(e);
            }
        } else if (StringUtils.isNotBlank(request.getParameter("deleteFileId"))) {
            Long fileId = Long.parseLong(request.getParameter("deleteFileId"));
            Integer clientId = Integer.parseInt(request.getParameter("entityId"));
            fileService.delete(clientId, fileId);
        }

        uploadedFiles = fileService.readAll(Integer.parseInt(request.getParameter("entityId")));
        
        for (UploadedFileDto uploadedFileDto : uploadedFiles) {
            if (!fileService.checkIfFileExists(entityId, uploadedFileDto.getName())) {
                uploadedFileDto.setExistOnDisk(false);
            }
        }

        ModelAndView modelAndView = new ModelAndView("viewUploadedFiles");
        modelAndView.addObject("entityId", String.valueOf(entityId));
        modelAndView.addObject("entityType", entityType.toString());
        modelAndView.addObject("backPageUrl", backPageUrl);
        modelAndView.addObject("uploadedFiles", uploadedFiles);
        return modelAndView;
    }

    @RequestMapping(value = "/uploadNewFile", method = RequestMethod.GET)
    public ModelAndView uploadNewFile(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("uploadNewFile");
        UploadedFileFormBean uploadedFileFormBean = new UploadedFileFormBean();
        modelAndView.addObject("entityId", request.getParameter("entityId"));
        modelAndView.addObject("entityType", request.getParameter("entityType"));
        modelAndView.addObject("backPageUrl", request.getParameter("backPageUrl"));
        modelAndView.addObject("formBean", uploadedFileFormBean);
        return modelAndView;
    }

    @RequestMapping(value = "/uploadNewFile", method = RequestMethod.POST)
    public ModelAndView showPreview(HttpServletRequest request,
            @ModelAttribute("formBean") @Valid UploadedFileFormBean formBean, BindingResult result, SessionStatus status) {

        ModelAndView modelAndView = new ModelAndView("redirect:/home.ftl");
        Integer entityId = Integer.parseInt(request.getParameter("entityId"));
        EntityType entityType = EntityType.valueOf(request.getParameter("entityType"));
        String backPageUrl = request.getParameter("backPageUrl");
        setFileService(entityType);

        if (StringUtils.isNotBlank(request.getParameter("CANCEL"))) {
            String backUrl = REDIRECT_TO_UPLOADED_FILES_LIST
                    + String.format(UPLOADED_FILES_LIST_REQUEST_PARAMETERS, request.getParameter("entityId"),
                            request.getParameter("entityType"), request.getParameter("backPageUrl"));
            modelAndView.setViewName(backUrl);
            status.setComplete();
        } else {
            if (result.hasErrors()) {
                modelAndView.setViewName("uploadNewFile");
            } else {
                modelAndView.setViewName("uploadNewFilePreview");
                modelAndView.addObject("fileExists", fileService.checkIfFileExists(entityId, formBean.getFile().getOriginalFilename()));
            }
            modelAndView.addObject("formBean", formBean);
            modelAndView.addObject("entityId", String.valueOf(entityId));
            modelAndView.addObject("entityType", entityType.toString());
            modelAndView.addObject("backPageUrl", backPageUrl);
        }
        return modelAndView;
    }

    @RequestMapping(value = "/uploadNewFilePreview", method = RequestMethod.POST)
    public ModelAndView formSubmit(HttpServletRequest request,
            @ModelAttribute("formBean") UploadedFileFormBean formBean, SessionStatus status) {
        ModelAndView modelAndView = new ModelAndView("redirect:/home.ftl");
        Integer entityId = Integer.parseInt(request.getParameter("entityId"));
        EntityType entityType = EntityType.valueOf(request.getParameter("entityType"));
        String backPageUrl = request.getParameter("backPageUrl");
        setFileService(entityType);
        try {
            if (!StringUtils.isNotBlank(request.getParameter("CANCEL"))) {
                UploadedFileDto uploadedFileDto = new UploadedFileDto(formBean.getFile().getOriginalFilename(),
                        formBean.getFile().getContentType(), (int) formBean.getFile().getSize(),
                        formBean.getDescription());
                fileService.create(entityId, formBean.getFile().getInputStream(), uploadedFileDto);
            }
            String backUrl = REDIRECT_TO_UPLOADED_FILES_LIST
                    + String.format(UPLOADED_FILES_LIST_REQUEST_PARAMETERS, entityId.toString(),
                            entityType.toString(), backPageUrl);
            modelAndView.setViewName(backUrl);
            status.setComplete();
        } catch (IOException e) {
            throw new MifosRuntimeException(e);
        }
        return modelAndView;
    }
    
    private void setFileService(EntityType entityType) {
        switch(entityType) {
        case CLIENT:
            fileService = clientFileService;
            break;
        case LOAN:
            fileService = loanFileService;
            break;
        default:
            break;
        }
    }
}
