package shoppinglist;

import entities.Item;
import entities.Allocation;
import util.DatabaseManager; 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;           
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;      
import javafx.scene.image.ImageView;  
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser; 
import javafx.stage.Modality;         
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.io.File; 
import java.sql.*; 
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap; 
import java.util.List;
import java.util.Map;

public class ShoppingListScreen { 

    private List<Item> mainList = new ArrayList<>();
    private List<Item> checkedList = new ArrayList<>();
    private List<Allocation> historyList = new ArrayList<>();
    
    private List<String> roommateNames = new ArrayList<>();

    private VBox mainListContainer = new VBox(8);
    private VBox checkedListContainer = new VBox(8);
    private VBox historyContainer = new VBox(12);

    private ScrollPane mainListScrollPane = new ScrollPane();
    private ScrollPane checkedListScrollPane = new ScrollPane();
    private ScrollPane historyScrollPane = new ScrollPane();

    private Stage primaryStage;
    private Runnable backAction; 
    
    private int currentRoomId;

    public ShoppingListScreen(Runnable backAction) {
        this.backAction = backAction;
        
        if (entities.Authentication.getCurrentUser() != null) {
            this.currentRoomId = entities.Authentication.getCurrentUser().getRoomId(); 
        } else {
            this.currentRoomId = 1; 
        }
        
        loadRoommatesFromDatabase();
    }

    private void loadRoommatesFromDatabase() {
        roommateNames.clear();
        String query = "SELECT username FROM users WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, currentRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roommateNames.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά τη φόρτωση των συγκατοίκων από τη βάση:");
            e.printStackTrace();
        }
        
//        if (roommateNames.isEmpty()) {
//            roommateNames.addAll(List.of("Giannis", "Manos", "Makis"));
//        }
    }

    public void display() {
        this.primaryStage = new Stage();
        primaryStage.setTitle("HOMY - Shopping List");

        loadItemsFromDatabase();

        // --- ΚΕΦΑΛΙΔΑ ---
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #475569; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #1E293B; -fx-cursor: hand; -fx-padding: 0 10 0 0;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #475569; -fx-cursor: hand; -fx-padding: 0 10 0 0;"));
        backBtn.setOnAction(e -> {
            primaryStage.close();
            if (backAction != null) {
                backAction.run();
            }
        });

        Label headerLabel = new Label("Shopping List");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        headerLabel.setTextFill(Color.web("#1E293B"));
        
        HBox headerBox = new HBox(5, backBtn, headerLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15, 20, 15, 20));
        headerBox.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0px 1px 0;");

        // --- ΑΡΙΣΤΕΡΗ ΣΤΗΛΗ (Lists) ---
        VBox leftColumn = new VBox(12); 
        leftColumn.setPadding(new Insets(15));
        leftColumn.setMinWidth(280);
        leftColumn.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 1px 0 0;");

        Label mainListTitle = new Label("Main List");
        mainListTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        mainListTitle.setTextFill(Color.web("#334155"));
        
        Button addItemBtn = new Button("+");
        addItemBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; -fx-min-width: 26px; -fx-min-height: 26px; -fx-max-width: 26px; -fx-max-height: 26px; -fx-padding: 0;");
        addItemBtn.setOnMouseEntered(e -> addItemBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; -fx-min-width: 26px; -fx-min-height: 26px; -fx-max-width: 26px; -fx-max-height: 26px; -fx-padding: 0;"));
        addItemBtn.setOnMouseExited(e -> addItemBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px; -fx-min-width: 26px; -fx-min-height: 26px; -fx-max-width: 26px; -fx-max-height: 26px; -fx-padding: 0;"));
        addItemBtn.setOnAction(e -> addInMainList());

        HBox mainTitleBox = new HBox(8, mainListTitle, addItemBtn);
        mainTitleBox.setAlignment(Pos.CENTER_LEFT);

        mainListScrollPane.setContent(mainListContainer);
        mainListScrollPane.setFitToWidth(true);
        mainListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        mainListScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");
        
        mainListScrollPane.setPrefHeight(160);
        mainListScrollPane.setMinHeight(160);
        mainListScrollPane.setMaxHeight(160);

        Label checkedListTitle = new Label("Checked Items");
        checkedListTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        checkedListTitle.setTextFill(Color.web("#334155"));

        checkedListScrollPane.setContent(checkedListContainer);
        checkedListScrollPane.setFitToWidth(true);
        checkedListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        checkedListScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");

        VBox.setVgrow(checkedListScrollPane, Priority.ALWAYS);
        leftColumn.getChildren().addAll(mainTitleBox, mainListScrollPane, checkedListTitle, checkedListScrollPane);

        // --- ΔΕΞΙΑ ΣΤΗΛΗ (Receipt & History) ---
        VBox rightColumn = new VBox(12); 
        rightColumn.setPadding(new Insets(15));
        rightColumn.setMinWidth(300);

        Label addReceiptLabel = new Label("Add Receipt");
        addReceiptLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        addReceiptLabel.setTextFill(Color.web("#334155"));

        StackPane receiptBox = new StackPane();
        receiptBox.setPrefHeight(65); 
        receiptBox.setMinHeight(65);
        receiptBox.setMaxHeight(65);
        receiptBox.setMaxWidth(Double.MAX_VALUE);
        receiptBox.setStyle("-fx-border-color: #CBD5E1; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #ffffff; -fx-cursor: hand;");
        receiptBox.setOnMouseEntered(e -> receiptBox.setStyle("-fx-border-color: #3B82F6; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #EFF6FF; -fx-cursor: hand;"));
        receiptBox.setOnMouseExited(e -> receiptBox.setStyle("-fx-border-color: #CBD5E1; -fx-border-style: dashed; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #ffffff; -fx-cursor: hand;"));

        Label bigPlus = new Label("+ Upload Invoice Image");
        bigPlus.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14)); 
        bigPlus.setTextFill(Color.web("#64748B"));
        receiptBox.getChildren().add(bigPlus);
        receiptBox.setOnMouseClicked(e -> addReceipt());

        Label historyTitle = new Label("History & Allocations");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        historyTitle.setTextFill(Color.web("#1E293B"));
        HBox historyHeader = new HBox(historyTitle);
        historyHeader.setAlignment(Pos.CENTER_LEFT);
        historyHeader.setPadding(new Insets(10, 0, 5, 0));
        historyHeader.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1px 0;"); 

        historyScrollPane.setContent(historyContainer);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setFitToHeight(true); 
        historyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        historyScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        historyContainer.setPadding(new Insets(5, 0, 0, 0));
        historyContainer.setStyle("-fx-background-color: transparent;");
        
        VBox.setVgrow(historyContainer, Priority.ALWAYS);
        VBox.setVgrow(historyScrollPane, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS); 
        
        rightColumn.getChildren().addAll(addReceiptLabel, receiptBox, historyHeader, historyScrollPane);

        // --- ΚΥΡΙΟ LAYOUT ---
        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        HBox centerLayout = new HBox(leftColumn, rightColumn);
        root.setStyle("-fx-background-color: #F8FAFC;");
        
        VBox.setVgrow(centerLayout, Priority.ALWAYS);
        root.setCenter(centerLayout);

        updateUI();

        Scene scene = new Scene(root, 620, 680);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void loadItemsFromDatabase() {
        mainList.clear();
        checkedList.clear();
        historyList.clear();

        String itemQuery = "SELECT item_id, item_name, quantity, is_checked FROM shopping_list WHERE room_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(itemQuery)) {
            ps.setInt(1, currentRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Item item = new Item(rs.getInt("item_id"), rs.getString("item_name"), rs.getInt("quantity"), rs.getBoolean("is_checked"));
                    if (item.isChecked()) checkedList.add(item);
                    else mainList.add(item);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        String allocQuery = "SELECT a.allocation_id, a.allocation_date, a.total_amount, a.receiver_username, a.image_path, a.is_done, " +
                             "s.roommate_username, s.amount_owed FROM allocations a " +
                             "LEFT JOIN allocation_shares s ON a.allocation_id = s.allocation_id " +
                             "WHERE a.room_id = ? ORDER BY a.allocation_id DESC";
        
        Map<Integer, Allocation> allocMap = new LinkedHashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(allocQuery)) {
            ps.setInt(1, currentRoomId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int allocId = rs.getInt("allocation_id");
                    
                    if (!allocMap.containsKey(allocId)) {
                        Date sqlDate = rs.getDate("allocation_date");
                        LocalDate localDate = (sqlDate != null) ? sqlDate.toLocalDate() : LocalDate.now();
                        
                        String imgPath = rs.getString("image_path");
                        File imgFile = (imgPath != null && !imgPath.isEmpty()) ? new File(imgPath) : null;

                        Map<String, Double> sharesMap = new HashMap<>();

                        Allocation alloc = new Allocation(
                            allocId,
                            localDate,
                            sharesMap,
                            imgFile,
                            rs.getDouble("total_amount"),
                            rs.getString("receiver_username"),
                            rs.getBoolean("is_done")
                        );
                        allocMap.put(allocId, alloc);
                    }
                    
                    String roommate = rs.getString("roommate_username");
                    if (roommate != null) {
                        allocMap.get(allocId).getMemberAmounts().put(roommate, rs.getDouble("amount_owed"));
                    }
                }
                historyList.addAll(allocMap.values());
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateUI() {
        mainListContainer.getChildren().clear();
        checkedListContainer.getChildren().clear();
        historyContainer.getChildren().clear();

        for (Item item : mainList) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-border-width: 1;");
            
            CheckBox cb = new CheckBox();
            cb.setCursor(Cursor.HAND);
            
            Label lbl = new Label(item.getName() + " x" + item.getQuantity());
            lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            lbl.setTextFill(Color.web("#334155"));
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;"));
            
            cb.setOnAction(e -> selectFromMainList(item));
            deleteBtn.setOnAction(e -> deleteItem(item));

            row.getChildren().addAll(cb, lbl, spacer, deleteBtn);
            mainListContainer.getChildren().add(row);
        }

        for (Item item : checkedList) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8; -fx-opacity: 0.75;");
            
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            cb.setCursor(Cursor.HAND);
            
            Text text = new Text(item.getName());
            text.setStrikethrough(true);
            text.setFont(Font.font("Segoe UI", 14));
            text.setFill(Color.web("#64748B"));
            text.setStyle("-fx-cursor: hand;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;");
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 13px;"));

            text.setOnMouseClicked(e -> selectFromChecked(item));
            
            cb.setOnAction(e -> {
                AddItemScreen editScreen = new AddItemScreen();
                Item updated = editScreen.insertItemQuantity(primaryStage, item.getName(), 1);
                
                if (updated != null) {
                    item.setQuantity(updated.getQuantity());
                    item.swap(); 
                    checkedList.remove(item);
                    mainList.add(0, item); 
                    updateItemInDatabase(item);
                } else {
                    cb.setSelected(true); 
                }
                updateUI();
            });
            
            deleteBtn.setOnAction(e -> deleteItem(item));

            row.getChildren().addAll(cb, text, spacer, deleteBtn);
            checkedListContainer.getChildren().add(row);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Allocation alloc : historyList) {
            VBox allocBox = new VBox(6);
            allocBox.setPadding(new Insets(10, 12, 10, 12));
            
            // ΔΙΟΡΘΩΘΗΚΕ: Τα εκκρεμή allocations (isDone == false) έχουν πλέον γκρίζο περίγραμμα (#CBD5E1)
            if (alloc.isDone()) {
                allocBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-border-width: 1;");
            } else {
                allocBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-border-color: #CBD5E1; -fx-border-radius: 10; -fx-border-width: 1.5;");
            }
            
            DropShadow cardShadow = new DropShadow(6, Color.web("#000000", 0.03));
            cardShadow.setOffsetY(1);
            allocBox.setEffect(cardShadow);
            
            if (alloc.getImageFile() != null) {
                allocBox.setCursor(Cursor.HAND);
                allocBox.setOnMouseClicked(e -> showReceiptPopup(alloc));
            }
            
            String dateStr = alloc.getDate().format(formatter);
            Label dateLabel = new Label(dateStr + "  •  Total: €" + String.format("%.2f", alloc.getTotalAmount()));
            
            if (alloc.isDone()) {
                dateLabel.setStyle("-fx-text-fill: #0F172A; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                dateLabel.setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold; -fx-font-size: 14px;"); // Γκρίζο κείμενο τίτλου για τα εκκρεμή
            }

            GridPane membersGrid = new GridPane();
            membersGrid.setHgap(30);
            membersGrid.setVgap(3);
            int rowIdx = 0;
            for (String member : alloc.getMemberAmounts().keySet()) {
                Label nameL = new Label(member);
                Label amtL = new Label("€" + String.format("%.2f", alloc.getMemberAmounts().get(member)));
                
                nameL.setFont(Font.font("Segoe UI", 12));
                amtL.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
                
                if (alloc.isDone()) {
                    nameL.setStyle("-fx-text-fill: #475569;");
                    amtL.setStyle("-fx-text-fill: #334155;");
                } else {
                    nameL.setStyle("-fx-text-fill: #94A3B8;");
                    amtL.setStyle("-fx-text-fill: #64748B;");
                }
                
                membersGrid.add(nameL, 0, rowIdx);
                membersGrid.add(amtL, 1, rowIdx);
                rowIdx++;
            }

            HBox actions = new HBox(8);
            actions.setPadding(new Insets(4, 0, 0, 0));
            if (!alloc.isDone()) {
                Button doneBtn = new Button("Done");
                Button editBtn = new Button("Edit");
                Button deleteBtn = new Button("Delete");

                String btnBase = "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;";
                doneBtn.setStyle(btnBase + "-fx-background-color: #10B981; -fx-text-fill: white;");
                doneBtn.setOnMouseEntered(e -> doneBtn.setStyle(btnBase + "-fx-background-color: #059669; -fx-text-fill: white;"));
                doneBtn.setOnMouseExited(e -> doneBtn.setStyle(btnBase + "-fx-background-color: #10B981; -fx-text-fill: white;"));

                editBtn.setStyle(btnBase + "-fx-background-color: #3B82F6; -fx-text-fill: white;");
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(btnBase + "-fx-background-color: #2563EB; -fx-text-fill: white;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(btnBase + "-fx-background-color: #3B82F6; -fx-text-fill: white;"));

                deleteBtn.setStyle(btnBase + "-fx-background-color: #EF4444; -fx-text-fill: white;");
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(btnBase + "-fx-background-color: #DC2626; -fx-text-fill: white;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(btnBase + "-fx-background-color: #EF4444; -fx-text-fill: white;"));

                doneBtn.setOnMouseClicked(evt -> evt.consume());
                editBtn.setOnMouseClicked(evt -> evt.consume());
                deleteBtn.setOnMouseClicked(evt -> evt.consume());

                doneBtn.setOnAction(e -> selectDone(alloc));
                editBtn.setOnAction(e -> selectEdit(alloc));
                deleteBtn.setOnAction(e -> selectDeleteAllocation(alloc));

                actions.getChildren().addAll(doneBtn, editBtn, deleteBtn);
            }

            allocBox.getChildren().addAll(dateLabel, membersGrid, actions);
            historyContainer.getChildren().add(allocBox);
        }
    }

    private void updateItemInDatabase(Item item) {
        String query = "UPDATE shopping_list SET quantity = ?, is_checked = ? WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, item.getQuantity());
            ps.setBoolean(2, item.isChecked());
            ps.setInt(3, item.getItemId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void showReceiptPopup(Allocation alloc) {
        if (alloc == null || alloc.getImageFile() == null) return;
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("Προβολή Απόδειξης");

        try {
            Image img = new Image(alloc.getImageFile().toURI().toString());
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(380); 
            imageView.setPreserveRatio(true);

            VBox container = new VBox(0);
            
            Label receiverInfoLabel = new Label("Paid by: " + alloc.getReceiver());
            receiverInfoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            receiverInfoLabel.setStyle("-fx-text-fill: #1E293B; -fx-padding: 10px; -fx-background-color: #F1F5F9;");
            container.getChildren().addAll(receiverInfoLabel, imageView);

            ScrollPane scrollPane = new ScrollPane(container);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-padding: 0;");

            Scene scene = new Scene(scrollPane, 385, Region.USE_COMPUTED_SIZE);
            stage.setScene(scene);
            stage.setResizable(false);
            
            stage.sizeToScene();
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); } 
    }

    public void addInMainList() {
        AddItemScreen itemScreen = new AddItemScreen();
        Item newItem = itemScreen.insertItemStatus(primaryStage, "", 1, true);
        returnItem(newItem);
    }

    public void returnItem(Item item) {
        if (item != null) {
            boolean exists = false;
            for (Item i : mainList) {
                if (i.getName().equalsIgnoreCase(item.getName())) {
                    i.setQuantity(i.getQuantity() + item.getQuantity());
                    exists = true;
                    updateItemInDatabase(i);
                    break;
                }
            }
            if (!exists) {
                Item foundInChecked = null;
                for (Item i : checkedList) {
                    if (i.getName().equalsIgnoreCase(item.getName())) {
                        foundInChecked = i;
                        break;
                    }
                }
                if (foundInChecked != null) {
                    foundInChecked.setQuantity(item.getQuantity()); 
                    foundInChecked.swap(); 
                    checkedList.remove(foundInChecked); 
                    mainList.add(0, foundInChecked); 
                    exists = true;
                    updateItemInDatabase(foundInChecked);
                }
            }
            if (!exists) {
                String query = "INSERT INTO shopping_list (room_id, item_name, quantity, is_checked) VALUES (?, ?, ?, ?)";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentRoomId);
                    ps.setString(2, item.getName());
                    ps.setInt(3, item.getQuantity());
                    ps.setBoolean(4, item.isChecked());
                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) item.setItemId(gk.getInt(1));
                    }
                } catch (SQLException e) { e.printStackTrace(); }
                mainList.add(0, item);
            }
            updateUI();
        }
    }

    public void selectFromMainList(Item item) {
        if (item != null) {
            item.swap(); 
            mainList.remove(item);
            checkedList.add(0, item);
            updateItemInDatabase(item);
            updateUI();
        }
    }

    public void selectFromChecked(Item item) {
        if (item != null) {
            AddItemScreen editScreen = new AddItemScreen();
            Item updated = editScreen.insertItemQuantity(primaryStage, item.getName(), item.getQuantity());
            if (updated != null) {
                item.setQuantity(updated.getQuantity());
                updateItemInDatabase(item);
                updateUI();
            }
        }
    }

    public void addReceipt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Επιλογή Φωτογραφίας Απόδειξης");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Αρχεία Εικόνας", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            SplitScreen splitScreen = new SplitScreen(roommateNames);
            Allocation newAlloc = splitScreen.insertAllocationStatus(primaryStage, selectedFile, null); 
            returnAllocation(newAlloc);
        }
    }

    public void returnAllocation(Allocation alloc) {
        if (alloc != null) {
            String allocQuery = "INSERT INTO allocations (room_id, allocation_date, total_amount, receiver_username, image_path, is_done) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(allocQuery, Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setInt(1, currentRoomId);
                ps.setDate(2, Date.valueOf(alloc.getDate()));
                ps.setDouble(3, alloc.getTotalAmount());
                ps.setString(4, alloc.getReceiver());
                ps.setString(5, (alloc.getImageFile() != null) ? alloc.getImageFile().getAbsolutePath() : null);
                ps.setBoolean(6, alloc.isDone());
                
                ps.executeUpdate();
                
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        alloc.setAllocationId(generatedKeys.getInt(1));
                    }
                }
                
                String shareQuery = "INSERT INTO allocation_shares (allocation_id, roommate_username, amount_owed) VALUES (?, ?, ?)";
                try (PreparedStatement psShare = conn.prepareStatement(shareQuery)) {
                    for (Map.Entry<String, Double> entry : alloc.getMemberAmounts().entrySet()) {
                        psShare.setInt(1, alloc.getAllocationId());
                        psShare.setString(2, entry.getKey());
                        psShare.setDouble(3, entry.getValue());
                        psShare.addBatch(); 
                    }
                    psShare.executeBatch();
                }
                
            } catch (SQLException e) {
                System.err.println("Σφάλμα κατά την εισαγωγή του allocation στη βάση:");
                e.printStackTrace();
            }

            historyList.add(0, alloc);
            updateUI();
        }
    }

    public void selectDone(Allocation alloc) {
        if (alloc != null) {
            alloc.done();
            String query = "UPDATE allocations SET is_done = TRUE WHERE allocation_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, alloc.getAllocationId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
            updateUI();
        }
    }

    public void selectEdit(Allocation alloc) {
        if (alloc != null) {
            SplitScreen splitScreen = new SplitScreen(roommateNames);
            
            Allocation updatedAlloc = splitScreen.insertAllocationStatus(primaryStage, alloc.getImageFile(), alloc);
            
            if (updatedAlloc != null) {
                String updateAllocQuery = "UPDATE allocations SET total_amount = ?, receiver_username = ?, allocation_date = ? WHERE allocation_id = ?";
                String deleteSharesQuery = "DELETE FROM allocation_shares WHERE allocation_id = ?";
                String insertShareQuery = "INSERT INTO allocation_shares (allocation_id, roommate_username, amount_owed) VALUES (?, ?, ?)";

                try (Connection conn = DatabaseManager.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement(updateAllocQuery)) {
                        ps.setDouble(1, updatedAlloc.getTotalAmount());
                        ps.setString(2, updatedAlloc.getReceiver());
                        ps.setDate(3, Date.valueOf(updatedAlloc.getDate()));
                        ps.setInt(4, alloc.getAllocationId()); 
                        ps.executeUpdate();
                    }

                    try (PreparedStatement psDel = conn.prepareStatement(deleteSharesQuery)) {
                        psDel.setInt(1, alloc.getAllocationId());
                        psDel.executeUpdate();
                    }

                    try (PreparedStatement psShare = conn.prepareStatement(insertShareQuery)) {
                        for (Map.Entry<String, Double> entry : updatedAlloc.getMemberAmounts().entrySet()) {
                            psShare.setInt(1, alloc.getAllocationId());
                            psShare.setString(2, entry.getKey());
                            psShare.setDouble(3, entry.getValue());
                            psShare.addBatch();
                        }
                        psShare.executeBatch();
                    }

                } catch (SQLException e) {
                    System.err.println("Σφάλμα κατά το real-time update του allocation:");
                    e.printStackTrace();
                }

                int index = historyList.indexOf(alloc);
                if (index != -1) {
                    updatedAlloc.setAllocationId(alloc.getAllocationId());
                    historyList.set(index, updatedAlloc);
                }
                updateUI();
            }
        }
    }

    public void selectDeleteAllocation(Allocation alloc) {
        if (alloc != null) {
            String query = "DELETE FROM allocations WHERE allocation_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, alloc.getAllocationId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
            historyList.remove(alloc); 
            updateUI(); 
        }
    }

    public void deleteItem(Item item) {
        if (item != null) {
            mainList.remove(item);
            checkedList.remove(item);
            
            String query = "DELETE FROM shopping_list WHERE item_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, item.getItemId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
            updateUI();
        }
    }
}