package shoppinglist;

import entities.Item;
import entities.Allocation;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListScreen { // <--- Καθαρή κλάση, χωρίς extends Application!

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
    private Runnable backAction; // Για τη διαχείριση της επιστροφής στο Hub

    // Constructor που δέχεται το backAction
    public ShoppingListScreen(Runnable backAction) {
        this.backAction = backAction;
    }

    // Η μέθοδος αναλαμβάνει πλέον εξολοκλήρου το χτίσιμο και την εμφάνιση του Stage
    public void display() {
        this.primaryStage = new Stage();
        primaryStage.setTitle("HOMY - Shopping List");

        // --- ΚΕΦΑΛΙΔΑ (Shopping List με κουμπί πίσω) ---
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
        rightColumn.setPadding(new Insets(10));
        rightColumn.setMinWidth(280);

        Label addReceiptLabel = new Label("Add receipt:");
        addReceiptLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        StackPane receiptBox = new StackPane();
        receiptBox.setPrefHeight(60); 
        receiptBox.setMinHeight(60);
        receiptBox.setMaxHeight(60);
        receiptBox.setMaxWidth(Double.MAX_VALUE);
        receiptBox.setStyle("-fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #ffffff; -fx-cursor: hand;");
        
        Label bigPlus = new Label("+");
        bigPlus.setFont(Font.font("Segoe UI", 28)); 
        receiptBox.getChildren().add(bigPlus);
        receiptBox.setOnMouseClicked(e -> addReceipt());

        Label historyTitle = new Label("History");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        HBox historyHeader = new HBox(historyTitle);
        historyHeader.setAlignment(Pos.CENTER);
        historyHeader.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1px 0 1px 0; -fx-padding: 5px;");

        historyScrollPane.setContent(historyContainer);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        historyScrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");

        VBox.setVgrow(historyScrollPane, Priority.ALWAYS);
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

    private void updateUI() {
        mainListContainer.getChildren().clear();
        checkedListContainer.getChildren().clear();
        historyContainer.getChildren().clear();

        // 1. Σχεδίαση Main List
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

        // 2. Σχεδίαση Checked Items
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
                } else {
                    cb.setSelected(true); 
                }
                updateUI();
            });
            
            deleteBtn.setOnAction(e -> deleteItem(item));

            row.getChildren().addAll(cb, text, deleteBtn);
            checkedListContainer.getChildren().add(row);
        }

        // 3. Σχεδίαση Ιστορικού (History)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Allocation alloc : historyList) {
            VBox allocBox = new VBox(3);
            allocBox.setPadding(new Insets(5));
            allocBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;"); 
            
            if (alloc.getImageFile() != null) {
                allocBox.setCursor(Cursor.HAND);
                allocBox.setOnMouseClicked(e -> showReceiptPopup(alloc.getImageFile()));
            }
            
            String dateStr = alloc.getDate().format(formatter);
            Label dateLabel = new Label(dateStr + " - Total: €" + alloc.getTotalAmount());
            dateLabel.setStyle(alloc.isDone() ? "-fx-text-fill: black; -fx-font-weight: bold;" : "-fx-text-fill: gray;");

            GridPane membersGrid = new GridPane();
            membersGrid.setHgap(20);
            int rowIdx = 0;
            for (String member : alloc.getMemberAmounts().keySet()) {
                Label nameL = new Label(member);
                Label amtL = new Label("€" + alloc.getMemberAmounts().get(member));
                
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

    public void showReceiptPopup(File imageFile) {
        if (imageFile == null) return;
        
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("Προβολή Απόδειξης");

        try {
            Image img = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(380); 
            imageView.setPreserveRatio(true);

            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-viewport-background-color: transparent;");

            Scene scene = new Scene(scrollPane, 400, 500);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception ex) {
            System.out.println("Σφάλμα προβολής εικόνας: " + ex.getMessage());
        }
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
                }
            }
            if (!exists) {
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
            updateUI();
        }
    }

    public void selectFromChecked(Item item) {
        if (item != null) {
            AddItemScreen editScreen = new AddItemScreen();
            Item updated = editScreen.insertItemQuantity(primaryStage, item.getName(), item.getQuantity());
            if (updated != null) {
                item.setQuantity(updated.getQuantity());
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
            historyList.add(0, alloc);
            updateUI();
        }
    }

    public void selectDone(Allocation alloc) {
        if (alloc != null) {
            alloc.done();
            updateUI();
        }
    }

    public void selectEdit(Allocation alloc) {
        if (alloc != null) {
            SplitScreen splitScreen = new SplitScreen();
            Allocation newAlloc = splitScreen.insertAllocationStatus(primaryStage, alloc.getImageFile(), alloc);
            if (newAlloc != null) {
                alloc.delete();
                historyList.remove(alloc);
                returnAllocation(newAlloc);
            }
        }
    }

    public void selectDeleteAllocation(Allocation alloc) {
        if (alloc != null) {
            alloc.delete(); 
            historyList.remove(alloc); 
            updateUI(); 
        }
    }

    public void deleteItem(Item item) {
        if (item != null) {
            item.delete();
            mainList.remove(item);
            checkedList.remove(item);
            updateUI();
        }
    }
}