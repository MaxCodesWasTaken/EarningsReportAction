module app.earningreportaction {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires org.json;

    opens app.earningreportaction to javafx.fxml;
    exports app.earningreportaction;
    exports app.earningreportaction.controller;
    exports app.earningreportaction.model;
    opens app.earningreportaction.controller to javafx.fxml;
}