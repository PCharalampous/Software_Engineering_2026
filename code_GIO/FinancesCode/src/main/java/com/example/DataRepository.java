package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataRepository {
    private static final ObservableList<Bill> bills = FXCollections.observableArrayList();

    static {
        // Mock data matching your UML history and pending tracking requirements
        bills.add(new Bill("Electricity", 120.50, "2026-05-10", "Giorgos, Alex", "Pending"));
        bills.add(new Bill("Internet", 35.00, "2026-05-01", "All Roommates", "Paid"));
        bills.add(new Bill("Water", 45.20, "2026-05-14", "Giorgos", "Pending"));
    }

    public static ObservableList<Bill> getBills() {
        return bills;
    }
}