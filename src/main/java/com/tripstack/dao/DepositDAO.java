package com.tripstack.dao;

import com.tripstack.exception.DataAccessException;
import com.tripstack.model.Deposit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for Deposit records (individual savings contributions
 * toward a Trip). Handles CRUD plus the running-total query used by the
 * Savings tab and Dashboard.
 */
public class DepositDAO {

    public Deposit create(Deposit deposit) throws DataAccessException {
        String sql = "INSERT INTO deposits (trip_id, deposit_date, amount, note) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, deposit.getTripId());
            ps.setString(2, deposit.getDepositDate().toString());
            ps.setDouble(3, deposit.getAmount());
            ps.setString(4, deposit.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    deposit.setId(keys.getInt(1));
                }
            }
            return deposit;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save the deposit.", e);
        }
    }

    public List<Deposit> findByTrip(int tripId) throws DataAccessException {
        String sql = "SELECT * FROM deposits WHERE trip_id = ? ORDER BY deposit_date DESC, id DESC";
        List<Deposit> deposits = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    deposits.add(new Deposit(
                            rs.getInt("id"),
                            rs.getInt("trip_id"),
                            LocalDate.parse(rs.getString("deposit_date")),
                            rs.getDouble("amount"),
                            rs.getString("note")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load deposits for trip #" + tripId + ".", e);
        }
        return deposits;
    }

    public double getTotalSaved(int tripId) throws DataAccessException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM deposits WHERE trip_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0.0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to calculate total saved for trip #" + tripId + ".", e);
        }
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM deposits WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete deposit #" + id + ".", e);
        }
    }
}
