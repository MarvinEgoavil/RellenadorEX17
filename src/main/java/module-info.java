module com.alucardstudio.rellenadorex17 {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;
    requires java.desktop;

    // Permite guardar preferencias de la aplicación,
    // como la última carpeta utilizada.
    requires java.prefs;

    opens com.alucardstudio.rellenadorex17 to javafx.fxml;
    exports com.alucardstudio.rellenadorex17;
}