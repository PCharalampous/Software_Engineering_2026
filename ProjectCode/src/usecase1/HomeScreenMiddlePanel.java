package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeScreenMiddlePanel {
	
	private TextField searchBar;
	private VBox centerBox;
	private Label headerLbl;
	private Separator separator;

	private HBox filterComp;
	
	HomeScreenMiddlePanel(){
		
		headerLbl = new Label("HOMY APP - home management");
		separator = new Separator();
		
		searchBar = new TextField();

        searchBar.setPromptText("Search...");
        
        
        centerBox = new VBox(10);
        filterComp = new HBox(10);
        
        
        filterComp.getChildren().addAll(rentRangeComponent(), selectGuestsComponent());
        
        centerBox.getChildren().addAll(headerLbl,separator,searchBar, filterComp);
        centerBox.setPadding(new Insets(10));

        centerBox.setAlignment(Pos.TOP_CENTER);
        
	}
	
	private VBox selectGuestsComponent(){
		VBox guestSelect = new VBox(5);
		Label guestsSpinLabel = new Label("Choose number of roomates");
		Spinner<Integer> guestsSpin = new Spinner<>(1, 10, 1);
		
		guestSelect.getChildren().addAll(guestsSpinLabel, guestsSpin);
		guestSelect.setAlignment(Pos.CENTER);
		return guestSelect;
	}
	
	
	private HBox rentRangeComponent(){
		
		HBox rentRange = new HBox(10);
		Label lmin = new Label("Rent range min value ");
		Label lmax = new Label("Rent range max value ");
		VBox minRentRange = new VBox(5);
		VBox maxRentRange = new VBox(5);
		Slider minVal = new Slider(0, 1000, 100);
		Slider maxVal = new Slider(0, 1000, 400);
		TextField minValText = new TextField("100");
		TextField maxValText = new TextField("400");
		
		//slider add listener
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
		
		//TextField properties
		minValText.setEditable(false);
		maxValText.setEditable(false);
		
		minRentRange.getChildren().addAll(minVal, minValText);
		maxRentRange.getChildren().addAll(maxVal,maxValText);
		
		rentRange.getChildren().addAll(lmin, minRentRange, lmax, maxRentRange);
		rentRange.setAlignment(Pos.CENTER);
		
		rentRangeCompStyling(rentRange, minVal, maxVal, minValText, maxValText, lmin, lmax);
		
		return rentRange;
	}
	
	
	private void rentRangeCompStyling(HBox rentRange, Slider minSlider, Slider maxSlider, 
			TextField minField, TextField maxField, Label lmin,Label lmax) {

	    minSlider.setStyle(
	        "-fx-accent: #3B82F6;"
	    );

	    maxSlider.setStyle(
	        "-fx-accent: #06B6D4;"
	    );

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
	        "-fx-font-weight: bold;" +
	        "-fx-text-fill: #2563eb;"
	    );

	    lmax.setStyle(
	        "-fx-font-weight: bold;" +
	        "-fx-text-fill: #0891b2;"
	    );
	    
	    
	}
	
	private void middlePanelStyling(VBox vbx, TextField sbar, HBox filters) {
		vbx.setStyle(
	        	"-fx-background-color: #D9D9FF;"+
	        	"-fx-border-color: #0000FF;" +
	        	"-fx-border-width: 2;"
	        );
		
		sbar.setStyle(
				"-fx-pref-width: 300px;" +
				"-fx-pref-height: 40px;"
			);
		
		filters.setStyle(
		        "-fx-background-color: white;" +
		        "-fx-background-radius: 18;" +
		        "-fx-border-radius: 18;" +
		        "-fx-border-color: #c7d2fe;" +
		        "-fx-border-width: 2;" +
		        "-fx-padding: 20;"
		    );
		
		filters.setAlignment(Pos.CENTER);
		
	}
	
	public VBox getMiddlePanel() {
		middlePanelStyling(this.centerBox, this.searchBar, this.filterComp);
		return this.centerBox;
	}
}
