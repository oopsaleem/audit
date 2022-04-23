module com.asap.urgentapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires poi;
    requires poi.ooxml;

    opens com.asap.urgentapp to javafx.fxml;
    exports com.asap.urgentapp;
}
