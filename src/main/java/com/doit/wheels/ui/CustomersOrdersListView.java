package com.doit.wheels.ui;

import com.doit.wheels.services.*;
import com.doit.wheels.ui.nested.CustomersListLayout;
import com.doit.wheels.ui.nested.OrdersListLayout;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@SpringComponent
@SpringView(name = "customer-orders-list")
@PreserveOnRefresh
public class CustomersOrdersListView extends VerticalLayout implements View {

    private final MessageByLocaleService messageByLocaleService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final PrintJobService printJobService;
    private final UserService userService;

    private Component lastUsedComponent;
    private Button customersNavigationButton;
    private Button ordersNavigationButton;

    @Autowired
    public CustomersOrdersListView(MessageByLocaleService messageByLocaleService, CustomerService customerService,
                                   OrderService orderService, PrintJobService printJobService, UserService userService) {
        this.messageByLocaleService = messageByLocaleService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.printJobService = printJobService;
        this.userService = userService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        init();
        getSession().setAttribute("previousView", "landing");
        if (getSession().getAttribute("showCustomerList") != null && Boolean.valueOf(getSession().getAttribute("showCustomerList").toString())){
            customersNavigationButton.click();
            getSession().setAttribute("showCustomerList", false);
        }
    }

    private void init() {
        CssLayout menubar = new CssLayout();
        menubar.addStyleName("user-management-menubar");

        ordersNavigationButton = new Button(messageByLocaleService.getMessage("orderView.navigation.order"));
        ordersNavigationButton.setId("orderView.navigation.order");
        ordersNavigationButton.addStyleName("create-user-button");
        ordersNavigationButton.setIcon(new ThemeResource("img/ico/orders.png"));
        ordersNavigationButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        ordersNavigationButton.addStyleName("clear-button");
        ordersNavigationButton.addStyleName("order-list-button");
        ordersNavigationButton.addStyleName("selected");
        ordersNavigationButton.addClickListener(clickEvent -> {
            makeButtonSelected(ordersNavigationButton);
            replaceComponent(new OrdersListLayout(messageByLocaleService, orderService, printJobService, userService));
        });

        customersNavigationButton = new Button(messageByLocaleService.getMessage("orderView.navigation.customer"));
        customersNavigationButton.setId("orderView.navigation.customer");
        customersNavigationButton.addStyleName("manage-user-access-button");
        customersNavigationButton.setIcon(new ThemeResource("img/ico/customers.png"));
        customersNavigationButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
        customersNavigationButton.addStyleName("clear-button");
        customersNavigationButton.addStyleName("customer-list-button");
        customersNavigationButton.addClickListener(clickEvent -> {
            makeButtonSelected(customersNavigationButton);
            replaceComponent(new CustomersListLayout(messageByLocaleService, customerService));
        });

        menubar.addComponents(ordersNavigationButton, customersNavigationButton);

        this.addComponent(menubar);
        lastUsedComponent = new OrdersListLayout(messageByLocaleService, orderService, printJobService, userService);
        this.addComponent(lastUsedComponent);
    }


    private void replaceComponent(Component newComponent) {
        replaceComponent(lastUsedComponent, newComponent);
        lastUsedComponent = newComponent;
    }

    private void makeButtonSelected(Button buttonToSelect) {
        if(buttonToSelect == ordersNavigationButton) {
            ordersNavigationButton.addStyleName("selected");
            customersNavigationButton.removeStyleName("selected");
        } else {
            ordersNavigationButton.removeStyleName("selected");
            customersNavigationButton.addStyleName("selected");
        }
    }
}
