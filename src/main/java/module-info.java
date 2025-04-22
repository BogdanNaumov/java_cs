module org.example.lab_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.example.lab_1 to javafx.fxml, com.google.gson;
    opens org.example.lab_1.client to com.google.gson;
    opens org.example.lab_1.server to com.google.gson;

    exports org.example.lab_1;
    exports org.example.lab_1.client;
    exports org.example.lab_1.server;
}