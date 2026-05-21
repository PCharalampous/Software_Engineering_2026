package search;

import entities.Application;
import entities.Authentication;
import entities.User;
import myapplications.SuccessScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.stage.Stage;
import ui.ConfirmationScreen;
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

	HomeScreenMiddlePanel(Stage homeScreenStage){
		
		headerLbl = new Label("HOMY APP - home management");
		separator = new Separator();
		
		searchBar = new TextField();
        searchBar.setPromptText("Press enter to search. Type...");
        
        centerBox = new VBox(10);
        filterComp = new HBox(10);
        
        filterComp.getChildren().addAll(rentRangeComponent(), selectGuestsComponent());
        
        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(10, 0, 10, 0));
        
        ScrollPane resultsScroll = new ScrollPane(resultsContainer);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-hbar-policy: never;");
        VBox.setVgrow(resultsScroll, Priority.ALWAYS);
        
        centerBox.getChildren().addAll(headerLbl, separator, searchBar, filterComp, resultsScroll);
        centerBox.setPadding(new Insets(10));
        centerBox.setAlignment(Pos.TOP_CENTER);
        
        middleScreenComponentsFunctiability();
        
        searchAndApplyFilters();
	}
	
	private void middleScreenComponentsFunctiability() {
		searchBar.setOnAction(event -> {
			searchBarBuffer = searchBar.getText();
			System.out.println("Search: " + searchBarBuffer);
			searchAndApplyFilters();
		});
	}
	
	private void searchAndApplyFilters() {
		resultsContainer.getChildren().clear();
		
		List<Application> dbApps = Application.loadAllApplications();
		List<Application> filteredApps = new ArrayList<>();
		
		String keyword = searchBarBuffer.toLowerCase().trim();
		double minRent = minVal.getValue();
		double maxRent = maxVal.getValue();
		int requiredRoommates = guestsSpin.getValue();
		
		for (Application app : dbApps) {
			boolean matchesLocation = keyword.isEmpty() || 
					(app.getLocation() != null && app.getLocation().toLowerCase().contains(keyword));
			
			boolean matchesRent = app.getRent() >= minRent && app.getRent() <= maxRent;
			
			boolean matchesRoommates = app.getRoommates() >= requiredRoommates;
			
			if (matchesLocation && matchesRent && matchesRoommates) {
				filteredApps.add(app);
			}
		}
		
		if (filteredApps.isEmpty()) {
			Label noResults = new Label("No available properties match your filters.");
			noResults.setStyle("-fx-text-fill: #64748B; -fx-font-style: italic; -fx-font-size: 14px; -fx-padding: 20;");
			resultsContainer.getChildren().add(noResults);
			return;
		}
		
		for (Application app : filteredApps) {
			VBox card = new VBox(8);
			card.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");
			
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
			
			Label descLbl = new Label(app.getDescription());
			descLbl.setStyle("-fx-text-fill: #1E293B; -fx-font-size: 13px;");
			descLbl.setWrapText(true);
			
			Button connectBtn = new Button("View & Connect");
			connectBtn.setMaxWidth(Double.MAX_VALUE);
			connectBtn.setStyle("-fx-background-color: #0000FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
			
			connectBtn.setOnAction(e -> {
				ConfirmationScreen confirScr = new ConfirmationScreen (
					"Confirm your actions", 
					"Do you want to send a roommate request to the room owner of this ad?",
					"Send Request",
					"-fx-background-color: #22C55E; -fx-text-fill: white;-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;",
					() -> {
						User sessionUser = Authentication.getCurrentUser();
						
						if (sessionUser != null) {
							int currentUserId = sessionUser.getId();
							
							if (currentUserId == app.getOwnerId()) {
								System.err.println("Δεν μπορείτε να στείλετε αίτημα σύνδεσης στη δική σας αγγελία.");
								return;
							}
							
							boolean success = app.sendRoomRequest(currentUserId);
							if (success) {
								SuccessScreen.display("Request Sent", "Success", "Your request was successfully sent to the room owner! Waiting for approval.");
							} else {
								System.err.println("Αποτυχία δημιουργίας εκκρεμούς αιτήματος.");
							}
						} else {
							System.err.println("Σφάλμα: Δεν βρέθηκε ενεργό session χρήστη.");
						}
					}
				);
				confirScr.show();
			});
			
			card.getChildren().addAll(row, locLbl, descLbl, connectBtn);
			resultsContainer.getChildren().add(card);
		}
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
	
	private HBox selectGuestsComponent(){
		HBox guestSelect = new HBox(5);
		Label guestsSpinLabel = new Label("Number of roomates:");
		guestsSpin = new Spinner<>(1, 10, 1);
		
		guestSelect.getChildren().addAll(guestsSpinLabel, guestsSpin);
		guestSelect.setAlignment(Pos.CENTER);
		selectGuestsStyling(guestsSpinLabel, guestsSpin);
		return guestSelect;
	}
	
	private void selectGuestsStyling(Label guestsLb, Spinner<Integer> guestSel) {
		guestsLb.setStyle(
			    "-fx-font-size: 14px;" +
			    "-fx-font-weight: bold;" +
			    "-fx-text-fill: #2563eb;"
			);
		
		guestSel.setStyle(
			    "-fx-background-color: #f8fbff;" +
			    "-fx-background-radius: 10;" +
			    "-fx-border-radius: 8;" +
			    "-fx-border-color: #93c5fd;" +
			    "-fx-border-width: 1;"
			);
		
		guestSel.getEditor().setStyle(
			    "-fx-background-color: transparent;" +
			    "-fx-font-size: 14px;" +
			    "-fx-text-fill: #2563eb;" +
			    "-fx-font-weight: bold;"
			);
		
		guestSel.setEditable(false);
	}
	
	private HBox rentRangeComponent(){
		HBox rentRange = new HBox(10);
		Label lmin = new Label("Rent range min value:");
		Label lmax = new Label("Rent range max value:");
		VBox minRentRange = new VBox(5);
		VBox maxRentRange = new VBox(5);
		minVal = new Slider(0, 1000, 100);
		maxVal = new Slider(100, 2000, 400);
		TextField minValText = new TextField("100");
		TextField maxValText = new TextField("400");
		
		minVal.valueProperty().addListener(
				(observable, oldValue, newValue) -> {
					minValText.setText(String.valueOf(newValue.intValue()));
				}
		);
		
		maxVal.valueProperty().addListener(
				(observable, oldValue, newValue) -> {
					maxValText.setText(String.valueOf(newValue.intValue()));
				}
		);
		
		minValText.setEditable(false);
		maxValText.setEditable(false);
		
		minRentRange.getChildren().addAll(minVal, minValText);
		maxRentRange.getChildren().addAll(maxVal,maxValText);
		
		rentRange.getChildren().addAll(lmin, minRentRange, lmax, maxRentRange);
		rentRange.setAlignment(Pos.CENTER);
		
		rentRangeCompStyling(minVal, maxVal, minValText, maxValText, lmin, lmax);
		
		return rentRange;
	}
	
	private void rentRangeCompStyling(Slider minSlider, Slider maxSlider, 
			TextField minField, TextField maxField, Label lmin,Label lmax) {

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

	    lmin.setStyle(
	    		"-fx-font-size: 14px;" +
	    		"-fx-font-weight: bold;" +
	    		"-fx-text-fill: #2563eb;"
	    );

	    lmax.setStyle(
	    		"-fx-font-size: 14px;" +
	    		"-fx-font-weight: bold;" +
	    		"-fx-text-fill: #2563eb;"
	    );
	}
	
	public VBox getMiddlePanel() {
		middlePanelStyling();
		return this.centerBox;
	}
}