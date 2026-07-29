package com.tripstack.model;

/**
 * Represents a single itemized cost belonging to a Trip
 * (e.g., deposit, tickets, hotel, flights, spending money).
 */
public class Expense {

    private int id;
    private int tripId;
    private String category;
    private String description;
    private double amount;

    public Expense() {
    }

    public Expense(int id, int tripId, String category, String description, double amount) {
        this.id = id;
        this.tripId = tripId;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
