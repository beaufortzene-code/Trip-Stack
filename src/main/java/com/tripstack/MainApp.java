package com.tripstack;

import com.tripstack.ui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import com.tripstack.dao.DatabaseManager;

/**
 * Entry point for the TripStack desktop application.
 *
 * TripStack helps a user plan and save for several trips or events at
 * once (cruises, concerts, theme parks) by tracking each trip's cost,
 * savings goal, and deadline, logging deposits and itemized expenses
 * against it, and flagging trips that are falling behind their savings
 * pace.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DashboardTab dashboardTab = new DashboardTab();
        TripsTab tripsTab = new TripsTab();
        SavingsTab savingsTab = new SavingsTab();
        ExpensesTab expensesTab = new ExpensesTab();
        CompareTab compareTab = new CompareTab();

        TabPane tabPane = new TabPane(dashboardTab, tripsTab, savingsTab, expensesTab, compareTab);

        // Whenever the user switches tabs, refresh that tab's data so it
        // reflects any trips/deposits/expenses added elsewhere.
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == dashboardTab) {
                dashboardTab.refresh();
            } else if (newTab == tripsTab) {
                tripsTab.refresh();
            } else if (newTab == savingsTab) {
                savingsTab.refreshTripList();
            } else if (newTab == expensesTab) {
                expensesTab.refreshTripList();
            } else if (newTab == compareTab) {
                compareTab.refreshGroups();
            }
        });

        Scene scene = new Scene(tabPane, 900, 620);
        primaryStage.setTitle("TripStack - Multi-Event Travel & Budget Planner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Ensure the SQLite connection is closed cleanly when the window closes.
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        Platform.setImplicitExit(true);
        launch(args);
    }
}
