module org.example.pw_projekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens org.example.pw_projekt to javafx.fxml;
    exports org.example.pw_projekt;
}