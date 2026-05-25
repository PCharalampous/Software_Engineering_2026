package search;

import entities.Application;
import entities.Authentication;
import entities.User;
import myapplications.SuccessScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.ConfirmationScreen;
import ui.ErrorScreen;
import main.HOMYApp;
import java.util.ArrayList;
import java.util.List;

public class HomeScreenMiddlePanel {
	
	private TextField searchBar;
	private VBox centerBox;
	private Label headerLbl;
	private Separator separator;
	private String searchBarBuffer = "";

	private HBox filterComp;
	
	private Slider minVal;
	private Slider maxVal;
	private Spinner<Integer> guestsSpin;
	
	private VBox resultsContainer;
	private Stage homeScreenStage;

	HomeScreenMiddlePanel(Stage homeScreenStage){
		this.homeScreenStage = homeScreenStage;
		headerLbl = new Label("HOMY APP - home management");
		separator = new Separator();
		
		searchBar = new TextField();
        searchBar.setPromptText("Press enter to search. Type...");
        
        centerBox = new VBox(10);
        filterComp = new HBox(10);
		
		// Φίλτρα ενοικίου και ατόμων
		minVal = new Slider(0, 1000, 0);
		maxVal = new Slider(0, 2000, 2000);
		guestsSpin = new Spinner<>(1, 10, 1);
        
        HBox rentComp = rentRangeComponent();
        filterComp.getChildren().addAll(rentComp, new Label("Roommates:"), guestsSpin);
        filterComp.setAlignment(Pos.CENTER);
        
        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        
        centerBox.getChildren().addAll(headerLbl, separator, searchBar, filterComp, scrollPane);
        centerBox.setPadding(new Insets(10));
        
        middlePanelStyling();
        
        // Event Listener για την αναζήτηση με Enter
        searchBar.setOnAction(e -> {
        	searchBarBuffer = searchBar.getText();
        	performSearch();
        });
        
        // Αρχικό φόρτωμα όλων των αγγελιών
        performSearch();
	}
	
	private void performSearch() {
		resultsContainer.getChildren().clear();
		
		// Φόρτωμα όλων των αγγελιών από τη βάση δεδομένων
		List<Application> allApplications = Application.loadAllApplications(); 
		User currentUser = Authentication.getCurrentUser();
		int currentUserId = (currentUser != null) ? currentUser.getId() : -1;

		String query = searchBarBuffer.toLowerCase().trim();
		double minRent = minVal.getValue();
		double maxRent = maxVal.getValue();
		int roommatesNeeded = guestsSpin.getValue();

		for (Application app : allApplications) {
			// ΔΙΟΡΘΩΣΗ: Αν η αγγελία ανήκει στον τρέχοντα χρήστη, την προσπερνάμε!
			if (app.getOwnerId() == currentUserId) {
				continue;
			}

			// Εφαρμογή των υπόλοιπων φίλτρων (Τοποθεσία, Ενοίκιο, Συγκάτοικοι)
			boolean matchesLocation = query.isEmpty() || (app.getLocation() != null && app.getLocation().toLowerCase().contains(query));
			boolean matchesRent = app.getRent() >= minRent && app.getRent() <= maxRent;
			boolean matchesRoommates = app.getRoommates() >= roommatesNeeded;

			if (matchesLocation && matchesRent && matchesRoommates) {
				VBox card = createApplicationCard(app);
				resultsContainer.getChildren().add(card);
			}
		}

		if (resultsContainer.getChildren().isEmpty()) {
			Label noResults = new Label("No available properties match your filters.");
			noResults.setStyle("-fx-font-style: italic; -fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 20;");
			resultsContainer.getChildren().add(noResults);
		}
	}

	private VBox createApplicationCard(Application app) {
		VBox card = new VBox(8);
		card.setStyle(
			"-fx-background-color: white; " +
			"-fx-background-radius: 8; " +
			"-fx-border-color: #CBD5E1; " +
			"-fx-border-radius: 8; " +
			"-fx-padding: 15;"
		);

		HBox row = new HBox();
		row.setAlignment(Pos.CENTER_LEFT);

		Label titleLbl = new Label(app.getTitle().toUpperCase());
		titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2563eb;");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Label rentLbl = new Label(String.format("%.0f €/month", app.getRent()));
		rentLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #14B8A6; -fx-font-size: 14px;");

		row.getChildren().addAll(titleLbl, spacer, rentLbl);

		Label locLbl = new Label(String.format("📍 %s  •  👥 %d Roommates wanted", app.getLocation(), app.getRoommates()));
		locLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

		Label descLbl = new Label();
		descLbl.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 13px;");
		descLbl.setWrapText(true);

		Button connectBtn = new Button("Apply");
		connectBtn.setMaxWidth(Double.MAX_VALUE);
		connectBtn.setStyle("-fx-background-color: #0000FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
		
		connectBtn.setOnAction(e -> {
			ConfirmationScreen confirScr = new ConfirmationScreen(
				"Confirm your actions", 
				"Do you want to send a roommate request to the room owner of this ad?",
				"Send Request",
				"-fx-background-color: #22C55E; -fx-text-fill: white;-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;",
				() -> {
					User sessionUser = Authentication.getCurrentUser();
					if (sessionUser != null) {
						// ΔΙΟΡΘΩΘΗΚΕ: Κλήση της σωστής μεθόδου sendRoomRequest
						boolean success = app.sendRoomRequest(sessionUser.getId());
						if (success) {
							// ΔΙΟΡΘΩΘΗΚΕ: Χρήση των 3 παραμέτρων που υποστηρίζει η SuccessScreen
							SuccessScreen.display("Request Sent", "Success", "Your request was successfully sent to the room owner! Waiting for approval.");
							//
						} else {
							System.err.println("Αποτυχία δημιουργίας εκκρεμούς αιτήματος.");
							ErrorScreen errScr = new ErrorScreen("Request failed", 
	                                 "Your Request did not send! Something went wrong!");
	                         errScr.show();
						}
					}
				}
			);
			confirScr.show();
		});
		
		// 2. NEW: VBox Card Click Action (Opens Details Popup)
	    card.setOnMouseClicked(e -> {
	        showApplicationDetailsPopup(app);
	    });
		
		card.getChildren().addAll(row, locLbl, descLbl, connectBtn);
		return card;
	}
	
	private void showApplicationDetailsPopup(Application app) {
	    Stage popupStage = new Stage();
	    popupStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with background windows
	    popupStage.setTitle("Application Details: " + app.getTitle());

	    VBox layout = new VBox(15);
	    layout.setStyle("-fx-padding: 20; -fx-background-color: #F8FAFC;");

	    // Add all the details you want the user to see
	    Label titleLabel = new Label(app.getTitle());
	    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

	    Label rentLabel = new Label("Rent: " + String.format("%.0f €/month", app.getRent()));
	    rentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #14B8A6;");

	    Label detailsLabel = new Label("Full Description:\n" + app.getHomeScreenDescription());
	    detailsLabel.setWrapText(true);
	    detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

	    Button closeBtn = new Button("Close");
	    closeBtn.setStyle("-fx-background-color: #64748B; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12;");
	    closeBtn.setOnAction(e -> popupStage.close());

	    layout.getChildren().addAll(titleLabel, rentLabel, detailsLabel, closeBtn);

	    Scene scene = new Scene(layout, 400, 400);
	    popupStage.setScene(scene);
	    popupStage.showAndWait();
	}
	
	private HBox rentRangeComponent() {
		HBox rentRange = new HBox(10);
		VBox minRentRange = new VBox(5);
		VBox maxRentRange = new VBox(5);
		
		Label lmin = new Label("Rent range min value:");
		Label lmax = new Label("Rent range max value:");
		
		TextField minValText = new TextField("0");
		TextField maxValText = new TextField("2000");
		
		minVal.valueProperty().addListener(
				(observable, oldValue, newValue) -> {
					minValText.setText(String.valueOf(newValue.intValue()));
					performSearch();
				}
		);
		
		maxVal.valueProperty().addListener(
				(observable, oldValue, newValue) -> {
					maxValText.setText(String.valueOf(newValue.intValue()));
					performSearch();
				}
		);
		
		guestsSpin.valueProperty().addListener((obs, oldVal, newVal) -> performSearch());
		
		minValText.setEditable(false);
		maxValText.setEditable(false);
		
		minRentRange.getChildren().addAll(minVal, minValText);
		maxRentRange.getChildren().addAll(maxVal, maxValText);
		
		rentRange.getChildren().addAll(lmin, minRentRange, lmax, maxRentRange);
		rentRange.setAlignment(Pos.CENTER);
		
		rentRangeCompStyling(minVal, maxVal, minValText, maxValText, lmin, lmax);
		
		return rentRange;
	}
	
	private void rentRangeCompStyling(Slider minSlider, Slider maxSlider, 
			TextField minField, TextField maxField, Label lmin, Label lmax) {

	    minSlider.setStyle("-fx-accent: #3B82F6;");
	    maxSlider.setStyle("-fx-accent: #06B6D4;");

	    minField.setStyle(
	        "-fx-background-radius: 10;" +
	        "-fx-border-radius: 10;" +
	        "-fx-border-color: #93c5fd;" +
	        "-fx-background-color: #f8fbff;"
	    );

	    maxField.setStyle(
	        "-fx-background-radius: 10;" +
	        "-fx-border-radius: 10;" +
	        "-fx-border-color: #67e8f9;" +
	        "-fx-background-color: #f0fdff;"
	    );
	    
	    lmin.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
	    lmax.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
	}

	private void middlePanelStyling() {
		centerBox.setStyle(
	        	"-fx-background-color: #D9D9FF;"+
	        	"-fx-border-color: #0000FF;" +
	        	"-fx-border-width: 2;"
	        );
		
		searchBar.setStyle(
				"-fx-pref-width: 300px;" +
				"-fx-pref-height: 40px;"
			);
		
		filterComp.setStyle(
		        "-fx-background-color: white;" +
		        "-fx-background-radius: 18;" +
		        "-fx-border-radius: 18;" +
		        "-fx-border-color: #c7d2fe;" +
		        "-fx-border-width: 2;" +
		        "-fx-padding: 20;"
		    );
		
		filterComp.setAlignment(Pos.CENTER);
	}
	
	@SuppressWarnings("exports")
	public VBox getMiddlePanel() {
		return this.centerBox;
	}
}