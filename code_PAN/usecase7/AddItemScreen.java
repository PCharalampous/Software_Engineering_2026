package usecase7;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AddItemScreen {
    private Item resultItem = null;

    // Methods από το Domain Model
    public Item insertItemStatus(Stage owner, String defaultName, int defaultQty, boolean nameEditable) {
        return showDialog(owner, defaultName, defaultQty, nameEditable);
    }

    public Item insertItemQuantity(Stage owner, String defaultName, int defaultQty) {
        return showDialog(owner, defaultName, defaultQty, false);
    }

    public Item returnItem() {
        return resultItem;
    }

    // Εσωτερική μέθοδος σχεδίασης του UI με Spinner
    private Item showDialog(Stage owner, String defaultName, int defaultQty, boolean nameEditable) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Στοιχεία Προϊόντος");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(defaultName);
        nameField.setEditable(nameEditable);
        
        Spinner<Integer> qtySpinner = new Spinner<>(1, 9999, defaultQty);
        qtySpinner.setEditable(true);

        grid.add(new Label("Όνομα προϊόντος:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Ποσότητα:"), 0, 1);
        grid.add(qtySpinner, 1, 1);

        Button confirmBtn = new Button("Confirm");
        confirmBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                ErrorScreen.show("Παρακαλώ συμπληρώστε το όνομα του προϊόντος!");
                return;
            }

            resultItem = new Item(name, qtySpinner.getValue());
            stage.close();
        });

        grid.add(confirmBtn, 1, 2);

        Scene scene = new Scene(grid, 300, 150);
        stage.setScene(scene);
        stage.showAndWait();

        return returnItem();
    }
}