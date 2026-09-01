package com.example.baccarat;



import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/** Starts the borderless main window and supplies shared resize support. */
public class MainApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/baccarat/baccarat.fxml"));
        Scene scene = new Scene(loader.load(), 420, 700);
        scene.getStylesheets().add(getClass().getResource("/com/example/baccarat/styles.css").toExternalForm());

        Pane topBar = (Pane) scene.lookup("#topBar");

        final boolean[] draggingWindow = {false};

        topBar.setOnMousePressed(event -> {
            if (isResizeCursor(scene.getCursor())) {
                draggingWindow[0] = false;
                return;
            }
            draggingWindow[0] = true;
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });


        topBar.setOnMouseDragged(event -> {
            if (!draggingWindow[0]) return;
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
        primaryStage.setTitle("Kevin's 500-1 Baccarat Plan");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setMinWidth(260);
        primaryStage.setMinHeight(400);
        primaryStage.setResizable(true);
        installResizeHandles(primaryStage, scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();


        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(0);
        primaryStage.setY(0);
        primaryStage.setHeight(700);


    }

    public static void main(String[] args) {
        launch(args);
    }

    private static boolean isResizeCursor(Cursor cursor) {
        return cursor == Cursor.NW_RESIZE || cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE
                || cursor == Cursor.W_RESIZE || cursor == Cursor.E_RESIZE
                || cursor == Cursor.SW_RESIZE || cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE;
    }

    /**
     * Resizes borderless stages from any edge or corner. Positions are calculated
     * from the initial drag point to avoid accumulating rounding and movement.
     */
    public static void installResizeHandles(Stage stage, Scene scene) {
        final double edge = 8;
        final Cursor[] activeCursor = {Cursor.DEFAULT};
        final double[] startX = new double[1], startY = new double[1], startW = new double[1], startH = new double[1];
        final double[] startStageX = new double[1], startStageY = new double[1];
        final double[] pendingX = new double[1], pendingY = new double[1], pendingW = new double[1], pendingH = new double[1];
        final boolean[] resizeQueued = {false};

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            scene.setCursor(resizeCursor(scene, e, edge));
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            if (activeCursor[0] == Cursor.DEFAULT) scene.setCursor(Cursor.DEFAULT);
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            activeCursor[0] = resizeCursor(scene, e, edge);
            startX[0] = e.getScreenX(); startY[0] = e.getScreenY();
            startW[0] = stage.getWidth(); startH[0] = stage.getHeight();
            startStageX[0] = stage.getX(); startStageY[0] = stage.getY();
            if (activeCursor[0] != Cursor.DEFAULT) e.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (activeCursor[0] == Cursor.DEFAULT) return;
            double dx = e.getScreenX() - startX[0], dy = e.getScreenY() - startY[0];
            Cursor cursor = activeCursor[0];
            double width = startW[0];
            double height = startH[0];
            double stageX = startStageX[0];
            double stageY = startStageY[0];
            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE)
                width = Math.max(stage.getMinWidth(), startW[0] + dx);
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE)
                height = Math.max(stage.getMinHeight(), startH[0] + dy);
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                width = Math.max(stage.getMinWidth(), startW[0] - dx);
                stageX = startStageX[0] + startW[0] - width;
            }
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                height = Math.max(stage.getMinHeight(), startH[0] - dy);
                stageY = startStageY[0] + startH[0] - height;
            }
            pendingX[0] = stageX;
            pendingY[0] = stageY;
            pendingW[0] = width;
            pendingH[0] = height;
            if (!resizeQueued[0]) {
                resizeQueued[0] = true;
                Platform.runLater(() -> {
                    // Coalescing mouse events prevents bottom-edge resizing from
                    // triggering multiple expensive layout passes per frame.
                    // Apply stage position before size to avoid transient layout jitter
                    stage.setX(pendingX[0]);
                    stage.setY(pendingY[0]);
                    stage.setWidth(pendingW[0]);
                    stage.setHeight(pendingH[0]);
                    resizeQueued[0] = false;
                });
            }
            e.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> activeCursor[0] = Cursor.DEFAULT);
    }

    private static Cursor resizeCursor(Scene scene, MouseEvent e, double edge) {
        boolean left = e.getSceneX() < edge;
        boolean right = e.getSceneX() > scene.getWidth() - edge;
        boolean top = e.getSceneY() < edge;
        boolean bottom = e.getSceneY() > scene.getHeight() - edge;
        return top && left ? Cursor.NW_RESIZE : top && right ? Cursor.NE_RESIZE
                    : bottom && left ? Cursor.SW_RESIZE : bottom && right ? Cursor.SE_RESIZE
                    : left ? Cursor.W_RESIZE : right ? Cursor.E_RESIZE : top ? Cursor.N_RESIZE
                    : bottom ? Cursor.S_RESIZE : Cursor.DEFAULT;
    }
}
