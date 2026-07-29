package com.tripstack.ui;

import com.tripstack.dao.TripDAO;
import com.tripstack.exception.DataAccessException;
import com.tripstack.model.Trip;
import com.tripstack.util.AlertUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Lets the user compare several "candidate" trips that share the same
 * Compare Group (set in the Trips tab) - for example, the same concert
 * in two different cities - side by side, then archive (delete) the
 * candidates that weren't chosen.
 */
public class CompareTab extends Tab {

    private final TripDAO tripDAO = new TripDAO();
    private final ComboBox<String> groupSelector = new ComboBox<>();
    private final TableView<Trip> table = new TableView<>();
    private final ObservableList<Trip> data = FXCollections.observableArrayList();
    private final Label infoLabel = new Label(
            "Tip: give two or more trips in the Trips tab the same \"Compare Group\" name to see them here.");

    public CompareTab() {
        setText("Compare");
        setClosable(false);
        setContent(buildContent());
        refreshGroups();
    }

    private Node buildContent() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        VBox top = new VBox(8);
        HBox selectorRow = new HBox(10, new Label("Compare Group:"), groupSelector);
        selectorRow.setAlignment(Pos.CENTER_LEFT);
        groupSelector.setPrefWidth(250);
        groupSelector.setOnAction(e -> onGroupSelected());
        infoLabel.setWrapText(true);
        top.getChildren().addAll(selectorRow, infoLabel);
        root.setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 12, 0));

        buildTable();
        root.setCenter(table);
        BorderPane.setMargin(table, new Insets(0, 0, 12, 0));

        Button archiveButton = new Button("Keep Selected, Archive Others");
        archiveButton.setOnAction(e -> handleArchiveOthers());
        root.setBottom(archiveButton);

        return root;
    }

    private void buildTable() {
        TableColumn<Trip, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));

        TableColumn<Trip, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getLocation()));

        TableColumn<Trip, String> costCol = new TableColumn<>("Total Cost");
        costCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.format("$%.2f", c.getValue().getTotalCost())));

        TableColumn<Trip, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getDeadline() == null ? "" : c.getValue().getDeadline().toString()));

        table.getColumns().addAll(List.of(nameCol, locationCol, costCol, deadlineCol));
        table.setItems(data);
        table.setPlaceholder(new Label("No candidates in this group yet."));
    }

    public void refreshGroups() {
        try {
            List<String> groups = tripDAO.findAllGroupNames();
            String previous = groupSelector.getValue();
            groupSelector.setItems(FXCollections.observableArrayList(groups));
            if (previous != null && groups.contains(previous)) {
                groupSelector.setValue(previous);
            } else if (!groups.isEmpty()) {
                groupSelector.setValue(groups.get(0));
            }
            onGroupSelected();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void onGroupSelected() {
        String group = groupSelector.getValue();
        if (group == null) {
            data.clear();
            return;
        }
        try {
            data.setAll(tripDAO.findByGroup(group));
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void handleArchiveOthers() {
        Trip keep = table.getSelectionModel().getSelectedItem();
        if (keep == null) {
            AlertUtil.showError("No Selection", "Select the candidate you want to keep first.");
            return;
        }
        boolean ok = AlertUtil.confirm("Archive Other Candidates",
                "Delete every other trip in this group and keep only \"" + keep.getName() + "\"?");
        if (!ok) {
            return;
        }
        try {
            for (Trip trip : data) {
                if (trip.getId() != keep.getId()) {
                    tripDAO.delete(trip.getId());
                }
            }
            refreshGroups();
        } catch (DataAccessException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }
}
