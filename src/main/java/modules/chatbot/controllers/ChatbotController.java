package modules.chatbot.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modules.chatbot.models.Message;
import modules.chatbot.services.ChatbotService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatbotController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputMessage;
    @FXML private Button btnEnvoyer;
    @FXML private ListView<String> questionsListView;

    private ChatbotService chatbotService;
    private ObservableList<Message> messages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatbotService = new ChatbotService();
        messages = FXCollections.observableArrayList();

        // Configurer l'envoi avec la touche Entrée
        inputMessage.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                envoyerMessage();
            }
        });

        // Charger les questions fréquentes
        questionsListView.setItems(FXCollections.observableArrayList(
                chatbotService.getQuestionsFrequentes()
        ));

        // Double-clic sur une question pour l'envoyer
        questionsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String question = questionsListView.getSelectionModel().getSelectedItem();
                if (question != null) {
                    inputMessage.setText(question);
                    envoyerMessage();
                }
            }
        });

        // Message de bienvenue
        ajouterMessageBot("👋 Bonjour " + SessionManager.getCurrentUserName() +
                " !\n\nJe suis votre assistant agricole virtuel.\n" +
                "Posez-moi vos questions sur :\n" +
                "🔧 Les équipements\n" +
                "🌾 Les cultures\n" +
                "🦠 Les maladies\n" +
                "💧 L'irrigation\n" +
                "🧪 Les engrais\n\n" +
                "💡 Tapez 'aide' pour voir toutes les commandes !");

        // Scroll automatique vers le bas quand de nouveaux messages arrivent
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void envoyerMessage() {
        String texte = inputMessage.getText().trim();
        if (texte.isEmpty()) return;

        // Ajouter le message de l'utilisateur
        ajouterMessageUser(texte);
        inputMessage.clear();

        // Obtenir et ajouter la réponse du bot
        String reponse = chatbotService.getReponse(texte);
        ajouterMessageBot(reponse);
    }

    private void ajouterMessageUser(String texte) {
        Message msg = new Message(texte, "Moi");
        messages.add(msg);
        ajouterBulleMessage(msg);
    }

    private void ajouterMessageBot(String texte) {
        Message msg = new Message(texte, "Bot");
        messages.add(msg);
        ajouterBulleMessage(msg);
    }

    private void ajouterBulleMessage(Message message) {
        HBox bulle = new HBox();
        bulle.setPadding(new Insets(5, 10, 5, 10));
        bulle.setMaxWidth(500);

        Label label = new Label(message.getContenu());
        label.setWrapText(true);

        if (message.isUser()) {
            // Message de l'utilisateur (aligné à droite)
            bulle.setAlignment(Pos.CENTER_RIGHT);
            label.setStyle("-fx-background-color: #4B8B3B; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 10;");
            bulle.getChildren().add(label);
        } else {
            // Message du bot (aligné à gauche avec heure)
            bulle.setAlignment(Pos.CENTER_LEFT);
            label.setStyle("-fx-background-color: #E8E8E8; -fx-text-fill: #2E3D27; -fx-padding: 10; -fx-background-radius: 10;");

            // Conteneur pour le label et l'heure
            HBox messageBox = new HBox(5);
            messageBox.setAlignment(Pos.CENTER_LEFT);

            Label heure = new Label(message.getHeure());
            heure.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");

            messageBox.getChildren().addAll(label, heure);
            bulle.getChildren().add(messageBox);
        }

        messagesContainer.getChildren().add(bulle);
    }

    @FXML
    private void effacerConversation() {
        messagesContainer.getChildren().clear();
        messages.clear();
        ajouterMessageBot("🗑️ Conversation effacée. Comment puis-je vous aider ?");
    }

    @FXML
    private void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard/admin_dashboard.fxml"));
            Stage stage = (Stage) inputMessage.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au dashboard");
        }
    }
}