package com.tripstack.dao;

import com.tripstack.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the single local SQLite database file (tripstack.db) used by the
 * whole application. All DAO classes obtain their connection through here.
 *
 * The database file is created automatically, in the working directory,
 * the first time the application runs.
 */
public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:tripstack.db";
    private static Connection connection;

    private DatabaseManager() {
        // utility class - not instantiable
    }

    /**
     * Returns a single shared connection to the local SQLite file,
     * opening and initializing it on first use.
     */
    public static synchronized Connection getConnection() throws DataAccessException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
                initializeSchema(connection);
            }
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect to the local database (tripstack.db).", e);
        }
    }

    private static void initializeSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS trips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    trip_date TEXT,
                    location TEXT,
                    total_cost REAL NOT NULL,
                    savings_goal REAL NOT NULL,
                    deadline TEXT NOT NULL,
                    group_name TEXT
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS deposits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    deposit_date TEXT NOT NULL,
                    amount REAL NOT NULL,
                    note TEXT,
                    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
                )
            """);
        }
    }

    /** Closes the shared connection. Call once when the application exits. */
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // Nothing meaningful to do on shutdown - just avoid crashing the exit path.
            System.err.println("Warning: failed to close database connection cleanly: " + e.getMessage());
        }
    }
}
