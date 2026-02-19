package org.example.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.example.model.Product;

public class ProductCard extends VBox {

    private Product product;
    private Button addToCartButton;

    public ProductCard(Product product, Runnable onAddToCart) {
        this.product = product;

        // Card styling
        this.getStyleClass().add("product-card");
        this.setPrefWidth(280);
        this.setPrefHeight(350);
        this.setSpacing(12);
        this.setPadding(new Insets(16));

        // Image placeholder with gradient background
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(140);
        imageContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #4B8B3B, #7DBF6C);" +
                        "-fx-background-radius: 12;"
        );

        // Product icon
        Label iconLabel = new Label(getProductIcon());
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Category badge
        Label categoryBadge = new Label(product.getCategory() != null ? product.getCategory() : "PRODUCT");
        categoryBadge.getStyleClass().add("category-badge");
        categoryBadge.setAlignment(Pos.TOP_RIGHT);

        StackPane.setAlignment(categoryBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(categoryBadge, new Insets(10));

        imageContainer.getChildren().addAll(iconLabel, categoryBadge);

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);

        // Supplier
        Label supplierLabel = new Label(product.getSupplier() != null ? product.getSupplier() : "Local Farm");
        supplierLabel.getStyleClass().add("supplier");

        // Price and stock row
        HBox priceStockRow = new HBox(10);
        priceStockRow.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("price");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stockLabel = new Label(product.getQuantity() + " kg");
        stockLabel.getStyleClass().add("stock");

        priceStockRow.getChildren().addAll(priceLabel, spacer, stockLabel);

        // Expiry date if exists
        HBox expiryRow = new HBox();
        if (product.getExpiryDate() != null) {
            Label expiryLabel = new Label("Expires: " + product.getExpiryDate());
            expiryLabel.getStyleClass().add("expiry");
            expiryRow.getChildren().add(expiryLabel);
        }

        // Add to cart button
        addToCartButton = new Button("Add to Cart 🛒");
        addToCartButton.getStyleClass().add("add-to-cart-btn");
        addToCartButton.setMaxWidth(Double.MAX_VALUE);
        addToCartButton.setOnAction(e -> onAddToCart.run());

        // Disable button if out of stock
        if (product.getQuantity() <= 0) {
            addToCartButton.setDisable(true);
            addToCartButton.setText("Out of Stock");
            addToCartButton.setStyle("-fx-background-color: #cccccc;");
        }

        // Add all elements to card
        this.getChildren().addAll(
                imageContainer,
                nameLabel,
                supplierLabel,
                priceStockRow,
                expiryRow,
                addToCartButton
        );
    }

    private String getProductIcon() {
        String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
        String name = product.getName().toLowerCase();

        if (category.contains("grain") || name.contains("corn") || name.contains("wheat")) {
            return "🌾";
        } else if (category.contains("hay") || name.contains("hay") || name.contains("grass")) {
            return "🌿";
        } else if (category.contains("vegetable") || name.contains("carrot") || name.contains("cabbage") ||
                name.contains("tomato") || name.contains("potato") || name.contains("onion")) {
            return "🥕";
        } else if (category.contains("fruit") || name.contains("apple") || name.contains("banana")) {
            return "🍎";
        } else if (category.contains("protein") || name.contains("protein") || name.contains("soybean")) {
            return "🥩";
        } else {
            return "📦";
        }
    }

    public Button getAddToCartButton() {
        return addToCartButton;
    }
}