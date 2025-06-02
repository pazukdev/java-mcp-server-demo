package com.example.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("") // Map this view to the root path
public class MainView extends VerticalLayout {

    public MainView() {
        TextField textField = new TextField("Enter your name");
        Button button = new Button("Say Hello");

        button.addClickListener(event -> {
            String name = textField.getValue();
            Notification.show("Hello, " + (name.isEmpty() ? "World" : name) + "!");
        });

        setAlignItems(Alignment.CENTER); // Center components
        add(textField, button);
    }
}
