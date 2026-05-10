package modules.parcelle.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import modules.parcelle.services.AIService;

public class AIChatController {

    @FXML
    private TextField questionField;

    @FXML
    private TextArea chatArea;

    // 🔹 Cette méthode est appelée automatiquement au chargement du FXML
    @FXML
    public void initialize() {

        // envoyer la question quand on appuie sur ENTER
        questionField.setOnAction(e -> envoyerQuestion());

    }

    @FXML
    private void envoyerQuestion() {

        String question = questionField.getText();

        if (question == null || question.trim().isEmpty()) {
            return;
        }

        chatArea.appendText("👨‍🌾 Agriculteur : " + question + "\n");

        questionField.clear();

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return AIService.poserQuestion(question);
            }
        };

        task.setOnSucceeded(event -> {
            String reponse = task.getValue();
            chatArea.appendText("🤖 IA : " + reponse + "\n\n");
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            chatArea.appendText("⚠️ Erreur IA : " + error.getMessage() + "\n\n");
        });

        new Thread(task).start();
    }
}