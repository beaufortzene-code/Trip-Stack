package com.tripstack.ui;

import com.tripstack.dao.ExpenseDAO;
import com.tripstack.dao.TripDAO;
import com.tripstack.exception.DataAccessException;
import com.tripstack.exception.ValidationException;
import com.tripstack.model.Expense;
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

import java.util.List;

/**
 * Tab for itemizing costs (deposit, tickets, hotel, flights, spending
 * money, etc.) that make up a trip's total, so the estimate isn't just
 * a single lump-sum number.
 */
public class ExpensesTab extends Tab {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final TripDAO tripDAO = new TripDAO();

    private final ComboBox<Trip> tripSelector = new ComboBox<>();
    private final TableView<Expense> table = new TableView<>();
    private final ObservableList<Expense> data = FXCollections.observableArrayList();

    private final ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
            "Deposit", "Tickets", "Hotel", "Flights", "Transportation", "Spending Money", "Other"));
    private final TextField descriptionField = new TextField();
    private final TextField amountField = new TextField();
    private final Label errorLabel = new Label();
    private final Label summaryLabel = new Label();

    public ExpensesTab() {
        setText("Expenses");
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
        top.getChildren().addAll(selectorRow, summaryLabel);
        root.setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 12, 0));

        buildTable();
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(12, 0, 12, 0));

        root.setBottom(buildForm());
        return root;
    }

    private void buildTable() {
        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCategory()));

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getDescription() == null ? "" : c.getValue().getDescription()));
        descCol.setPrefWidth(250);

        TableColumn<Expense, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("$%.2f", c.getValue().getAmount())));

        table.getColumns().addAll(List.of(categoryCol, descCol, amountCol));
        table.setItems(data);
        table.setPlaceholder(new Label("No expenses logged for this trip yet."));
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12, 0, 0, 0));

        categoryBox.setPromptText("Category");
        descriptionField.setPromptText("Description (optional)");
        amountField.setPromptText("Amount, e.g. 300.00");

        grid.add(new Label("Category:"), 0, 0);
        grid.add(categoryBox, 1, 0);
        grid.add(new Label("Amount ($):"), 2, 0);
        grid.add(amountField, 3, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1, 3, 1);

        errorLabel.setStyle("-fx-text-fill: #c0392b;");
        grid.add(errorLabel, 0, 2, 4, 1);

        Button addButton = new Button("Add Expense");
        addButton.setOnAction(e -> handleAddExpense());

        Button deleteButton = new Button("Delete Selected Expense");
        deleteButton.setOnAction(e -> handleDeleteExpense());

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
            return;
        }
        try {
            data.setAll(expenseDAO.findByTrip(trip.getId()));
            double total = expenseDAO.getTotalExpenses(trip.getId());
            summaryLabel.setText(String.format("Itemized total: $%.2f  (Trip estimate: $%.2f)", total, trip.getTotalCost()));
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleAddExpense() {
        errorLabel.setText("");
        Trip trip = tripSelector.getValue();
        if (trip == null) {
            errorLabel.setText("Select a trip first.");
            return;
        }
        try {
            Expense expense = buildExpenseFromForm(trip.getId());
            expenseDAO.create(expense);
            clearForm();
            onTripSelected();
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleDeleteExpense() {
        Expense selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Select an expense in the table first.");
            return;
        }
        boolean ok = AlertUtil.confirm("Delete Expense", "Delete this expense entry?");
        if (!ok) {
            return;
        }
        try {
            expenseDAO.delete(selected.getId());
            onTripSelected();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private Expense buildExpenseFromForm(int tripId) throws ValidationException {
        String category = categoryBox.getValue();
        if (category == null || category.isBlank()) {
            throw new ValidationException("Please choose a category.");
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
        String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        return new Expense(0, tripId, category, description.isEmpty() ? null : description, amount);
    }

    private void clearForm() {
        categoryBox.setValue(null);
        descriptionField.clear();
        amountField.clear();
        errorLabel.setText("");
    }
}
