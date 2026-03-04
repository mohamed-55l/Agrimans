package modules.chatbot.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private final StringProperty contenu;
    private final StringProperty expediteur;
    private final StringProperty heure;

    public Message(String contenu, String expediteur) {
        this.contenu = new SimpleStringProperty(contenu);
        this.expediteur = new SimpleStringProperty(expediteur);
        this.heure = new SimpleStringProperty(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

    public String getContenu() { return contenu.get(); }
    public StringProperty contenuProperty() { return contenu; }

    public String getExpediteur() { return expediteur.get(); }
    public StringProperty expediteurProperty() { return expediteur; }

    public String getHeure() { return heure.get(); }
    public StringProperty heureProperty() { return heure; }

    public boolean isUser() {
        return "Moi".equals(expediteur.get());
    }
}