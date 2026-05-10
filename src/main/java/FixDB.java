import core.database.Mydb;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class FixDB {
    public static void main(String[] args) {
        Connection cnx = Mydb.getInstance().getCnx();
        if (cnx == null) {
            System.out.println("❌ Erreur de connexion DB");
            return;
        }

        try (Statement st = cnx.createStatement()) {
            System.out.println("🔧 Correction de la table demande...");
            try {
                st.execute("ALTER TABLE demande DROP FOREIGN KEY demande_ibfk_1");
                System.out.println("✅ FK demande_ibfk_1 supprimée");
            } catch (SQLException e) {
                System.out.println("⚠️ Impossible de supprimer demande_ibfk_1 (peut-être déjà supprimée) : " + e.getMessage());
            }

            try {
                st.execute("ALTER TABLE demande ADD CONSTRAINT fk_demande_user_new FOREIGN KEY (agriculteur_id) REFERENCES users(id) ON DELETE CASCADE");
                System.out.println("✅ Nouvelle FK ajoutée pour demande vers la table users");
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de l'ajout de la FK pour demande : " + e.getMessage());
            }

            System.out.println("\n🔧 Correction de la table review...");
            try {
                st.execute("ALTER TABLE review DROP FOREIGN KEY fk_review_user");
                System.out.println("✅ FK fk_review_user supprimée");
            } catch (SQLException e) {
                System.out.println("⚠️ Impossible de supprimer fk_review_user : " + e.getMessage());
            }

            try {
                st.execute("ALTER TABLE review ADD CONSTRAINT fk_review_users_new FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");
                System.out.println("✅ Nouvelle FK ajoutée pour review vers la table users");
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de l'ajout de la FK pour review : " + e.getMessage());
            }
            
            System.out.println("🎉 Corrections terminées !");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
