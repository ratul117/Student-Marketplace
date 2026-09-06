package com.marketplace1;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProductCell extends ListCell<String> {

    private final VBox cardContainer = new VBox(4);
    private final Label titleLabel = new Label();
    private final Label descLabel = new Label();
    private final Label priceLabel = new Label();
    private final Label sellerLabel = new Label();
    private final HBox topRow = new HBox();

    public ProductCell() {
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4f46e5;");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        sellerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-background-color: #f1f5f9; -fx-padding: 2 6; -fx-background-radius: 4;");

        // Top Row Setup (Title & Price Side by Side)
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        topRow.getChildren().addAll(titleLabel, priceLabel);

        cardContainer.setPadding(new Insets(8, 12, 8, 12));
        cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        cardContainer.getChildren().addAll(topRow, descLabel, sellerLabel);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            // Data Parsing assuming format: "Title: X | Desc: Y | Price: $Z | Seller: W"
            try {
                String[] parts = item.split("\\|");
                String title = parts[0].replace("Title:", "").trim();
                String desc = parts.length > 1 ? parts[1].replace("Desc:", "").trim() : "";
                String price = parts.length > 2 ? parts[2].replace("Price:", "").trim() : "";
                String seller = parts.length > 3 ? parts[3].replace("Seller:", "").trim() : "";

                titleLabel.setText(title);
                descLabel.setText(desc);
                priceLabel.setText("$" + price);
                sellerLabel.setText("Seller: " + seller);

                setGraphic(cardContainer);
            } catch (Exception e) {
                setText(item);
                setGraphic(null);
            }
        }
    }
}