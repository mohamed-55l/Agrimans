package core.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * Classe utilitaire pour les boîtes de dialogue
 *
 * RÔLE: Centraliser et simplifier l'affichage des alertes
 * Évite de répéter le code de création d'alertes partout
 */
public class AlertUtils {

    /**
     * Affiche une alerte d'information
     * @param title Titre de la fenêtre
     * @param message Message à afficher
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur
     * @param title Titre de la fenêtre
     * @param message Message à afficher
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'avertissement
     * @param title Titre de la fenêtre
     * @param message Message à afficher
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de confirmation
     * @param title Titre de la fenêtre
     * @param message Message à afficher
     * @return true si l'utilisateur a cliqué sur OK, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get() == ButtonType.OK;
    }

    /**
     * Affiche une alerte personnalisée
     * @param type Type d'alerte (ERROR, INFORMATION, etc.)
     * @param title Titre
     * @param message Message
     */
    public static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}