module com.vybhav.ecko {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;
    requires org.apache.pdfbox;
    requires com.google.auth.oauth2;
    requires firebase.admin;
    requires com.google.auth;
    requires org.json;
    requires java.desktop;


    opens com.vybhav.ecko to javafx.fxml;
    exports com.vybhav.ecko;
}