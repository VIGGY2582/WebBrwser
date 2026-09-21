package org.example.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite local database connection and table initialization.
 */
public class DatabaseManager {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = "data/studysphere.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    /**
     * Initializes the database, creating directory and tables if they don't exist.
     */
    public static void initDatabase() {
        try {
            // Ensure data directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Create tables
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // History table
                String historySql = "CREATE TABLE IF NOT EXISTS history (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "title TEXT, " +
                                    "url TEXT, " +
                                    "visited_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                    ");";
                stmt.execute(historySql);

                // Bookmarks table
                String bookmarksSql = "CREATE TABLE IF NOT EXISTS bookmarks (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "title TEXT, " +
                                      "url TEXT UNIQUE, " +
                                      "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                      ");";
                stmt.execute(bookmarksSql);
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /**
     * Obtains a new connection to the local SQLite database.
     *
     * @return Connection instance
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
