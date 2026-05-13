package modules.marketplace.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import modules.marketplace.models.Product;
import modules.marketplace.services.CartService;
import modules.marketplace.services.ExchangeRateService;
import modules.marketplace.services.ProductService;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ProductController {

    // ── FXML bindings ────────────────────────────────────────────
    @FXML private Label userNameLabel;
    @FXML private Label cartCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> currencySelector;
    @FXML private FlowPane productsContainer;

    // Seller tab
    @FXML private VBox addProductForm;
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextField supplierField;
    @FXML private TextField imageField;
    @FXML private DatePicker expiryDatePicker;

    // ── Services ─────────────────────────────────────────────────
    private final ProductService     productService = new ProductService();
    private final CartService        cartService    = new CartService();
    private final ExchangeRateService xrService     = new ExchangeRateService();

    private String currentCurrency = "TND";
    private List<Product> allProducts;

    // ────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            userNameLabel.setText("Bienvenue, " + SessionManager.getCurrentUserName());
        }

        // Categories — mirrors Symfony's category options
        categoryFilter.getItems().addAll(
            "Toutes les catégories", "VEGETABLES", "FRUITS",
            "GRAINS", "HAY", "Outils", "Graines", "Engrais", "Produits de récolte"
        );
        categoryFilter.setValue("Toutes les catégories");

        if (categoryField != null) {
            categoryField.getItems().addAll(
                "VEGETABLES", "FRUITS", "GRAINS", "HAY",
                "Outils", "Graines", "Engrais", "Produits de récolte"
            );
        }

        // Currency selector
        if (currencySelector != null) {
            currencySelector.getItems().addAll("TND", "EUR", "USD");
            currencySelector.setValue("TND");
            currencySelector.setOnAction(e -> {
                currentCurrency = currencySelector.getValue();
                applyFilters();
            });
        }

        // Live search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        }
        categoryFilter.valueProperty().addListener((obs, old, val) -> applyFilters());

        loadAllProducts();
        updateCartBadge();
        hideAddProductForm();
    }

    // ── Load & filter ─────────────────────────────────────────────
    private void loadAllProducts() {
        allProducts = productService.getAllProducts();
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String cat    = categoryFilter.getValue();

        List<Product> filtered = allProducts.stream()
            .filter(p -> {
                boolean matchSearch = search.isEmpty()
                    || p.getName().toLowerCase().contains(search)
                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(search))
                    || (p.getSupplier()    != null && p.getSupplier().toLowerCase().contains(search));
                boolean matchCat = cat == null
                    || cat.equals("Toutes les catégories")
                    || cat.equalsIgnoreCase(p.getCategory());
                return matchSearch && matchCat;
            })
            .collect(Collectors.toList());

        renderProducts(filtered);
    }

    private void renderProducts(List<Product> products) {
        productsContainer.getChildren().clear();
        if (products.isEmpty()) {
            Label empty = new Label("Aucun produit trouvé.");
            empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 30;");
            productsContainer.getChildren().add(empty);
            return;
        }
        for (Product p : products) {
            productsContainer.getChildren().add(createProductCard(p));
        }
    }

    // ── Product card — mirrors Symfony product/_products_partial.html.twig ──
    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPrefWidth(230);
        card.setPadding(new Insets(0, 0, 14, 0));
        card.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: #334155;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0, 0, 3);"
        );

        // ── Image area ──
        StackPane imagePane = new StackPane();
        imagePane.setStyle(
            "-fx-background-color: rgba(0,0,0,0.25);" +
            "-fx-background-radius: 14 14 0 0;" +
            "-fx-min-height: 110px; -fx-pref-height: 110px;"
        );
        Label imgLabel = new Label(p.getImage() != null && !p.getImage().isBlank()
                ? p.getImage() : "📦");
        imgLabel.setStyle("-fx-font-size: 42px;");

        // Category badge (top-left)
        Label catBadge = new Label(p.getCategory() != null ? p.getCategory().toUpperCase() : "PRODUIT");
        catBadge.setStyle(
            "-fx-background-color: rgba(16,185,129,0.2); -fx-text-fill: #10B981;" +
            "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 3 7; -fx-background-radius: 10;"
        );
        StackPane.setAlignment(catBadge, Pos.TOP_LEFT);
        StackPane.setMargin(catBadge, new Insets(8, 0, 0, 8));

        // Stock badge (top-right)
        String stockText  = p.getQuantity() > 0 ? p.getQuantity() + " kg" : "Rupture";
        String stockColor = p.getQuantity() > 0 ? "rgba(16,185,129,0.2)" : "rgba(239,68,68,0.2)";
        String stockTxt   = p.getQuantity() > 0 ? "#10B981" : "#F87171";
        Label stockBadge = new Label(stockText);
        stockBadge.setStyle(
            "-fx-background-color: " + stockColor + "; -fx-text-fill: " + stockTxt + ";" +
            "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 3 7; -fx-background-radius: 10;"
        );
        StackPane.setAlignment(stockBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(stockBadge, new Insets(8, 8, 0, 0));

        imagePane.getChildren().addAll(imgLabel, catBadge, stockBadge);

        // ── Body ──
        VBox body = new VBox(6);
        body.setPadding(new Insets(10, 14, 0, 14));

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");
        name.setWrapText(true);

        // Price (converted if needed)
        String priceStr;
        if ("TND".equals(currentCurrency)) {
            priceStr = String.format("%.2f TND", p.getPrice());
        } else {
            double conv = xrService.convert(p.getPrice(), currentCurrency);
            priceStr = String.format("%.2f %s", conv, currentCurrency) +
                       String.format(" (%.2f TND)", p.getPrice());
        }
        Label price = new Label(priceStr);
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #10B981;");

        Label supplier = new Label(p.getSupplier() != null ? "🚚 " + p.getSupplier() : "");
        supplier.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        // ── Add to Cart row (quantity spinner + button) ──
        HBox addRow = new HBox(8);
        addRow.setPadding(new Insets(6, 14, 0, 14));
        addRow.setAlignment(Pos.CENTER_LEFT);

        // Quantity spinner
        Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, p.getQuantity()), 1);
        qtySpinner.setPrefWidth(78);
        qtySpinner.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-background-color: #0F172A;" +
            "-fx-border-color: #334155; -fx-border-radius: 7; -fx-background-radius: 7;"
        );
        qtySpinner.setEditable(true);

        Button addToCart = new Button("🛒 Ajouter");
        HBox.setHgrow(addToCart, Priority.ALWAYS);
        addToCart.setMaxWidth(Double.MAX_VALUE);
        boolean inStock = p.getQuantity() > 0;
        addToCart.setDisable(!inStock);
        addToCart.setStyle(
            "-fx-background-color: " + (inStock ? "#10B981" : "#334155") + ";" +
            "-fx-text-fill: " + (inStock ? "white" : "#64748B") + ";" +
            "-fx-font-weight: bold; -fx-font-size: 12px;" +
            "-fx-background-radius: 8; -fx-padding: 7 12; -fx-cursor: " + (inStock ? "hand" : "default") + ";"
        );

        addToCart.setOnMouseEntered(e -> {
            if (inStock) addToCart.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12px;" +
                "-fx-background-radius: 8; -fx-padding: 7 12; -fx-cursor: hand;"
            );
        });
        addToCart.setOnMouseExited(e -> {
            if (inStock) addToCart.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12px;" +
                "-fx-background-radius: 8; -fx-padding: 7 12; -fx-cursor: hand;"
            );
        });

        addToCart.setOnAction(e -> {
            if (SessionManager.getCurrentUser() == null) {
                AlertUtils.showError("Non connecté", "Veuillez vous connecter pour ajouter au panier.");
                return;
            }
            int qty = qtySpinner.getValue();
            cartService.addToCart(SessionManager.getCurrentUser().getId(), p.getId(), qty);
            updateCartBadge();
            AlertUtils.showInfo("Panier", qty + " kg de « " + p.getName() + " » ajouté(s) au panier !");
        });

        addRow.getChildren().addAll(qtySpinner, addToCart);
        body.getChildren().addAll(name, price, supplier);

        card.getChildren().addAll(imagePane, body, addRow);
        return card;
    }

    // ── Cart badge ────────────────────────────────────────────────
    private void updateCartBadge() {
        if (SessionManager.getCurrentUser() != null && cartCountLabel != null) {
            int count = cartService.getCartItems(SessionManager.getCurrentUser().getId()).size();
            cartCountLabel.setText(count > 0 ? String.valueOf(count) : "");
            cartCountLabel.setVisible(count > 0);
        }
    }

    // ── Open Cart modal ────────────────────────────────────────────
    @FXML private void openCart() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/marketplace/cart.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Mon Panier — Agrimans");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> updateCartBadge());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le panier.");
        }
    }

    @FXML private void openChatbot() {
        openModal("/fxml/parcelle/AIChat.fxml", "Assistant IA", 600, 500);
    }

    @FXML private void handleLogout(ActionEvent e) {
        AlertUtils.showInfo("Info", "Utilisez le menu latéral pour vous déconnecter.");
    }

    // ── Add Product (seller tab) ───────────────────────────────────
    @FXML private void showAddProductForm() {
        if (addProductForm != null) {
            addProductForm.setVisible(true);
            addProductForm.setManaged(true);
        }
    }

    @FXML private void hideAddProductForm() {
        if (addProductForm != null) {
            addProductForm.setVisible(false);
            addProductForm.setManaged(false);
        }
    }

    @FXML private void checkSeasonalInfo()   { AlertUtils.showInfo("Info", "Pas encore implémenté."); }
    @FXML private void showPlantingCalendar(){ AlertUtils.showInfo("Info", "Pas encore implémenté."); }
    @FXML private void showHolidays()        { AlertUtils.showInfo("Info", "Pas encore implémenté."); }

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
