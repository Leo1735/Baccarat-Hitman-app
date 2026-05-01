module com.example.baccarat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.baccarat to javafx.fxml;
    exports com.example.baccarat;
}