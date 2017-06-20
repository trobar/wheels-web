package com.doit.wheels.services.impl;

import com.doit.wheels.services.MessageByLocaleService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@Component
public class MessageByLocaleServiceImpl implements MessageByLocaleService {

    @Autowired
    MessageSource messageSource;

    @Override
    public String getMessage(String id) {
        Locale locale = VaadinSession.getCurrent().getLocale();
        return messageSource.getMessage(id, null, locale);
    }

    @Override
    public void updateLocale(final HasComponents ui, Locale locale) {
        final ResourceBundle rb = ResourceBundle.getBundle("locale/messages", locale);
        // locale may not be null, howvever the current UI Locale may be null!
        if (locale.equals(VaadinSession.getCurrent().getLocale())) {
            return;
        }
        walkComponentTree(ui, c -> {
            final String id = c.getId();
            if (id == null) {
                return;
            }
            if (c instanceof Label){
                ((Label) c).setValue(rb.getString(c.getId()));
            }
            else if (c instanceof Button){
                c.setCaption(rb.getString(c.getId()));
            }
        });
    }

    // recursively walk the Component true
    private static void walkComponentTree(com.vaadin.ui.Component c, Consumer<com.vaadin.ui.Component> visitor) {
        visitor.accept(c);
        if (c instanceof HasComponents) {
            for (com.vaadin.ui.Component child : ((HasComponents)c)) {
                walkComponentTree(child, visitor);
            }
        }
    }

}
