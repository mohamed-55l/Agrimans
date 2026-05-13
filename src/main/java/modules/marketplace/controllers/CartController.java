package modules.marketplace.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import modules.marketplace.models.CartItem;
import modules.marketplace.services.CartService;
import modules.marketplace.services.ExchangeRateService;
import modules.marketplace.services.ProductService;

import java.util.List;
import java.util.Optional;

public class CartController {

    // ── FXML bindings ────────────────────────────────────────────
    @FXML private Label cartCountLabel;
    @FXML private Label totalTNDLabel;
    @FXML private Label totalConvertedLabel;
    @FXML private Label currencyLabel;
    @FXML private VBox  cartItemsContainer;
    @FXML private VBox  emptyCartPane;
    @FXML private VBox  filledCartPane;
    @FXML private ComboBox<String> currencyCombo;

    // ── Services ─────────────────────────────────────────────────
    private final CartService        cartService   = new CartService();
    private final ProductService     productService = new ProductService();
    private final ExchangeRateService xrService    = new ExchangeRateService();

    private String currentCurrency = "TND";
    private List<CartItem> cartItems;

    // ────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (currencyCombo != null) {
            currencyCombo.getItems().setAll("TND", "EUR", "USD");
            currencyCombo.setValue("TND");
            currencyCombo.setOnAction(e -> {
                currentCurrency = currencyCombo.getValue();
                refreshTotals();
            });
        }
        loadCart();
    }

    // ── Load / refresh ───────────────────────────────────────────
    private void loadCart() {
        if (SessionManager.getCurrentUser() == null) return;
        int userId = SessionManager.getCurrentUser().getId();
        cartItems = cartService.getCartItems(userId);
        renderCart();
    }

    private void renderCart() {
        cartItemsContainer.getChildren().clear();

        if (cartItems == null || cartItems.isEmpty()) {
            emptyCartPane.setVisible(true);
            emptyCartPane.setManaged(true);
            filledCartPane.setVisible(false);
            filledCartPane.setManaged(false);
            if (cartCountLabel != null) cartCountLabel.setText("0 article(s)");
            return;
        }

        emptyCartPane.setVisible(false);
        emptyCartPane.setManaged(false);
        filledCartPane.setVisible(true);
        filledCartPane.setManaged(true);

        if (cartCountLabel != null)
            cartCountLabel.setText(cartItems.size() + " article(s)");

        for (CartItem item : cartItems) {
            cartItemsContainer.getChildren().add(buildCartRow(item));
        }
        refreshTotals();
    }

    /** Build a single cart row — mirrors Symfony cart/index.html.twig <tr> */
    private HBox buildCartRow(CartItem item) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #334155;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12;"
        );

        // Product image / emoji
        Label icon = new Label(item.getProduct().getImage() != null && !item.getProduct().getImage().isBlank()
                ? item.getProduct().getImage() : "📦");
        icon.setStyle("-fx-font-size: 26px; -fx-min-width: 40px;");

        // Product name + category badge
        VBox nameBox = new VBox(4);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label name = new Label(item.getProduct().getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label cat = new Label(item.getProduct().getCategory() != null
                ? item.getProduct().getCategory().toUpperCase() : "PRODUIT");
        cat.setStyle(
            "-fx-background-color: rgba(16,185,129,0.18);" +
            "-fx-text-fill: #10B981;" +
            "-fx-font-size: 10px; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-radius: 10;"
        );

        Label supplier = new Label(item.getProduct().getSupplier() != null
                ? "Producteur: " + item.getProduct().getSupplier() : "");
        supplier.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        nameBox.getChildren().addAll(name, cat, supplier);

        // Unit price
        Label unitPrice = new Label(formatPrice(item.getProduct().getPrice()));
        unitPrice.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-min-width: 90px; -fx-alignment: CENTER;");

        // Quantity with +/- controls
        HBox qtyBox = new HBox(6);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.setStyle("-fx-min-width: 120px;");

        Button minus = new Button("−");
        minus.setStyle(
            "-fx-background-color: #334155; -fx-text-fill: #F1F5F9;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 6; -fx-padding: 2 10; -fx-cursor: hand;"
        );

        Label qtyLabel = new Label(String.format("%.0f kg", item.getQuantity()));
        qtyLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #F1F5F9; -fx-min-width: 50px; -fx-alignment: CENTER;");

        Button plus = new Button("+");
        plus.setStyle(
            "-fx-background-color: #334155; -fx-text-fill: #F1F5F9;" +
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-background-radius: 6; -fx-padding: 2 10; -fx-cursor: hand;"
        );

        minus.setOnAction(e -> {
            if (item.getQuantity() > 1) {
                cartService.updateQuantity(item.getId(), (int)(item.getQuantity() - 1));
                loadCart();
            } else {
                cartService.removeFromCart(item.getId());
                loadCart();
            }
        });
        plus.setOnAction(e -> {
            cartService.updateQuantity(item.getId(), (int)(item.getQuantity() + 1));
            loadCart();
        });

        qtyBox.getChildren().addAll(minus, qtyLabel, plus);

        // Subtotal
        double lineTotal = item.getProduct().getPrice() * item.getQuantity();
        Label subtotal = new Label(formatPrice((float) lineTotal));
        subtotal.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-text-fill: #10B981; -fx-min-width: 100px; -fx-alignment: CENTER_RIGHT;"
        );

        // Remove button (🗑 red — Symfony's trash icon)
        Button removeBtn = new Button("🗑");
        removeBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.15);" +
            "-fx-text-fill: #F87171;" +
            "-fx-border-color: rgba(239,68,68,0.4);" +
            "-fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-font-size: 14px;" +
            "-fx-padding: 6 10; -fx-cursor: hand;"
        );
        removeBtn.setOnMouseEntered(ev -> removeBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.35); -fx-text-fill: #FCA5A5;" +
            "-fx-border-color: #F87171; -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 6 10; -fx-cursor: hand;"
        ));
        removeBtn.setOnMouseExited(ev -> removeBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #F87171;" +
            "-fx-border-color: rgba(239,68,68,0.4); -fx-border-width: 1; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 6 10; -fx-cursor: hand;"
        ));
        removeBtn.setOnAction(e -> {
            cartService.removeFromCart(item.getId());
            loadCart();
        });

        row.getChildren().addAll(icon, nameBox, unitPrice, qtyBox, subtotal, removeBtn);
        return row;
    }

    private void refreshTotals() {
        if (cartItems == null) return;
        float totalTND = 0;
        for (CartItem item : cartItems) {
            totalTND += item.getProduct().getPrice() * item.getQuantity();
        }
        if (totalTNDLabel != null)
            totalTNDLabel.setText(String.format("%.2f TND", totalTND));

        if (totalConvertedLabel != null && !"TND".equals(currentCurrency)) {
            double converted = xrService.convert(totalTND, currentCurrency);
            totalConvertedLabel.setText(String.format("≈ %.2f %s", converted, currentCurrency));
            totalConvertedLabel.setVisible(true);
        } else if (totalConvertedLabel != null) {
            totalConvertedLabel.setVisible(false);
        }
    }

    private String formatPrice(float amountTND) {
        if ("TND".equals(currentCurrency)) {
            return String.format("%.2f TND", amountTND);
        }
        double converted = xrService.convert(amountTND, currentCurrency);
        return String.format("%.2f %s", converted, currentCurrency);
    }

    // ── Actions ──────────────────────────────────────────────────

    /** Vider le panier — Symfony: clearCart() */
    @FXML
    private void handleClearCart(ActionEvent event) {
        if (cartItems == null || cartItems.isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vider le panier");
        confirm.setHeaderText("Êtes-vous sûr de vouloir vider tout le panier ?");
        confirm.setContentText("Cette action retirera tous les articles.");
        styleConfirmDialog(confirm, false);

        ButtonType clearType  = new ButtonType("Vider", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(clearType, cancelType);
        styleConfirmButtons(confirm, clearType, cancelType, true);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == clearType) {
            cartService.clearCart(SessionManager.getCurrentUser().getId());
            loadCart();
            AlertUtils.showInfo("Panier vidé", "Votre panier a été vidé.");
        }
    }

    /** Checkout — mirrors Symfony checkout + stock deduction (no Stripe in Java, simulated) */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems == null || cartItems.isEmpty()) {
            AlertUtils.showError("Panier vide", "Veuillez ajouter des produits d'abord.");
            return;
        }

        // Calculate total
        float totalTND = 0;
        for (CartItem item : cartItems) totalTND += item.getProduct().getPrice() * item.getQuantity();

        double totalEUR = xrService.convert(totalTND, "EUR");
        double totalUSD = xrService.convert(totalTND, "USD");

        // Checkout confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la commande");
        confirm.setHeaderText("Récapitulatif de votre commande");
        confirm.setContentText(String.format(
            "Total : %.2f TND\n" +
            "       ≈ %.2f EUR\n" +
            "       ≈ %.2f USD\n\n" +
            "%d article(s) dans votre panier.\n\n" +
            "Confirmer le paiement ?",
            totalTND, totalEUR, totalUSD, cartItems.size()
        ));
        styleConfirmDialog(confirm, false);

        ButtonType payType    = new ButtonType("✔  Payer " + String.format("%.2f TND", totalTND), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(payType, cancelType);
        styleConfirmButtons(confirm, payType, cancelType, false);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == payType) {
            // Deduct stock — mirrors Symfony checkoutSuccess()
            for (CartItem item : cartItems) {
                int newQty = Math.max(0, item.getProduct().getQuantity() - (int) item.getQuantity());
                item.getProduct().setQuantity(newQty);
                productService.updateProductQuantity(item.getProduct().getId(), newQty);
            }
            // Clear cart
            cartService.clearCart(SessionManager.getCurrentUser().getId());
            loadCart();
            AlertUtils.showInfo("Paiement réussi ! 🎉",
                "Merci pour votre commande !\n" +
                "Montant payé : " + String.format("%.2f TND", totalTND) + "\n" +
                "Votre commande a été enregistrée."
            );
        }
    }

    /** Back button — close the modal window */
    @FXML
    private void backToProducts(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    // ── Helper: style dialogs like Symfony dark theme ─────────────
    private void styleConfirmDialog(Alert alert, boolean danger) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle(
            "-fx-background-color: #1E293B;" +
            "-fx-border-color: " + (danger ? "rgba(239,68,68,0.4)" : "rgba(16,185,129,0.3)") + ";" +
            "-fx-border-radius: 12; -fx-background-radius: 12;"
        );
        Node content = pane.lookup(".content.label");
        if (content != null) content.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 13px;");
        Node header = pane.lookup(".header-panel");
        if (header != null) header.setStyle("-fx-background-color: transparent;");
    }

    private void styleConfirmButtons(Alert alert, ButtonType primary, ButtonType cancel, boolean danger) {
        alert.setOnShown(ev -> {
            DialogPane pane = alert.getDialogPane();
            Button primaryBtn = (Button) pane.lookupButton(primary);
            Button cancelBtn  = (Button) pane.lookupButton(cancel);
            String bg = danger ? "#e74c3c" : "#10B981";
            if (primaryBtn != null)
                primaryBtn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
            if (cancelBtn != null)
                cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;" +
                        "-fx-border-color: #64748B; -fx-border-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;");
        });
    }
}
