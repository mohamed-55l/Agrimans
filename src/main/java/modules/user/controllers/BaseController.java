package modules.user.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class BaseController {

    protected Stage stage;

    private double xOffset;
    private double yOffset;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ================= SWITCH SCENE =================
    protected void switchScene(String fxmlPath, Node source) {
        try {
            // نبعثو الـ fxmlPath طول كإسم كامل يبدا بـ /
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            Parent root = loader.load();
            BaseController controller = loader.getController();
            Stage currentStage = (Stage) source.getScene().getWindow();

            controller.setStage(currentStage);
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ================= WINDOW CONTROLS =================

    @javafx.fxml.FXML
    public void handleClose(ActionEvent event) {

        Stage stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        stage.close();
    }

    @javafx.fxml.FXML
    public void handleMinimize(ActionEvent event) {

        Stage stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        stage.setIconified(true);
    }

    @javafx.fxml.FXML
    public void handleMaximize(ActionEvent event) {

        Stage stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        stage.setMaximized(!stage.isMaximized());
    }

    // ================= DRAG WINDOW =================

    @javafx.fxml.FXML
    public void handleMousePressed(MouseEvent event) {

        stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @javafx.fxml.FXML
    public void handleMouseDragged(MouseEvent event) {

        stage =
                (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

}
