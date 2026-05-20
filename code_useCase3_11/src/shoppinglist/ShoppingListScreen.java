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

    private VBox mainListContainer = new VBox(5);
    private VBox checkedListContainer = new VBox(5);
    private VBox historyContainer = new VBox(10);

    private ScrollPane mainListScrollPane = new ScrollPane();
    private ScrollPane checkedListScrollPane = new ScrollPane();
    private ScrollPane historyScrollPane = new ScrollPane();

    private Stage primaryStage;
    private Runnable backAction; 
    
    private final int currentRoomId = 1;

    public ShoppingListScreen(Runnable backAction) {
        this.backAction = backAction;
    }

    public void display() {
        this.primaryStage = new Stage();
        primaryStage.setTitle("HOMY - Shopping List");

        loadItemsFromDatabase();

        // --- ΚΕΦΑΛΙΔΑ ---
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            primaryStage.close();
            if (backAction != null) {
                backAction.run();
            }
        });

        Label headerLabel = new Label("Shopping List");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        
        HBox headerBox = new HBox(10, backBtn, headerLabel);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1px 0;");

        // --- ΑΡΙΣΤΕΡΗ ΣΤΗΛΗ (Lists) ---
        VBox leftColumn = new VBox(10); 
        leftColumn.setPadding(new Insets(10));
        leftColumn.setMinWidth(260);
        leftColumn.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 1px 0 0;");

        Label mainListTitle = new Label("Main List ");
        mainListTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Button addItemBtn = new Button("+");
        addItemBtn.setStyle("-fx-background-radius: 15; -fx-font-weight: bold; -fx-cursor: hand;");
        addItemBtn.setOnAction(e -> addInMainList());

        HBox mainTitleBox = new HBox(5, mainListTitle, addItemBtn);
        mainTitleBox.setAlignment(Pos.CENTER_LEFT);

        mainListScrollPane.setContent(mainListContainer);
        mainListScrollPane.setFitToWidth(true);
        mainListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        mainListScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");
        
        mainListScrollPane.setPrefHeight(140);
        mainListScrollPane.setMinHeight(140);
        mainListScrollPane.setMaxHeight(140);

        Label checkedListTitle = new Label("Checked items:");
        checkedListTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        checkedListScrollPane.setContent(checkedListContainer);
        checkedListScrollPane.setFitToWidth(true);
        checkedListScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        checkedListScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");

        VBox.setVgrow(checkedListScrollPane, Priority.ALWAYS);
        leftColumn.getChildren().addAll(mainTitleBox, mainListScrollPane, checkedListTitle, checkedListScrollPane);

        // --- ΔΕΞΙΑ ΣΤΗΛΗ (Receipt & History) ---
        VBox rightColumn = new VBox(10); 
        rightColumn.setPadding(new Insets(10, 0, 10, 10));
        rightColumn.setMinWidth(280);

        Label addReceiptLabel = new Label("Add receipt:");
        addReceiptLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        VBox.setMargin(addReceiptLabel, new Insets(0, 10, 0, 0)); 

        StackPane receiptBox = new StackPane();
        receiptBox.setPrefHeight(60); 
        receiptBox.setMinHeight(60);
        receiptBox.setMaxHeight(60);
        receiptBox.setMaxWidth(Double.MAX_VALUE);
        receiptBox.setStyle("-fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #ffffff; -fx-cursor: hand;");
        VBox.setMargin(receiptBox, new Insets(0, 10, 0, 0)); 

        Label bigPlus = new Label("+");
        bigPlus.setFont(Font.font("Segoe UI", 28)); 
        receiptBox.getChildren().add(bigPlus);
        receiptBox.setOnMouseClicked(e -> addReceipt());

        Label historyTitle = new Label("History");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        HBox historyHeader = new HBox(historyTitle);
        historyHeader.setAlignment(Pos.CENTER);
        historyHeader.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1px 0 1px 0; -fx-padding: 5px;");
        VBox.setMargin(historyHeader, new Insets(0, 10, 0, 0)); 

        historyScrollPane.setContent(historyContainer);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setFitToHeight(true); 
        historyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        historyScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        historyContainer.setPadding(new Insets(0, 10, 0, 0));
        historyContainer.setStyle("-fx-background-color: transparent;");
        
        VBox.setVgrow(historyContainer, Priority.ALWAYS);
        VBox.setVgrow(historyScrollPane, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS); 
        
        rightColumn.getChildren().addAll(addReceiptLabel, receiptBox, historyHeader, historyScrollPane);

        // --- ΚΥΡΙΟ LAYOUT ---
        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        HBox centerLayout = new HBox(leftColumn, rightColumn);
        root.setStyle("-fx-background-color: #F8FAF9;");
        
        VBox.setVgrow(centerLayout, Priority.ALWAYS);
        root.setCenter(centerLayout);

        updateUI();

        Scene scene = new Scene(root, 580, 650);
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
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            CheckBox cb = new CheckBox();
            Label lbl = new Label(item.getName() + " x" + item.getQuantity());
            lbl.setFont(Font.font("Segoe UI", 14));
            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-cursor: hand;");
            
            cb.setOnAction(e -> selectFromMainList(item));
            deleteBtn.setOnAction(e -> deleteItem(item));

            row.getChildren().addAll(cb, lbl, deleteBtn);
            mainListContainer.getChildren().add(row);
        }

        for (Item item : checkedList) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            
            Text text = new Text(item.getName());
            text.setStrikethrough(true);
            text.setFont(Font.font("Segoe UI", 14));

            Button deleteBtn = new Button("X");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-cursor: hand;");

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

            row.getChildren().addAll(cb, text, deleteBtn);
            checkedListContainer.getChildren().add(row);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Allocation alloc : historyList) {
            VBox allocBox = new VBox(3);
            allocBox.setPadding(new Insets(5));
            allocBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;"); 
            allocBox.setMaxWidth(Double.MAX_VALUE);
            
            if (alloc.getImageFile() != null) {
                allocBox.setCursor(Cursor.HAND);
                allocBox.setOnMouseClicked(e -> showReceiptPopup(alloc));
            }
            
            String dateStr = alloc.getDate().format(formatter);
            Label dateLabel = new Label(dateStr + " - Total: €" + String.format("%.2f", alloc.getTotalAmount()));
            dateLabel.setStyle(alloc.isDone() ? "-fx-text-fill: black; -fx-font-weight: bold;" : "-fx-text-fill: gray;");

            GridPane membersGrid = new GridPane();
            membersGrid.setHgap(20);
            int rowIdx = 0;
            for (String member : alloc.getMemberAmounts().keySet()) {
                Label nameL = new Label(member);
                Label amtL = new Label("€" + String.format("%.2f", alloc.getMemberAmounts().get(member)));
                
                if (!alloc.isDone()) {
                    nameL.setStyle("-fx-text-fill: gray;");
                    amtL.setStyle("-fx-text-fill: gray;");
                }
                
                membersGrid.add(nameL, 0, rowIdx);
                membersGrid.add(amtL, 1, rowIdx);
                rowIdx++;
            }

            HBox actions = new HBox(5);
            actions.setPadding(new Insets(5, 0, 5, 0));
            if (!alloc.isDone()) {
                Button doneBtn = new Button("Done");
                Button editBtn = new Button("Edit");
                Button deleteBtn = new Button("Delete");

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
            receiverInfoLabel.setStyle("-fx-text-fill: #1E293B; -fx-padding: 8px; -fx-background-color: #F1F5F9;");
            receiverInfoLabel.setMaxWidth(Double.MAX_VALUE);
            receiverInfoLabel.setAlignment(Pos.CENTER);

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
            SplitScreen splitScreen = new SplitScreen();
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

    // ΔΙΟΡΘΩΣΗ: Πλέον γίνεται UPDATE της υπάρχουσας εγγραφής στη βάση αντί για σβήσιμο και επανεισαγωγή!
    public void selectEdit(Allocation alloc) {
        if (alloc != null) {
            SplitScreen splitScreen = new SplitScreen();
            
            // Ανοίγουμε το SplitScreen δίνοντας το τρέχον alloc (Edit Mode)
            Allocation updatedAlloc = splitScreen.insertAllocationStatus(primaryStage, alloc.getImageFile(), alloc);
            
            if (updatedAlloc != null) {
                // Αν ο χρήστης πάτησε Confirm, εκτελούμε UPDATE στη βάση δεδομένων κρατώντας το ΙΔΙΟ ID
                String updateAllocQuery = "UPDATE allocations SET total_amount = ?, receiver_username = ?, allocation_date = ? WHERE allocation_id = ?";
                String deleteSharesQuery = "DELETE FROM allocation_shares WHERE allocation_id = ?";
                String insertShareQuery = "INSERT INTO allocation_shares (allocation_id, roommate_username, amount_owed) VALUES (?, ?, ?)";

                try (Connection conn = DatabaseManager.getConnection()) {
                    // 1. Update των γενικών στοιχείων του Allocation
                    try (PreparedStatement ps = conn.prepareStatement(updateAllocQuery)) {
                        ps.setDouble(1, updatedAlloc.getTotalAmount());
                        ps.setString(2, updatedAlloc.getReceiver());
                        ps.setDate(3, Date.valueOf(updatedAlloc.getDate()));
                        ps.setInt(4, alloc.getAllocationId()); // Κρατάμε το αρχικό allocation_id!
                        ps.executeUpdate();
                    }

                    // 2. Διαγραφή των παλιών μεριδίων (shares)
                    try (PreparedStatement psDel = conn.prepareStatement(deleteSharesQuery)) {
                        psDel.setInt(1, alloc.getAllocationId());
                        psDel.executeUpdate();
                    }

                    // 3. Εισαγωγή των νέων μεριδίων για το ίδιο allocation_id
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

                // Ανανέωση των τοπικών λιστών στη μνήμη της εφαρμογής για να δείξει αμέσως τις αλλαγές
                int index = historyList.indexOf(alloc);
                if (index != -1) {
                    // Θέτουμε το σωστό ID και στο νέο αντικείμενο της μνήμης
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