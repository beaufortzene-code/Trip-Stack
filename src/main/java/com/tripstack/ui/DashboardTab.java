package com.tripstack.ui;

import com.tripstack.dao.DepositDAO;
import com.tripstack.dao.TripDAO;
import com.tripstack.exception.DataAccessException;
import com.tripstack.model.Trip;
import com.tripstack.util.AlertUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Read-only dashboard: every trip sorted by upcoming deadline, with the
 * amount saved so far and a flag for trips that are behind the savings
 * pace they would need to hit their goal on time.
 *
 * "Behind pace" is a simple heuristic: remaining amount needed divided by
 * days remaining. If that daily amount is large relative to what's left,
 * or the deadline has already passed without the goal being met, the
 * trip is flagged. This is meant as a helpful nudge, not a precise
 * financial calculation.
 */
public class DashboardTab extends Tab {

    private final TripDAO tripDAO = new TripDAO();
    private final DepositDAO depositDAO = new DepositDAO();
    private final TableView<DashboardRow> table = new TableView<>();
    private final ObservableList<DashboardRow> data = FXCollections.observableArrayList();

    public DashboardTab() {
        setText("Dashboard");
        setClosable(false);
        setContent(buildContent());
        refresh();
    }

    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        buildTable();
        root.setCenter(table);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refresh());
        BorderPane.setMargin(refreshButton, new Insets(12, 0, 0, 0));
        root.setBottom(refreshButton);

        return root;
    }

    private void buildTable() {
        TableColumn<DashboardRow, String> nameCol = new TableColumn<>("Trip");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().name));

        TableColumn<DashboardRow, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().deadline.toString()));

        TableColumn<DashboardRow, String> daysCol = new TableColumn<>("Days Left");
        daysCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().daysLeft)));

        TableColumn<DashboardRow, String> savedCol = new TableColumn<>("Saved / Goal");
        savedCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                String.format("$%.2f / $%.2f", c.getValue().saved, c.getValue().goal)));

        TableColumn<DashboardRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status));

        table.getColumns().addAll(List.of(nameCol, deadlineCol, daysCol, savedCol, statusCol));
        table.setItems(data);
        table.setPlaceholder(new Label("No trips yet - add one in the Trips tab."));
    }

    public void refresh() {
        try {
            List<Trip> trips = tripDAO.findAll(); // already sorted by deadline ascending
            data.clear();
            for (Trip trip : trips) {
                double saved = depositDAO.getTotalSaved(trip.getId());
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), trip.getDeadline());
                double remaining = trip.getSavingsGoal() - saved;
                String status;
                if (remaining <= 0) {
                    status = "Funded";
                } else if (daysLeft <= 0) {
                    status = "Past deadline - short by $" + String.format("%.2f", remaining);
                } else {
                    double perDay = remaining / daysLeft;
                    status = perDay > 15 ? String.format("Behind pace ($%.2f/day needed)", perDay) : "On track";
                }
                data.add(new DashboardRow(trip.getName(), trip.getDeadline(), daysLeft, saved, trip.getSavingsGoal(), status));
            }
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    /** Simple row holder for the dashboard table. */
    private static class DashboardRow {
        final String name;
        final LocalDate deadline;
        final long daysLeft;
        final double saved;
        final double goal;
        final String status;

        DashboardRow(String name, LocalDate deadline, long daysLeft, double saved, double goal, String status) {
            this.name = name;
            this.deadline = deadline;
            this.daysLeft = daysLeft;
            this.saved = saved;
            this.goal = goal;
            this.status = status;
        }
    }
}
