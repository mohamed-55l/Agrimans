package modules.marketplace.controllers;

import core.session.SessionManager;
import core.utils.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import modules.marketplace.models.CartItem;
import modules.marketplace.services.CartService;

import java.util.List;

public class CartController {

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colProduct;
    @FXML private TableColumn<CartItem, Float> colQuantity;
    @FXML private TableColumn<CartItem, String> colPrice;
    @FXML private TableColumn<CartItem, String> colTotal;
    @FXML private TableColumn<CartItem, Void> colAction;
    @FXML private Label totalLabel;

    private CartService cartService = new CartService();
    private ObservableList<CartItem> cartList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colProduct.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f TND", cellData.getValue().getProduct().getPrice())));
        colTotal.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.2f TND", cellData.getValue().getQuantity() * cellData.getValue().getProduct().getPrice())));
        
        setupActionColumn();
        loadCartData();
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    CartItem data = getTableView().getItems().get(getIndex());
                    cartService.removeFromCart(data.getId());
                    loadCartData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    private void loadCartData() {
        if (SessionManager.getCurrentUser() != null) {
            cartList.clear();
            List<CartItem> items = cartService.getCartItems(SessionManager.getCurrentUser().getId());
            cartList.addAll(items);
            cartTable.setItems(cartList);
            calculateTotal(items);
        }
    }

    private void calculateTotal(List<CartItem> items) {
        float total = 0;
        for (CartItem item : items) {
            total += item.getQuantity() * item.getProduct().getPrice();
        }
        totalLabel.setText(String.format("%.2f TND", total));
    }

    @FXML
    private void backToProducts(ActionEvent event) {
        // Option 1: si c'est une modal, fermer la fenêtre
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartList.isEmpty()) {
            AlertUtils.showError("Panier vide", "Veuillez ajouter des produits d'abord.");
            return;
        }

        AlertUtils.showInfo("Commande validée", "Votre achat a été enregistré avec succès !");
        
        if (SessionManager.getCurrentUser() != null) {
            cartService.clearCart(SessionManager.getCurrentUser().getId());
            loadCartData();
        }
    }
}
