package com.tripstack.dao;

import com.tripstack.exception.DataAccessException;
import com.tripstack.model.Trip;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object for Trip records. Handles all CRUD operations
 * against the "trips" table.
 */
public class TripDAO {

    public Trip create(Trip trip) throws DataAccessException {
        String sql = """
            INSERT INTO trips (name, category, trip_date, location, total_cost, savings_goal, deadline, group_name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTrip(ps, trip);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    trip.setId(keys.getInt(1));
                }
            }
            return trip;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save the new trip.", e);
        }
    }

    public List<Trip> findAll() throws DataAccessException {
        String sql = "SELECT * FROM trips ORDER BY deadline ASC";
        List<Trip> trips = new ArrayList<>();
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                trips.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load trips.", e);
        }
        return trips;
    }

    public Optional<Trip> findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM trips WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load trip #" + id + ".", e);
        }
    }

    public void update(Trip trip) throws DataAccessException {
        String sql = """
            UPDATE trips
            SET name = ?, category = ?, trip_date = ?, location = ?,
                total_cost = ?, savings_goal = ?, deadline = ?, group_name = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            bindTrip(ps, trip);
            ps.setInt(9, trip.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("No trip found with id " + trip.getId() + " to update.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update trip \"" + trip.getName() + "\".", e);
        }
    }

    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM trips WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete trip #" + id + ".", e);
        }
    }

    public List<String> findAllGroupNames() throws DataAccessException {
        String sql = "SELECT DISTINCT group_name FROM trips WHERE group_name IS NOT NULL AND TRIM(group_name) <> '' ORDER BY group_name";
        List<String> groups = new ArrayList<>();
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(rs.getString("group_name"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load comparison groups.", e);
        }
        return groups;
    }

    public List<Trip> findByGroup(String groupName) throws DataAccessException {
        String sql = "SELECT * FROM trips WHERE group_name = ? ORDER BY total_cost ASC";
        List<Trip> trips = new ArrayList<>();
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load candidates for group \"" + groupName + "\".", e);
        }
        return trips;
    }

    private void bindTrip(PreparedStatement ps, Trip trip) throws SQLException {
        ps.setString(1, trip.getName());
        ps.setString(2, trip.getCategory());
        ps.setString(3, trip.getTripDate() == null ? null : trip.getTripDate().toString());
        ps.setString(4, trip.getLocation());
        ps.setDouble(5, trip.getTotalCost());
        ps.setDouble(6, trip.getSavingsGoal());
        ps.setString(7, trip.getDeadline().toString());
        ps.setString(8, (trip.getGroupName() == null || trip.getGroupName().isBlank()) ? null : trip.getGroupName().trim());
    }

    private Trip mapRow(ResultSet rs) throws SQLException {
        String tripDateStr = rs.getString("trip_date");
        String deadlineStr = rs.getString("deadline");
        return new Trip(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                tripDateStr == null ? null : LocalDate.parse(tripDateStr),
                rs.getString("location"),
                rs.getDouble("total_cost"),
                rs.getDouble("savings_goal"),
                deadlineStr == null ? null : LocalDate.parse(deadlineStr),
                rs.getString("group_name")
        );
    }
}
