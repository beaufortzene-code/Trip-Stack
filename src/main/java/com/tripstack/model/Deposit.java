package com.tripstack.model;

import java.time.LocalDate;

/**
 * Represents a single savings contribution (deposit) made toward a specific Trip.
 */
public class Deposit {

    private int id;
    private int tripId;
    private LocalDate depositDate;
    private double amount;
    private String note;

    public Deposit() {
    }

    public Deposit(int id, int tripId, LocalDate depositDate, double amount, String note) {
        this.id = id;
        this.tripId = tripId;
        this.depositDate = depositDate;
        this.amount = amount;
        this.note = note;
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

    public LocalDate getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(LocalDate depositDate) {
        this.depositDate = depositDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
