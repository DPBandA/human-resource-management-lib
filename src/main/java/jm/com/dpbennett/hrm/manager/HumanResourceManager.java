/*
Human Resource Management (HRM) 
Copyright (C) 2020  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
package jm.com.dpbennett.hrm.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.hrm.Business;
import jm.com.dpbennett.business.entity.hrm.BusinessOffice;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.DepartmentUnit;
import jm.com.dpbennett.business.entity.hrm.Division;
import jm.com.dpbennett.business.entity.hrm.Email;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.EmployeePosition;
import jm.com.dpbennett.business.entity.hrm.Internet;
import jm.com.dpbennett.business.entity.hrm.User;
import jm.com.dpbennett.business.entity.hrm.Laboratory;
import jm.com.dpbennett.business.entity.hrm.Manufacturer;
import jm.com.dpbennett.business.entity.sm.Preference;
import jm.com.dpbennett.business.entity.hrm.Subgroup;
import jm.com.dpbennett.business.entity.sc.MarketProduct;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.hrm.validator.AddressValidator;
import jm.com.dpbennett.hrm.validator.ContactValidator;
import jm.com.dpbennett.sm.Authentication.AuthenticationListener;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.Utils;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Desmond Bennett
 */
public class HumanResourceManager implements Serializable, AuthenticationListener {

    @PersistenceUnit(unitName = "JMTSPU")
    private EntityManagerFactory EMF;
    private String dateSearchField;
    private String dateSearchPeriod;
    private String searchType;
    private Boolean startSearchDateDisabled;
    private Boolean endSearchDateDisabled;
    private Boolean searchTextVisible;
    private Boolean isActiveUsersOnly;
    private Boolean isActiveEmployeesOnly;
    private Boolean isActiveEmployeePositionsOnly;
    private Boolean isActiveDepartmentsOnly;
    private Boolean isActiveBusinessesOnly;
    private Boolean isActiveSubgroupsOnly;
    private Boolean isActiveDivisionsOnly;
    private Boolean isActiveManufacturersOnly;
    private Boolean isActiveEmailsOnly;
    private Date startDate;
    private Date endDate;
    private String searchText;
    private String employeeSearchText;
    private String employeePositionSearchText;
    private String departmentSearchText;
    private String businessSearchText;
    private String subgroupSearchText;
    private String divisionSearchText;
    private String manufacturerSearchText;
    private String emailSearchText;
    private List<Employee> foundEmployees;
    private List<EmployeePosition> foundEmployeePositions;
    private List<Department> foundDepartments;
    private List<Business> foundBusinesses;
    private List<Subgroup> foundSubgroups;
    private List<Division> foundDivisions;
    private List<Manufacturer> foundManufacturers;
    private List<Email> foundEmails;
    private DualListModel<Employee> employeeDualList;
    private DualListModel<Department> departmentDualList;
    private DualListModel<Subgroup> subgroupDualList;
    // Selected objects
    private Employee selectedEmployee;
    private EmployeePosition selectedEmployeePosition;
    private Department selectedDepartment;
    private Subgroup selectedSubgroup;
    private Division selectedDivision;
    private Business selectedBusiness;
    private Email selectedEmail;
    private Contact selectedContact;
    private Address selectedAddress;
    // User related
    private User selectedUser;
    private User foundUser;
    private String userSearchText;
    private List<User> foundUsers;
    private Manufacturer selectedManufacturer;
    private Boolean edit;
    /**
     * Creates a new instance of SystemManager
     */
    public HumanResourceManager() {
        init();
    }

    private void init() {
        reset();

        getSystemManager().addSingleAuthenticationListener(this);
    }

    public List<SelectItem> getManufacturerStatuses() {
        ArrayList statuses = new ArrayList();

        statuses.addAll(getStringListAsSelectItems(getEntityManager(), "factoryStatusList"));

        return statuses;
    }

    public List<SelectItem> getRegistrationStatuses() {
        ArrayList statuses = new ArrayList();

        statuses.addAll(getStringListAsSelectItems(getEntityManager(), "registrationStatusList"));

        return statuses;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public List<SelectItem> getDepartmentLabelList() {

        return getStringListAsSelectItems(getEntityManager(),
                "departmentLabels");
    }

    public List getContactTypes() {

        return getStringListAsSelectItems(getEntityManager(), "personalContactTypes");
    }

    public List<String> completePreferenceValue(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<String> preferenceValues = Preference.findAllPreferenceValues(em, query);

            return preferenceValues;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void closePreferencesDialog2(CloseEvent closeEvent) {
        closePreferencesDialog1(null);
    }

    public void closePreferencesDialog1(ActionEvent actionEvent) {

        PrimeFaces.current().ajax().update("headerForm");
        PrimeFaces.current().executeScript("PF('preferencesDialog').hide();");
    }

    /**
     * Gets the SystemManager object as a session bean.
     *
     * @return
     */
    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public Email getSelectedEmail() {
        return selectedEmail;
    }

    public void setSelectedEmail(Email selectedEmail) {
        this.selectedEmail = selectedEmail;
    }

    public Boolean getIsActiveEmailsOnly() {
        return isActiveEmailsOnly;
    }

    public void setIsActiveEmailsOnly(Boolean isActiveEmailsOnly) {
        this.isActiveEmailsOnly = isActiveEmailsOnly;
    }

    public void updatePrivileges(AjaxBehaviorEvent event) {
        switch (event.getComponent().getId()) {
            // Job Privileges
            case "canEnterJob":
                selectedUser.getPrivilege().
                        setCanEnterDepartmentJob(selectedUser.getPrivilege().getCanEnterJob());
                selectedUser.getPrivilege().
                        setCanEnterOwnJob(selectedUser.getPrivilege().getCanEnterJob());
                break;
            case "canEditJob":
                selectedUser.getPrivilege().setCanEditDepartmentJob(selectedUser.
                        getPrivilege().getCanEditJob());
                selectedUser.getPrivilege().setCanEditOwnJob(selectedUser.getPrivilege().getCanEditJob());
                break;
            case "canEnterDepartmentJob":
            case "canEnterOwnJob":
            case "canEditDepartmentalJob":
            case "canEditOwnJob":
            case "canApproveJobCosting":
            // Organizational Privileges    
            case "canAddClient":
            case "canAddSupplier":
            case "canDeleteClient":
            case "canAddEmployee":
            case "canDeleteEmployee":
            case "canAddDepartment":
            case "canDeleteDepartment":
            case "canBeSuperUser":
                break;
            default:
                break;
        }

        selectedUser.getPrivilege().setIsDirty(true);
    }

    public void updateModuleAccess(AjaxBehaviorEvent event) {
        switch (event.getComponent().getId()) {
            case "canAccessComplianceUnit":
                getSelectedUser().getModules().setComplianceModule(getSelectedUser().
                        getPrivilege().getCanAccessComplianceUnit());
                break;
            case "canAccessCertificationUnit":
                getSelectedUser().getModules().setCertificationModule(getSelectedUser().
                        getPrivilege().getCanAccessCertificationUnit());
                break;
            case "canAccessFoodsUnit":
                getSelectedUser().getModules().setFoodsModule(getSelectedUser().
                        getPrivilege().getCanAccessFoodsUnit());
                break;
            case "canAccessJobManagementUnit":
                getSelectedUser().getModules().setJobManagementAndTrackingModule(getSelectedUser().
                        getPrivilege().getCanAccessJobManagementUnit());
                break;
            case "canAccessLegalMetrologyUnit":
                getSelectedUser().getModules().setLegalMetrologyModule(getSelectedUser().
                        getPrivilege().getCanAccessLegalMetrologyUnit());
                break;
            case "canAccessLegalOfficeUnit":
                getSelectedUser().getModules().setLegalOfficeModule(getSelectedUser().
                        getPrivilege().getCanAccessLegalOfficeUnit());
                break;
            case "canAccessServiceRequestUnit":
                getSelectedUser().getModules().setServiceRequestModule(getSelectedUser().
                        getPrivilege().getCanAccessServiceRequestUnit());
                break;
            case "canAccessStandardsUnit":
                getSelectedUser().getModules().setStandardsModule(getSelectedUser().
                        getPrivilege().getCanAccessStandardsUnit());
                break;
            case "canAccessCRMUnit":
                getSelectedUser().getModules().setCrmModule(getSelectedUser().
                        getPrivilege().getCanAccessCRMUnit());
                break;
            case "canBeFinancialAdministrator":
                getSelectedUser().getModules().setFinancialAdminModule(getSelectedUser().
                        getPrivilege().getCanBeFinancialAdministrator());
                break;
            case "canAccessHRMUnit":
                getSelectedUser().getModules().setHrmModule(getSelectedUser().
                        getPrivilege().getCanAccessHRMUnit());
                break;
            case "canBeJMTSAdministrator":
                getSelectedUser().getModules().setAdminModule(getSelectedUser().
                        getPrivilege().getCanBeJMTSAdministrator());
                break;
            case "canAccessReportUnit":
                getSelectedUser().getModules().setReportModule(getSelectedUser().
                        getPrivilege().getCanAccessReportUnit());
                break;
            default:
                break;

        }

        getSelectedUser().getPrivilege().setIsDirty(true);
        getSelectedUser().getModules().setIsDirty(true);
    }

    public List<User> getFoundUsers() {
        if (foundUsers == null) {
            foundUsers = User.findAllActiveJobManagerUsers(getEntityManager());
        }
        return foundUsers;
    }

    public String getUserSearchText() {
        return userSearchText;
    }

    public void setUserSearchText(String userSearchText) {
        this.userSearchText = userSearchText;
    }

    public void doUserSearch() {

        if (getIsActiveUsersOnly()) {
            foundUsers = User.findActiveJobManagerUsersByName(getEntityManager(), getUserSearchText());
        } else {
            foundUsers = User.findJobManagerUsersByName(getEntityManager(), getUserSearchText());
        }

    }

    public String getFoundUser() {

        if (foundUser != null) {
            return foundUser.getUsername();
        } else {
            foundUser = new User();
            foundUser.setUsername("");

            return foundUser.getUsername();
        }
    }

    public void setFoundUser(String username) {
        foundUser.setUsername(username);
    }

    public void editUser() {
        PrimeFacesUtils.openDialog(getSelectedUser(), "userDialog", true, true, true, 430, 750);
    }

    public User getSelectedUser() {
        // init with current logged on user if null
        if (selectedUser == null) {
            selectedUser = new User();
        }

        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public void cancelUserEdit(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedUser(ActionEvent actionEvent) {

        selectedUser.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void updateSelectedUserEmployee() {
        if (selectedUser.getEmployee() != null) {
            if (selectedUser.getEmployee().getId() != null) {
                selectedUser.setEmployee(Employee.findEmployeeById(getEntityManager(), selectedUser.getEmployee().getId()));
            } else {
                Employee employee = Employee.findDefaultEmployee(getEntityManager(), "--", "--", true);
                if (selectedUser.getEmployee() != null) {
                    selectedUser.setEmployee(employee);
                }
            }
        } else {
            Employee employee = Employee.findDefaultEmployee(getEntityManager(), "--", "--", true);
            if (selectedUser.getEmployee() != null) {
                selectedUser.setEmployee(employee);
            }
        }
    }

    public void updateSelectedUser() {

        EntityManager em = getEntityManager();

        if (selectedUser.getId() != null) {
            selectedUser = User.findJobManagerUserById(em, selectedUser.getId());
        }
    }

    public void updateFoundUser(SelectEvent event) {

        EntityManager em = getEntityManager();

        User u = User.findJobManagerUserByUsername(em, foundUser.getUsername().trim());
        if (u != null) {
            foundUser = u;
            selectedUser = u;
        }
    }

    public List<String> completeUser(String query) {

        try {
            List<User> users = User.findJobManagerUsersByUsername(getEntityManager(), query);
            List<String> suggestions = new ArrayList<>();
            if (users != null) {
                if (!users.isEmpty()) {
                    for (User u : users) {
                        suggestions.add(u.getUsername());
                    }
                }
            }

            return suggestions;
        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void createNewUser() {
        EntityManager em = getEntityManager();

        selectedUser = new User();
        selectedUser.setEmployee(Employee.findDefaultEmployee(em, "--", "--", true));

        editUser();
    }

    public void updateUserPrivilege(ValueChangeEvent event) {
    }

    public void handleUserDialogReturn() {
    }

    public static String getDepartmentFullCode(
            EntityManager em,
            Department department) {

        String code = department.getCode();

        Subgroup subgroup = Subgroup.findByDepartment(em, department);
        if (subgroup != null) {
            code = code + "-" + subgroup.getCode();
            Division division = Division.findBySubgroup(em, subgroup);
            if (division != null) {
                code = code + "-" + division.getCode();
            }
        }

        return code;
    }

    public List<Laboratory> completeLaboratory(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<Laboratory> laboratories = Laboratory.findLaboratoriesByName(em, query);

            return laboratories;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<DepartmentUnit> completeDepartmentUnit(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<DepartmentUnit> departmentUnits = DepartmentUnit.findDepartmentUnitsByName(em, query);

            return departmentUnits;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<EmployeePosition> completeActiveEmployeePosition(String query) {
        EntityManager em;

        try {

            em = getEntityManager();
            List<EmployeePosition> employeePositions
                    = EmployeePosition.findActiveEmployeePositionsByTitle(em, query);

            if (employeePositions != null) {
                return employeePositions;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Employee> completeActiveEmployee(String query) {
        EntityManager em;

        try {

            em = getEntityManager();
            List<Employee> employees = Employee.findActiveEmployeesByName(em, query);

            if (employees != null) {
                return employees;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Department> completeActiveDepartment(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<Department> departments = Department.findActiveDepartmentsByName(em, query);

            return departments;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<BusinessOffice> completeActiveBusinessOffice(String query) {
        EntityManager em;

        try {
            em = getEntityManager();

            List<BusinessOffice> businessOffices = BusinessOffice.findActiveBusinessOfficesByName(em, query);

            return businessOffices;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Boolean getIsActiveEmployeePositionsOnly() {
        return isActiveEmployeePositionsOnly;
    }

    public void setIsActiveEmployeePositionsOnly(Boolean isActiveEmployeePositionsOnly) {
        this.isActiveEmployeePositionsOnly = isActiveEmployeePositionsOnly;
    }

    public List<EmployeePosition> getFoundEmployeePositions() {
        if (foundEmployeePositions == null) {
            foundEmployeePositions = EmployeePosition.findAllActiveEmployeePositions(getEntityManager());
        }
        return foundEmployeePositions;
    }

    public void setFoundEmployeePositions(List<EmployeePosition> foundEmployeePositions) {
        this.foundEmployeePositions = foundEmployeePositions;
    }

    public String getEmployeePositionSearchText() {
        return employeePositionSearchText;
    }

    public void setEmployeePositionSearchText(String employeePositionSearchText) {
        this.employeePositionSearchText = employeePositionSearchText;
    }

    public EmployeePosition getSelectedEmployeePosition() {
        if (selectedEmployeePosition == null) {
            selectedEmployeePosition = new EmployeePosition();
        }
        return selectedEmployeePosition;
    }

    public void setSelectedEmployeePosition(EmployeePosition selectedEmployeePosition) {
        this.selectedEmployeePosition = selectedEmployeePosition;
    }

    public String getApplicationHeader() {
        return "Human Resource Management";
    }

    public MainTabView getMainTabView() {

        return getSystemManager().getMainTabView();
    }

    public List<Division> getFoundDivisions() {
        if (foundDivisions == null) {
            foundDivisions = Division.findAllActive(getEntityManager());
        }
        return foundDivisions;
    }

    public String getDivisionSearchText() {
        return divisionSearchText;
    }

    public void setDivisionSearchText(String divisionSearchText) {
        this.divisionSearchText = divisionSearchText;
    }

    public Boolean getIsActiveDivisionsOnly() {
        return isActiveDivisionsOnly;
    }

    public void setIsActiveDivisionsOnly(Boolean isActiveDivisionsOnly) {
        this.isActiveDivisionsOnly = isActiveDivisionsOnly;
    }

    public DualListModel<Employee> getEmployeeDualList() {
        return employeeDualList;
    }

    public void setEmployeeDualList(DualListModel<Employee> employeeDualList) {
        this.employeeDualList = employeeDualList;
    }

    public DualListModel<Department> getDepartmentDualList() {
        return departmentDualList;
    }

    public void setDepartmentDualList(DualListModel<Department> departmentDualList) {
        this.departmentDualList = departmentDualList;
    }

    public DualListModel<Subgroup> getSubgroupDualList() {
        return subgroupDualList;
    }

    public void setSubgroupDualList(DualListModel<Subgroup> subgroupDualList) {
        this.subgroupDualList = subgroupDualList;
    }

    public String getSubgroupSearchText() {

        return subgroupSearchText;
    }

    public void setSubgroupSearchText(String subgroupSearchText) {
        this.subgroupSearchText = subgroupSearchText;
    }

    public String getBusinessSearchText() {
        if (businessSearchText == null) {
            businessSearchText = "";
        }

        return businessSearchText;
    }

    public void setBusinessSearchText(String businessSearchText) {
        this.businessSearchText = businessSearchText;
    }

    public Business getSelectedBusiness() {
        return selectedBusiness;
    }

    public void setSelectedBusiness(Business selectedBusiness) {
        this.selectedBusiness = selectedBusiness;
    }

    public void reset() {
        searchType = "General";
        dateSearchField = "dateReceived";
        dateSearchPeriod = "thisMonth";
        searchTextVisible = true;
        searchText = "";
        employeeSearchText = "";
        employeePositionSearchText = "";
        departmentSearchText = "";
        subgroupSearchText = "";
        divisionSearchText = "";
        businessSearchText = "";
        manufacturerSearchText = "";
        emailSearchText = "";
        userSearchText = "";
        isActiveUsersOnly = true;
        isActiveEmployeesOnly = true;
        isActiveEmployeePositionsOnly = true;
        isActiveDepartmentsOnly = true;
        isActiveBusinessesOnly = true;
        isActiveSubgroupsOnly = true;
        isActiveDivisionsOnly = true;
        isActiveManufacturersOnly = true;
        isActiveEmailsOnly = true;
    }

    public Boolean getIsActiveDepartmentsOnly() {

        return isActiveDepartmentsOnly;
    }

    public Boolean getIsActiveSubgroupsOnly() {

        return isActiveSubgroupsOnly;
    }

    public void setIsActiveSubgroupsOnly(Boolean isActiveSubgroupsOnly) {
        this.isActiveSubgroupsOnly = isActiveSubgroupsOnly;
    }

    public Boolean getIsActiveBusinessesOnly() {
        return isActiveBusinessesOnly;
    }

    public void setIsActiveBusinessesOnly(Boolean isActiveBusinessesOnly) {
        this.isActiveBusinessesOnly = isActiveBusinessesOnly;
    }

    public void setIsActiveDepartmentsOnly(Boolean isActiveDepartmentsOnly) {
        this.isActiveDepartmentsOnly = isActiveDepartmentsOnly;
    }

    public Boolean getIsActiveUsersOnly() {

        return isActiveUsersOnly;
    }

    public void setIsActiveUsersOnly(Boolean isActiveUsersOnly) {
        this.isActiveUsersOnly = isActiveUsersOnly;
    }

    public Boolean getIsActiveEmployeesOnly() {

        return isActiveEmployeesOnly;
    }

    public void setIsActiveEmployeesOnly(Boolean isActiveEmployeesOnly) {
        this.isActiveEmployeesOnly = isActiveEmployeesOnly;
    }

    public Department getSelectedDepartment() {
        if (selectedDepartment == null) {
            selectedDepartment = new Department();
        }
        return selectedDepartment;
    }

    public void setSelectedDepartment(Department selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }

    public Subgroup getSelectedSubgroup() {
        if (selectedSubgroup == null) {
            selectedSubgroup = new Subgroup();
        }

        return selectedSubgroup;
    }

    public Division getSelectedDivision() {
        return selectedDivision;
    }

    public void setSelectedDivision(Division selectedDivision) {
        this.selectedDivision = selectedDivision;
    }

    public void setSelectedSubgroup(Subgroup selectedSubgroup) {
        this.selectedSubgroup = selectedSubgroup;
    }

    public List<Department> getFoundDepartments() {
        if (foundDepartments == null) {
            foundDepartments = Department.findAllActiveDepartments(getEntityManager());
        }
        return foundDepartments;
    }

    public List<Subgroup> getFoundSubgroups() {
        if (foundSubgroups == null) {
            foundSubgroups = Subgroup.findAllActive(getEntityManager());
        }
        return foundSubgroups;
    }

    public List<Business> getFoundBusinesses() {
        if (foundBusinesses == null) {
            foundBusinesses = Business.findAllBusinesses(getEntityManager());
        }

        return foundBusinesses;
    }

    public void okPickList() {
        closeDialog(null);
    }

    public void addSubgroupDepartmentsDialogReturn() {

        getSelectedSubgroup().setDepartments(departmentDualList.getTarget());

    }

    public void addDivisionDepartmentsDialogReturn() {

        getSelectedDivision().setDepartments(departmentDualList.getTarget());

    }

    public void addDivisionSubgroupsDialogReturn() {

        getSelectedDivision().setSubgroups(subgroupDualList.getTarget());

    }

    public void addBusinessDepartmentsDialogReturn() {

        getSelectedBusiness().setDepartments(departmentDualList.getTarget());

    }

    public void setFoundDepartments(List<Department> foundDepartments) {
        this.foundDepartments = foundDepartments;
    }

    public String getDepartmentSearchText() {
        return departmentSearchText;
    }

    public void setDepartmentSearchText(String departmentSearchText) {
        this.departmentSearchText = departmentSearchText;
    }

    public String getEmployeeSearchText() {
        return employeeSearchText;
    }

    public void setEmployeeSearchText(String employeeSearchText) {
        this.employeeSearchText = employeeSearchText;
    }

    public void doDepartmentSearch() {

        if (getIsActiveDepartmentsOnly()) {
            foundDepartments = Department.findActiveDepartmentsByName(getEntityManager(), getDepartmentSearchText());
        } else {
            foundDepartments = Department.findDepartmentsByName(getEntityManager(), getDepartmentSearchText());
        }

    }

    public void doEmployeePositionSearch() {

        if (getIsActiveEmployeePositionsOnly()) {
            foundEmployeePositions = EmployeePosition.findActiveEmployeePositionsByTitle(getEntityManager(), getEmployeePositionSearchText());
        } else {
            foundEmployeePositions = EmployeePosition.findEmployeePositionsByTitle(getEntityManager(), getEmployeePositionSearchText());
        }

    }

    public List<Email> getFoundEmails() {
        if (foundEmails == null) {
            foundEmails = Email.findAllActiveEmails(getEntityManager());
        }

        return foundEmails;
    }

    public void setFoundEmails(List<Email> foundEmails) {
        this.foundEmails = foundEmails;
    }

    public String getEmailSearchText() {
        return emailSearchText;
    }

    public void setEmailSearchText(String emailSearchText) {
        this.emailSearchText = emailSearchText;
    }

    public void doEmailSearch() {

        if (getIsActiveEmailsOnly()) {
            foundEmails = Email.findActiveEmails(getEntityManager(), getEmailSearchText());
        } else {
            foundEmails = Email.findEmails(getEntityManager(), getEmailSearchText());
        }

    }

    public void editEmailTemplate() {
        PrimeFacesUtils.openDialog(null, "emailTemplateDialog", true, true, true, 550, 700);
    }

    public void createNewEmailTemplate() {

        selectedEmail = new Email();

        editEmailTemplate();
    }

    // tk make system options
    public List getEmailCategories() {
        ArrayList categories = new ArrayList();

        categories.add(new SelectItem("", ""));
        categories.add(new SelectItem("Purchase Requisition", "Purchase Requisition"));
        categories.add(new SelectItem("Job", "Job"));

        return categories;
    }

    // tk make system options
    public List getEmailTypes() {
        ArrayList categories = new ArrayList();

        categories.add(new SelectItem("", ""));
        categories.add(new SelectItem("Template", "Template"));
        categories.add(new SelectItem("Instance", "Instance"));

        return categories;
    }

    // tk make system options
    public List getContentTypes() {
        ArrayList types = new ArrayList();

        types.add(new SelectItem("text/plain", "text/plain"));
        types.add(new SelectItem("text/html", "text/html"));
        types.add(new SelectItem("text/html; charset=utf-8", "text/html; charset=utf-8"));

        return types;
    }

    public void saveSelectedEmail() {

        selectedEmail.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void doSubgroupSearch() {
        foundSubgroups = Subgroup.findAllByName(getEntityManager(), getSubgroupSearchText());
    }

    public void doDivisionSearch() {
        foundDivisions = Division.findAllByName(getEntityManager(), getDivisionSearchText());
    }

    public void doBusinessSearch() {
        foundBusinesses = Business.findBusinessesByName(getEntityManager(), getBusinessSearchText());
    }

    public void doEmployeeSearch() {

        if (getIsActiveEmployeesOnly()) {
            foundEmployees = Employee.findActiveEmployees(getEntityManager(), getEmployeeSearchText());
        } else {
            foundEmployees = Employee.findEmployees(getEntityManager(), getEmployeeSearchText());
        }

    }

    public void onMainViewTabChange(TabChangeEvent event) {
       
    }

    public void openHumanResourceBrowser() {

        getMainTabView().openTab("Human Resource");
    }

    public void editDepartment() {
        PrimeFacesUtils.openDialog(null, "departmentDialog", true, true, true, 600, 800);
    }

    public void editEmployeePosition() {
        PrimeFacesUtils.openDialog(null, "employeePositionDialog", true, true, true, 0, 725);
    }

    public void editSubgroup() {
        PrimeFacesUtils.openDialog(null, "subgroupDialog", true, true, true, 600, 700);
    }

    public void editDivision() {
        PrimeFacesUtils.openDialog(null, "divisionDialog", true, true, true, 600, 700);
    }

    public void editBusiness() {
        PrimeFacesUtils.openDialog(null, "businessDialog", true, true, true, 600, 700);
    }

    public void editEmployee() {

        PrimeFacesUtils.openDialog(null, "employeeDialog", true, true, true, 375, 700);
    }

    public Employee getSelectedEmployee() {

        if (selectedEmployee == null) {
            selectedEmployee = Employee.findDefaultEmployee(getEntityManager(), "--", "--", true);
        }

        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;

        this.selectedEmployee = Employee.findEmployeeById(getEntityManager(), selectedEmployee.getId());

    }

    public Boolean getSearchTextVisible() {
        return searchTextVisible;
    }

    public void setSearchTextVisible(Boolean searchTextVisible) {
        this.searchTextVisible = searchTextVisible;
    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDepartment() {

        selectedDepartment.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedBusiness() {

        selectedBusiness.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedSubgroup() {

        selectedSubgroup.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDivision() {

        selectedDivision.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedEmployee(ActionEvent actionEvent) {

        // Ensure that the employee's fullname is updated
        selectedEmployee.getName();

        selectedEmployee.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedEmployeePosition(ActionEvent actionEvent) {

        selectedEmployeePosition.save(getEntityManager());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void updateSelectedEmployeeDepartment() {
        if (selectedEmployee.getDepartment() != null) {
            if (selectedEmployee.getDepartment().getId() != null) {
                selectedEmployee.setDepartment(Department.findDepartmentById(getEntityManager(), selectedEmployee.getDepartment().getId()));
            } else {
                selectedEmployee.setDepartment(Department.findDefaultDepartment(getEntityManager(), "--"));
            }
        }
    }

    public void updateSelectedDepartmentHead() {
        if (selectedDepartment.getHead().getId() != null) {
            selectedDepartment.setHead(Employee.findEmployeeById(getEntityManager(), selectedDepartment.getHead().getId()));
        } else {
            selectedDepartment.setHead(Employee.findDefaultEmployee(getEntityManager(), "--", "--", true));
        }
    }

    public List getPersonalTitles() {
        return Utils.getPersonalTitles();
    }

    public List getSexes() {
        return Utils.getSexes();
    }

    public void createNewDepartment() {

        selectedDepartment = new Department();

        editDepartment();
    }

    public void createNewEmployeePosition() {

        selectedEmployeePosition = new EmployeePosition();

        editEmployeePosition();
    }

    public void openDepartmentPickListDialog() {
        PrimeFacesUtils.openDialog(null, "departmentPickListDialog", true, true, true, 320, 500);
    }

    public void addSubgroupDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager());
        List<Department> target = selectedSubgroup.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void addDepartmentStaff() {
        List<Employee> source = Employee.findAllActiveEmployees(getEntityManager());
        List<Employee> target = selectedDepartment.getStaff();

        source.removeAll(target);

        employeeDualList = new DualListModel<>(source, target);

        openEmployeePickListDialog();
    }

    public void addDepartmentStaffDialogReturn() {

        getSelectedDepartment().setStaff(employeeDualList.getTarget());

    }

    public void openEmployeePickListDialog() {
        PrimeFacesUtils.openDialog(null, "employeePickListDialog", true, true, true, 320, 500);
    }

    public void addDivisionDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager());
        List<Department> target = selectedDivision.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void addDivisionSubgroups() {
        List<Subgroup> source = Subgroup.findAllActive(getEntityManager());
        List<Subgroup> target = selectedDivision.getSubgroups();

        source.removeAll(target);

        subgroupDualList = new DualListModel<>(source, target);

        openSubgroupPickListDialog();
    }

    public void openSubgroupPickListDialog() {
        PrimeFacesUtils.openDialog(null, "subgroupPickListDialog", true, true, true, 320, 500);
    }

    public void addBusinessDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager());
        List<Department> target = selectedBusiness.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void createNewBusiness() {

        selectedBusiness = new Business();

        PrimeFacesUtils.openDialog(null, "businessDialog", true, true, true, 600, 700);
    }

    public void createNewSubgroup() {

        selectedSubgroup = new Subgroup();

        PrimeFacesUtils.openDialog(null, "subgroupDialog", true, true, true, 600, 700);
    }

    public void createNewEmployee() {

        selectedEmployee = new Employee();

        editEmployee();
    }

    public void createNewDivision() {

        selectedDivision = new Division();

        PrimeFacesUtils.openDialog(null, "divisionDialog", true, true, true, 600, 700);
    }

    public String getDateSearchField() {
        return dateSearchField;
    }

    public void setDateSearchField(String dateSearchField) {
        this.dateSearchField = dateSearchField;
    }

    public String getDateSearchPeriod() {
        return dateSearchPeriod;
    }

    public void setDateSearchPeriod(String dateSearchPeriod) {
        this.dateSearchPeriod = dateSearchPeriod;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getEndSearchDateDisabled() {
        return endSearchDateDisabled;
    }

    public void setEndSearchDateDisabled(Boolean endSearchDateDisabled) {
        this.endSearchDateDisabled = endSearchDateDisabled;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Boolean getStartSearchDateDisabled() {
        return startSearchDateDisabled;
    }

    public void setStartSearchDateDisabled(Boolean startSearchDateDisabled) {
        this.startSearchDateDisabled = startSearchDateDisabled;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public List<BusinessOffice> getBusinessOffices() {
        return BusinessOffice.findAllBusinessOffices(getEntityManager());
    }

    public List<Department> getDepartments() {
        return Department.findAllDepartments(getEntityManager());
    }

    public List<Employee> getEmployees() {
        return Employee.findAllEmployees(getEntityManager());
    }

    public List<Employee> getFoundEmployees() {
        if (foundEmployees == null) {
            foundEmployees = Employee.findAllActiveEmployees(getEntityManager());
        }

        return foundEmployees;
    }

    public void updatePreferences() {
        getUser().save(getEntityManager());
    }

    public void updatePreferedJobTableView(SelectEvent event) {
        getUser().save(getEntityManager());
    }

    public void updateDashboardTabs(AjaxBehaviorEvent event) {

        switch (event.getComponent().getId()) {
            case "jobManagementAndTrackingUnit":
                getSystemManager().getDashboard().addTab("Job Management",
                        getUser().getModules().getJobManagementAndTrackingModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "financialAdminUnit":
                getSystemManager().getDashboard().addTab("Financial Administration",
                        getUser().getModules().getFinancialAdminModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "adminUnit":
                getSystemManager().getDashboard().addTab("System Administration",
                        getUser().getModules().getAdminModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "complianceUnit":
                getSystemManager().getDashboard().addTab("Standards Compliance",
                        getUser().getModules().getComplianceModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "foodsUnit":
                getSystemManager().getDashboard().addTab("Foods Inspectorate",
                        getUser().getModules().getFoodsModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "standardsUnit":
                getSystemManager().getDashboard().addTab("Standards",
                        getUser().getModules().getStandardsModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "certificationUnit":
                getSystemManager().getDashboard().addTab("Certification",
                        getUser().getModules().getCertificationModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "serviceRequestUnit":
                getSystemManager().getDashboard().addTab("Service Request",
                        getUser().getModules().getServiceRequestModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "legalOfficeUnit":
                getSystemManager().getDashboard().addTab("Legal Office",
                        getUser().getModules().getLegalOfficeModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "crmUnit":
                getSystemManager().getDashboard().addTab("Client Management",
                        getUser().getModules().getCrmModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "legalMetrologyUnit":
                getSystemManager().getDashboard().addTab("Legal Metrology",
                        getUser().getModules().getLegalMetrologyModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "hrmUnit":
                getSystemManager().getDashboard().addTab("Human Resource",
                        getUser().getModules().getHrmModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            case "reportUnit":
                getSystemManager().getDashboard().addTab("Report Management",
                        getUser().getModules().getReportModule());
                getUser().getModules().setIsDirty(true);
                getUser().save(getEntityManager());
                break;
            default:
                break;
        }

    }

    public User getUser() {
        return getSystemManager().getUser();
    }

    public EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public void doDefaultSearch() {
        switch (getSystemManager().getDashboard().getSelectedTabId()) {
            case "Human Resource":

                break;
            default:
                break;
        }
    }

    private void initDashboard() {

        if (getUser().getModules().getHrmModule()) {
            getSystemManager().getDashboard().openTab("Human Resource");
        }

    }

    private void initMainTabView() {

        if (getUser().getModules().getHrmModule()) {
            openHumanResourceBrowser();
        }

    }

    @Override
    public void completeLogin() {
        initDashboard();
        initMainTabView();
    }

    @Override
    public void completeLogout() {
        reset();
    }

    // Manufacturer Management
    public Manufacturer getSelectedManufacturer() {
        if (selectedManufacturer == null) {
            return new Manufacturer();
        }
        return selectedManufacturer;
    }

    public void setSelectedManufacturer(Manufacturer selectedManufacturer) {
        this.selectedManufacturer = selectedManufacturer;
    }

    public List<Manufacturer> getFoundManufacturers() {
        if (foundManufacturers == null) {
            foundManufacturers = Manufacturer.findAllActiveManufacturers(getEntityManager());
        }
        return foundManufacturers;
    }

    public void setFoundManufacturers(List<Manufacturer> foundManufacturers) {
        this.foundManufacturers = foundManufacturers;
    }

    public void createNewManufacturer() {
        createNewManufacturer(true);

        editSelectedManufacturer();
    }

    public void createNewManufacturer(Boolean active) {
        selectedManufacturer = new Manufacturer("");
    }

    public void editSelectedManufacturer() {

        PrimeFacesUtils.openDialog(null, "hr/manufacturer/manufacturerDialog", true, true, true, 500, 700);
    }

    public String getManufacturerSearchText() {
        return manufacturerSearchText;
    }

    public void setManufacturerSearchText(String manufacturerSearchText) {
        this.manufacturerSearchText = manufacturerSearchText;
    }

    public Boolean getIsActiveManufacturersOnly() {
        return isActiveManufacturersOnly;
    }

    public void setIsActiveManufacturersOnly(Boolean isActiveManufacturersOnly) {
        this.isActiveManufacturersOnly = isActiveManufacturersOnly;
    }

    public void doManufacturerSearch() {

        if (getIsActiveManufacturersOnly()) {
            foundManufacturers = Manufacturer.findActiveManufacturersByAnyPartOfName(getEntityManager(), manufacturerSearchText);
        } else {
            foundManufacturers = Manufacturer.findManufacturersByAnyPartOfName(getEntityManager(), manufacturerSearchText);
        }
    }

    public void onManufacturerCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager(),
                getFoundManufacturers().get(event.getRowIndex()));
    }

    public int getNumManufacturersFound() {
        return getFoundManufacturers().size();
    }

    public void okManufacturer() {
        Boolean hasValidAddress = false;
        Boolean hasValidContact = false;

        try {

            // Validate 
            // Check for a valid address
            for (Address address : selectedManufacturer.getAddresses()) {
                hasValidAddress = hasValidAddress || AddressValidator.validate(address);
            }
            if (!hasValidAddress) {
                PrimeFacesUtils.addMessage("Address Required",
                        "A valid address was not entered for this manufacturer",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Check for a valid contact
            for (Contact contact : getSelectedManufacturer().getContacts()) {
                hasValidContact = hasValidContact || ContactValidator.validate(contact);
            }
            if (!hasValidContact) {
                PrimeFacesUtils.addMessage("Contact Required",
                        "A valid contact was not entered for this manufacturer",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            // Do save
            if (getSelectedManufacturer().getIsDirty()) {

                getSelectedManufacturer().save(getEntityManager());
                getSelectedManufacturer().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void cancelManufacturerEdit(ActionEvent actionEvent) {

        getSelectedManufacturer().setIsDirty(false);

        // Remove unsaved addresses
        Iterator addressIterator = getSelectedManufacturer().getAddresses().iterator();
        Address address;
        while (addressIterator.hasNext()) {
            address = (Address) addressIterator.next();
            if (address.getId() == null) {
                addressIterator.remove();
            }
        }
        // Remove unsaved contacts
        Iterator contactIterator = getSelectedManufacturer().getContacts().iterator();
        Contact contact;
        while (contactIterator.hasNext()) {
            contact = (Contact) contactIterator.next();
            if (contact.getId() == null) {
                contactIterator.remove();
            }
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updateManufacturer() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void updateManufacturerName(AjaxBehaviorEvent event) {
        selectedManufacturer.setName(selectedManufacturer.getName().trim());

        updateManufacturer();
    }

    public List<MarketProduct> completeActiveMarketProduct(String query) {
        try {
            return MarketProduct.findActiveMarketProductsByAnyPartOfNameOrDescription(
                    getEntityManager(),
                    query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public void createNewAddress() {
        selectedAddress = null;

        // Find an existing invalid or blank address and use it as the neww address
        for (Address address : getSelectedManufacturer().getAddresses()) {
            if (address.getAddressLine1().trim().isEmpty()) {
                selectedAddress = address;
                break;
            }
        }

        // No existing blank or invalid address found so creating new one.
        if (selectedAddress == null) {
            selectedAddress = new Address("", "Billing");
        }

        setEdit(false);

        getSelectedManufacturer().setIsDirty(false);
    }

    public void createNewContact() {
        selectedContact = null;

        for (Contact contact : getSelectedManufacturer().getContacts()) {
            if (contact.getFirstName().trim().isEmpty()) {
                selectedContact = contact;
                break;
            }
        }

        if (selectedContact == null) {
            selectedContact = new Contact("", "", "Main");
            selectedContact.setInternet(new Internet());
        }

        setEdit(false);

        getSelectedManufacturer().setIsDirty(false);
    }

    public Contact getSelectedContact() {
        return selectedContact;
    }

    public void setSelectedContact(Contact selectedContact) {
        this.selectedContact = selectedContact;

        setEdit(true);
    }

    public Address getSelectedAddress() {
        return selectedAddress;
    }

    public void setSelectedAddress(Address selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    public List<Address> getAddressesModel() {
        return getSelectedManufacturer().getAddresses();
    }

    public List<Contact> getContactsModel() {
        return getSelectedManufacturer().getContacts();
    }

    public void okAddress() {

        selectedAddress = selectedAddress.prepare();

        if (getIsNewAddress()) {
            getSelectedManufacturer().getAddresses().add(selectedAddress);
        }

        PrimeFaces.current().executeScript("PF('addressFormDialog').hide();");

    }

    public void okContact() {

        selectedContact = selectedContact.prepare();

        if (getIsNewContact()) {
            getSelectedManufacturer().getContacts().add(selectedContact);
        }

        PrimeFaces.current().executeScript("PF('contactFormDialog').hide();");

    }

    public Boolean getIsNewContact() {
        return getSelectedContact().getId() == null && !getEdit();
    }

    public Boolean getIsNewAddress() {
        return getSelectedAddress().getId() == null && !getEdit();
    }

    public void updateContact() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void updateAddress() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void removeContact() {
        getSelectedManufacturer().getContacts().remove(selectedContact);
        getSelectedManufacturer().setIsDirty(true);
        selectedContact = null;
    }

    public void removeAddress() {
        getSelectedManufacturer().getAddresses().remove(selectedAddress);
        getSelectedManufacturer().setIsDirty(true);
        selectedAddress = null;
    }

    public List<Manufacturer> completeActiveManufacturer(String query) {
        try {
            return Manufacturer.findActiveManufacturersByAnyPartOfName(getEntityManager(), query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

}
