import core.database.Mydb;
import core.session.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Démarrage de la nouvelle interface...");

            // Vérifier la connexion à la base de données
            if (!Mydb.getInstance().isConnected()) {
                System.err.println("❌ Base de données non connectée");
                return;
            }
            System.out.println("✅ Base de données OK");

            // Charger la page de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/user/login.fxml"));

            primaryStage.setTitle("Agrimans - Connexion");
            primaryStage.setScene(new Scene(root, 1200, 700));
            primaryStage.show();

            System.out.println("✅ Nouvelle interface démarrée");

        } catch (Exception e) {
            System.err.println("❌ Erreur:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}