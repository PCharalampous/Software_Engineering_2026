package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Issue {
    private final StringProperty type;
    private final StringProperty reportedBy;
    private final StringProperty payers;
    private final StringProperty date;

    public Issue(String type, String reportedBy, String payers, String date) {
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
    }

    public StringProperty typeProperty() { return type; }
    public StringProperty reportedByProperty() { return reportedBy; }
    public StringProperty payersProperty() { return payers; }
    public StringProperty dateProperty() { return date; }

    public String getType() { return type.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getPayers() { return payers.get(); }
    public String getDate() { return date.get(); }
}