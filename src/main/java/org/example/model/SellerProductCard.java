package org.example.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.model.Product;

public class SellerProductCard extends VBox {

    private Product product;
    private Button editButton;
    private Button deleteButton;

    public SellerProductCard(Product product, Runnable onEdit, Runnable onDelete) {
        this.product = product;

        this.getStyleClass().add("product-card");
        this.setPrefWidth(280);
        this.setSpacing(12);
        this.setPadding(new Insets(16));

        // Category badge
        Label categoryBadge = new Label(product.getCategory() != null ? product.getCategory() : "PRODUCT");
        categoryBadge.getStyleClass().add("category-badge");

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E3D27;");

        // Stock info
        Label stockLabel = new Label("Stock: " + product.getQuantity() + " kg");
        stockLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Price
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4B8B3B;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        editButton = new Button("✏️ Edit");
        editButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8;");
        editButton.setPrefWidth(80);
        editButton.setOnAction(e -> onEdit.run());

        deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8;");
        deleteButton.setPrefWidth(80);
        deleteButton.setOnAction(e -> onDelete.run());

        buttonBox.getChildren().addAll(editButton, deleteButton);

        this.getChildren().addAll(
                categoryBadge,
                nameLabel,
                stockLabel,
                priceLabel,
                buttonBox
        );
    }
}