package com.tripstack.dao;

import com.tripstack.exception.DataAccessException;
import com.tripstack.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for Expense records (itemized costs belonging to a Trip,
 * e.g., deposit, tickets, hotel, flights, spending money).
 */
public class ExpenseDAO {

    public Expense create(Expense expense) throws DataAccessException {
        String sql = "INSERT INTO expenses (trip_id, category, description, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expense.getTripId());
            ps.setString(2, expense.getCategory());
            ps.setString(3, expense.getDescription());
            ps.setDouble(4, expense.getAmount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expense.setId(keys.getInt(1));
                }
            }
            return expense;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save the expense.", e);
        }
    }

    public List<Expense> findByTrip(int tripId) throws DataAccessException {
        String sql = "SELECT * FROM expenses WHERE trip_id = ? ORDER BY id DESC";
        List<Expense> expenses = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(new Expense(
                            rs.getInt("id"),
                            rs.getInt("trip_id"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load expenses for trip #" + tripId + ".", e);
        }
        return expenses;
    }

    public double getTotalExpenses(int tripId) throws DataAccessException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses WHERE trip_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to calculate total expenses for trip #" + tripId + ".", e);
        }
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete expense #" + id + ".", e);
        }
    }
}
