import core.database.Mydb;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Démarrage de l'application...");

            // Vérifier la connexion à la base de données
            if (!Mydb.getInstance().isConnected()) {
                System.err.println("❌ Base de données non connectée");
                return;
            }
            System.out.println("✅ Base de données OK");

            // ── Charger la page de connexion complète (avec signup + vérification)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user/login-view.fxml"));

            primaryStage.setTitle("Agrimans - Connexion");
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Page de login chargée");

        } catch (Exception e) {
            System.err.println("❌ Erreur au démarrage:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}