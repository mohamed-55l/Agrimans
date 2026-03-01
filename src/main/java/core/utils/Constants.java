package core.utils;

import javafx.scene.paint.Color;

/**
 * Constantes de l'application
 */
public class Constants {

    // Couleurs
    public static final String COLOR_PRIMARY = "#4B8B3B";
    public static final String COLOR_SECONDARY = "#2E3D27";
    public static final String COLOR_BACKGROUND = "#F0E8D8";
    public static final String COLOR_ACCENT = "#7DBF6C";
    public static final String COLOR_DANGER = "#e74c3c";
    public static final String COLOR_WARNING = "#f39c12";
    public static final String COLOR_SUCCESS = "#27ae60";
    public static final String COLOR_INFO = "#3498db";

    // États équipement
    public static final String ETAT_DISPONIBLE = "Disponible";
    public static final String ETAT_EN_PANNE = "En panne";
    public static final String ETAT_MAINTENANCE = "En maintenance";
    public static final String ETAT_ASSIGNE = "Assigné";

    // Rôles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_AGRICULTEUR = "AGRICULTEUR";

    // Types de demande
    public static final String DEMANDE_EQUIPEMENT = "EQUIPEMENT";
    public static final String DEMANDE_MAINTENANCE = "MAINTENANCE";
    public static final String DEMANDE_CONGE = "CONGE";
    public static final String DEMANDE_AUTRE = "AUTRE";

    // Statuts demande
    public static final String STATUT_EN_ATTENTE = "En attente";
    public static final String STATUT_ACCEPTE = "Accepté";
    public static final String STATUT_REFUSE = "Refusé";

    // Statuts parcelle
    public static final String PARCELLE_PREPARATION = "En préparation";
    public static final String PARCELLE_CULTURE = "En culture";
    public static final String PARCELLE_JACHERE = "En jachère";
    public static final String PARCELLE_RECOLTE = "Récolté";

    // États santé animal
    public static final String SANTE_BON = "Bon";
    public static final String SANTE_MALADE = "Malade";
    public static final String SANTE_TRAITEMENT = "En traitement";
    public static final String SANTE_SURVEILLER = "À surveiller";

    // Chemins FXML
    public static final String FXML_LOGIN = "/fxml/user/login.fxml";
    public static final String FXML_ADMIN_DASHBOARD = "/fxml/dashboard/admin_dashboard.fxml";
    public static final String FXML_USER_DASHBOARD = "/fxml/dashboard/user_dashboard.fxml";
    public static final String FXML_LAYOUT = "/fxml/layout/layout.fxml";
}