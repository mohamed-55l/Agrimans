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
            // POUR TESTER EN ADMIN - Décommentez cette ligne
            // User admin = new User(1, "Admin", "Chef", "admin@agrimans.com", "ADMIN");
            // SessionManager.login(admin);

            // POUR TESTER EN USER - Décommentez cette ligne
            User user = new User(2, "Jean", "Agriculteur", "jean@agrimans.com", "AGRICULTEUR");
            SessionManager.login(user);

            // Charger directement le layout correspondant
            String fxmlPath = SessionManager.isAdmin() ?
                    "/fxml/layout/admin_layout.fxml" :
                    "/fxml/layout/user_layout.fxml";

            System.out.println("🚀 Démarrage avec: " + fxmlPath);

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            primaryStage.setTitle("Agrimans - " + SessionManager.getCurrentUserName());
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}