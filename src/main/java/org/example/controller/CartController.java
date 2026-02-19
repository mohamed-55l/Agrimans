package org.example.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.dao.CartDAO;
import org.example.model.CartItem;
import org.example.model.Session;
import org.example.model.user;

import java.io.IOException;
import java.util.Optional;

public class CartController {

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> colProduct;

    @FXML
    private TableColumn<CartItem, Integer> colQuantity;

    @FXML
    private TableColumn<CartItem, Double> colPrice;

    @FXML
    private TableColumn<CartItem, Double> colTotal;

    @FXML
    private TableColumn<CartItem, Void> colAction;

    @FXML
    private Label totalLabel;

    private CartDAO cartDAO = new CartDAO();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("CartController initialized");
        setupTableColumns();
        loadCartItems();
    }

    private void setupTableColumns() {
        colProduct.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colPrice.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getProduct().getPrice()).asObject());

        colTotal.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(
                        cellData.getValue().getProduct().getPrice() * cellData.getValue().getQuantity()
                ).asObject());

        // Add remove button column
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    handleRemoveItem(item);
                });
                removeButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    private void loadCartItems() {
        user currentUser = Session.getCurrentUser();
        System.out.println("Loading cart items for user: " + (currentUser != null ? currentUser.getName() : "null"));

        if (currentUser == null) {
            showAlert("Error", "Please login first!", Alert.AlertType.ERROR);
            return;
        }

        cartItems.clear();
        cartItems.addAll(cartDAO.getCartItems(currentUser.getId()));
        cartTable.setItems(cartItems);
        updateTotal();

        System.out.println("Cart loaded with " + cartItems.size() + " items");
    }

    private void updateTotal() {
        double total = cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    public void handleRemoveItem(CartItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Item");
        confirm.setHeaderText("Remove from cart");
        confirm.setContentText("Are you sure you want to remove " + item.getProduct().getName() + " from your cart?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cartDAO.removeFromCart(item.getId());
            loadCartItems(); // Refresh the cart
            showAlert("Success", "Item removed from cart!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Error", "Your cart is empty!", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Checkout");
        confirm.setHeaderText("Complete your purchase");
        confirm.setContentText("Total amount: " + totalLabel.getText() + "\nProceed to checkout?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            user currentUser = Session.getCurrentUser();
            cartDAO.clearCart(currentUser.getId());
            showAlert("Success", "Order placed successfully! Total: " + totalLabel.getText(),
                    Alert.AlertType.INFORMATION);
            loadCartItems(); // This will show empty cart
        }
    }

    @FXML
    public void backToProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/products.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) cartTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - Products");
        } catch (IOException e) {
            showAlert("Error", "Could not load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}