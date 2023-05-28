module com.vybhav.ecko {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.io;
    requires org.apache.pdfbox;


    opens com.vybhav.ecko to javafx.fxml;
    exports com.vybhav.ecko;
}