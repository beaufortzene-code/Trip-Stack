package com.tripstack.ui;

import com.tripstack.dao.TripDAO;
import com.tripstack.exception.DataAccessException;
import com.tripstack.exception.ValidationException;
import com.tripstack.model.Trip;
import com.tripstack.util.AlertUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
 * Tab that lists all trips and provides Create/Update/Delete operations
 * through a form. Selecting a row loads it into the form for editing.
 */
public class TripsTab extends Tab {

    private final TripDAO tripDAO = new TripDAO();
    private final ObservableList<Trip> data = FXCollections.observableArrayList();
    private final TableView<Trip> table = new TableView<>();

    private final TextField nameField = new TextField();
    private final ComboBox<String> categoryBox = new ComboBox<>(
            FXCollections.observableArrayList("Cruise", "Concert", "Theme Park", "Other"));
    private final DatePicker tripDatePicker = new DatePicker();
    private final TextField locationField = new TextField();
    private final TextField totalCostField = new TextField();
    private final TextField savingsGoalField = new TextField();
    private final DatePicker deadlinePicker = new DatePicker();
    private final TextField groupField = new TextField();
    private final Label errorLabel = new Label();

    private Trip selectedTrip = null;

    public TripsTab() {
        setText("Trips");
        setClosable(false);
        setContent(buildContent());
        refresh();
    }

    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        buildTable();
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(0, 0, 12, 0));

        root.setBottom(buildForm());
        return root;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Trip, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));

        TableColumn<Trip, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCategory()));

        TableColumn<Trip, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLocation()));

        TableColumn<Trip, String> costCol = new TableColumn<>("Total Cost");
        costCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("$%.2f", c.getValue().getTotalCost())));

        TableColumn<Trip, String> goalCol = new TableColumn<>("Savings Goal");
        goalCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("$%.2f", c.getValue().getSavingsGoal())));

        TableColumn<Trip, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                c.getValue().getDeadline() == null ? "" : c.getValue().getDeadline().toString()));

        table.getColumns().addAll(List.of(nameCol, categoryCol, locationCol, costCol, goalCol, deadlineCol));
        table.setItems(data);
        table.setPlaceholder(new Label("No trips yet - add one below."));

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadIntoForm(newVal);
            }
        });
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12, 0, 0, 0));

        categoryBox.setPromptText("Category");
        tripDatePicker.setPromptText("Trip date (optional)");
        deadlinePicker.setPromptText("Savings deadline");
        locationField.setPromptText("Location");
        totalCostField.setPromptText("Total cost, e.g. 1200.00");
        savingsGoalField.setPromptText("Savings goal, e.g. 1200.00");
        groupField.setPromptText("Compare group (optional)");

        int r = 0;
        grid.add(new Label("Name:"), 0, r);
        grid.add(nameField, 1, r);
        grid.add(new Label("Category:"), 2, r);
        grid.add(categoryBox, 3, r);
        r++;

        grid.add(new Label("Location:"), 0, r);
        grid.add(locationField, 1, r);
        grid.add(new Label("Trip Date:"), 2, r);
        grid.add(tripDatePicker, 3, r);
        r++;

        grid.add(new Label("Total Cost ($):"), 0, r);
        grid.add(totalCostField, 1, r);
        grid.add(new Label("Savings Goal ($):"), 2, r);
        grid.add(savingsGoalField, 3, r);
        r++;

        grid.add(new Label("Deadline:"), 0, r);
        grid.add(deadlinePicker, 1, r);
        grid.add(new Label("Compare Group:"), 2, r);
        grid.add(groupField, 3, r);
        r++;

        errorLabel.setStyle("-fx-text-fill: #c0392b;");
        grid.add(errorLabel, 0, r, 4, 1);
        r++;

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> handleSave());

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> handleDelete());

        Button clearButton = new Button("Clear / New");
        clearButton.setOnAction(e -> clearForm());

        HBox buttons = new HBox(10, saveButton, deleteButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        grid.add(buttons, 0, r, 4, 1);

        return grid;
    }

    public void refresh() {
        try {
            data.setAll(tripDAO.findAll());
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleSave() {
        errorLabel.setText("");
        try {
            Trip trip = buildTripFromForm();
            if (selectedTrip == null) {
                tripDAO.create(trip);
                AlertUtil.showInfo("Trip Saved", "\"" + trip.getName() + "\" was added.");
            } else {
                trip.setId(selectedTrip.getId());
                tripDAO.update(trip);
                AlertUtil.showInfo("Trip Updated", "\"" + trip.getName() + "\" was updated.");
            }
            clearForm();
            refresh();
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedTrip == null) {
            errorLabel.setText("Select a trip in the table first.");
            return;
        }
        boolean ok = AlertUtil.confirm("Delete Trip",
                "Delete \"" + selectedTrip.getName() + "\"? This also removes its deposits and expenses.");
        if (!ok) {
            return;
        }
        try {
            tripDAO.delete(selectedTrip.getId());
            clearForm();
            refresh();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private Trip buildTripFromForm() throws ValidationException {
        String name = safeTrim(nameField.getText());
        if (name.isEmpty()) {
            throw new ValidationException("Trip name is required.");
        }

        String category = categoryBox.getValue();
        if (category == null || category.isBlank()) {
            throw new ValidationException("Please choose a category.");
        }

        LocalDate deadline = deadlinePicker.getValue();
        if (deadline == null) {
            throw new ValidationException("Savings deadline is required.");
        }
        if (deadline.isBefore(LocalDate.now())) {
            throw new ValidationException("Deadline cannot be in the past.");
        }

        LocalDate tripDate = tripDatePicker.getValue(); // optional

        double totalCost = parsePositiveOrZero(totalCostField.getText(), "Total cost");
        double savingsGoal = parsePositiveOrZero(savingsGoalField.getText(), "Savings goal");

        String location = safeTrim(locationField.getText());
        String group = safeTrim(groupField.getText());

        return new Trip(0, name, category, tripDate, location, totalCost, savingsGoal, deadline,
                group.isEmpty() ? null : group);
    }

    private double parsePositiveOrZero(String text, String fieldLabel) throws ValidationException {
        String trimmed = safeTrim(text);
        if (trimmed.isEmpty()) {
            throw new ValidationException(fieldLabel + " is required.");
        }
        double value;
        try {
            value = Double.parseDouble(trimmed);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldLabel + " must be a valid number.");
        }
        if (value < 0) {
            throw new ValidationException(fieldLabel + " cannot be negative.");
        }
        return value;
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private void loadIntoForm(Trip trip) {
        selectedTrip = trip;
        nameField.setText(trip.getName());
        categoryBox.setValue(trip.getCategory());
        tripDatePicker.setValue(trip.getTripDate());
        locationField.setText(trip.getLocation());
        totalCostField.setText(String.valueOf(trip.getTotalCost()));
        savingsGoalField.setText(String.valueOf(trip.getSavingsGoal()));
        deadlinePicker.setValue(trip.getDeadline());
        groupField.setText(trip.getGroupName() == null ? "" : trip.getGroupName());
        errorLabel.setText("");
    }

    private void clearForm() {
        selectedTrip = null;
        nameField.clear();
        categoryBox.setValue(null);
        tripDatePicker.setValue(null);
        locationField.clear();
        totalCostField.clear();
        savingsGoalField.clear();
        deadlinePicker.setValue(null);
        groupField.clear();
        errorLabel.setText("");
        table.getSelectionModel().clearSelection();
    }
}
