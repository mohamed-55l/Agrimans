import java.net.URL;
import java.io.File;

public class TestFXML {
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("🔍 DIAGNOSTIC DES RESSOURCES");
        System.out.println("=================================");

        // 1. Afficher le répertoire de travail
        System.out.println("📁 Répertoire de travail: " + System.getProperty("user.dir"));

        // 2. Vérifier le classpath
        System.out.println("\n📦 Classpath:");
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(";");
        for (String path : paths) {
            if (path.contains("target") || path.contains("classes")) {
                System.out.println("   " + path);
            }
        }

        // 3. Vérifier le répertoire des classes
        String classesDir = TestFXML.class.getResource("/").getPath();
        System.out.println("\n📂 Répertoire des classes: " + classesDir);

        // 4. Lister les fichiers dans resources/fxml
        System.out.println("\n📋 Recherche des fichiers FXML:");

        String[] chemins = {
                "/fxml/layout/layout.fxml",
                "fxml/layout/layout.fxml",
                "/layout.fxml",
                "layout.fxml",
                "/fxml/layout.fxml",
                "fxml/layout.fxml",
                "../resources/fxml/layout/layout.fxml"
        };

        for (String chemin : chemins) {
            URL url = TestFXML.class.getResource(chemin);
            System.out.println(chemin + " : " + (url != null ? "✅" : "❌"));
            if (url != null) {
                System.out.println("   → " + url.getPath());
            }
        }

        // 5. Vérifier le répertoire resources physique
        System.out.println("\n📁 Vérification physique:");
        File resourcesDir = new File("src/main/resources");
        if (resourcesDir.exists()) {
            System.out.println("✅ src/main/resources existe");
            listerDossier(resourcesDir, "");
        } else {
            System.out.println("❌ src/main/resources n'existe pas");

            // Essayer d'autres chemins
            File altDir = new File("resources");
            if (altDir.exists()) {
                System.out.println("✅ resources existe");
                listerDossier(altDir, "");
            }
        }
    }

    private static void listerDossier(File dossier, String indent) {
        File[] fichiers = dossier.listFiles();
        if (fichiers != null) {
            for (File f : fichiers) {
                System.out.println(indent + "   " + (f.isDirectory() ? "📁" : "📄") + " " + f.getName());
                if (f.isDirectory()) {
                    listerDossier(f, indent + "   ");
                }
            }
        }
    }
}