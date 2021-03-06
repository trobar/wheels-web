package com.doit.wheels.ui;

import com.doit.wheels.dao.entities.Country;
import com.doit.wheels.dao.entities.Customer;
import com.doit.wheels.dao.entities.CustomerContact;
import com.doit.wheels.dao.entities.Order;
import com.doit.wheels.dao.entities.basic.AbstractModel;
import com.doit.wheels.services.CountryService;
import com.doit.wheels.services.CustomerService;
import com.doit.wheels.services.MessageByLocaleService;
import com.doit.wheels.utils.confirmdialog.ConfirmDialog;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
@Configurable
@SpringComponent
@SpringView(name = "create-edit-customer")
public class CreateEditCustomerView extends VerticalLayout implements View {

    public String CURRENT_MODE ;

    private final String CREATE = "Create";
    private final String EDIT = "Edit";

    private final String mandatory = ":*";
    private final CustomerService customerService;
    private final CountryService countryService;

    private final MessageByLocaleService messageByLocaleService;

    private Binder<Customer> customerBinder;
    private Binder<CustomerContact> customerContactBinder;

    private TextField customerNo;
    private TextField firstname;
    private TextField lastname;
    private TextField companyName;
    private TextField address1;
    private TextField address2;
    private TextField zipCode;
    private TextField city;
    private ComboBox<Country> country;
    private TextField email;
    private TextField phone;
    private TextField mobile;

    private TextField contactFirstName;
    private TextField contactLastName;
    private TextField contactEmail;
    private TextField contactPhone;
    private TextField contactMobile;

    private Grid<CustomerContact> contactGrid;

    private Button createContactButton;
    private Button editContactButton;
    private Button deleteContactButton;

    private Customer customer;

    private Customer unchagedCustomer;


    @Autowired
    public CreateEditCustomerView(CustomerService customerService, CountryService countryService,
                                  MessageByLocaleService messageByLocaleService) {
        this.customerService = customerService;
        this.countryService = countryService;
        this.messageByLocaleService = messageByLocaleService;
    }

    private void init() {
        Map<String, AbstractModel> data = (Map<String, AbstractModel>) getUI().getData();
        if (data != null && data.toString().length() > 0) {
            Customer sharedCustomer = (Customer) ((Map) data).get("CUSTOMER");
            if (sharedCustomer != null) {
                customer = sharedCustomer;
                try {
                    unchagedCustomer = (Customer) customer.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                CURRENT_MODE = sharedCustomer.getId() == null ? CREATE : EDIT;
            } else {
                customer = new Customer();
                customer.setCustomerContacts(new ArrayList<>());
                CURRENT_MODE = CREATE;
            }
            getUI().setData(data);
        } else {
            customer = new Customer();
            customer.setCustomerContacts(new ArrayList<>());
            CURRENT_MODE = CREATE;
        }

        VerticalLayout customerLayout = new VerticalLayout();
        String createCustomerLabel = CURRENT_MODE.equals(CREATE) ? "customerView.customer.new" : "customerView.customer.edit";
        Label newCustomerLabel = new Label(messageByLocaleService.getMessage(createCustomerLabel));
        newCustomerLabel.setId(createCustomerLabel);

        customerLayout.addComponents(newCustomerLabel, initCustomerFields());

        VerticalLayout customerContactLayout = new VerticalLayout();
        Label customerContactLabel = new Label(messageByLocaleService.getMessage("customerView.customerContact.title"));
        customerContactLabel.setId("customerView.customerContact.title");
        customerContactLayout.addComponents(customerContactLabel, initCustomerContactForm());
        customerContactLayout.addStyleName("customer-column");
        customerLayout.addStyleName("customer-column");
        customerContactLabel.addStyleName("contact-label");



        HorizontalLayout layout = new HorizontalLayout();
        VerticalLayout customerContactsGridLayout = initCustomerContactsGrid();
        layout.addComponents(customerLayout, customerContactLayout, customerContactsGridLayout);
        layout.setComponentAlignment(customerLayout, Alignment.TOP_LEFT);
        layout.setComponentAlignment(customerContactLayout, Alignment.TOP_CENTER);
        layout.setComponentAlignment(customerContactsGridLayout, Alignment.TOP_RIGHT);
        layout.setWidth("100%");
        layout.addStyleName("customer-content");
        customerContactLayout.addComponent(customerContactsGridLayout);
        customerContactsGridLayout.addStyleName("contact-grid");

        String createButtonCode = CURRENT_MODE.equals(CREATE) ? "customerView.customer.create" : "customerView.customer.save";

        Button createCustomerButton = new Button(messageByLocaleService.getMessage(createButtonCode));
        createCustomerButton.setId(createButtonCode);
        createCustomerButton.addStyleName("theme-buttons");
        createCustomerButton.setWidth("165px");
        createCustomerButton.addClickListener(clickEvent -> saveCustomer());

        this.addComponents(layout, createCustomerButton);
        this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        this.setComponentAlignment(createCustomerButton, Alignment.TOP_CENTER);

        initBinderAndValidation();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        init();
    }

    private VerticalLayout initCustomerFields() {
        VerticalLayout mainLayout = new VerticalLayout();
        customerNo = new TextField();
        firstname = new TextField();
        lastname = new TextField();
        companyName = new TextField();
        address1 = new TextField();
        address2 = new TextField();
        zipCode = new TextField();
        city = new TextField();
        country = new ComboBox<>();
        country.setItems(countryService.findAll());
        country.setItemCaptionGenerator(Country::getDescription);
        email = new TextField();
        phone = new TextField();
        mobile = new TextField();

        HorizontalLayout numberLayout = new HorizontalLayout();
        Label numberLabel = new Label(messageByLocaleService.getMessage("customerView.customer.number"));
        numberLabel.addStyleName("user-management-label");
        numberLabel.setId("customerView.customer.number");
        numberLayout.addComponents(numberLabel, customerNo);

        HorizontalLayout companyNameLayout = new HorizontalLayout();
        Label companyNameLabel = new Label(messageByLocaleService.getMessage("customerView.customer.companyName"));
        companyNameLabel.addStyleName("user-management-label");
        companyNameLabel.setId("customerView.customer.companyName");
        companyNameLayout.addComponents(companyNameLabel, companyName);

        HorizontalLayout firstnamelayout = new HorizontalLayout();
        Label firstnameLabel = new Label(messageByLocaleService.getMessage("customerView.customer.firstName") + mandatory);
        firstnameLabel.addStyleName("user-management-label");
        firstnameLabel.setId("customerView.customer.firstName");
        firstnamelayout.addComponents(firstnameLabel, firstname);

        HorizontalLayout lastnameLayout = new HorizontalLayout();
        Label lastnameLabel = new Label(messageByLocaleService.getMessage("customerView.customer.lastName") + mandatory);
        lastnameLabel.addStyleName("user-management-label");
        lastnameLabel.setId("customerView.customer.lastName");
        lastnameLayout.addComponents(lastnameLabel, lastname);

        HorizontalLayout addressLayout = new HorizontalLayout();
        Label addressLabel = new Label(messageByLocaleService.getMessage("customerView.customer.address"));
        addressLabel.addStyleName("user-management-label");
        addressLabel.setId("customerView.customer.address");
        addressLayout.addComponents(addressLabel, address1);

        HorizontalLayout adddress2Layout = new HorizontalLayout();
        Label emptyLabel = new Label("");
        emptyLabel.addStyleName("user-management-label");
        adddress2Layout.addComponents(emptyLabel, address2);

        HorizontalLayout zipCodeLayout = new HorizontalLayout();
        Label zipLabel = new Label(messageByLocaleService.getMessage("customerView.customer.zip"));
        zipLabel.addStyleName("user-management-label");
        zipLabel.setId("customerView.customer.zip");
        zipCodeLayout.addComponents(zipLabel, zipCode);

        HorizontalLayout cityLayout = new HorizontalLayout();
        Label cityLabel = new Label(messageByLocaleService.getMessage("customerView.customer.city"));
        cityLabel.addStyleName("user-management-label");
        cityLabel.setId("customerView.customer.city");
        cityLayout.addComponents(cityLabel, city);

        HorizontalLayout countryLayout = new HorizontalLayout();
        Label countryLabel = new Label(messageByLocaleService.getMessage("customerView.customer.country"));
        countryLabel.addStyleName("user-management-label");
        countryLabel.setId("customerView.customer.country");
        countryLayout.addComponents(countryLabel, country);

        HorizontalLayout emailLayout = new HorizontalLayout();
        Label emailLabel = new Label(messageByLocaleService.getMessage("customerView.customer.email"));
        emailLabel.addStyleName("user-management-label");
        emailLabel.setId("customerView.customer.email");
        emailLayout.addComponents(emailLabel, email);

        HorizontalLayout phoneLayout = new HorizontalLayout();
        Label phoneLabel = new Label(messageByLocaleService.getMessage("customerView.customer.phone"));
        phoneLabel.addStyleName("user-management-label");
        phoneLabel.setId("customerView.customer.phone");
        phoneLayout.addComponents(phoneLabel, phone);

        HorizontalLayout mobileLayout = new HorizontalLayout();
        Label mobileLabel = new Label(messageByLocaleService.getMessage("customerView.customer.mobile"));
        mobileLabel.addStyleName("user-management-label");
        mobileLabel.setId("customerView.customer.mobile");
        mobileLayout.addComponents(mobileLabel, mobile);

        mainLayout.addComponents(numberLayout, companyNameLayout, firstnamelayout,
                lastnameLayout, addressLayout, adddress2Layout, zipCodeLayout,
                cityLayout, countryLayout, emailLayout, phoneLayout, mobileLayout);

        return mainLayout;
    }

    private void saveCustomer() {
        try {
            customerBinder.validate();
            customerBinder.writeBean(customer);
            customerService.save(customer);
            Customer emptyCustomer = new Customer();
            emptyCustomer.setCustomerContacts(new ArrayList<>());
            customerBinder.setBean(emptyCustomer);
            contactGrid.setItems(emptyCustomer.getCustomerContacts());
            showAddNotification();
            if (getUI().getData() != null) {
                Order order = (Order) ((Map<String, AbstractModel>) getUI().getData()).get("ORDER");
                if(order != null) {
                    Map<String, AbstractModel> args = new HashMap<>();
                    args.put("CUSTOMER", customer);
                    args.put("ORDER", order);
                    getUI().setData(args);
                    getUI().getNavigator().navigateTo("new-order");
                }
                else {
                    getUI().getNavigator().navigateTo("customer-orders-list");
                }
            } else {
                getUI().getNavigator().navigateTo("customer-orders-list");
            }
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private void initBinderAndValidation() {
        customerBinder = new Binder<>(Customer.class);
        customerBinder.bindInstanceFields(this);

        StringLengthValidator notEmptyValidator = new StringLengthValidator(
                messageByLocaleService.getMessage("userManagement.validation.notEmpty"), 1, 50);
        customerBinder.forField(firstname).withValidator(notEmptyValidator).bind(Customer::getFirstname, Customer::setFirstname);
        customerBinder.forField(lastname).withValidator(notEmptyValidator).bind(Customer::getLastname, Customer::setLastname);
        customerBinder.setBean(customer);

        customerContactBinder = new Binder<>(CustomerContact.class);
        customerContactBinder.forField(contactFirstName).withValidator(notEmptyValidator)
                .bind(CustomerContact::getFirstname, CustomerContact::setFirstname);
        customerContactBinder.forField(contactLastName).withValidator(notEmptyValidator)
                .bind(CustomerContact::getLastname, CustomerContact::setLastname);
        customerContactBinder.forField(contactEmail).bind(CustomerContact::getEmail, CustomerContact::setEmail);
        customerContactBinder.forField(contactPhone).bind(CustomerContact::getPhone, CustomerContact::setPhone);
        customerContactBinder.forField(contactMobile).bind(CustomerContact::getMobile, CustomerContact::setMobile);
        customerContactBinder.setBean(new CustomerContact());
    }

    private HorizontalLayout initCustomerContactForm() {
        HorizontalLayout mainLayout = new HorizontalLayout();

        VerticalLayout inputLayout = new VerticalLayout();
        contactFirstName = new TextField();
        contactLastName = new TextField();
        contactEmail = new TextField();
        contactPhone = new TextField();
        contactMobile = new TextField();

        mainLayout.addStyleName("main-layout");

        HorizontalLayout firstnamelayout = new HorizontalLayout();
        Label firstnameLabel = new Label(messageByLocaleService.getMessage("customerView.customer.firstName") + mandatory);
        firstnameLabel.addStyleName("user-management-label");
        firstnameLabel.setId("customerView.customer.firstName");
        firstnamelayout.addComponents(firstnameLabel, contactFirstName);

        HorizontalLayout lastnameLayout = new HorizontalLayout();
        Label lastnameLabel = new Label(messageByLocaleService.getMessage("customerView.customer.lastName") + mandatory);
        lastnameLabel.addStyleName("user-management-label");
        lastnameLabel.setId("customerView.customer.lastName");
        lastnameLayout.addComponents(lastnameLabel, contactLastName);

        HorizontalLayout emailLayout = new HorizontalLayout();
        Label emailLabel = new Label(messageByLocaleService.getMessage("customerView.customer.email"));
        emailLabel.addStyleName("user-management-label");
        emailLabel.setId("customerView.customer.email");
        emailLayout.addComponents(emailLabel, contactEmail);

        HorizontalLayout phoneLayout = new HorizontalLayout();
        Label phoneLabel = new Label(messageByLocaleService.getMessage("customerView.customer.phone"));
        phoneLabel.addStyleName("user-management-label");
        phoneLabel.setId("customerView.customer.phone");
        phoneLayout.addComponents(phoneLabel, contactPhone);

        HorizontalLayout mobileLayout = new HorizontalLayout();
        Label mobileLabel = new Label(messageByLocaleService.getMessage("customerView.customer.mobile"));
        mobileLabel.addStyleName("user-management-label");
        mobileLabel.setId("customerView.customer.mobile");
        mobileLayout.addComponents(mobileLabel, contactMobile);

        inputLayout.addComponents(firstnamelayout, lastnameLayout, emailLayout, phoneLayout, mobileLayout);

        VerticalLayout buttonLayout = new VerticalLayout();

        buttonLayout.setHeight("100%");
        createContactButton = new Button(messageByLocaleService.getMessage("customerView.customerContact.create"));
        createContactButton.setId("customerView.customerContact.create");
        createContactButton.addStyleName("theme-buttons");
        createContactButton.setWidth("165px");
        createContactButton.addClickListener(clickEvent -> saveContact());

        editContactButton = new Button(messageByLocaleService.getMessage("customerView.customerContact.edit"));
        editContactButton.setId("customerView.customerContact.edit");
        editContactButton.addStyleName("theme-buttons");
        editContactButton.setWidth("165px");
        editContactButton.addClickListener(clickEvent -> updateContact());

        deleteContactButton = new Button(messageByLocaleService.getMessage("customerView.customerContact.delete"));
        deleteContactButton.setId("customerView.customerContact.delete");
        deleteContactButton.addStyleName("theme-buttons");
        deleteContactButton.setWidth("165px");
        deleteContactButton.addClickListener(clickEvent -> deleteContact());

        editContactButton.setEnabled(false);
        deleteContactButton.setEnabled(false);

        buttonLayout.addComponents(createContactButton, editContactButton, deleteContactButton);
        buttonLayout.setComponentAlignment(editContactButton, Alignment.MIDDLE_CENTER);
        buttonLayout.setComponentAlignment(deleteContactButton, Alignment.BOTTOM_CENTER);

        mainLayout.addComponents(inputLayout, buttonLayout);

        return mainLayout;
    }

    private VerticalLayout initCustomerContactsGrid() {
        VerticalLayout layout = new VerticalLayout();
        Label contactsListLabel = new Label(messageByLocaleService.getMessage("customerView.customerContact.list.title"));
        contactsListLabel.setId("customerView.customerContact.list.title");

        contactGrid = new Grid<>(CustomerContact.class);
        contactGrid.setColumns("firstname", "lastname", "email", "phone", "mobile");

        Label firstnameHeader = new Label(messageByLocaleService.getMessage("customerView.customer.firstName"));
        firstnameHeader.setId("customerView.customer.firstName");
        contactGrid.getDefaultHeaderRow().getCell("firstname").setComponent(firstnameHeader);

        Label lastnameHeader = new Label(messageByLocaleService.getMessage("customerView.customer.lastName"));
        lastnameHeader.setId("customerView.customer.lastName");
        contactGrid.getDefaultHeaderRow().getCell("lastname").setComponent(lastnameHeader);

        Label emailHeader = new Label(messageByLocaleService.getMessage("customerView.customer.email"));
        emailHeader.setId("customerView.customer.email");
        contactGrid.getDefaultHeaderRow().getCell("email").setComponent(emailHeader);

        Label phoneHeader = new Label(messageByLocaleService.getMessage("customerView.customer.phone"));
        phoneHeader.setId("customerView.customer.phone");
        contactGrid.getDefaultHeaderRow().getCell("phone").setComponent(phoneHeader);

        Label mobileHeader = new Label(messageByLocaleService.getMessage("customerView.customer.mobile"));
        mobileHeader.setId("customerView.customer.mobile");
        contactGrid.getDefaultHeaderRow().getCell("mobile").setComponent(mobileHeader);


        contactGrid.setItems(customer.getCustomerContacts());
        contactGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        contactGrid.addSelectionListener(selectionEvent -> {
            if (selectionEvent.getFirstSelectedItem().isPresent()) {
                CustomerContact contact = selectionEvent.getFirstSelectedItem().get();
                refreshCustomerContactBinder(contact);
                editContactButton.setEnabled(true);
                deleteContactButton.setEnabled(true);
                createContactButton.setEnabled(false);
            } else {
                refreshCustomerContactBinder();
                editContactButton.setEnabled(false);
                deleteContactButton.setEnabled(false);
                createContactButton.setEnabled(true);
            }
        });

        layout.addComponents(contactsListLabel, contactGrid);
        return layout;
    }

    private void saveContact() {
        try {
            CustomerContact contact = customerContactBinder.getBean();
            customerContactBinder.validate();
            customerContactBinder.writeBean(contact);
            contact.setCustomer(customer);
            customer.getCustomerContacts().add(contact);
            contactGrid.setItems(customer.getCustomerContacts());
            refreshCustomerContactBinder();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private void updateContact() {
        contactGrid.setItems(customer.getCustomerContacts());
        refreshCustomerContactBinder();
    }

    private void deleteContact() {
        CustomerContact bean = customerContactBinder.getBean();
        customer.getCustomerContacts().remove(bean);
        contactGrid.setItems(customer.getCustomerContacts());
        refreshCustomerContactBinder();
    }

    private void refreshCustomerContactBinder(CustomerContact... contact) {
        customerContactBinder.setBean(contact.length > 0 ? contact[0] : new CustomerContact());
    }

    private void showAddNotification() {
        String notificationMessage = CURRENT_MODE.equals(CREATE) ? "customerView.customer.whenCreated" : "customerView.customer.whenEdited";
        Notification saveNotification = new Notification(messageByLocaleService.getMessage(notificationMessage));
        saveNotification.setDelayMsec(3000);
        saveNotification.show(Page.getCurrent());
    }

    void saveChangesPopup() {
        if (!unchagedCustomer.equals(customer)) {
            ConfirmDialog.show(getUI(),
                    messageByLocaleService.getMessage("save.notification.title"),
                    messageByLocaleService.getMessage("save.notification.body"),
                    messageByLocaleService.getMessage("save.notification.okCaption"),
                    messageByLocaleService.getMessage("save.notification.cancelCaption"),
                    messageByLocaleService.getMessage("save.notification.notOkCaption"),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            customerBinder.validate();
                            try {
                                customerBinder.writeBean(customer);
                                customerService.save(customerBinder.getBean());
                                getSession().setAttribute("showCustomerList", true);
                                getUI().getNavigator().navigateTo(getSession().getAttribute("previousView").toString());
                            } catch (ValidationException e) {
                                e.printStackTrace();
                            }
                        }
                        if (dialog.isNotConfirmed()) {
                            getSession().setAttribute("showCustomerList", true);
                            getUI().getNavigator().navigateTo(getSession().getAttribute("previousView").toString());
                        }
                    });
        }
        else {
            getSession().setAttribute("showCustomerList", true);
            getUI().getNavigator().navigateTo(getSession().getAttribute("previousView").toString());
        }
    }

}
