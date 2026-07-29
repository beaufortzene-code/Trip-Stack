package com.tripstack.model;

import java.time.LocalDate;

/**
 * Represents a single trip or event the user is planning and saving for
 * (e.g., a cruise, a concert, a theme park visit).
 */
public class Trip {

    private int id;
    private String name;
    private String category;
    private LocalDate tripDate;
    private String location;
    private double totalCost;
    private double savingsGoal;
    private LocalDate deadline;
    // Optional: trips that share the same groupName are "candidate" versions
    // of the same event being compared against each other (Compare tab).
    private String groupName;

    public Trip() {
    }

    public Trip(int id, String name, String category, LocalDate tripDate, String location,
                double totalCost, double savingsGoal, LocalDate deadline, String groupName) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tripDate = tripDate;
        this.location = location;
        this.totalCost = totalCost;
        this.savingsGoal = savingsGoal;
        this.deadline = deadline;
        this.groupName = groupName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getSavingsGoal() {
        return savingsGoal;
    }

    public void setSavingsGoal(double savingsGoal) {
        this.savingsGoal = savingsGoal;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        // Used by ComboBox display in Savings/Expenses/Compare tabs.
        return name + " (" + category + ")";
    }
}
