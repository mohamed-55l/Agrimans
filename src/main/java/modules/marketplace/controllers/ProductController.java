package modules.marketplace.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modules.marketplace.models.Product;
import modules.marketplace.services.CartService;
import modules.marketplace.services.ProductService;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class ProductController {

    @FXML private Label userNameLabel;
    @FXML private Label cartCountLabel;
    @FXML private TabPane tabPane;
    @FXML private Tab sellerTab;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private FlowPane productsContainer;
    
    @FXML private TextField sellerSearchField;
    @FXML private VBox addProductForm;
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextField supplierField;
    @FXML private TextField imageField;
    @FXML private DatePicker expiryDatePicker;

    private ProductService productService = new ProductService();
    private CartService cartService = new CartService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            userNameLabel.setText("Welcome " + SessionManager.getCurrentUserName());
        }

        categoryFilter.getItems().addAll("Toutes les catégories", "Outils", "Graines", "Engrais", "Produits de récolte");
        categoryField.getItems().addAll("Outils", "Graines", "Engrais", "Produits de récolte");
        
        loadAllProducts();
        updateCartCount();
        
        // Cacher le formulaire de produit par défaut
        hideAddProductForm();
    }

    private void loadAllProducts() {
        productsContainer.getChildren().clear();
        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            productsContainer.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");
        card.setPrefWidth(200);

        Label icon = new Label(p.getImageUrl() != null ? p.getImageUrl() : "📦");
        icon.setStyle("-fx-font-size: 30px;");
        
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label price = new Label(p.getPrice() + " TND");
        price.setTextFill(Color.GREEN);
        
        Button addToCart = new Button("Add to Cart");
        addToCart.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-cursor: hand;");
        addToCart.setOnAction(e -> {
            if (SessionManager.getCurrentUser() != null) {
                cartService.addToCart(SessionManager.getCurrentUser().getId(), p.getId(), 1);
                updateCartCount();
                AlertUtils.showInfo("Succès", p.getName() + " ajouté au panier.");
            } else {
                AlertUtils.showError("Erreur", "Veuillez vous connecter.");
            }
        });

        card.getChildren().addAll(icon, name, price, addToCart);
        return card;
    }

    private void updateCartCount() {
        if (SessionManager.getCurrentUser() != null) {
            int count = cartService.getCartItems(SessionManager.getCurrentUser().getId()).size();
            cartCountLabel.setText("(" + count + ")");
        }
    }

    @FXML private void openCart() {
        openModal("/fxml/marketplace/cart.fxml", "Mon Panier", 800, 600);
    }

    @FXML private void openChatbot() {
        openModal("/fxml/parcelle/AIChat.fxml", "Assistant IA", 600, 500);
    }

    @FXML private void handleLogout(ActionEvent e) {
        // La déconnexion est gérée par le layout principal
        AlertUtils.showInfo("Info", "Utilisez le menu latéral pour vous déconnecter.");
    }

    @FXML private void showAddProductForm() {
        addProductForm.setVisible(true);
        addProductForm.setManaged(true);
    }

    @FXML private void hideAddProductForm() {
        addProductForm.setVisible(false);
        addProductForm.setManaged(false);
    }

    @FXML private void checkSeasonalInfo() { AlertUtils.showInfo("Info", "Pas encore implémenté."); }
    @FXML private void showPlantingCalendar() { AlertUtils.showInfo("Info", "Pas encore implémenté."); }
    @FXML private void showHolidays() { AlertUtils.showInfo("Info", "Pas encore implémenté."); }

    @FXML private void handleSaveProduct() {
        try {
            Product p = new Product();
            p.setUserId(SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 1);
            p.setName(nameField.getText());
            p.setDescription(descField.getText());
            p.setPrice(Float.parseFloat(priceField.getText()));
            p.setQuantity(Float.parseFloat(quantityField.getText()));
            p.setCategoryName(categoryField.getValue());
            p.setSupplier(supplierField.getText());
            p.setImageUrl(imageField.getText());
            
            if (expiryDatePicker.getValue() != null) {
                p.setExpiryDate(Date.valueOf(expiryDatePicker.getValue()));
            }

            productService.addProduct(p);
            AlertUtils.showInfo("Succès", "Produit ajouté !");
            hideAddProductForm();
            loadAllProducts();
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Veuillez vérifier vos champs.");
        }
    }

    private void openModal(String fxml, String title, int width, int height) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
