package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.components.ProductCard;
import org.example.components.SellerProductCard;
import org.example.dao.CartDAO;
import org.example.dao.ProductDAO;
import org.example.model.Product;
import org.example.model.Session;
import org.example.model.user;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab sellerTab;

    @FXML
    private Label userNameLabel;

    @FXML
    private FlowPane productsContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private FlowPane sellerProductsContainer;
    @FXML
    private TextField sellerSearchField;

    @FXML
    private VBox addProductForm;
    @FXML
    private TextField nameField;
    @FXML
    private TextField descField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField imageField;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private TextField supplierField;
    @FXML
    private DatePicker expiryDatePicker;

    private ProductDAO productDAO = new ProductDAO();
    private CartDAO cartDAO = new CartDAO();
    private List<Product> allProducts;
    private List<Product> sellerProducts;

    @FXML
    public void initialize() {
        System.out.println("=== ProductController Initializing ===");

        user currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.out.println("❌ No user logged in!");
            return;
        }

        System.out.println("✅ User logged in: " + currentUser.getName());
        System.out.println("   User ID: " + currentUser.getId());
        System.out.println("   User Role: '" + currentUser.getRole() + "'");

        updateUserInfo();
        setupFilters();
        loadProducts();

        categoryField.getItems().addAll("GRAINS", "HAY", "FORAGE", "VEGETABLES", "FRUITS", "PROTEIN", "OTHER");
        categoryFilter.getItems().addAll("All Categories", "GRAINS", "HAY", "FORAGE", "VEGETABLES", "FRUITS", "PROTEIN");
        categoryFilter.setValue("All Categories");

        if (sellerSearchField != null) {
            sellerSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterSellerProducts());
        }

        boolean canSell = "SELLER".equals(currentUser.getRole()) || "AGRIMAN".equals(currentUser.getRole());
        System.out.println("User can sell? " + canSell + " (Role: " + currentUser.getRole() + ")");

        if (!canSell) {
            System.out.println("Removing seller tab (user cannot sell)");
            if (tabPane != null && sellerTab != null) {
                tabPane.getTabs().remove(sellerTab);
            }
        } else {
            System.out.println("Seller tab will be visible for " + currentUser.getRole());
        }

        hideAddProductForm();
        System.out.println("=== ProductController initialized ===");
    }

    private void updateUserInfo() {
        user currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText("Welcome, " + currentUser.getName());
        }
    }

    private void setupFilters() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProducts());
        categoryFilter.setOnAction(e -> filterProducts());
    }

    private void loadProducts() {
        allProducts = productDAO.findAll();
        displayProducts(allProducts);

        user currentUser = Session.getCurrentUser();
        if (currentUser != null) {
            sellerProducts = allProducts.stream()
                    .filter(p -> p.getSellerId() == currentUser.getId())
                    .collect(Collectors.toList());
            displaySellerProducts(sellerProducts);
        }
    }

    private void filterProducts() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategory = categoryFilter.getValue();

        List<Product> filtered = allProducts.stream()
                .filter(p -> {
                    if (searchText.isEmpty()) return true;
                    return p.getName().toLowerCase().contains(searchText) ||
                            p.getDescription().toLowerCase().contains(searchText) ||
                            (p.getSupplier() != null && p.getSupplier().toLowerCase().contains(searchText));
                })
                .filter(p -> {
                    if (selectedCategory == null || selectedCategory.equals("All Categories")) return true;
                    return selectedCategory.equals(p.getCategory());
                })
                .toList();

        displayProducts(filtered);
    }

    private void filterSellerProducts() {
        if (sellerProducts == null) return;

        String searchText = sellerSearchField.getText().toLowerCase().trim();

        List<Product> filtered = sellerProducts.stream()
                .filter(p -> {
                    if (searchText.isEmpty()) return true;
                    return p.getName().toLowerCase().contains(searchText) ||
                            p.getDescription().toLowerCase().contains(searchText) ||
                            (p.getCategory() != null && p.getCategory().toLowerCase().contains(searchText));
                })
                .toList();

        displaySellerProducts(filtered);
    }

    private void displayProducts(List<Product> products) {
        productsContainer.getChildren().clear();
        productsContainer.setHgap(20);
        productsContainer.setVgap(20);
        productsContainer.setPadding(new Insets(20));

        for (Product product : products) {
            ProductCard card = new ProductCard(product, () -> handleAddToCart(product));
            productsContainer.getChildren().add(card);
        }

        if (products.isEmpty()) {
            Label noProductsLabel = new Label("No products found");
            noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            productsContainer.getChildren().add(noProductsLabel);
        }
    }

    private void displaySellerProducts(List<Product> products) {
        if (sellerProductsContainer == null) return;

        sellerProductsContainer.getChildren().clear();
        sellerProductsContainer.setHgap(20);
        sellerProductsContainer.setVgap(20);
        sellerProductsContainer.setPadding(new Insets(20));

        for (Product product : products) {
            SellerProductCard card = new SellerProductCard(product,
                    () -> handleEditProduct(product),
                    () -> handleDeleteProduct(product));
            sellerProductsContainer.getChildren().add(card);
        }

        if (products.isEmpty()) {
            Label noProductsLabel = new Label("You haven't added any products yet. Click 'Add New Product' to start selling!");
            noProductsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 40;");
            sellerProductsContainer.getChildren().add(noProductsLabel);
        }
    }

    @FXML
    public void showAddProductForm() {
        clearForm();
        addProductForm.setManaged(true);
        addProductForm.setVisible(true);
    }

    @FXML
    public void hideAddProductForm() {
        addProductForm.setManaged(false);
        addProductForm.setVisible(false);
    }

    @FXML
    public void switchToSellerTab() {
        tabPane.getSelectionModel().select(sellerTab);
    }

    @FXML
    public void handleAddToCart(Product product) {
        try {
            user currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert("Error", "Please login first!", Alert.AlertType.ERROR);
                return;
            }

            if (currentUser.getId() == product.getSellerId()) {
                showAlert("Error", "You cannot buy your own products!", Alert.AlertType.ERROR);
                return;
            }

            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Add to Cart");
            dialog.setHeaderText("Product: " + product.getName());
            dialog.setContentText("Enter quantity (kg):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                int quantity = Integer.parseInt(result.get());

                if (quantity <= 0) {
                    showAlert("Error", "Quantity must be greater than 0!", Alert.AlertType.ERROR);
                    return;
                }

                if (quantity > product.getQuantity()) {
                    showAlert("Error", "Not enough stock! Available: " + product.getQuantity() + " kg", Alert.AlertType.ERROR);
                    return;
                }

                cartDAO.addToCart(currentUser.getId(), product.getId(), quantity);
                showAlert("Success", quantity + " kg of " + product.getName() + " added to cart!", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleAddProduct() {
        try {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String priceText = priceField.getText().trim();
            String qtyText = quantityField.getText().trim();
            String category = categoryField.getValue();
            String supplier = supplierField.getText().trim();

            if (name.isEmpty() || desc.isEmpty() || priceText.isEmpty() || qtyText.isEmpty() || category == null) {
                showAlert("Error", "Please fill in all required fields!", Alert.AlertType.ERROR);
                return;
            }

            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(qtyText);

            user currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert("Error", "You must be logged in!", Alert.AlertType.ERROR);
                return;
            }

            Product product = new Product(name, desc, price, quantity, imageField.getText().trim(), currentUser.getId());
            product.setCategory(category);
            product.setSupplier(supplier.isEmpty() ? currentUser.getName() + "'s Farm" : supplier);
            product.setExpiryDate(expiryDatePicker.getValue() != null ? expiryDatePicker.getValue().toString() : null);

            productDAO.add(product);

            hideAddProductForm();
            loadProducts();

            showAlert("Success", "✅ Product added successfully!", Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Error", "Price and Quantity must be valid numbers!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleEditProduct(Product product) {
        nameField.setText(product.getName());
        descField.setText(product.getDescription());
        priceField.setText(String.valueOf(product.getPrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        imageField.setText(product.getImage());
        categoryField.setValue(product.getCategory());
        supplierField.setText(product.getSupplier());

        showAddProductForm();
        showAlert("Info", "Edit functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void handleDeleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Delete " + product.getName());
        confirm.setContentText("Are you sure you want to delete this product?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            showAlert("Success", "Product deleted successfully!", Alert.AlertType.INFORMATION);
            loadProducts();
        }
    }

    @FXML
    public void clearForm() {
        nameField.clear();
        descField.clear();
        priceField.clear();
        quantityField.clear();
        imageField.clear();
        categoryField.setValue(null);
        supplierField.clear();
        expiryDatePicker.setValue(null);
    }

    @FXML
    public void openCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) productsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - My Cart");
        } catch (IOException e) {
            showAlert("Error", "Could not open cart: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        Session.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) productsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Agrimans Marketplace - Login");
        } catch (IOException e) {
            showAlert("Error", "Could not logout: " + e.getMessage(), Alert.AlertType.ERROR);
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