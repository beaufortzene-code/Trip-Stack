package com.tripstack.ui;

import com.tripstack.dao.DepositDAO;
import com.tripstack.dao.TripDAO;
import com.tripstack.exception.DataAccessException;
import com.tripstack.exception.ValidationException;
import com.tripstack.model.Deposit;
import com.tripstack.model.Trip;
import com.tripstack.util.AlertUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Tab for logging and reviewing deposits (savings contributions) toward
 * a single selected trip. Shows a running total saved vs. the trip's goal.
 */
public class SavingsTab extends Tab {

    private final TripDAO tripDAO = new TripDAO();
    private final DepositDAO depositDAO = new DepositDAO();

    private final ComboBox<Trip> tripSelector = new ComboBox<>();
    private final TableView<Deposit> table = new TableView<>();
    private final ObservableList<Deposit> data = FXCollections.observableArrayList();

    private final DatePicker depositDatePicker = new DatePicker(LocalDate.now());
    private final TextField amountField = new TextField();
    private final TextField noteField = new TextField();
    private final Label errorLabel = new Label();
    private final Label summaryLabel = new Label();
    private final ProgressBar progressBar = new ProgressBar(0);

    public SavingsTab() {
        setText("Savings");
        setClosable(false);
        setContent(buildContent());
        refreshTripList();
    }

    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        VBox top = new VBox(8);
        HBox selectorRow = new HBox(10, new Label("Trip:"), tripSelector);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        tripSelector.setPrefWidth(300);
        tripSelector.setOnAction(e -> onTripSelected());

        summaryLabel.setStyle("-fx-font-weight: bold;");
        progressBar.setPrefWidth(300);

        top.getChildren().addAll(selectorRow, summaryLabel, progressBar);
        root.setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 12, 0));

        buildTable();
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 12, 0));

        root.setBottom(buildForm());
        return root;
    }

    private void buildTable() {
        TableColumn<Deposit, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDepositDate().toString()));

        TableColumn<Deposit, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("$%.2f", c.getValue().getAmount())));

        TableColumn<Deposit, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getNote() == null ? "" : c.getValue().getNote()));
        noteCol.setPrefWidth(250);

        table.getColumns().addAll(List.of(dateCol, amountCol, noteCol));
        table.setItems(data);
        table.setPlaceholder(new Label("No deposits logged for this trip yet."));
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12, 0, 0, 0));

        amountField.setPromptText("Amount, e.g. 150.00");
        noteField.setPromptText("Note (optional)");

        grid.add(new Label("Date:"), 0, 0);
        grid.add(depositDatePicker, 1, 0);
        grid.add(new Label("Amount ($):"), 2, 0);
        grid.add(amountField, 3, 0);
        grid.add(new Label("Note:"), 0, 1);
        grid.add(noteField, 1, 1, 3, 1);

        errorLabel.setStyle("-fx-text-fill: #c0392b;");
        grid.add(errorLabel, 0, 2, 4, 1);

        Button addButton = new Button("Add Deposit");
        addButton.setOnAction(e -> handleAddDeposit());

        Button deleteButton = new Button("Delete Selected Deposit");
        deleteButton.setOnAction(e -> handleDeleteDeposit());

        HBox buttons = new HBox(10, addButton, deleteButton);
        grid.add(buttons, 0, 3, 4, 1);

        return grid;
    }

    /** Called by other tabs (e.g. after adding a trip) to refresh the trip dropdown. */
    public void refreshTripList() {
        try {
            List<Trip> trips = tripDAO.findAll();
            Trip previouslySelected = tripSelector.getValue();
            tripSelector.setItems(FXCollections.observableArrayList(trips));
            if (previouslySelected != null && trips.stream().anyMatch(t -> t.getId() == previouslySelected.getId())) {
                tripSelector.setValue(previouslySelected);
            } else if (!trips.isEmpty()) {
                tripSelector.setValue(trips.get(0));
            }
            onTripSelected();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void onTripSelected() {
        Trip trip = tripSelector.getValue();
        if (trip == null) {
            data.clear();
            summaryLabel.setText("No trips available - add one in the Trips tab first.");
            progressBar.setProgress(0);
            return;
        }
        try {
            data.setAll(depositDAO.findByTrip(trip.getId()));
            double saved = depositDAO.getTotalSaved(trip.getId());
            double goal = trip.getSavingsGoal();
            double fraction = goal <= 0 ? 0 : Math.min(1.0, saved / goal);
            summaryLabel.setText(String.format("Saved $%.2f of $%.2f goal (%.0f%%)", saved, goal, fraction * 100));
            progressBar.setProgress(fraction);
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleAddDeposit() {
        errorLabel.setText("");
        Trip trip = tripSelector.getValue();
        if (trip == null) {
            errorLabel.setText("Select a trip first.");
            return;
        }
        try {
            Deposit deposit = buildDepositFromForm(trip.getId());
            depositDAO.create(deposit);
            clearForm();
            onTripSelected();
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleDeleteDeposit() {
        Deposit selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Select a deposit in the table first.");
            return;
        }
        boolean ok = AlertUtil.confirm("Delete Deposit", "Delete this deposit entry?");
        if (!ok) {
            return;
        }
        try {
            depositDAO.delete(selected.getId());
            onTripSelected();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private Deposit buildDepositFromForm(int tripId) throws ValidationException {
        LocalDate date = depositDatePicker.getValue();
        if (date == null) {
            throw new ValidationException("Deposit date is required.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Deposit date cannot be in the future.");
        }
        String amountText = amountField.getText() == null ? "" : amountField.getText().trim();
        if (amountText.isEmpty()) {
            throw new ValidationException("Amount is required.");
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            throw new ValidationException("Amount must be a valid number.");
        }
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
        String note = noteField.getText() == null ? "" : noteField.getText().trim();
        return new Deposit(0, tripId, date, amount, note.isEmpty() ? null : note);
    }

    private void clearForm() {
        depositDatePicker.setValue(LocalDate.now());
        amountField.clear();
        noteField.clear();
        errorLabel.setText("");
    }
}
