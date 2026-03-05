package modules.sentiment.services;

import core.session.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import modules.review.models.Review;
import modules.sentiment.models.AnalyseSentiment;

import java.util.List;
import java.util.Optional;

public class SentimentNotificationService {

    private Stage ownerStage;

    public SentimentNotificationService(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    /**
     * Notifie un warning d'analyse de sentiment
     */
    public void notifierWarning(AnalyseSentiment analyse, Review review) {
        Platform.runLater(() -> {
            Popup popup = new Popup();

            VBox content = new VBox(10);
            content.setStyle("-fx-background-color: #e74c3c; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
            content.setAlignment(Pos.CENTER);
            content.setPrefWidth(400);
            content.setPrefHeight(200);

            Label lblTitre = new Label("⚠️ ALERTE SENTIMENT NÉGATIF");
            lblTitre.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 18px;");

            Label lblReview = new Label("Review #" + review.getId());
            lblReview.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            Label lblCommentaire = new Label("\"" + tronquer(review.getCommentaire(), 100) + "\"");
            lblCommentaire.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-style: italic;");
            lblCommentaire.setWrapText(true);

            Label lblMots = new Label("Mots détectés: " + String.join(", ", analyse.getMotsClesNegatifs()));
            lblMots.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            content.getChildren().addAll(lblTitre, lblReview, lblCommentaire, lblMots);
            popup.getContent().add(content);

            if (ownerStage != null) {
                popup.show(ownerStage);
                popup.setX(ownerStage.getX() + ownerStage.getWidth() - 420);
                popup.setY(ownerStage.getY() + 100);
            }

            // Disparaît après 8 secondes
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(8));
            pause.setOnFinished(e -> popup.hide());
            pause.play();
        });
    }

    /**
     * Alerte popup pour les warnings critiques
     */
    public void alerterWarningCritique(AnalyseSentiment analyse, Review review) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ ALERTE CRITIQUE");
            alert.setHeaderText("Sentiment très négatif détecté");

            String content = String.format(
                    "Review #%d\n\n" +
                            "Commentaire: \"%s\"\n\n" +
                            "Mots négatifs: %s\n\n" +
                            "Voulez-vous voir cette review ?",
                    review.getId(),
                    tronquer(review.getCommentaire(), 200),
                    String.join(", ", analyse.getMotsClesNegatifs())
            );

            alert.setContentText(content);

            ButtonType btnVoir = new ButtonType("Voir la review");
            ButtonType btnIgnorer = new ButtonType("Ignorer");

            alert.getButtonTypes().setAll(btnVoir, btnIgnorer);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnVoir) {
                // Naviguer vers la review
                naviguerVersReview(review.getId());
            }
        });
    }

    /**
     * Notification groupée pour plusieurs warnings
     */
    public void notifierWarningsGroupes(List<AnalyseSentiment> warnings, List<Review> reviews) {
        if (warnings.isEmpty()) return;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Alertes sentiment");
            alert.setHeaderText(warnings.size() + " avis négatifs détectés");

            StringBuilder content = new StringBuilder();
            for (int i = 0; i < Math.min(warnings.size(), 5); i++) {
                AnalyseSentiment a = warnings.get(i);
                Review r = reviews.stream()
                        .filter(rev -> rev.getId() == a.getReviewId())
                        .findFirst().orElse(null);

                if (r != null) {
                    content.append("• ")
                            .append(tronquer(r.getCommentaire(), 50))
                            .append(" → ")
                            .append(String.join(", ", a.getMotsClesNegatifs()))
                            .append("\n");
                }
            }

            if (warnings.size() > 5) {
                content.append("... et ").append(warnings.size() - 5).append(" autres");
            }

            alert.setContentText(content.toString());
            alert.show();
        });
    }

    private String tronquer(String texte, int maxLength) {
        if (texte.length() <= maxLength) return texte;
        return texte.substring(0, maxLength) + "...";
    }

    private void naviguerVersReview(int reviewId) {
        // À implémenter selon votre navigation
        System.out.println("Navigation vers review #" + reviewId);
    }
}