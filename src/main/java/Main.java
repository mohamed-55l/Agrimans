import core.session.SessionManager;
import modules.user.models.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // POUR TESTER EN ADMIN
            User admin = new User();
            admin.setFullName("Admin Chef");
            admin.setEmail("admin@agrimans.com");
            admin.setRole("ADMIN");
            SessionManager.login(admin);

            String fxmlPath = "/fxml/user/login-view.fxml";
            
            System.out.println("🚀 Démarrage de l'application avec : " + fxmlPath);
            
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            primaryStage.setTitle("Agrimans - Admin Dashboard");
            primaryStage.setScene(new Scene(root, 1366, 768));
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}