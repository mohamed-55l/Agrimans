package modules.marketplace.controllers;

import core.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import modules.marketplace.models.Product;
import modules.marketplace.services.ProductService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MarketplaceDashboardController {

    // ── Stats labels ────────────────────────────────────────────
    @FXML private Label totalProductsLabel;
    @FXML private Label totalValueLabel;
    @FXML private FlowPane productsGrid;

    // ── Add-product overlay ──────────────────────────────────────
    @FXML private VBox addProductOverlay;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField supplierField;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextField imageField;

    // ── Edit-product overlay ─────────────────────────────────────
    @FXML private VBox editProductOverlay;
    @FXML private Label editProductTitle;
    @FXML private TextField editNameField;
    @FXML private ComboBox<String> editCategoryField;
    @FXML private TextArea editDescField;
    @FXML private TextField editPriceField;
    @FXML private TextField editQuantityField;
    @FXML private TextField editSupplierField;
    @FXML private DatePicker editExpiryDatePicker;
    @FXML private TextField editImageField;

    private ProductService productService = new ProductService();

    /** Id of the product currently being edited */
    private int editingProductId = -1;

    // ────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadDashboardData();
    }

    // ── Load / refresh ───────────────────────────────────────────
    private void loadDashboardData() {
        productsGrid.getChildren().clear();

        List<Product> products = productService.getAllProducts();

        int   totalProducts = products.size();
        float totalValue    = 0;

        for (Product p : products) {
            totalValue += (p.getPrice() * p.getQuantity());
            productsGrid.getChildren().add(createProductCard(p));
        }

        totalProductsLabel.setText(String.valueOf(totalProducts));
        totalValueLabel.setText(String.format("%.2f TND", totalValue));
    }

    // ── Card builder (with Edit + Delete buttons) ─────────────────
    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-padding: 15;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #334155;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);"
        );

        // Header: category badge + weight
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label(p.getCategory() != null ? p.getCategory().toUpperCase() : "PRODUIT");
        badge.setStyle(
            "-fx-background-color: rgba(16,185,129,0.15);" +
            "-fx-text-fill: #10B981;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 4 8;" +
            "-fx-background-radius: 12;"
        );

        Label weight = new Label(p.getQuantity() + " kg");
        weight.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        // Stock indicator
        String stockColor = p.getQuantity() > 0 ? "#10B981" : "#EF4444";
        String stockText  = p.getQuantity() > 0 ? "En stock" : "Rupture";
        Label stockBadge = new Label(stockText);
        stockBadge.setStyle(
            "-fx-background-color: rgba(0,0,0,0.2);" +
            "-fx-text-fill: " + stockColor + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 3 6;" +
            "-fx-background-radius: 8;"
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(badge, weight, spacer, stockBadge);

        // Product name
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");
        name.setWrapText(true);

        // Supplier
        Label supplier = new Label("Producteur: " + (p.getSupplier() != null ? p.getSupplier() : "—"));
        supplier.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        // Expiry date
        if (p.getExpiryDate() != null) {
            Label expiry = new Label("Exp: " + p.getExpiryDate().toString());
            expiry.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
            card.getChildren().add(expiry);
        }

        // Price
        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label price = new Label(String.format("%.2f TND", p.getPrice()));
        price.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10B981;");
        priceBox.getChildren().add(price);

        // ── Action buttons (Edit + Delete) ──────────────────────
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        Button editBtn = new Button("✏  Modifier");
        editBtn.setStyle(
            "-fx-background-color: rgba(59,130,246,0.15);" +
            "-fx-text-fill: #60A5FA;" +
            "-fx-border-color: rgba(59,130,246,0.4);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;"
        );
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
            "-fx-background-color: rgba(59,130,246,0.3);" +
            "-fx-text-fill: #93C5FD;" +
            "-fx-border-color: #60A5FA;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;"
        ));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(
            "-fx-background-color: rgba(59,130,246,0.15);" +
            "-fx-text-fill: #60A5FA;" +
            "-fx-border-color: rgba(59,130,246,0.4);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 14;" +
            "-fx-cursor: hand;"
        ));
        editBtn.setOnAction(e -> openEditOverlay(p));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.15);" +
            "-fx-text-fill: #F87171;" +
            "-fx-border-color: rgba(239,68,68,0.4);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 5 10;" +
            "-fx-cursor: hand;"
        );
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.35);" +
            "-fx-text-fill: #FCA5A5;" +
            "-fx-border-color: #F87171;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 5 10;" +
            "-fx-cursor: hand;"
        ));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.15);" +
            "-fx-text-fill: #F87171;" +
            "-fx-border-color: rgba(239,68,68,0.4);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 5 10;" +
            "-fx-cursor: hand;"
        ));
        deleteBtn.setOnAction(e -> confirmAndDelete(p));

        actionBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(header, name, supplier, priceBox, actionBox);
        return card;
    }

    // ── Delete confirmation (styled like Symfony modal) ───────────
    private void confirmAndDelete(Product p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer ce produit ?");
        alert.setHeaderText("Vous allez supprimer « " + p.getName() + " »");
        alert.setContentText("Cette action est irréversible. Voulez-vous continuer ?");

        // Style the dialog
        DialogPane pane = alert.getDialogPane();
        pane.setStyle(
            "-fx-background-color: #1a2a1a;" +
            "-fx-border-color: rgba(231,76,60,0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        pane.lookup(".content.label").setStyle("-fx-text-fill: rgba(255,255,255,0.7);");

        ButtonType deleteType  = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType  = new ButtonType("Annuler",   ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteType, cancelType);

        // Style buttons after dialog is shown
        alert.setOnShown(ev -> {
            Button deleteB = (Button) pane.lookupButton(deleteType);
            Button cancelB = (Button) pane.lookupButton(cancelType);
            deleteB.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"
            );
            cancelB.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #94A3B8;" +
                "-fx-border-color: #64748B; -fx-border-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"
            );
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteType) {
            productService.deleteProduct(p.getId());
            loadDashboardData();
            AlertUtils.showInfo("Succès", "Produit « " + p.getName() + " » supprimé.");
        }
    }

    // ── Edit overlay ──────────────────────────────────────────────
    private void openEditOverlay(Product p) {
        editingProductId = p.getId();

        // Title
        editProductTitle.setText("#" + p.getId() + " — " + p.getName());

        // Pre-fill fields
        editNameField.setText(p.getName() != null ? p.getName() : "");
        editDescField.setText(p.getDescription() != null ? p.getDescription() : "");
        editPriceField.setText(String.valueOf(p.getPrice()));
        editQuantityField.setText(String.valueOf(p.getQuantity()));
        editSupplierField.setText(p.getSupplier() != null ? p.getSupplier() : "");
        editImageField.setText(p.getImage() != null ? p.getImage() : "");

        // Category combo
        if (editCategoryField.getItems().isEmpty()) {
            editCategoryField.getItems().addAll(
                "Outils", "Graines", "Engrais", "Produits de récolte",
                "VEGETABLES", "FRUITS", "SEEDS", "TOOLS", "FERTILIZERS"
            );
        }
        editCategoryField.setValue(p.getCategory());

        // Expiry date
        if (p.getExpiryDate() != null) {
            editExpiryDatePicker.setValue(p.getExpiryDate().toLocalDate());
        } else {
            editExpiryDatePicker.setValue(null);
        }

        editProductOverlay.setVisible(true);
        editProductOverlay.toFront();
    }

    @FXML
    private void hideEditProductForm() {
        editProductOverlay.setVisible(false);
        editingProductId = -1;
    }

    @FXML
    private void handleUpdateProduct(ActionEvent event) {
        if (editingProductId < 0) return;
        try {
            String name     = editNameField.getText().trim();
            String desc     = editDescField.getText().trim();
            String priceStr = editPriceField.getText().trim();
            String qtyStr   = editQuantityField.getText().trim();

            // Basic validation (mirrors Symfony form constraints)
            if (name.isEmpty()) {
                AlertUtils.showError("Validation", "Le nom du produit est obligatoire.");
                return;
            }
            if (desc.length() < 10) {
                AlertUtils.showError("Validation", "La description doit contenir au moins 10 caractères.");
                return;
            }
            float  price    = Float.parseFloat(priceStr);
            int    quantity = Integer.parseInt(qtyStr);
            if (price <= 0) {
                AlertUtils.showError("Validation", "Le prix doit être positif (> 0).");
                return;
            }
            if (quantity <= 0) {
                AlertUtils.showError("Validation", "La quantité doit être positive (> 0).");
                return;
            }

            Product p = new Product();
            p.setId(editingProductId);
            p.setName(name);
            p.setDescription(desc);
            p.setPrice(price);
            p.setQuantity(quantity);
            p.setCategory(editCategoryField.getValue());
            p.setSupplier(editSupplierField.getText().trim());
            p.setImage(editImageField.getText().trim());

            if (editExpiryDatePicker.getValue() != null) {
                LocalDate ld = editExpiryDatePicker.getValue();
                if (ld.isBefore(LocalDate.now().plusDays(1))) {
                    AlertUtils.showError("Validation", "La date d'expiration doit être au minimum demain.");
                    return;
                }
                p.setExpiryDate(Date.valueOf(ld));
            }

            productService.updateProduct(p);
            AlertUtils.showInfo("Succès", "Produit mis à jour !");
            hideEditProductForm();
            loadDashboardData();

        } catch (NumberFormatException e) {
            AlertUtils.showError("Erreur", "Prix et quantité doivent être des nombres valides.");
        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible de mettre à jour le produit.");
            e.printStackTrace();
        }
    }

    // ── Add-product overlay ───────────────────────────────────────
    @FXML
    private void showAddProductForm() {
        if (categoryField.getItems().isEmpty()) {
            categoryField.getItems().addAll(
                "Outils", "Graines", "Engrais", "Produits de récolte",
                "VEGETABLES", "FRUITS", "SEEDS", "TOOLS", "FERTILIZERS"
            );
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
            p.setSellerId(core.session.SessionManager.getCurrentUser() != null
                    ? core.session.SessionManager.getCurrentUser().getId() : 1);
            p.setName(nameField.getText());
            p.setDescription(descField.getText());
            p.setPrice(Float.parseFloat(priceField.getText()));
            p.setQuantity(Integer.parseInt(quantityField.getText()));
            p.setCategory(categoryField.getValue());
            p.setSupplier(supplierField.getText());
            p.setImage(imageField.getText());

            if (expiryDatePicker.getValue() != null) {
                p.setExpiryDate(Date.valueOf(expiryDatePicker.getValue()));
            }

            productService.addProduct(p);
            AlertUtils.showInfo("Succès", "Produit ajouté !");
            hideAddProductForm();
            loadDashboardData();

            // Reset fields
            nameField.clear(); descField.clear(); priceField.clear();
            quantityField.clear(); supplierField.clear(); imageField.clear();
            expiryDatePicker.setValue(null); categoryField.setValue(null);

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
