package com.marketplace1;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class HelloController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private BorderPane mainLayout;
    private StackPane contentArea;
    private Button notificationsButton;
    private Animation pageTransition;

    // "ID: 1 | Title: X | Desc: Y ..." থেকে নির্দিষ্ট key-এর value বের করে
    static String field(String item, String key) {
        for (String part : item.split("\\|")) {
            String p = part.trim();
            if (p.startsWith(key + ":")) return p.substring(key.length() + 1).trim();
        }
        return "";
    }

    static int idOf(String item) {
        try { return Integer.parseInt(field(item, "ID")); } catch (NumberFormatException e) { return -1; }
    }

    private static String stars(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append('★');
        return result.toString();
    }

    private static StackPane icon(String name, double size, String color) {
        String path;
        switch (name) {
            case "cap": path = "M2 9 12 3l10 6-10 6L2 9zm4 3.2V17c3.8 2.8 8.2 2.8 12 0v-4.8l-6 3.6-6-3.6z"; break;
            case "user": path = "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm-8 9a8 8 0 0 1 16 0H4z"; break;
            case "bag": path = "M4 8h16l1 13H3L4 8zm4 0V6a4 4 0 0 1 8 0v2h-2V6a2 2 0 0 0-4 0v2H8z"; break;
            case "box": path = "M3 7 12 2l9 5v10l-9 5-9-5V7zm2 1v8l6 3.3v-8L5 8zm8 11.3 6-3.3V8l-6 3.3v8zM6.7 6.7l5.3 3 5.3-3L12 3.8l-5.3 2.9z"; break;
            case "plus": path = "M10 2h4v8h8v4h-8v8h-4v-8H2v-4h8z"; break;
            case "cart": path = "M2 3h3l2.2 12.2a2 2 0 0 0 2 1.8h9.3l3-9H6M9 21a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm9 0a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z"; break;
            case "history": path = "M12 3a9 9 0 1 1-8.5 6H1l4-5 4 5H5.6A7 7 0 1 0 12 5V3zm-1 4h2v5l4 2-1 1.7-5-2.7V7z"; break;
            case "bell": path = "M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9zm-8 11h4a2 2 0 0 1-4 0z"; break;
            case "check": path = "M9 16.2 4.8 12l-1.4 1.4L9 19l12-12-1.4-1.4z"; break;
            case "logout": path = "M4 3h9v2H6v14h7v2H4V3zm12 4 5 5-5 5v-3H9v-4h7V7z"; break;
            case "trash": path = "M5 7h14l-1 14H6L5 7zm3-4h8l1 2h4v2H3V5h4l1-2zm1 6v9h2V9H9zm4 0v9h2V9h-2z"; break;
            case "book": path = "M4 4c3-1 6 0 8 2v15c-2-2-5-3-8-2V4zm16 0c-3-1-6 0-8 2v15c2-2 5-3 8-2V4z"; break;
            case "monitor": path = "M3 4h18v13H3V4zm7 15h4v2h4v1H6v-1h4v-2z"; break;
            case "chair": path = "M7 3h10v8H7V3zm-2 9h14v3H5v-3zm2 3h2v7H7v-7zm8 0h2v7h-2v-7z"; break;
            case "pencil": path = "M4 16.5 16.8 3.7a2.4 2.4 0 0 1 3.4 3.4L7.4 19.9 3 21l1-4.5zM14.8 5.7l3.5 3.5"; break;
            case "shirt": path = "M8 3 4 5l-3 5 4 2 2-3v12h10V9l2 3 4-2-3-5-4-2a5 5 0 0 1-8 0z"; break;
            case "sparkle": path = "m12 2 2.1 7.9L22 12l-7.9 2.1L12 22l-2.1-7.9L2 12l7.9-2.1L12 2zm8 13 .8 2.2L23 18l-2.2.8L20 21l-.8-2.2L17 18l2.2-.8L20 15z"; break;
            case "camera": path = "M8 4 6.5 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2.5L16 4H8zm4 4a5 5 0 1 1 0 10 5 5 0 0 1 0-10zm0 2a3 3 0 1 0 0 6 3 3 0 0 0 0-6z"; break;
            default: path = "M12 2 14 9 21 12 14 14 12 22 10 14 3 12 10 9z";
        }
        SVGPath shape = new SVGPath();
        shape.setContent(path);
        shape.setFill(Color.web(color));
        shape.setScaleX(size / 24.0);
        shape.setScaleY(size / 24.0);
        StackPane iconBox = new StackPane(shape);
        iconBox.setMinSize(size, size);
        iconBox.setPrefSize(size, size);
        iconBox.setMaxSize(size, size);
        return iconBox;
    }

    /** Selected Student Market mark: a white shopping bag, violet S, and gold accent. */
    private static StackPane marketplaceLogo(double size) {
        StackPane logo = new StackPane();
        logo.setMinSize(size, size);
        logo.setPrefSize(size, size);
        logo.setMaxSize(size, size);
        logo.setStyle("-fx-background-color: linear-gradient(to bottom right, #5b5ce2, #3730a3); -fx-background-radius: " + (size * 0.25) + ";");

        SVGPath bag = new SVGPath();
        bag.setContent("M4 8h16l1.5 13h-19L4 8zm4 0V6a4 4 0 0 1 8 0v2h-2V6a2 2 0 0 0-4 0v2H8z");
        bag.setFill(Color.WHITE);
        bag.setScaleX(size / 27.0);
        bag.setScaleY(size / 27.0);

        Label letter = new Label("S");
        letter.setStyle("-fx-font-size: " + (size * 0.39) + "px; -fx-font-weight: 900; -fx-text-fill: #4338ca;");
        letter.setTranslateY(size * 0.04);

        Circle accent = new Circle(size * 0.07, Color.web("#fbbf24"));
        StackPane.setAlignment(accent, Pos.TOP_RIGHT);
        StackPane.setMargin(accent, new Insets(size * 0.10, size * 0.10, 0, 0));
        logo.getChildren().addAll(bag, letter, accent);
        return logo;
    }

    private static String categoryIcon(String category) {
        switch (category) {
            case "Books": return "book";
            case "Electronics": return "monitor";
            case "Furniture": return "chair";
            case "Stationery": return "pencil";
            case "Clothing": return "shirt";
            default: return "sparkle";
        }
    }

    /** Swap dashboard pages with a short, subtle fade and horizontal motion. */
    private void showPage(javafx.scene.Node page) {
        if (contentArea == null) return;
        if (pageTransition != null) pageTransition.stop();
        javafx.scene.Node visiblePage = page;
        if (!(page instanceof ScrollPane)) {
            ScrollPane pageScroll = new ScrollPane(page);
            pageScroll.setFitToWidth(true);
            // Keep the page at its natural height so overflow produces a real vertical scrollbar.
            pageScroll.setFitToHeight(false);
            pageScroll.setPannable(true);
            pageScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
            visiblePage = pageScroll;
        }
        contentArea.getChildren().setAll(visiblePage);
        visiblePage.setOpacity(0);
        visiblePage.setTranslateX(12);

        FadeTransition fade = new FadeTransition(Duration.millis(190), visiblePage);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), visiblePage);
        slide.setFromX(12);
        slide.setToX(0);
        pageTransition = new ParallelTransition(fade, slide);
        pageTransition.play();
    }

    private static class ProductCell extends ListCell<String> {
        private final VBox cardContainer = new VBox(4);
        private final Label titleLabel = new Label();
        private final Label descLabel = new Label();
        private final Label priceLabel = new Label();
        private final Label categoryLabel = new Label();
        private final Label sellerLabel = new Label();
        private final ImageView photoView = new ImageView();
        private final StackPane photoPlaceholder = icon("camera", 26, "#94a3b8");
        private final StackPane photoFrame = new StackPane();
        private final HBox topRow = new HBox();
        private final HBox badgeRow = new HBox(8);
        private final HBox bodyRow = new HBox(12);
        private final VBox textContent = new VBox(5);

        public ProductCell() {
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4f46e5;");
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            descLabel.setWrapText(true);

            categoryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7; -fx-background-color: #e0f2fe; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");
            sellerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-background-color: #f1f5f9; -fx-padding: 2 6; -fx-background-radius: 4;");
            categoryLabel.setGraphicTextGap(5);
            sellerLabel.setGraphicTextGap(5);

            badgeRow.getChildren().addAll(categoryLabel, sellerLabel);

            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            topRow.setSpacing(12);
            topRow.getChildren().addAll(titleLabel, priceLabel);

            cardContainer.setPadding(new Insets(12));
            cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e8edf5; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(15, 23, 42, 0.05), 8, 0, 0, 2);");
            photoView.setFitWidth(104);
            photoView.setFitHeight(88);
            photoView.setPreserveRatio(true);
            photoView.setSmooth(true);
            photoFrame.setMinSize(112, 96);
            photoFrame.setPrefSize(112, 96);
            photoFrame.setMaxSize(112, 96);
            photoFrame.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 9px;");
            photoFrame.getChildren().addAll(photoPlaceholder, photoView);
            textContent.getChildren().addAll(topRow, descLabel, badgeRow);
            HBox.setHgrow(textContent, Priority.ALWAYS);
            bodyRow.getChildren().addAll(photoFrame, textContent);
            cardContainer.getChildren().add(bodyRow);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                try {
                    String title = field(item, "Title");
                    String desc = field(item, "Desc");
                    String price = field(item, "Price");
                    String category = field(item, "Category");
                    if (category.isEmpty()) category = "General";
                    String seller = field(item, "Seller");
                    String imagePath = field(item, "Image");

                    titleLabel.setText(title);
                    descLabel.setText(desc);
                    priceLabel.setText("BDT " + price);
                    categoryLabel.setText(category);
                    categoryLabel.setGraphic(icon(categoryIcon(category), 12, "#0284c7"));
                    sellerLabel.setText("Seller: " + seller);
                    sellerLabel.setGraphic(icon("user", 11, "#64748b"));
                    if (imagePath.trim().isEmpty()) {
                        photoView.setImage(null);
                        photoView.setVisible(false);
                        photoPlaceholder.setVisible(true);
                    } else {
                        photoView.setVisible(true);
                        photoPlaceholder.setVisible(false);
                        Image productPhoto = new Image(Paths.get(imagePath).toAbsolutePath().toUri().toString(), 104, 88, true, true, true);
                        photoView.setImage(productPhoto);
                        photoPlaceholder.setVisible(productPhoto.isError());
                    }

                    setGraphic(cardContainer);
                } catch (Exception e) {
                    setText(item);
                    setGraphic(null);
                }
            }
        }
    }

    private static class NotificationCell extends ListCell<DatabaseHandler.Notification> {
        private final VBox card = new VBox(6);
        private final Label title = new Label();
        private final Label message = new Label();
        private final Label time = new Label();
        private final Label status = new Label();
        private final HBox heading = new HBox(10);
        private final Region spacer = new Region();

        NotificationCell() {
            title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #172554;");
            message.setWrapText(true);
            message.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
            time.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            status.setText("NEW");
            status.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #4338ca; -fx-background-color: #eef2ff; -fx-background-radius: 99px; -fx-padding: 4 8;");
            HBox.setHgrow(spacer, Priority.ALWAYS);
            heading.getChildren().addAll(title, spacer, status);
            card.setPadding(new Insets(14, 16, 14, 16));
            card.getChildren().addAll(heading, message, time);
        }

        @Override
        protected void updateItem(DatabaseHandler.Notification notification, boolean empty) {
            super.updateItem(notification, empty);
            if (empty || notification == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            boolean sold = "SOLD".equals(notification.getType());
            title.setText(sold ? "Your product was sold" : "Purchase confirmed");
            title.setGraphic(icon(sold ? "bag" : "cart", 15, sold ? "#059669" : "#4f46e5"));
            title.setGraphicTextGap(8);
            message.setText(notification.getProductName() + " — " + notification.getMessage());
            time.setText("Product #" + notification.getProductId() + "  ·  " + notification.getCreatedAt());
            status.setVisible(!notification.isRead());
            status.setManaged(!notification.isRead());
            card.setStyle(notification.isRead()
                    ? "-fx-background-color: #ffffff; -fx-border-color: #e8edf5; -fx-border-radius: 12px; -fx-background-radius: 12px;"
                    : "-fx-background-color: #f5f7ff; -fx-border-color: #c7d2fe; -fx-border-radius: 12px; -fx-background-radius: 12px;");
            setGraphic(card);
            setText(null);
        }
    }

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password == null || password.isEmpty()) {
            showAlert("Missing login details", "Enter both your email and password to sign in.", AlertType.WARNING);
            return;
        }

        if (DatabaseHandler.loginUser(email, password)) {
            Stage stage = (Stage) emailField.getScene().getWindow();
            openMainDashboardLayout(stage, email);
        } else {
            showAlert("Error", "Invalid Email or Password!", AlertType.ERROR);
        }
    }

    private void openMainDashboardLayout(Stage stage, String userEmail) {
        mainLayout = new BorderPane();
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(22));

        VBox sidebar = createSidebar(stage, userEmail);
        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(contentArea);
        mainLayout.setBottom(createFooter());

        showViewProducts(userEmail);

        Scene scene = new Scene(mainLayout, 1080, 700);
        attachStyle(scene);

        stage.setTitle("Student Marketplace - Dashboard");
        stage.setScene(scene);
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 20, 8, 20));
        footer.getStyleClass().add("app-footer");

        Label copyright = new Label("© " + java.time.Year.now().getValue() + " Student Marketplace");
        copyright.getStyleClass().add("footer-copyright");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink about = footerLink("About");
        about.setOnAction(e -> showFooterInfo("About Student Marketplace",
                "A campus marketplace for students to discover, list, and buy pre-loved essentials."));
        Hyperlink help = footerLink("Help & Safety");
        help.setOnAction(e -> showFooterInfo("Help & Safety",
                "Check item details before arranging an exchange. Never share your password. Use Report seller or Block seller from a product details page if something feels unsafe."));
        Hyperlink privacy = footerLink("Privacy");
        privacy.setOnAction(e -> showFooterInfo("Privacy",
                "Other students see seller names, not email addresses. You can manage blocked sellers or delete your account from Privacy & Safety in the sidebar."));
        Hyperlink terms = footerLink("Terms");
        terms.setOnAction(e -> showFooterInfo("Marketplace guidelines",
                "Post accurate descriptions and photos, list only items you may sell, and treat other students respectfully. Arrange exchanges carefully and follow your campus rules."));
        Hyperlink contact = footerLink("Contact Us");
        contact.setOnAction(e -> showFooterInfo("Contact Us",
                "For a safety concern, report the seller from the product details page. For account or technical help, contact your campus marketplace administrator."));

        footer.getChildren().addAll(copyright, spacer, about, help, privacy, terms, contact);
        return footer;
    }

    private Hyperlink footerLink(String text) {
        Hyperlink link = new Hyperlink(text);
        link.getStyleClass().add("footer-link");
        return link;
    }

    private void showFooterInfo(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION);
    }

    private VBox createSidebar(Stage stage, String userEmail) {
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(238);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 15, 20, 15));

        Label brand = new Label("Student Market");
        brand.setGraphic(marketplaceLogo(30));
        brand.setGraphicTextGap(9);
        brand.getStyleClass().add("sidebar-brand");

        Label userLbl = new Label(userEmail);
        userLbl.setGraphic(icon("user", 13, "#a5b4fc"));
        userLbl.setGraphicTextGap(7);
        userLbl.getStyleClass().add("sidebar-user");

        Button btnView = createNavButton("View Products", "bag");
        Button btnMyProds = createNavButton("My Products", "box");
        Button btnAdd = createNavButton("Add Product", "plus");
        Button btnPurchases = createNavButton("My Purchases", "cart");
        Button btnMessages = createNavButton("Messages", "bell");
        notificationsButton = createNavButton("Notifications", "bell");
        updateNotificationButton(userEmail);
        Button btnSold = createNavButton("Sold History", "history");
        Button btnPrivacy = createNavButton("Privacy & Safety", "user");
        Button btnLogout = createNavButton("Logout", "logout");
        btnLogout.getStyleClass().add("nav-btn-logout");
        btnLogout.setGraphic(icon("logout", 17, "#ffffff"));

        btnView.setOnAction(e -> showViewProducts(userEmail));
        btnMyProds.setOnAction(e -> showMyProducts(userEmail));
        btnAdd.setOnAction(e -> showAddProduct(userEmail));
        btnPurchases.setOnAction(e -> showMyPurchases(userEmail));
        btnMessages.setOnAction(e -> showMessages(userEmail));
        notificationsButton.setOnAction(e -> showNotifications(userEmail));
        btnSold.setOnAction(e -> showSoldHistory(userEmail));
        btnPrivacy.setOnAction(e -> showPrivacySafety(stage, userEmail));
        btnLogout.setOnAction(e -> openLoginWindow(stage));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, userLbl, new Separator(), btnView, btnMyProds, btnAdd,
                btnPurchases, btnMessages, notificationsButton, btnSold, btnPrivacy);
        if (DatabaseHandler.isAdmin(userEmail)) {
            Button btnAdmin = createNavButton("Admin Panel", "monitor");
            btnAdmin.setOnAction(e -> showAdminPanel(userEmail));
            sidebar.getChildren().add(btnAdmin);
        }
        sidebar.getChildren().addAll(spacer, btnLogout);
        return sidebar;
    }

    private void updateNotificationButton(String userEmail) {
        if (notificationsButton != null) {
            int unread = DatabaseHandler.countUnreadNotifications(userEmail);
            notificationsButton.setText(unread == 0 ? "Notifications" : "Notifications (" + unread + ")");
        }
    }

    private Button createNavButton(String text, String iconName) {
        Button btn = new Button(text);
        btn.setGraphic(icon(iconName, 17, "#cbd5e1"));
        btn.setGraphicTextGap(12);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-btn");
        return btn;
    }

    private void showViewProducts(String userEmail) {
        VBox box = new VBox(12);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(20));

        Label title = new Label("Discover your next great find");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Pre-loved essentials, shared by students on your campus.");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        HBox filterBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> filterCategory = new ComboBox<>();
        filterCategory.getItems().addAll("All Categories", "Books", "Electronics", "Furniture", "Stationery", "Clothing", "Others");
        filterCategory.setValue("All Categories");

        filterBox.getChildren().addAll(searchField, filterCategory);

        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("product-list");
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(param -> new ProductCell());
        listView.setPlaceholder(new Label("No products yet. Check back soon or add the first listing."));

        List<String> allProducts = new java.util.ArrayList<>(DatabaseHandler.getAllProducts(userEmail));
        listView.getItems().addAll(allProducts);

        Runnable applyFilters = () -> {
            String searchText = searchField.getText().toLowerCase(java.util.Locale.ROOT);
            String selectedCategory = filterCategory.getValue();

            listView.getItems().clear();
            for (String product : allProducts) {
                boolean matchesSearch = product.toLowerCase().contains(searchText);
                boolean matchesCategory = "All Categories".equals(selectedCategory) || product.contains("Category: " + selectedCategory);

                if (matchesSearch && matchesCategory) {
                    listView.getItems().add(product);
                }
            }
        };

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        filterCategory.setOnAction(e -> applyFilters.run());

        Button buyBtn = new Button("Buy Selected Product");
        buyBtn.setGraphic(icon("cart", 16, "#ffffff"));
        buyBtn.getStyleClass().add("dash-btn-primary");
        buyBtn.setPrefHeight(36);

        Button detailsBtn = new Button("View Details");
        detailsBtn.setGraphic(icon("sparkle", 15, "#4338ca"));
        detailsBtn.getStyleClass().add("dash-btn-secondary");
        detailsBtn.setPrefHeight(36);
        detailsBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) showAlert("Choose a product", "Select a product first to view its details.", AlertType.INFORMATION);
            else showProductDetails(selected, userEmail);
        });
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                showProductDetails(listView.getSelectionModel().getSelectedItem(), userEmail);
            }
        });

        buyBtn.setOnAction(e -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (DatabaseHandler.isSeller(idOf(selectedItem), userEmail)) {
                    showAlert("Warning", "You cannot buy your own product!", AlertType.WARNING);
                    return;
                }

                showPaymentCheckout(selectedItem, userEmail, () -> {
                    allProducts.remove(selectedItem);
                    listView.getItems().remove(selectedItem);
                    updateNotificationButton(userEmail);
                });
            } else {
                showAlert("Warning", "Please select a product to buy!", AlertType.WARNING);
            }
        });

        HBox actions = new HBox(10, detailsBtn, buyBtn);
        box.getChildren().addAll(title, subtitle, filterBox, listView, actions);
        showPage(box);
    }

    private void showPaymentCheckout(String product, String buyerEmail, Runnable onReserved) {
        int productId = idOf(product);
        if (!DatabaseHandler.isProductAvailableTo(productId, buyerEmail) || DatabaseHandler.isSeller(productId, buyerEmail)) {
            showAlert("Listing unavailable", "This item is no longer available to buy.", AlertType.WARNING);
            return;
        }
        Stage checkout = new Stage();
        checkout.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        checkout.initOwner(contentArea.getScene().getWindow());
        checkout.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        checkout.setTitle("Checkout · Student Market");

        Label eyebrow = new Label("SECURE CHECKOUT");
        eyebrow.setStyle("-fx-text-fill:#a5b4fc;-fx-font-size:10px;-fx-font-weight:bold;");
        Label title = new Label("Review your order");
        title.setStyle("-fx-text-fill:white;-fx-font-size:21px;-fx-font-weight:bold;");
        Label subtitle = new Label("Choose how you plan to pay the seller at handover.");
        subtitle.setStyle("-fx-text-fill:#cbd5e1;-fx-font-size:12px;");
        Button close = new Button("×");
        close.setStyle("-fx-background-color:rgba(255,255,255,0.14);-fx-text-fill:white;-fx-font-size:20px;-fx-background-radius:20;-fx-min-width:34;-fx-min-height:34;-fx-cursor:hand;");
        close.setOnAction(e -> checkout.close());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(eyebrow, spacer, close); top.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(7, top, title, subtitle);
        header.setPadding(new Insets(21, 24, 21, 24));
        header.setStyle("-fx-background-color:linear-gradient(to right,#312e81,#4338ca);-fx-background-radius:16 16 0 0;");

        VBox orderCard = new VBox(6);
        Label itemName = new Label(field(product, "Title"));
        itemName.setStyle("-fx-text-fill:#172554;-fx-font-size:15px;-fx-font-weight:bold;");
        Label seller = new Label("Seller: " + field(product, "Seller"));
        seller.setStyle("-fx-text-fill:#64748b;-fx-font-size:11px;");
        Label category = new Label(field(product, "Category")); category.setStyle("-fx-text-fill:#64748b;-fx-font-size:11px;");
        Region totalSpacer = new Region(); HBox.setHgrow(totalSpacer, Priority.ALWAYS);
        Label totalTitle = new Label("Total due to seller"); totalTitle.setStyle("-fx-text-fill:#475569;-fx-font-size:12px;");
        Label total = new Label("BDT " + field(product, "Price")); total.setStyle("-fx-text-fill:#4338ca;-fx-font-size:19px;-fx-font-weight:bold;");
        HBox totalRow = new HBox(totalTitle, totalSpacer, total); totalRow.setAlignment(Pos.CENTER_LEFT);
        orderCard.getChildren().addAll(itemName, seller, category, new Separator(), totalRow);
        orderCard.setPadding(new Insets(14));
        orderCard.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-radius:12;");

        Label methodTitle = new Label("Payment method");
        methodTitle.setStyle("-fx-text-fill:#172554;-fx-font-size:13px;-fx-font-weight:bold;");
        ToggleGroup methods = new ToggleGroup();
        String[][] options = {
                {"Cash at meetup", "Inspect the item, then pay in cash at your campus handover.", "bag"},
                {"bKash at meetup", "Arrange a direct transfer with the seller after confirming the item.", "cart"},
                {"Nagad at meetup", "Arrange a direct transfer with the seller after confirming the item.", "sparkle"}
        };
        VBox methodChoices = new VBox(8);
        for (int i = 0; i < options.length; i++) {
            String[] option = options[i];
            ToggleButton choice = new ToggleButton();
            choice.setToggleGroup(methods);
            choice.setUserData(option[0]);
            choice.setMaxWidth(Double.MAX_VALUE);
            choice.setPrefHeight(55);
            Label method = new Label(option[0]); method.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#172554;");
            Label desc = new Label(option[1]); desc.setWrapText(true); desc.setStyle("-fx-font-size:10px;-fx-text-fill:#64748b;");
            HBox row = new HBox(12, icon(option[2], 18, "#4f46e5"), new VBox(4, method, desc));
            row.setAlignment(Pos.CENTER_LEFT);
            choice.setGraphic(row);
            choice.setStyle("-fx-background-color:white;-fx-border-color:#e2e8f0;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:9 12;-fx-cursor:hand;-fx-alignment:center-left;");
            choice.selectedProperty().addListener((obs, old, selected) -> choice.setStyle(selected
                    ? "-fx-background-color:#eef2ff;-fx-border-color:#6366f1;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:9 12;-fx-cursor:hand;-fx-alignment:center-left;"
                    : "-fx-background-color:white;-fx-border-color:#e2e8f0;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:9 12;-fx-cursor:hand;-fx-alignment:center-left;"));
            methodChoices.getChildren().add(choice);
            if (i == 0) choice.setSelected(true);
        }
        Label safety = new Label("This version does not process online payments. You will arrange payment directly with the seller. Never share your wallet PIN or OTP.");
        safety.setWrapText(true);
        safety.setStyle("-fx-background-color:#fffbeb;-fx-background-radius:10;-fx-padding:10 12;-fx-text-fill:#92400e;-fx-font-size:11px;");

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 16;-fx-cursor:hand;");
        cancel.setOnAction(e -> checkout.close());
        Button confirm = new Button("Confirm order");
        confirm.setGraphic(icon("check", 14, "#ffffff"));
        confirm.setStyle("-fx-background-color:#4f46e5;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 16;-fx-cursor:hand;");
        confirm.setOnAction(e -> {
            Toggle selected = methods.getSelectedToggle();
            String selectedMethod = selected == null ? null : (String) selected.getUserData();
            if (!DatabaseHandler.buyProduct(productId, buyerEmail, selectedMethod)) {
                showAlert("Order unavailable", "This product may have just been purchased or is no longer available.", AlertType.WARNING);
                checkout.close();
                return;
            }
            checkout.close();
            onReserved.run();
            showAlert("Order reserved", "The seller has been notified of your order and payment preference. Use Messages to arrange payment and handover.", AlertType.INFORMATION);
        });
        HBox actions = new HBox(10, cancel, confirm); actions.setAlignment(Pos.CENTER_RIGHT);
        VBox body = new VBox(12, orderCard, methodTitle, methodChoices, safety, actions);
        body.setPadding(new Insets(18, 24, 22, 24));
        VBox root = new VBox(header, body);
        root.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:#e2e8f0;-fx-border-radius:16;-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.20),28,0.18,0,12);");
        Scene scene = new Scene(root, 620, 610); scene.setFill(Color.TRANSPARENT); attachStyle(scene);
        checkout.setScene(scene); checkout.sizeToScene(); checkout.showAndWait();
    }

    private void showProductDetails(String product, String userEmail) {
        if (!DatabaseHandler.isProductAvailableTo(idOf(product), userEmail)) {
            showAlert("Listing unavailable", "This product is no longer available or its seller is blocked.", AlertType.INFORMATION);
            showViewProducts(userEmail);
            return;
        }
        VBox page = new VBox(18);
        page.getStyleClass().add("dash-card");
        page.setPadding(new Insets(24));

        Button backButton = new Button("Back to products");
        backButton.setGraphic(icon("bag", 15, "#4338ca"));
        backButton.getStyleClass().add("dash-btn-secondary");
        backButton.setOnAction(e -> showViewProducts(userEmail));

        Label pageTitle = new Label("Product details");
        pageTitle.getStyleClass().add("dash-title");
        HBox pageHeader = new HBox(16, backButton, pageTitle);
        pageHeader.setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(330);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        StackPane photoPlaceholder = icon("camera", 34, "#94a3b8");
        StackPane photoPanel = new StackPane(photoPlaceholder, imageView);
        photoPanel.setMinSize(350, 320);
        photoPanel.setPrefSize(350, 320);
        photoPanel.setMaxSize(350, 320);
        photoPanel.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 14px;");
        String photoPath = field(product, "Image");
        if (photoPath.trim().isEmpty()) {
            imageView.setVisible(false);
        } else {
            Image image = new Image(Paths.get(photoPath).toAbsolutePath().toUri().toString(), 330, 300, true, true, true);
            imageView.setImage(image);
            photoPlaceholder.setVisible(image.isError());
        }

        String productName = field(product, "Title");
        String category = field(product, "Category");
        Label nameLabel = new Label(productName);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        Label priceLabel = new Label("BDT " + field(product, "Price"));
        priceLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4f46e5;");
        Label categoryLabel = new Label(category.isEmpty() ? "General" : category);
        categoryLabel.setGraphic(icon(categoryIcon(category), 15, "#0284c7"));
        categoryLabel.setGraphicTextGap(7);
        categoryLabel.setStyle("-fx-background-color: #e0f2fe; -fx-background-radius: 99px; -fx-padding: 7 12; -fx-text-fill: #0369a1; -fx-font-weight: bold;");
        Label sellerLabel = new Label(field(product, "Seller"));
        sellerLabel.setGraphic(icon("user", 15, "#64748b"));
        sellerLabel.setGraphicTextGap(8);
        sellerLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        Label sellerRating = new Label("Seller rating: " + DatabaseHandler.getSellerRatingForProduct(idOf(product)));
        sellerRating.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b45309;");
        Label sellerHeading = new Label("Seller");
        sellerHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        Label descriptionHeading = new Label("About this item");
        descriptionHeading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        Label description = new Label(field(product, "Desc"));
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        VBox details = new VBox(14, nameLabel, priceLabel, categoryLabel, new Separator(), sellerHeading, sellerLabel, sellerRating,
                new Separator(), descriptionHeading, description);
        List<String> reviews = DatabaseHandler.getSellerReviewsForProduct(idOf(product));
        if (!reviews.isEmpty()) {
            Label reviewsTitle = new Label("Recent seller reviews");
            reviewsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #172554;");
            VBox reviewList = new VBox(7);
            for (String review : reviews) {
                Label reviewCard = new Label(review);
                reviewCard.setWrapText(true);
                reviewCard.setMaxWidth(Double.MAX_VALUE);
                reviewCard.setStyle("-fx-background-color: #fffbeb; -fx-background-radius: 9px; -fx-padding: 9 11; -fx-text-fill: #475569; -fx-font-size: 11px;");
                reviewList.getChildren().add(reviewCard);
            }
            details.getChildren().addAll(new Separator(), reviewsTitle, reviewList);
        }
        HBox.setHgrow(details, Priority.ALWAYS);

        Button buyButton = new Button("Buy this product");
        buyButton.setGraphic(icon("cart", 16, "#ffffff"));
        buyButton.getStyleClass().add("dash-btn-primary");
        buyButton.setPrefHeight(40);
        if (DatabaseHandler.isSeller(idOf(product), userEmail)) {
            buyButton.setDisable(true);
            buyButton.setText("Your listing");
        }
        buyButton.setOnAction(e -> {
            showPaymentCheckout(product, userEmail, () -> {
                updateNotificationButton(userEmail);
                showAlert("Order reserved", "Your order for “" + productName + "” is reserved. Coordinate the handover and payment with the seller.", AlertType.INFORMATION);
                showViewProducts(userEmail);
            });
        });
        details.getChildren().add(buyButton);

        if (!DatabaseHandler.isSeller(idOf(product), userEmail)) {
            Button messageSeller = new Button("Message seller");
            messageSeller.setGraphic(icon("bell", 15, "#4338ca"));
            messageSeller.getStyleClass().add("dash-btn-secondary");
            messageSeller.setOnAction(e -> {
                String counterpart = DatabaseHandler.getChatCounterpart(userEmail, idOf(product));
                if (counterpart == null) showAlert("Chat unavailable", "This listing is no longer open for messages.", AlertType.INFORMATION);
                else openChat(idOf(product), userEmail, counterpart, field(product, "Seller"), productName);
            });
            details.getChildren().add(messageSeller);
        }

        if (!DatabaseHandler.isSeller(idOf(product), userEmail)) {
            Button reportButton = new Button("Report seller");
            reportButton.setGraphic(icon("history", 15, "#b45309"));
            reportButton.getStyleClass().add("dash-btn-secondary");
            reportButton.setOnAction(e -> reportSeller(idOf(product), userEmail));

            Button blockButton = new Button("Block seller");
            blockButton.setGraphic(icon("user", 15, "#be123c"));
            blockButton.getStyleClass().add("dash-btn-danger");
            blockButton.setOnAction(e -> {
                Alert confirm = new Alert(AlertType.CONFIRMATION, "You will no longer see this seller's listings. You can unblock them in Privacy & Safety.", ButtonType.CANCEL, ButtonType.OK);
                confirm.setTitle("Block seller");
                confirm.setHeaderText("Block " + field(product, "Seller") + "?");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    if (DatabaseHandler.blockSeller(userEmail, idOf(product))) {
                        showAlert("Seller blocked", "Their listings are now hidden from your marketplace.", AlertType.INFORMATION);
                        showViewProducts(userEmail);
                    } else showAlert("Could not block seller", "Please try again.", AlertType.ERROR);
                }
            });
            HBox safetyActions = new HBox(10, reportButton, blockButton);
            details.getChildren().add(safetyActions);
        }

        HBox content = new HBox(26, photoPanel, details);
        content.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(content, Priority.ALWAYS);
        page.getChildren().addAll(pageHeader, content);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        showPage(scroll);
    }

    private void showMyProducts(String userEmail) {
        VBox box = new VBox(12);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(20));

        Label title = new Label("My Listed Products");
        title.getStyleClass().add("dash-title");

        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("product-list");
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(param -> new ProductCell());

        listView.getItems().addAll(DatabaseHandler.getMyProducts(userEmail));

        Button editBtn = new Button("Edit Selected Product");
        editBtn.setGraphic(icon("pencil", 16, "#4338ca"));
        editBtn.getStyleClass().add("dash-btn-secondary");
        editBtn.setPrefHeight(36);
        editBtn.setOnAction(e -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                showAlert("Select a listing", "Choose one of your products to edit.", AlertType.WARNING);
                return;
            }
            editProductDialog(selectedItem, userEmail, () -> showMyProducts(userEmail));
        });

        Button deleteBtn = new Button("Delete Selected Product");
        deleteBtn.setGraphic(icon("trash", 16, "#be123c"));
        deleteBtn.getStyleClass().add("dash-btn-danger");
        deleteBtn.setPrefHeight(36);

        deleteBtn.setOnAction(e -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (DatabaseHandler.deleteProduct(idOf(selectedItem), userEmail)) {
                    listView.getItems().remove(selectedItem);
                    showAlert("Success", "Product permanently deleted!", AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Could not delete product!", AlertType.ERROR);
                }
            } else {
                showAlert("Warning", "Please select a product to delete!", AlertType.WARNING);
            }
        });

        box.getChildren().addAll(title, listView, new HBox(10, editBtn, deleteBtn));
        showPage(box);
    }

    private void editProductDialog(String product, String userEmail, Runnable onSaved) {
        Stage editor = new Stage();
        editor.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        editor.initOwner(contentArea.getScene().getWindow());
        editor.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        editor.setTitle("Edit listing · Student Market");
        editor.setMinWidth(650);

        Label eyebrow = new Label("YOUR LISTING");
        eyebrow.setStyle("-fx-text-fill:#a5b4fc;-fx-font-size:10px;-fx-font-weight:bold;-fx-letter-spacing:1.3px;");
        Label heading = new Label("Edit product details");
        heading.setStyle("-fx-text-fill:white;-fx-font-size:22px;-fx-font-weight:bold;");
        Label subheading = new Label("Keep your listing clear and up to date for other students.");
        subheading.setStyle("-fx-text-fill:#cbd5e1;-fx-font-size:12px;");
        Button closeEditor = new Button("×");
        closeEditor.setStyle("-fx-background-color:rgba(255,255,255,0.14);-fx-text-fill:white;-fx-font-size:20px;-fx-background-radius:20;-fx-min-width:34;-fx-min-height:34;-fx-cursor:hand;");
        closeEditor.setOnAction(e -> editor.close());
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox headerTop = new HBox(eyebrow, headerSpacer, closeEditor);
        headerTop.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(6, headerTop, heading, subheading);
        header.setPadding(new Insets(22, 26, 22, 26));
        header.setStyle("-fx-background-color:linear-gradient(to right,#312e81,#4338ca);-fx-background-radius:16 16 0 0;");
        double[] dragOffset = new double[2];
        header.setOnMousePressed(e -> { dragOffset[0] = e.getScreenX() - editor.getX(); dragOffset[1] = e.getScreenY() - editor.getY(); });
        header.setOnMouseDragged(e -> { editor.setX(e.getScreenX() - dragOffset[0]); editor.setY(e.getScreenY() - dragOffset[1]); });

        TextField name = new TextField(field(product, "Title"));
        name.setPromptText("e.g. Scientific calculator");
        TextArea description = new TextArea(field(product, "Desc"));
        description.setPromptText("Describe condition, features, or useful details...");
        description.setWrapText(true);
        description.setPrefRowCount(4);
        TextField price = new TextField(field(product, "Price"));
        price.setPromptText("0.00");
        price.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,7}(\\.\\d{0,2})?") ? change : null));
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Books", "Electronics", "Furniture", "Stationery", "Clothing", "Others", "General");
        String currentCategory = field(product, "Category");
        if (!category.getItems().contains(currentCategory)) category.getItems().add(currentCategory);
        category.setValue(currentCategory);
        category.setMaxWidth(Double.MAX_VALUE);
        for (javafx.scene.Node field : new javafx.scene.Node[]{name, description, price, category})
            field.setStyle(field.getStyle() + ";-fx-font-size:13px;-fx-background-color:#f8fafc;-fx-border-color:#dbe3ee;-fx-border-radius:10;-fx-background-radius:10;");

        String[] photoPath = {field(product, "Image")};
        ImageView photoPreview = new ImageView();
        photoPreview.setFitWidth(94); photoPreview.setFitHeight(82); photoPreview.setPreserveRatio(true); photoPreview.setSmooth(true);
        if (photoPath[0] != null && !photoPath[0].isEmpty()) {
            try { photoPreview.setImage(new Image(Paths.get(photoPath[0]).toUri().toString(), 94, 82, true, true)); }
            catch (Exception ignored) { }
        }
        StackPane previewFrame = new StackPane(photoPreview);
        previewFrame.setMinSize(112, 100); previewFrame.setMaxSize(112, 100);
        previewFrame.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-radius:12;");
        Label photoName = new Label(photoPath[0] == null || photoPath[0].isEmpty() ? "No photo selected" : Paths.get(photoPath[0]).getFileName().toString());
        photoName.setStyle("-fx-text-fill:#64748b;-fx-font-size:11px;");
        Button choosePhoto = new Button("Choose new photo");
        choosePhoto.getStyleClass().add("dash-btn-secondary");
        choosePhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a product photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File chosen = chooser.showOpenDialog(editor);
            if (chosen != null) {
                Image candidate = new Image(chosen.toURI().toString(), 94, 82, true, true);
                if (candidate.isError()) { showAlert("Unsupported photo", "Choose a valid image file.", AlertType.WARNING); return; }
                String copied = copyProductPhoto(chosen);
                if (copied == null) { showAlert("Photo could not be saved", "Please choose another photo.", AlertType.ERROR); return; }
                photoPath[0] = copied; photoPreview.setImage(candidate); photoName.setText(chosen.getName());
            }
        });
        Button removePhoto = new Button("Remove photo");
        removePhoto.setStyle("-fx-background-color:transparent;-fx-text-fill:#be123c;-fx-font-weight:bold;-fx-cursor:hand;");
        removePhoto.setOnAction(e -> { photoPath[0] = null; photoPreview.setImage(null); photoName.setText("No photo selected"); });
        VBox photoActions = new VBox(8, photoName, new HBox(8, choosePhoto, removePhoto));
        photoActions.setAlignment(Pos.CENTER_LEFT);
        HBox photoRow = new HBox(16, previewFrame, photoActions);
        photoRow.setAlignment(Pos.CENTER_LEFT);
        VBox photoCard = new VBox(11, new Label("Product photo"), photoRow);
        photoCard.setPadding(new Insets(14));
        photoCard.setStyle("-fx-background-color:#f8fafc;-fx-border-color:#e5eaf2;-fx-border-radius:12;-fx-background-radius:12;");

        VBox form = new VBox(7);
        form.getChildren().addAll(formField("Product name", name), formField("Description", description));
        HBox priceCategory = new HBox(14, formField("Price (BDT)", price), formField("Category", category));
        HBox.setHgrow(priceCategory.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(priceCategory.getChildren().get(1), Priority.ALWAYS);
        ((VBox) priceCategory.getChildren().get(0)).setMaxWidth(Double.MAX_VALUE);
        ((VBox) priceCategory.getChildren().get(1)).setMaxWidth(Double.MAX_VALUE);
        form.getChildren().addAll(priceCategory, photoCard);

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 18;-fx-cursor:hand;");
        cancel.setOnAction(e -> editor.close());
        Button save = new Button("Save changes");
        save.setGraphic(icon("check", 15, "#ffffff"));
        save.getStyleClass().add("dash-btn-primary");
        save.setStyle("-fx-background-color:#4f46e5;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 18;-fx-cursor:hand;");
        save.setOnAction(e -> {
            String productName = name.getText().trim();
            String productDescription = description.getText().trim();
            double productPrice;
            try { productPrice = Double.parseDouble(price.getText()); }
            catch (NumberFormatException ex) { showAlert("Invalid price", "Enter a valid price in BDT.", AlertType.WARNING); return; }
            if (productName.isEmpty() || productDescription.isEmpty() || category.getValue() == null || productPrice <= 0) {
                showAlert("Check product details", "Name, description, category and a price greater than zero are required.", AlertType.WARNING); return;
            }
            if (!DatabaseHandler.updateProduct(idOf(product), productName, productDescription, productPrice, category.getValue(), userEmail, photoPath[0])) {
                showAlert("Update failed", "This listing could not be updated. It may have been sold or removed.", AlertType.ERROR); return;
            }
            editor.close();
            onSaved.run();
        });
        HBox actions = new HBox(10, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        VBox body = new VBox(16, form, actions);
        body.setPadding(new Insets(22, 26, 24, 26));
        VBox root = new VBox(header, body);
        root.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:#e2e8f0;-fx-border-radius:16;-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.20),28,0.18,0,12);");
        Scene scene = new Scene(root, 690, 590);
        attachStyle(scene);
        scene.setFill(Color.TRANSPARENT);
        editor.setScene(scene);
        editor.sizeToScene();
        editor.showAndWait();
    }

    private VBox formField(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill:#334155;-fx-font-size:12px;-fx-font-weight:bold;");
        return new VBox(7, label, control);
    }

    private void showAddProduct(String userEmail) {
        VBox box = new VBox(13);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(24));
        box.setMaxWidth(560);

        Label title = new Label("Create a listing");
        title.getStyleClass().add("dash-title");
        Label helper = new Label("Add clear details and a photo so other students know what to expect.");
        helper.setWrapText(true);
        helper.setStyle("-fx-text-fill: #64748b;");

        TextField nameF = new TextField(); nameF.setPromptText("Product Name");
        nameF.setMaxWidth(Double.MAX_VALUE);
        TextArea descF = new TextArea();
        descF.setPromptText("Describe its condition, features, or anything a buyer should know");
        descF.setWrapText(true);
        descF.setPrefRowCount(3);
        descF.setMaxWidth(Double.MAX_VALUE);
        TextField priceF = new TextField();
        priceF.setPromptText("Price in BDT");
        priceF.setMaxWidth(Double.MAX_VALUE);
        priceF.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,7}(\\.\\d{0,2})?") ? change : null));

        Label categoryTitle = new Label("Category");
        categoryTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        ToggleGroup categoryGroup = new ToggleGroup();
        FlowPane categoryChoices = new FlowPane(8, 8);
        String[][] categories = {{"Books", "book"}, {"Electronics", "monitor"}, {"Furniture", "chair"},
                {"Stationery", "pencil"}, {"Clothing", "shirt"}, {"Others", "sparkle"}};
        for (String[] category : categories) {
            ToggleButton choice = new ToggleButton(category[0]);
            choice.setGraphic(icon(category[1], 15, "#64748b"));
            choice.setGraphicTextGap(7);
            choice.setUserData(category[0]);
            choice.setToggleGroup(categoryGroup);
            choice.getStyleClass().add("category-choice");
            if (category[0].equals("Books")) choice.setSelected(true);
            choice.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    choice.setGraphic(icon(category[1], 15, isSelected ? "#4338ca" : "#64748b")));
            categoryChoices.getChildren().add(choice);
        }

        Label photoTitle = new Label("Product photo (optional)");
        photoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        ImageView preview = new ImageView();
        preview.setFitWidth(112);
        preview.setFitHeight(96);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        StackPane previewFrame = new StackPane(preview);
        previewFrame.setMinSize(128, 104);
        previewFrame.setMaxSize(128, 104);
        previewFrame.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #dbe3ee; -fx-border-radius: 10; -fx-background-radius: 10;");
        Label photoStatus = new Label("No photo selected");
        photoStatus.setStyle("-fx-text-fill: #64748b;");
        Button choosePhoto = new Button("Choose photo");
        choosePhoto.getStyleClass().add("photo-btn");
        File[] selectedPhoto = new File[1];
        choosePhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a product photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File file = chooser.showOpenDialog(choosePhoto.getScene().getWindow());
            if (file != null) {
                Image candidate = new Image(file.toURI().toString(), 112, 96, true, true);
                if (candidate.isError()) {
                    showAlert("Unsupported photo", "Choose a valid PNG, JPG, GIF, or BMP image.", AlertType.WARNING);
                    return;
                }
                selectedPhoto[0] = file;
                preview.setImage(candidate);
                photoStatus.setText(file.getName());
            }
        });
        HBox photoRow = new HBox(14, previewFrame, new VBox(9, choosePhoto, photoStatus));
        photoRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Save Product");
        saveBtn.setGraphic(icon("plus", 16, "#ffffff"));
        saveBtn.getStyleClass().add("dash-btn-primary");
        saveBtn.setPrefHeight(38);
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        saveBtn.setOnAction(e -> {
            try {
                String pName = nameF.getText().trim();
                String pDesc = descF.getText().trim();
                double pPrice = Double.parseDouble(priceF.getText());
                Toggle selectedCategory = categoryGroup.getSelectedToggle();
                String pCategory = selectedCategory instanceof ToggleButton
                        ? (String) ((ToggleButton) selectedCategory).getUserData() : null;

                if (pName.isEmpty() || pDesc.isEmpty() || pCategory == null) {
                    showAlert("Missing details", "Enter a name and description, then choose a category.", AlertType.WARNING);
                    return;
                }
                if (pPrice <= 0) {
                    showAlert("Invalid price", "Enter a price greater than BDT 0.", AlertType.WARNING);
                    return;
                }

                String storedPhoto = selectedPhoto[0] == null ? null : copyProductPhoto(selectedPhoto[0]);
                if (selectedPhoto[0] != null && storedPhoto == null) {
                    showAlert("Photo could not be saved", "Please choose the photo again or continue without one.", AlertType.ERROR);
                    return;
                }
                if (DatabaseHandler.addProduct(pName, pDesc, pPrice, pCategory, userEmail, storedPhoto)) {
                    showAlert("Success", "Product Added Successfully!", AlertType.INFORMATION);
                    showViewProducts(userEmail);
                } else {
                    showAlert("Error", "Failed to add product!", AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid price!", AlertType.ERROR);
            }
        });

        box.getChildren().addAll(title, helper, labelled("Product name", nameF), labelled("Description", descF),
                labelled("Price (BDT)", priceF), categoryTitle, categoryChoices, photoTitle, photoRow, saveBtn);

        ScrollPane formScroll = new ScrollPane(box);
        formScroll.setFitToWidth(true);
        formScroll.setMaxWidth(600);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        StackPane centerContainer = new StackPane(formScroll);
        showPage(centerContainer);
    }

    private VBox labelled(String title, javafx.scene.Node control) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        return new VBox(6, label, control);
    }

    private String copyProductPhoto(File source) {
        try {
            Path directory = Paths.get("product-images");
            Files.createDirectories(directory);
            String fileName = java.util.UUID.randomUUID() + "-" + source.getName().replaceAll("[^A-Za-z0-9._-]", "_");
            Path destination = directory.resolve(fileName);
            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toAbsolutePath().toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void showMyPurchases(String userEmail) {
        VBox box = new VBox(16);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(24));

        Label title = new Label("Your purchases");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Select a purchase below to message its seller or leave a rating and review.");
        subtitle.getStyleClass().add("dash-subtitle");

        List<String> purchases = DatabaseHandler.getMyPurchases(userEmail);
        Label count = new Label(purchases.size() + (purchases.size() == 1 ? " item" : " items"));
        count.getStyleClass().add("purchase-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(12, title, spacer, count);
        header.setAlignment(Pos.CENTER_LEFT);

        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("product-list");
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(param -> new ProductCell());
        listView.setPlaceholder(new Label("You have not purchased anything yet. Products you buy will appear here."));
        listView.getItems().addAll(purchases);

        Button messageSeller = new Button("Message seller");
        messageSeller.setGraphic(icon("bell", 15, "#4338ca"));
        messageSeller.getStyleClass().add("dash-btn-secondary");
        messageSeller.setDisable(true);
        Button rateSeller = new Button("Rate & review seller");
        rateSeller.setGraphic(icon("sparkle", 15, "#b45309"));
        rateSeller.getStyleClass().add("dash-btn-primary");
        rateSeller.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selected) -> {
            boolean valid = selected != null && DatabaseHandler.hasPurchasedProduct(userEmail, idOf(selected));
            messageSeller.setDisable(!valid);
            rateSeller.setDisable(!valid);
        });
        messageSeller.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String counterpart = DatabaseHandler.getChatCounterpart(userEmail, idOf(selected));
                if (counterpart != null) openChat(idOf(selected), userEmail, counterpart, field(selected, "Seller"), field(selected, "Title"));
            }
        });
        rateSeller.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) openReviewDialog(idOf(selected), userEmail, field(selected, "Seller"));
        });
        HBox actions = new HBox(10, messageSeller, rateSeller);
        box.getChildren().addAll(header, subtitle, actions, listView);
        showPage(box);
    }

    private void openReviewDialog(int productId, String buyerEmail, String sellerName) {
        Stage editor = new Stage();
        editor.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        editor.initOwner(contentArea.getScene().getWindow());
        editor.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        editor.setTitle("Rate your experience");
        int[] selectedRating = {5};

        Label eyebrow = new Label("VERIFIED PURCHASE");
        eyebrow.setStyle("-fx-text-fill:#a5b4fc;-fx-font-size:10px;-fx-font-weight:bold;");
        Label heading = new Label("Rate your experience");
        heading.setStyle("-fx-text-fill:white;-fx-font-size:21px;-fx-font-weight:bold;");
        Label subtitle = new Label("Share an honest review of your purchase from " + sellerName + ".");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill:#cbd5e1;-fx-font-size:12px;");
        Button close = new Button("×");
        close.setStyle("-fx-background-color:rgba(255,255,255,0.14);-fx-text-fill:white;-fx-font-size:20px;-fx-background-radius:20;-fx-min-width:34;-fx-min-height:34;-fx-cursor:hand;");
        close.setOnAction(e -> editor.close());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(eyebrow, spacer, close); top.setAlignment(Pos.CENTER_LEFT);
        VBox header = new VBox(7, top, heading, subtitle);
        header.setPadding(new Insets(22, 26, 22, 26));
        header.setStyle("-fx-background-color:linear-gradient(to right,#312e81,#4338ca);-fx-background-radius:16 16 0 0;");

        Label ratingLabel = new Label("5 out of 5 · Excellent");
        ratingLabel.setStyle("-fx-text-fill:#172554;-fx-font-size:15px;-fx-font-weight:bold;");
        HBox starsRow = new HBox(5); starsRow.setAlignment(Pos.CENTER_LEFT);
        Button[] starButtons = new Button[5];
        String[] ratingNames = {"", "Poor", "Fair", "Good", "Very good", "Excellent"};
        Runnable paintStars = () -> {
            ratingLabel.setText(selectedRating[0] + " out of 5 · " + ratingNames[selectedRating[0]]);
            for (int i = 0; i < starButtons.length; i++) {
                starButtons[i].setText(i < selectedRating[0] ? "★" : "☆");
                starButtons[i].setStyle("-fx-background-color:transparent;-fx-text-fill:" + (i < selectedRating[0] ? "#f59e0b" : "#cbd5e1")
                        + ";-fx-font-size:29px;-fx-padding:0 2;-fx-cursor:hand;");
            }
        };
        for (int i = 0; i < starButtons.length; i++) {
            final int score = i + 1;
            Button star = new Button(); starButtons[i] = star;
            star.setOnAction(e -> { selectedRating[0] = score; paintStars.run(); });
            starsRow.getChildren().add(star);
        }
        paintStars.run();
        VBox ratingCard = new VBox(7, new Label("Your rating"), starsRow, ratingLabel);
        ratingCard.setPadding(new Insets(15));
        ratingCard.setStyle("-fx-background-color:#fffbeb;-fx-border-color:#fde68a;-fx-border-radius:12;-fx-background-radius:12;");
        TextArea comment = new TextArea();
        comment.setPromptText("Tell other students about the communication, item condition, and handover… (optional)");
        comment.setWrapText(true);
        comment.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 600 ? change : null));
        comment.setPrefRowCount(4);
        Label reviewLabel = new Label("Review (optional)");
        reviewLabel.setStyle("-fx-text-fill:#334155;-fx-font-weight:bold;-fx-font-size:12px;");
        VBox reviewBox = new VBox(7, reviewLabel, comment);

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 18;-fx-cursor:hand;");
        cancel.setOnAction(e -> editor.close());
        Button save = new Button("Submit review");
        save.setGraphic(icon("check", 15, "#ffffff"));
        save.setStyle("-fx-background-color:#4f46e5;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:10 18;-fx-cursor:hand;");
        save.setOnAction(e -> {
            if (DatabaseHandler.addSellerReview(buyerEmail, productId, selectedRating[0], comment.getText())) {
                editor.close();
                showAlert("Review saved", "Thanks for sharing your experience.", AlertType.INFORMATION);
                showMyPurchases(buyerEmail);
            } else showAlert("Could not save review", "Only verified buyers can review a purchase.", AlertType.WARNING);
        });
        HBox actions = new HBox(10, cancel, save); actions.setAlignment(Pos.CENTER_RIGHT);
        VBox body = new VBox(16, ratingCard, reviewBox, actions); body.setPadding(new Insets(22, 26, 24, 26));
        VBox root = new VBox(header, body);
        root.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:#e2e8f0;-fx-border-radius:16;-fx-effect:dropshadow(gaussian,rgba(15,23,42,0.20),28,0.18,0,12);");
        Scene scene = new Scene(root, 590, 480); scene.setFill(Color.TRANSPARENT); attachStyle(scene);
        editor.setScene(scene); editor.sizeToScene(); editor.showAndWait();
    }

    private void showMessages(String userEmail) {
        VBox box = new VBox(14);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(22));
        Label title = new Label("Messages");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Private conversations stay linked to a marketplace listing. Email addresses are not shown.");
        subtitle.getStyleClass().add("dash-subtitle");
        ListView<DatabaseHandler.ChatConversation> conversations = new ListView<>();
        conversations.getStyleClass().add("product-list");
        conversations.getItems().setAll(DatabaseHandler.getChatConversations(userEmail));
        conversations.setPlaceholder(new Label("No conversations yet. Open a product and choose Message seller to start."));
        VBox.setVgrow(conversations, Priority.ALWAYS);
        Button open = new Button("Open conversation");
        open.setGraphic(icon("bell", 15, "#ffffff"));
        open.getStyleClass().add("dash-btn-primary");
        open.setDisable(true);
        conversations.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> open.setDisable(selected == null));
        open.setOnAction(e -> {
            DatabaseHandler.ChatConversation selected = conversations.getSelectionModel().getSelectedItem();
            if (selected != null) openChat(selected.productId, userEmail, selected.otherEmail, selected.otherName, selected.productName);
            showMessages(userEmail);
        });
        conversations.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && conversations.getSelectionModel().getSelectedItem() != null) open.fire();
        });
        box.getChildren().addAll(title, subtitle, conversations, open);
        showPage(box);
    }

    private void openChat(int productId, String userEmail, String otherEmail, String otherName, String productName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Marketplace messages");
        dialog.setHeaderText(productName + "  ·  Chat with " + otherName);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ListView<DatabaseHandler.ChatMessage> messages = new ListView<>();
        messages.setCellFactory(list -> new ListCell<DatabaseHandler.ChatMessage>() {
            private final Label text = new Label();
            { text.setWrapText(true); text.setMaxWidth(420); }
            @Override protected void updateItem(DatabaseHandler.ChatMessage item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                text.setText(item.toString());
                text.setStyle(item.mine
                        ? "-fx-background-color: #eef2ff; -fx-background-radius: 11px; -fx-padding: 10 12; -fx-text-fill: #312e81;"
                        : "-fx-background-color: #f1f5f9; -fx-background-radius: 11px; -fx-padding: 10 12; -fx-text-fill: #334155;");
                HBox row = new HBox(text); row.setAlignment(item.mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT); row.setPadding(new Insets(4));
                setGraphic(row); setText(null);
            }
        });
        messages.setPlaceholder(new Label("Say hello to start the conversation."));
        VBox.setVgrow(messages, Priority.ALWAYS);
        TextArea compose = new TextArea();
        compose.setPromptText("Write a message… (up to 1,500 characters)");
        compose.setWrapText(true);
        compose.setPrefRowCount(3);
        compose.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 1500 ? change : null));
        Button send = new Button("Send message");
        send.setGraphic(icon("plus", 14, "#ffffff"));
        send.getStyleClass().add("dash-btn-primary");
        Runnable refresh = () -> { messages.getItems().setAll(DatabaseHandler.getChatMessages(userEmail, productId, otherEmail)); if (!messages.getItems().isEmpty()) messages.scrollTo(messages.getItems().size()-1); };
        refresh.run();
        send.setOnAction(e -> {
            String message = compose.getText();
            if (message.trim().isEmpty()) return;
            if (DatabaseHandler.sendChatMessage(userEmail, productId, otherEmail, message)) { compose.clear(); refresh.run(); }
            else showAlert("Message not sent", "This conversation may be unavailable or blocked.", AlertType.WARNING);
        });
        VBox content = new VBox(10, messages, compose, send);
        content.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(560, 520);
        dialog.initOwner((Stage) contentArea.getScene().getWindow());
        dialog.showAndWait();
    }

    private void showNotifications(String userEmail) {
        VBox box = new VBox(16);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(24));

        Label title = new Label("Notifications");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Updates about purchases and sales from your marketplace activity.");
        subtitle.getStyleClass().add("dash-subtitle");
        List<DatabaseHandler.Notification> notifications = DatabaseHandler.getNotifications(userEmail);
        int unread = DatabaseHandler.countUnreadNotifications(userEmail);
        Label unreadBadge = new Label(unread + " unread");
        unreadBadge.getStyleClass().add("purchase-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox heading = new HBox(12, title, spacer, unreadBadge);
        heading.setAlignment(Pos.CENTER_LEFT);

        ListView<DatabaseHandler.Notification> list = new ListView<>();
        list.getStyleClass().add("product-list");
        VBox.setVgrow(list, Priority.ALWAYS);
        list.setItems(javafx.collections.FXCollections.observableArrayList(notifications));
        list.setCellFactory(param -> new NotificationCell());
        list.setPlaceholder(new Label("No notifications yet. Purchase updates will appear here."));

        Button markSelected = new Button("Mark selected as read");
        markSelected.getStyleClass().add("dash-btn-secondary");
        markSelected.setGraphic(icon("check", 15, "#4338ca"));
        Button markAll = new Button("Mark all as read");
        markAll.getStyleClass().add("dash-btn-primary");
        markAll.setGraphic(icon("check", 15, "#ffffff"));
        markAll.setDisable(unread == 0);
        markSelected.setDisable(true);
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) ->
                markSelected.setDisable(selected == null || selected.isRead()));
        markSelected.setOnAction(e -> {
            DatabaseHandler.Notification selected = list.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                DatabaseHandler.markNotificationRead(selected.getId(), userEmail);
                updateNotificationButton(userEmail);
                showNotifications(userEmail);
            }
        });
        markAll.setOnAction(e -> {
            DatabaseHandler.markAllNotificationsRead(userEmail);
            updateNotificationButton(userEmail);
            showNotifications(userEmail);
        });

        HBox actions = new HBox(10, markSelected, markAll);
        box.getChildren().addAll(heading, subtitle, list, actions);
        showPage(box);
    }

    private void showSoldHistory(String userEmail) {
        VBox page = new VBox(16);
        page.getStyleClass().add("dash-card");
        page.setPadding(new Insets(24));
        Label title = new Label("Sold History"); title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Completed sales, earnings, and buyer names for your listings."); subtitle.getStyleClass().add("dash-subtitle");
        List<String> sales = DatabaseHandler.getSoldNotifications(userEmail);
        Label count = new Label(sales.size() + (sales.size() == 1 ? " completed sale" : " completed sales"));
        count.getStyleClass().add("purchase-count");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox heading = new HBox(12, title, spacer, count); heading.setAlignment(Pos.CENTER_LEFT);
        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("product-list");
        listView.getItems().setAll(sales);
        listView.setPlaceholder(new Label("No completed sales yet. Your sold items will appear here."));
        listView.setCellFactory(view -> new ListCell<String>() {
            private final ImageView photo = new ImageView();
            private final StackPane photoFrame = new StackPane(photo);
            private final Label status = new Label("SOLD");
            private final Label product = new Label();
            private final Label price = new Label();
            private final Label buyer = new Label();
            private final Region rowSpacer = new Region();
            private final HBox cardHeading = new HBox(10);
            private final VBox text = new VBox(8);
            private final HBox card = new HBox(15);
            {
                photo.setFitWidth(78); photo.setFitHeight(72); photo.setPreserveRatio(true); photo.setSmooth(true);
                photoFrame.setMinSize(92, 88); photoFrame.setMaxSize(92, 88);
                photoFrame.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:12;-fx-border-color:#e2e8f0;-fx-border-radius:12;");
                status.setStyle("-fx-background-color:#ecfdf5;-fx-text-fill:#047857;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-radius:30;-fx-padding:5 9;");
                product.setStyle("-fx-text-fill:#172554;-fx-font-size:15px;-fx-font-weight:bold;");
                price.setStyle("-fx-text-fill:#4338ca;-fx-font-size:13px;-fx-font-weight:bold;");
                buyer.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                cardHeading.getChildren().addAll(product, rowSpacer, status);
                text.getChildren().addAll(cardHeading, price, buyer);
                card.getChildren().addAll(photoFrame, text); card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(13)); card.setMaxWidth(Double.MAX_VALUE);
                card.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:#e5eaf2;-fx-border-radius:14;-fx-effect:dropshadow(three-pass-box,rgba(15,23,42,0.07),12,0,0,3);");
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                product.setText(field(item, "Title"));
                price.setText(field(item, "Price"));
                buyer.setText("Buyer  ·  " + field(item, "Buyer"));
                photo.setImage(null);
                String path = field(item, "Photo");
                if (path != null && !path.isEmpty()) {
                    try { Image candidate = new Image(Paths.get(path).toUri().toString(), 78, 72, true, true); if (!candidate.isError()) photo.setImage(candidate); }
                    catch (Exception ignored) { }
                }
                setGraphic(card); setText(null);
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);
        page.getChildren().addAll(heading, subtitle, listView);
        showPage(page);
    }

    private void reportSeller(int productId, String userEmail) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Report seller");
        dialog.setHeaderText("Tell us what happened. Reports are private.");
        ButtonType send = new ButtonType("Submit report", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(send, ButtonType.CANCEL);
        ComboBox<String> reason = new ComboBox<>();
        reason.getItems().addAll("Scam or misleading listing", "Harassment or unsafe behavior", "Prohibited item", "Other concern");
        reason.setValue(reason.getItems().get(0));
        TextArea details = new TextArea();
        details.setPromptText("Optional details (do not include passwords or private information)");
        details.setWrapText(true);
        details.setPrefRowCount(4);
        VBox content = new VBox(10, new Label("Reason"), reason, new Label("Details"), details);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait().filter(send::equals).ifPresent(button -> {
            if (DatabaseHandler.reportSeller(userEmail, productId, reason.getValue(), details.getText())) {
                showAlert("Report submitted", "Thank you. The report has been saved for review.", AlertType.INFORMATION);
            } else showAlert("Could not submit report", "This listing may no longer be available.", AlertType.ERROR);
        });
    }

    private void showPrivacySafety(Stage stage, String userEmail) {
        VBox box = new VBox(16);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(24));
        Label title = new Label("Privacy & Safety");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Manage blocked sellers, report unsafe listings, or permanently remove your account.");
        subtitle.getStyleClass().add("dash-subtitle");

        Label blockedTitle = new Label("Blocked sellers");
        blockedTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        ListView<DatabaseHandler.BlockedUser> blocked = new ListView<>();
        blocked.getStyleClass().add("product-list");
        blocked.setPrefHeight(210);
        blocked.setPlaceholder(new Label("You have not blocked anyone."));
        blocked.getItems().setAll(DatabaseHandler.getBlockedUsers(userEmail));
        Button unblock = new Button("Unblock selected seller");
        unblock.getStyleClass().add("dash-btn-secondary");
        unblock.setDisable(true);
        blocked.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, item) -> unblock.setDisable(item == null));
        unblock.setOnAction(e -> {
            DatabaseHandler.BlockedUser selected = blocked.getSelectionModel().getSelectedItem();
            if (selected != null && DatabaseHandler.unblockSeller(userEmail, selected.getEmail())) {
                blocked.getItems().remove(selected);
                showViewProducts(userEmail);
                showPrivacySafety(stage, userEmail);
            }
        });

        Separator separator = new Separator();
        Label deleteTitle = new Label("Delete account and personal data");
        deleteTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #9f1239;");
        Label deleteInfo = new Label("This permanently removes your account, listings, purchase records, notifications, reports, and blocks from this local marketplace database.");
        deleteInfo.setWrapText(true);
        deleteInfo.setStyle("-fx-text-fill: #64748b;");
        PasswordField password = new PasswordField();
        password.setPromptText("Confirm your current password");
        TextField confirmation = new TextField();
        confirmation.setPromptText("Type DELETE to confirm");
        Button deleteAccount = new Button("Permanently delete my account");
        deleteAccount.getStyleClass().add("dash-btn-danger");
        deleteAccount.setOnAction(e -> {
            if (!"DELETE".equals(confirmation.getText().trim())) {
                showAlert("Confirmation required", "Type DELETE to confirm account removal.", AlertType.WARNING);
                return;
            }
            if (password.getText().isEmpty()) {
                showAlert("Password required", "Enter your current password to verify account ownership.", AlertType.WARNING);
                return;
            }
            Alert confirm = new Alert(AlertType.CONFIRMATION, "This cannot be undone. Your marketplace listings and history will be removed.", ButtonType.CANCEL, ButtonType.OK);
            confirm.setTitle("Delete account");
            confirm.setHeaderText("Permanently delete this account?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (DatabaseHandler.deleteAccount(userEmail, password.getText())) {
                    showAlert("Account deleted", "Your account and associated marketplace data have been removed.", AlertType.INFORMATION);
                    openLoginWindow(stage);
                } else showAlert("Could not delete account", "The password was incorrect or the account could not be removed.", AlertType.ERROR);
            }
        });

        VBox deletePanel = new VBox(10, deleteTitle, deleteInfo, labelled("Password", password), labelled("Confirmation", confirmation), deleteAccount);
        deletePanel.setPadding(new Insets(16));
        deletePanel.setStyle("-fx-background-color: #fff7f8; -fx-background-radius: 12px; -fx-border-color: #fecdd3; -fx-border-radius: 12px;");
        VBox content = new VBox(12, title, subtitle, blockedTitle, blocked, unblock, separator, deletePanel);
        VBox.setVgrow(blocked, Priority.ALWAYS);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        showPage(scroll);
    }

    private void showAdminPanel(String adminEmail) {
        if (!DatabaseHandler.isAdmin(adminEmail)) {
            showAlert("Access denied", "This account is not authorized to use the admin panel.", AlertType.ERROR);
            showViewProducts(adminEmail);
            return;
        }
        VBox box = new VBox(14);
        box.getStyleClass().add("dash-card");
        box.setPadding(new Insets(22));
        Label title = new Label("Admin Panel");
        title.getStyleClass().add("dash-title");
        Label subtitle = new Label("Manage student accounts, listings, and submitted safety reports.");
        subtitle.getStyleClass().add("dash-subtitle");

        int[] counts = DatabaseHandler.getAdminCounts(adminEmail);
        HBox metrics = new HBox(12, adminMetric("ACTIVE USERS", counts[0]),
                adminMetric("AVAILABLE LISTINGS", counts[1]), adminMetric("OPEN REPORTS", counts[2]));
        for (javafx.scene.Node n : metrics.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        Label sectionTitle = new Label("Marketplace control center");
        sectionTitle.setGraphic(icon("monitor", 17, "#4338ca"));
        sectionTitle.setGraphicTextGap(8);
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("admin-tabs");
        tabs.getTabs().addAll(adminTab("Overview", new VBox(18, metrics,
                        new Label("Review marketplace health, investigate reports, and take action on accounts or listings."))),
                adminUsersTab(adminEmail), adminListingsTab(adminEmail), adminReportsTab(adminEmail), adminOrdersTab(adminEmail));
        for (Tab tab : tabs.getTabs()) tab.setClosable(false);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        box.getChildren().addAll(title, subtitle, sectionTitle, tabs);
        showPage(box);
    }

    private VBox adminMetric(String caption, int count) {
        Label number = new Label(Integer.toString(count));
        number.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4338ca;");
        Label label = new Label(caption);
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        VBox metric = new VBox(6, number, label);
        metric.setPadding(new Insets(15));
        metric.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12px; -fx-border-color: #e8edf5; -fx-border-radius: 12px;");
        return metric;
    }

    private Tab adminTab(String title, javafx.scene.Node content) { return new Tab(title, content); }

    private Tab adminUsersTab(String adminEmail) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        Label hint = new Label("Suspended accounts cannot sign in or publish visible listings. The admin account is protected.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #64748b;");
        ListView<DatabaseHandler.AdminUser> users = new ListView<>();
        List<DatabaseHandler.AdminUser> allUsers = DatabaseHandler.getAdminUsers(adminEmail);
        users.getItems().setAll(allUsers);
        users.getStyleClass().add("product-list");
        users.setCellFactory(list -> new ListCell<DatabaseHandler.AdminUser>() {
            private final Label name = new Label();
            private final Label email = new Label();
            private final Label details = new Label();
            private final Label state = new Label();
            private final Region spacer = new Region();
            private final HBox row = new HBox(12);
            private final VBox labels = new VBox(4);
            { name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #172554;");
              email.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
              details.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
              HBox.setHgrow(spacer, Priority.ALWAYS); labels.getChildren().addAll(name, email, details);
              row.setAlignment(Pos.CENTER_LEFT); row.getChildren().addAll(labels, spacer, state); }
            @Override protected void updateItem(DatabaseHandler.AdminUser user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) { setGraphic(null); setText(null); return; }
                name.setText(user.name == null || user.name.trim().isEmpty() ? "Student" : user.name);
                email.setText(user.email);
                details.setText("Student ID: " + (user.studentId == null ? "—" : user.studentId)
                        + "  ·  Account type: " + (user.role == null ? "Student" : user.role));
                state.setText(user.disabled ? "SUSPENDED" : (DatabaseHandler.isAdmin(user.email) ? "ADMIN" : "ACTIVE"));
                state.setStyle(user.disabled
                        ? "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #be123c; -fx-background-color: #fff1f2; -fx-background-radius: 99px; -fx-padding: 5 9;"
                        : "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #047857; -fx-background-color: #ecfdf5; -fx-background-radius: 99px; -fx-padding: 5 9;");
                row.setPadding(new Insets(8, 10, 8, 10));
                row.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: #e8edf5; -fx-border-radius: 10px;");
                setGraphic(row); setText(null);
            }
        });
        users.setPlaceholder(new Label("No users found."));
        VBox.setVgrow(users, Priority.ALWAYS);
        TextField search = adminSearch("Search by student name or email...");
        search.textProperty().addListener((o, old, value) -> {
            String query = value.toLowerCase(java.util.Locale.ROOT).trim();
            users.getItems().setAll(allUsers.stream().filter(u -> (u.name + " " + u.email).toLowerCase(java.util.Locale.ROOT).contains(query)).collect(java.util.stream.Collectors.toList()));
        });
        Button refresh = new Button("Refresh users");
        refresh.getStyleClass().add("dash-btn-secondary");
        refresh.setOnAction(e -> showAdminPanel(adminEmail));
        Button toggle = new Button("Suspend selected user");
        toggle.getStyleClass().add("dash-btn-danger");
        toggle.setDisable(true);
        users.getSelectionModel().selectedItemProperty().addListener((o, old, selected) -> {
            toggle.setDisable(selected == null || DatabaseHandler.isAdmin(selected.email));
            toggle.setText(selected != null && selected.disabled ? "Restore selected user" : "Suspend selected user");
        });
        toggle.setOnAction(e -> {
            DatabaseHandler.AdminUser selected = users.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            boolean suspend = !selected.disabled;
            Alert confirm = new Alert(AlertType.CONFIRMATION,
                    suspend ? "This user will not be able to sign in until restored." : "This user will be able to sign in again.",
                    ButtonType.CANCEL, ButtonType.OK);
            confirm.setHeaderText((suspend ? "Suspend " : "Restore ") + selected.name + "?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (DatabaseHandler.setUserDisabled(adminEmail, selected.email, suspend)) showAdminPanel(adminEmail);
                else showAlert("Could not update user", "Please try again.", AlertType.ERROR);
            }
        });
        content.getChildren().addAll(hint, new HBox(10, search, refresh), users, toggle);
        return adminTab("Users", content);
    }

    private Tab adminListingsTab(String adminEmail) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        ListView<String> listings = new ListView<>();
        listings.setCellFactory(p -> new ProductCell());
        List<String> allListings = DatabaseHandler.getAdminProducts(adminEmail);
        listings.getItems().setAll(allListings);
        listings.getStyleClass().add("product-list");
        listings.setPlaceholder(new Label("No available listings."));
        VBox.setVgrow(listings, Priority.ALWAYS);
        TextField search = adminSearch("Search listings by product, seller, or category...");
        search.textProperty().addListener((o, old, value) -> {
            String query = value.toLowerCase(java.util.Locale.ROOT).trim();
            listings.getItems().setAll(allListings.stream().filter(item -> item.toLowerCase(java.util.Locale.ROOT).contains(query)).collect(java.util.stream.Collectors.toList()));
        });
        Button refresh = new Button("Refresh listings");
        refresh.getStyleClass().add("dash-btn-secondary");
        refresh.setOnAction(e -> showAdminPanel(adminEmail));
        Button remove = new Button("Remove selected listing");
        remove.setGraphic(icon("trash", 15, "#be123c"));
        remove.getStyleClass().add("dash-btn-danger");
        remove.setDisable(true);
        listings.getSelectionModel().selectedItemProperty().addListener((o, old, selected) -> remove.setDisable(selected == null));
        remove.setOnAction(e -> {
            String selected = listings.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Alert confirm = new Alert(AlertType.CONFIRMATION, "This removes the selected listing.", ButtonType.CANCEL, ButtonType.OK);
            confirm.setHeaderText("Remove “" + field(selected, "Title") + "”?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (DatabaseHandler.deleteProductAsAdmin(adminEmail, idOf(selected))) showAdminPanel(adminEmail);
                else showAlert("Could not remove listing", "The listing may already have been removed.", AlertType.ERROR);
            }
        });
        content.getChildren().addAll(new HBox(10, search, refresh), listings, remove);
        return adminTab("Listings", content);
    }

    private Tab adminOrdersTab(String adminEmail) {
        VBox content = new VBox(13);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color:white;");
        Label heading = new Label("Orders & conversations");
        heading.setStyle("-fx-text-fill:#172554;-fx-font-size:17px;-fx-font-weight:bold;");
        Label subtitle = new Label("Read-only audit of completed purchases and messages for each order.");
        subtitle.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");

        List<DatabaseHandler.AdminOrder> allOrders = DatabaseHandler.getAdminOrders(adminEmail);
        Label count = new Label(allOrders.size() + (allOrders.size() == 1 ? " order" : " orders"));
        count.getStyleClass().add("purchase-count");
        TextField search = adminSearch("Search product, buyer, seller, or payment method...");
        search.getStyleClass().add("admin-search");
        Button refresh = new Button("Refresh"); refresh.getStyleClass().add("dash-btn-secondary");
        refresh.setOnAction(e -> showAdminPanel(adminEmail));
        HBox searchRow = new HBox(10, search, count, refresh); searchRow.setAlignment(Pos.CENTER_LEFT);

        ListView<DatabaseHandler.AdminOrder> orders = new ListView<>();
        orders.getStyleClass().add("product-list");
        orders.getItems().setAll(allOrders);
        orders.setPrefHeight(220);
        orders.setMinHeight(140);
        orders.setPlaceholder(new Label("No completed purchases have been recorded yet."));
        orders.setCellFactory(view -> new ListCell<DatabaseHandler.AdminOrder>() {
            private final Label product = new Label();
            private final Label buyer = new Label();
            private final Label amount = new Label();
            private final Label method = new Label();
            private final Region spacer = new Region();
            private final VBox labels = new VBox(5);
            private final VBox card = new VBox(7);
            private final HBox top = new HBox(10);
            {
                product.setStyle("-fx-text-fill:#172554;-fx-font-size:13px;-fx-font-weight:bold;");
                buyer.setStyle("-fx-text-fill:#64748b;-fx-font-size:11px;");
                amount.setStyle("-fx-text-fill:#4338ca;-fx-font-size:12px;-fx-font-weight:bold;");
                method.setStyle("-fx-text-fill:#047857;-fx-background-color:#ecfdf5;-fx-background-radius:30;-fx-padding:5 8;-fx-font-size:10px;-fx-font-weight:bold;");
                labels.getChildren().addAll(product,buyer);
                HBox.setHgrow(spacer,Priority.ALWAYS); top.setAlignment(Pos.CENTER_LEFT); top.getChildren().addAll(labels,spacer,amount);
                card.getChildren().addAll(top,method); card.setPadding(new Insets(11,13,11,13));
                card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e5eaf2;-fx-border-radius:12;");
            }
            @Override protected void updateItem(DatabaseHandler.AdminOrder order, boolean empty) {
                super.updateItem(order,empty);
                if (empty || order==null) { setGraphic(null); setText(null); return; }
                product.setText(order.productName == null ? "Product" : order.productName);
                buyer.setText("Buyer: " + order.buyerName + "  ·  Seller: " + order.sellerName);
                amount.setText("BDT " + String.format(java.util.Locale.ROOT,"%.2f",order.price));
                method.setText(order.paymentMethod);
                setGraphic(card); setText(null);
            }
        });
        search.textProperty().addListener((obs,old,value) -> {
            String q=value.toLowerCase(java.util.Locale.ROOT).trim();
            orders.getItems().setAll(allOrders.stream().filter(o -> (o.productName+" "+o.buyerName+" "+o.buyerEmail+" "+o.sellerName+" "+o.sellerEmail+" "+o.paymentMethod)
                    .toLowerCase(java.util.Locale.ROOT).contains(q)).collect(java.util.stream.Collectors.toList()));
        });

        Label auditBadge = new Label("ADMIN AUDIT · READ ONLY");
        auditBadge.setStyle("-fx-text-fill:#4338ca;-fx-font-size:10px;-fx-font-weight:bold;-fx-background-color:#eef2ff;-fx-background-radius:30;-fx-padding:6 10;");
        Label detailsTitle = new Label("Select an order to inspect its details");
        detailsTitle.setStyle("-fx-text-fill:#172554;-fx-font-size:14px;-fx-font-weight:bold;");
        Label details = new Label("Buyer, seller, payment preference, and order time will appear here.");
        details.setWrapText(true); details.setStyle("-fx-text-fill:#475569;-fx-font-size:11px;-fx-line-spacing:3px;");
        VBox orderDetails = new VBox(9, new HBox(auditBadge), detailsTitle, details);
        orderDetails.setPadding(new Insets(14));
        orderDetails.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12;-fx-border-color:#e5eaf2;-fx-border-radius:12;");

        Label messagesTitle = new Label("Order messages");
        messagesTitle.setStyle("-fx-text-fill:#172554;-fx-font-size:13px;-fx-font-weight:bold;");
        ListView<DatabaseHandler.AdminOrderMessage> messages = new ListView<>();
        messages.getStyleClass().add("product-list");
        messages.setPrefHeight(200); messages.setMinHeight(120);
        messages.setPlaceholder(new Label("No buyer–seller messages are recorded for this order."));
        messages.setCellFactory(view -> new ListCell<DatabaseHandler.AdminOrderMessage>() {
            private final Label sender = new Label();
            private final Label time = new Label();
            private final Label text = new Label();
            private final Region spacer = new Region();
            private final HBox header = new HBox(8);
            private final VBox card = new VBox(7);
            {
                sender.setStyle("-fx-text-fill:#312e81;-fx-font-size:11px;-fx-font-weight:bold;");
                time.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:10px;");
                text.setStyle("-fx-text-fill:#334155;-fx-font-size:12px;"); text.setWrapText(true); text.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(spacer,Priority.ALWAYS); header.getChildren().addAll(sender,spacer,time);
                card.getChildren().addAll(header,text); card.setPadding(new Insets(11,13,11,13));
                card.setStyle("-fx-background-color:white;-fx-background-radius:11;-fx-border-color:#e5eaf2;-fx-border-radius:11;");
            }
            @Override protected void updateItem(DatabaseHandler.AdminOrderMessage message, boolean empty) {
                super.updateItem(message,empty);
                if (empty || message==null) { setGraphic(null); setText(null); return; }
                sender.setText(message.senderRole+" · "+message.senderName+" · "+message.senderEmail);
                time.setText(message.sentAt == null ? "" : message.sentAt);
                text.setText(message.message);
                setGraphic(card); setText(null);
            }
        });
        orders.getSelectionModel().selectedItemProperty().addListener((obs,old,selected) -> {
            if (selected == null) {
                detailsTitle.setText("Select an order to inspect its details");
                details.setText("Buyer, seller, payment preference, and order time will appear here.");
                messages.getItems().clear();
            } else {
                detailsTitle.setText("Order #"+selected.productId+" · "+selected.productName);
                details.setText("Category: "+selected.category+"\nAmount: BDT "+String.format(java.util.Locale.ROOT,"%.2f",selected.price)
                        +"\nPayment preference: "+selected.paymentMethod+"\nOrder time: "+selected.purchasedAt
                        +"\n\nBuyer: "+selected.buyerName+" · "+selected.buyerEmail+" · Student ID: "+(selected.buyerStudentId==null?"—":selected.buyerStudentId)
                        +"\nSeller: "+selected.sellerName+" · "+selected.sellerEmail+" · Student ID: "+(selected.sellerStudentId==null?"—":selected.sellerStudentId));
                messages.getItems().setAll(DatabaseHandler.getAdminOrderMessages(adminEmail,selected.productId));
            }
        });
        if (!orders.getItems().isEmpty()) orders.getSelectionModel().selectFirst();
        VBox detailColumn = new VBox(10, orderDetails, messagesTitle, messages);
        VBox.setVgrow(messages,Priority.ALWAYS);
        VBox contentColumn = new VBox(10, heading, subtitle, searchRow, orders, detailColumn);
        VBox.setVgrow(detailColumn,Priority.ALWAYS);
        contentColumn.setPadding(new Insets(18));
        contentColumn.setStyle("-fx-background-color:white;");
        ScrollPane scroll = new ScrollPane(contentColumn); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
        return adminTab("Orders & Chats",scroll);
    }

    private Tab adminReportsTab(String adminEmail) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color:#ffffff;");
        Label heading = new Label("Safety reports"); heading.setStyle("-fx-text-fill:#172554;-fx-font-size:17px;-fx-font-weight:bold;");
        Label hint = new Label("Review student concerns and record a clear outcome."); hint.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");
        ListView<DatabaseHandler.AdminReport> reports = new ListView<>();
        List<DatabaseHandler.AdminReport> allReports = DatabaseHandler.getAdminReports(adminEmail);
        reports.getItems().setAll(allReports);
        reports.getStyleClass().add("product-list");
        reports.setCellFactory(list -> new ListCell<DatabaseHandler.AdminReport>() {
            private final Label reason = new Label();
            private final Label summaryLine = new Label();
            private final Label state = new Label();
            private final Region spacer = new Region();
            private final HBox row = new HBox(10);
            private final VBox labels = new VBox(6);
            private final VBox card = new VBox(7);
            { reason.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #172554;");
              summaryLine.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;"); summaryLine.setMaxWidth(600); summaryLine.setEllipsisString("…");
              HBox.setHgrow(spacer, Priority.ALWAYS); row.setAlignment(Pos.CENTER_LEFT); row.getChildren().addAll(reason, spacer, state);
              labels.getChildren().addAll(row, summaryLine); card.getChildren().add(labels); card.setPadding(new Insets(12,14,12,14));
              card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e5eaf2;-fx-border-radius:12;"); }
            @Override protected void updateItem(DatabaseHandler.AdminReport report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) { setGraphic(null); setText(null); return; }
                reason.setText("#" + report.id + "  ·  " + report.reason);
                String firstLine = report.summary == null ? "" : report.summary.replace('\n', ' ');
                summaryLine.setText(firstLine);
                state.setText(report.status);
                boolean open = "OPEN".equals(report.status);
                state.setStyle(open
                        ? "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #b45309; -fx-background-color: #fff7ed; -fx-background-radius: 99px; -fx-padding: 5 9;"
                        : "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #047857; -fx-background-color: #ecfdf5; -fx-background-radius: 99px; -fx-padding: 5 9;");
                setGraphic(card); setText(null);
            }
        });
        reports.setPlaceholder(new Label("No safety reports yet."));
        reports.setMinHeight(170);
        reports.setPrefHeight(270);
        VBox.setVgrow(reports, Priority.ALWAYS);
        TextField search = adminSearch("Search reports by reason, name, or status...");
        search.getStyleClass().add("admin-search");
        search.textProperty().addListener((o, old, value) -> {
            String query = value.toLowerCase(java.util.Locale.ROOT).trim();
            reports.getItems().setAll(allReports.stream().filter(r -> (r.summary + " " + r.reason + " " + r.status).toLowerCase(java.util.Locale.ROOT).contains(query)).collect(java.util.stream.Collectors.toList()));
        });
        Button refresh = new Button("Refresh reports");
        refresh.getStyleClass().add("dash-btn-secondary");
        refresh.setOnAction(e -> showAdminPanel(adminEmail));
        HBox searchRow = new HBox(10, search, refresh); searchRow.setAlignment(Pos.CENTER_LEFT);
        Label detailTitle = new Label("Report details"); detailTitle.setStyle("-fx-text-fill:#172554;-fx-font-size:13px;-fx-font-weight:bold;");
        Label summary = new Label("Select a report above to review its listing, people involved, reason, and submitted details.");
        summary.setWrapText(true); summary.setMaxWidth(Double.MAX_VALUE);
        summary.setStyle("-fx-text-fill:#475569;-fx-font-size:12px;-fx-line-spacing:3px;");
        VBox detailCard = new VBox(8, detailTitle, summary);
        detailCard.setPadding(new Insets(14));
        detailCard.setStyle("-fx-background-color:#f8fafc;-fx-background-radius:12;-fx-border-color:#e5eaf2;-fx-border-radius:12;");
        reports.getSelectionModel().selectedItemProperty().addListener((o, old, selected) -> {
            detailTitle.setText(selected == null ? "Report details" : "Report #" + selected.id + " · " + selected.status);
            summary.setText(selected == null ? "Select a report above to review its listing, people involved, reason, and submitted details." : selected.summary);
        });
        Button resolve = new Button("Mark resolved");
        resolve.getStyleClass().add("dash-btn-primary");
        Button dismiss = new Button("Dismiss report");
        dismiss.getStyleClass().add("dash-btn-danger");
        resolve.setDisable(true); dismiss.setDisable(true);
        reports.getSelectionModel().selectedItemProperty().addListener((o, old, selected) -> {
            boolean open = selected != null && "OPEN".equals(selected.status);
            resolve.setDisable(!open); dismiss.setDisable(!open);
        });
        resolve.setOnAction(e -> reviewAdminReport(adminEmail, reports.getSelectionModel().getSelectedItem(), "RESOLVED"));
        dismiss.setOnAction(e -> reviewAdminReport(adminEmail, reports.getSelectionModel().getSelectedItem(), "DISMISSED"));
        HBox actions = new HBox(10, resolve, dismiss); actions.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().addAll(heading, hint, searchRow, reports, detailCard, actions);
        ScrollPane reportScroll = new ScrollPane(content);
        reportScroll.setFitToWidth(true);
        reportScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-color:transparent;");
        return adminTab("Reports", reportScroll);
    }

    private TextField adminSearch(String prompt) {
        TextField search = new TextField();
        search.setPromptText(prompt);
        HBox.setHgrow(search, Priority.ALWAYS);
        return search;
    }

    private void reviewAdminReport(String adminEmail, DatabaseHandler.AdminReport report, String status) {
        if (report != null && DatabaseHandler.reviewReport(adminEmail, report.id, status)) showAdminPanel(adminEmail);
        else if (report != null) showAlert("Could not update report", "Please try again.", AlertType.ERROR);
    }

    private ListView<String> createSimpleListView(String tagColor) {
        ListView<String> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setCellFactory(param -> new ListCell<String>() {
            private final VBox cardContainer = new VBox(4);
            private final Label titleLabel = new Label();
            private final Label infoLabel = new Label();

            {
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
                infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + tagColor + ";");
                cardContainer.setPadding(new Insets(8, 12, 8, 12));
                cardContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                cardContainer.getChildren().addAll(titleLabel, infoLabel);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    titleLabel.setText(item.split("\\|")[0].trim());
                    infoLabel.setText(item.contains("|") ? item.substring(item.indexOf("|") + 1).trim() : "");
                    setGraphic(cardContainer);
                }
            }
        });
        return listView;
    }

    public void openLoginWindow(Stage stage) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 480);

        AnchorPane cardBox = new AnchorPane();
        cardBox.setLayoutX(140); cardBox.setLayoutY(30);
        cardBox.setPrefSize(320, 410);
        cardBox.getStyleClass().add("card-box");

        Label title = new Label("Student Market");
        title.setLayoutX(40); title.setLayoutY(40);
        title.setGraphic(marketplaceLogo(30));
        title.setGraphicTextGap(9);
        title.getStyleClass().add("header-title");

        Label subTitle = new Label("Sign in to access your account");
        subTitle.setLayoutX(65); subTitle.setLayoutY(78);
        subTitle.getStyleClass().add("sub-title");

        TextField emailF = new TextField(); emailF.setPromptText("Email Address"); emailF.setLayoutX(30); emailF.setLayoutY(120); emailF.setPrefWidth(260);
        PasswordField passF = new PasswordField(); passF.setPromptText("Password"); passF.setLayoutX(30); passF.setLayoutY(165); passF.setPrefWidth(260);

        Button loginBtn = new Button("Log In");
        loginBtn.setLayoutX(30); loginBtn.setLayoutY(220); loginBtn.setPrefWidth(260); loginBtn.setPrefHeight(34);
        loginBtn.getStyleClass().add("btn-primary");

        loginBtn.setOnAction(e -> {
            String email = emailF.getText().trim();
            String password = passF.getText();
            if (email.isEmpty() || password == null || password.isEmpty()) {
                showAlert("Missing login details", "Enter both your email and password to sign in.", AlertType.WARNING);
                return;
            }
            if (DatabaseHandler.loginUser(email, password)) {
                openMainDashboardLayout(stage, email);
            } else {
                showAlert("Error", "Invalid Email or Password!", AlertType.ERROR);
            }
        });

        Button regGoBtn = new Button("Don't have an account? Sign up");
        regGoBtn.setLayoutX(30); regGoBtn.setLayoutY(340); regGoBtn.setPrefWidth(260);
        regGoBtn.getStyleClass().add("btn-secondary");
        regGoBtn.setOnAction(e -> openRegisterWindow(stage));

        cardBox.getChildren().addAll(title, subTitle, emailF, passF, loginBtn, regGoBtn);
        root.getChildren().add(cardBox);

        Scene scene = new Scene(root, 600, 480);
        attachStyle(scene);

        stage.setTitle("Student Marketplace - Login");
        stage.setScene(scene);
    }

    private void openRegisterWindow(Stage stage) {
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 650);

        AnchorPane cardBox = new AnchorPane();
        cardBox.setLayoutX(120); cardBox.setLayoutY(16);
        cardBox.setPrefSize(360, 610);
        cardBox.getStyleClass().add("card-box");

        Label title = new Label("Create your account");
        title.setLayoutX(32); title.setLayoutY(20);
        title.getStyleClass().add("header-title");

        Label subtitle = new Label("A few details to join your campus marketplace");
        subtitle.setLayoutX(32); subtitle.setLayoutY(52); subtitle.getStyleClass().add("sub-title");

        TextField nameF = new TextField(); nameF.setPromptText("Full name"); nameF.setLayoutX(32); nameF.setLayoutY(91); nameF.setPrefWidth(296);
        nameF.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 80 ? change : null));
        TextField idF = new TextField(); idF.setPromptText("Student ID"); idF.setLayoutX(32); idF.setLayoutY(136); idF.setPrefWidth(296);
        idF.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 50 ? change : null));
        TextField emailF = new TextField(); emailF.setPromptText("Email address (name@example.com)"); emailF.setLayoutX(32); emailF.setLayoutY(181); emailF.setPrefWidth(296);
        emailF.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 254 ? change : null));
        TextField confirmEmailF = new TextField(); confirmEmailF.setPromptText("Confirm email address"); confirmEmailF.setLayoutX(32); confirmEmailF.setLayoutY(226); confirmEmailF.setPrefWidth(296);
        confirmEmailF.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> change.getControlNewText().length() <= 254 ? change : null));
        PasswordField passF = new PasswordField(); passF.setPromptText("Create a password"); passF.setLayoutX(32); passF.setLayoutY(271); passF.setPrefWidth(296);
        PasswordField confirmF = new PasswordField(); confirmF.setPromptText("Confirm password"); confirmF.setLayoutX(32); confirmF.setLayoutY(316); confirmF.setPrefWidth(296);

        Label passwordRules = new Label("Use 10+ characters with uppercase, lowercase, a number and a symbol. No spaces.");
        passwordRules.setWrapText(true);
        passwordRules.setLayoutX(34); passwordRules.setLayoutY(356); passwordRules.setPrefWidth(292); passwordRules.setPrefHeight(42);
        passwordRules.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label passwordStrength = new Label("Password strength: not entered");
        passwordStrength.setLayoutX(34); passwordStrength.setLayoutY(403);
        passwordStrength.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        passF.textProperty().addListener((obs, oldValue, value) -> {
            if (value.isEmpty()) {
                passwordStrength.setText("Password strength: not entered");
                passwordStrength.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
            } else if (DatabaseHandler.isStrongPassword(value)) {
                passwordStrength.setText("Strong password");
                passwordStrength.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #059669;");
            } else {
                passwordStrength.setText("Not strong enough yet");
                passwordStrength.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
            }
        });
        TextField roleF = new TextField("Student"); roleF.setLayoutX(32); roleF.setLayoutY(430); roleF.setPrefWidth(296);
        roleF.setEditable(false); roleF.setFocusTraversable(false);
        roleF.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;");
        Label roleHint = new Label("Marketplace account type");
        roleHint.setLayoutX(36); roleHint.setLayoutY(463); roleHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");

        Button regBtn = new Button("Register Now");
        regBtn.setLayoutX(32); regBtn.setLayoutY(493); regBtn.setPrefWidth(296); regBtn.setPrefHeight(38);
        regBtn.getStyleClass().add("btn-primary");

        regBtn.setOnAction(e -> {
            String email = emailF.getText().trim();
            String validation = DatabaseHandler.validateRegistration(nameF.getText(), idF.getText(), email, passF.getText());
            if (!validation.isEmpty()) {
                showAlert("Check your details", validation, AlertType.WARNING);
                return;
            }
            if (!email.equalsIgnoreCase(confirmEmailF.getText().trim())) {
                showAlert("Email addresses do not match", "Check that both email fields contain the same address.", AlertType.WARNING);
                return;
            }
            if (!passF.getText().equals(confirmF.getText())) {
                showAlert("Passwords do not match", "Re-enter the same password in both password fields.", AlertType.WARNING);
                return;
            }
            if (DatabaseHandler.registerUser(nameF.getText(), idF.getText(), email, passF.getText(), "Student")) {
                showAlert("Account created", "Your account is ready. Sign in with " + email + " and your password.", AlertType.INFORMATION);
                openLoginWindow(stage);
            } else {
                showAlert("Account already exists", "That email address or student ID is already registered. Try signing in or use different details.", AlertType.WARNING);
            }
        });

        Button backBtn = new Button("Already have an account? Log In");
        backBtn.setLayoutX(32); backBtn.setLayoutY(551); backBtn.setPrefWidth(296);
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> openLoginWindow(stage));

        cardBox.getChildren().addAll(title, subtitle, nameF, idF, emailF, confirmEmailF, passF, confirmF,
                passwordRules, passwordStrength, roleF, roleHint, regBtn, backBtn);
        root.getChildren().add(cardBox);

        Scene scene = new Scene(root, 600, 650);
        attachStyle(scene);
        stage.setTitle("Student Registration");
        stage.setScene(scene);
    }

    private void attachStyle(Scene scene) {
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ignored) {}
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
