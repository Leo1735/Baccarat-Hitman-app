package com.example.baccarat;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class MainApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/baccarat/baccarat.fxml"));
        Scene scene = new Scene(loader.load(), 300, 400);
        scene.getStylesheets().add(getClass().getResource("/com/example/baccarat/styles.css").toExternalForm());
// Example: get the top bar node by fx:id (from controller or lookup)
        Pane topBar = (Pane) scene.lookup("#topBar");

        // When mouse pressed on the top bar: record initial offsets
        topBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // When mouse dragged on the top bar: move the stage accordingly
        topBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        primaryStage.setTitle("Kevin's 500-1 Baccarat Plan");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED); // removes white bar & borders
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

        // Position on the left edge of the screen
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setHeight(650);


    }

    public static void main(String[] args) {
        launch(args);
    }
}

