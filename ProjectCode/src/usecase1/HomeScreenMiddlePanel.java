package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeScreenMiddlePanel {
	
	private TextField searchBar;
	private VBox centerBox;
	private Label headerLbl, guestsSpinLabel;
	private Separator separator;
	private Spinner<Integer> guestsSpin;
	private HBox filterComp;
	
	HomeScreenMiddlePanel(){
		
		headerLbl = new Label("HOMY APP - home management");
		separator = new Separator();
		
		searchBar = new TextField();

        searchBar.setPromptText("Search...");
        guestsSpin = new Spinner<>(1, 10, 1);
        
        centerBox = new VBox(10);
        filterComp = new HBox(5);
        
        guestsSpinLabel = new Label("Choose number of roomates");
        filterComp.getChildren().addAll(rentRangeComponent(), guestsSpinLabel,guestsSpin);
        
        centerBox.getChildren().addAll(headerLbl,separator,searchBar, filterComp);
        centerBox.setPadding(new Insets(10));

        centerBox.setAlignment(Pos.TOP_CENTER);
        
	}
	
	private HBox rentRangeComponent(){
		HBox rentRange = new HBox(5);
		Label lmin = new Label("Rent range min value");
		Label lmax = new Label("Rent range max value");
		Spinner<Integer> minVal = new Spinner<>(0, 10000, 0);
		Spinner<Integer> maxVal = new Spinner<>(0, 10000, 0);
		rentRange.getChildren().addAll(lmin, minVal, lmax,maxVal);
		return rentRange;
	}
	
	
	private void middlePanelStyling(VBox vbx, TextField sbar, HBox filt) {
		vbx.setStyle(
	        	"-fx-background-color: #D9D9FF;"+
	        	"-fx-border-color: #0000FF;" +
	        	"-fx-border-width: 2;"
	        );
		
		sbar.setStyle(
				"-fx-pref-width: 300px;" +
				"-fx-pref-height: 40px;"
			);
		filt.setAlignment(Pos.CENTER);
	}
	
	public VBox getMiddlePanel() {
		middlePanelStyling(this.centerBox, this.searchBar, this.filterComp);
		return this.centerBox;
	}
}
