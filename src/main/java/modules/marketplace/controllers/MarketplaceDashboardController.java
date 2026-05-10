package modules.marketplace.controllers;

import core.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.marketplace.models.Product;
import modules.marketplace.services.ProductService;

import java.util.List;

public class MarketplaceDashboardController {

    @FXML private Label totalProductsLabel;
    @FXML private Label totalValueLabel;
    @FXML private FlowPane productsGrid;

    private ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        productsGrid.getChildren().clear();
        
        List<Product> products = productService.getAllProducts();
        
        int totalProducts = 0;
        float totalValue = 0;

        for (Product p : products) {
            totalProducts++;
            totalValue += (p.getPrice() * p.getQuantity());
            
            // Render card
            productsGrid.getChildren().add(createAdminProductCard(p));
        }

        totalProductsLabel.setText(String.valueOf(totalProducts));
        totalValueLabel.setText(String.format("%.2f TND", totalValue));
    }

    private VBox createAdminProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: #1E293B; -fx-padding: 15; -fx-background-radius: 12; " +
                      "-fx-border-color: #334155; -fx-border-width: 1; -fx-border-radius: 12; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label(p.getCategory() != null ? p.getCategory().toUpperCase() : "PRODUIT");
        badge.setStyle("-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10B981; " +
                       "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 12;");

        Label weight = new Label(p.getQuantity() + " kg");
        weight.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        header.getChildren().addAll(badge, weight);

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label supplier = new Label("Producteur: " + (p.getSupplier() != null ? p.getSupplier() : "Ferme locale"));
        supplier.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(String.format("%.2f TND", p.getPrice()));
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10B981;");

        priceBox.getChildren().add(price);

        card.getChildren().addAll(header, name, supplier, priceBox);
        return card;
    }

    @FXML private VBox addProductOverlay;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField supplierField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextField imageField;

    @FXML
    private void showAddProductForm() {
        if (categoryField.getItems().isEmpty()) {
            categoryField.getItems().addAll("Outils", "Graines", "Engrais", "Produits de récolte");
        }
        addProductOverlay.setVisible(true);
        addProductOverlay.toFront();
    }

    @FXML
    private void hideAddProductForm() {
        addProductOverlay.setVisible(false);
    }

    @FXML
    private void handleSaveProduct(ActionEvent event) {
        try {
            Product p = new Product();
            p.setSellerId(core.session.SessionManager.getCurrentUser() != null ? core.session.SessionManager.getCurrentUser().getId() : 1);
            p.setName(nameField.getText());
            p.setDescription(descField.getText());
            p.setPrice(Float.parseFloat(priceField.getText()));
            p.setQuantity(Integer.parseInt(quantityField.getText()));
            p.setCategory(categoryField.getValue());
            p.setSupplier(supplierField.getText());
            p.setImage(imageField.getText());

            if (expiryDatePicker.getValue() != null) {
                p.setExpiryDate(java.sql.Date.valueOf(expiryDatePicker.getValue()));
            }

            productService.addProduct(p);
            AlertUtils.showInfo("Succès", "Produit ajouté !");
            hideAddProductForm();
            loadDashboardData();
            
            // Reset fields
            nameField.clear(); descField.clear(); priceField.clear(); quantityField.clear();
            supplierField.clear(); imageField.clear(); expiryDatePicker.setValue(null);
            categoryField.setValue(null);

        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Veuillez entrer des valeurs numériques valides pour le prix et la quantité.");
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Veuillez vérifier vos champs.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AlertUtils.showInfo("Info", "Utilisez le menu latéral de l'application pour vous déconnecter.");
    }
}
