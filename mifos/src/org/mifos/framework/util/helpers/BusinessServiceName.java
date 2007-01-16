package org.mifos.framework.util.helpers;

import org.mifos.application.accounts.business.service.AccountBusinessService;
import org.mifos.application.accounts.financial.business.service.FinancialBusinessService;
import org.mifos.application.accounts.loan.business.service.LoanBusinessService;
import org.mifos.application.accounts.savings.business.service.SavingsBusinessService;
import org.mifos.application.admin.business.service.AdminBusinessService;
import org.mifos.application.bulkentry.business.service.BulkEntryBusinessService;
import org.mifos.application.customer.business.service.CustomerBusinessService;
import org.mifos.application.customer.center.business.service.CenterBusinessService;
import org.mifos.application.customer.client.business.service.ClientBusinessService;
import org.mifos.application.customer.group.business.service.GroupBusinessService;
import org.mifos.application.fees.business.service.FeeBusinessService;
import org.mifos.application.fund.business.service.FundBusinessService;
import org.mifos.application.master.business.service.MasterDataService;
import org.mifos.application.meeting.business.service.MeetingBusinessService;
import org.mifos.application.office.business.service.OfficeBusinessService;
import org.mifos.application.office.business.service.OfficeHierarchyBusinessService;
import org.mifos.application.personnel.business.service.PersonnelBusinessService;
import org.mifos.application.productdefinition.business.service.LoanPrdBusinessService;
import org.mifos.application.productdefinition.business.service.SavingsPrdBusinessService;
import org.mifos.application.reports.business.service.ReportsBusinessService;
import org.mifos.application.rolesandpermission.business.service.RolesPermissionsBusinessService;
import org.mifos.framework.components.audit.business.service.AuditBusinessService;

public enum BusinessServiceName {
	Savings(SavingsBusinessService.class),
	Customer(CustomerBusinessService.class),
	MasterDataService(MasterDataService.class),
	BulkEntryService(BulkEntryBusinessService.class),
	Accounts(AccountBusinessService.class),
	SavingsProduct(SavingsPrdBusinessService.class),
	Financial(FinancialBusinessService.class),
	Loan(LoanBusinessService.class),
	ReportsService(ReportsBusinessService.class),
	FeesService(FeeBusinessService.class),
	Personnel(PersonnelBusinessService.class),
	Center(CenterBusinessService.class),
	Client(ClientBusinessService.class),
	Group(GroupBusinessService.class),
	Office(OfficeBusinessService.class),
	LoanProduct(LoanPrdBusinessService.class),
	OfficeHierarchy(OfficeHierarchyBusinessService.class),
	Meeting(MeetingBusinessService.class),
	RolesPermissions(RolesPermissionsBusinessService.class),
	Admin(AdminBusinessService.class),
	fund(FundBusinessService.class),
	AuditLog(AuditBusinessService.class);

	private String name;

	private BusinessServiceName(String name) {
		this.name = name;
	}

	private BusinessServiceName(Class service) {
		this(service.getName());
	}

	public String getName() {
		return name;
	}

}
