package org.example.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling browsing history CRUD operations in SQLite.
 */
public class HistoryDAO {

    /**
     * Data model representing a browsing history entry.
     */
    public static class HistoryItem {
        private final int id;
        private final String title;
        private final String url;
        private final String visitedAt;

        public HistoryItem(int id, String title, String url, String visitedAt) {
            this.id = id;
            this.title = (title != null && !title.trim().isEmpty()) ? title.trim() : url;
            this.url = url;
            this.visitedAt = visitedAt != null ? visitedAt : "";
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getVisitedAt() {
            return visitedAt;
        }

        @Override
        public String toString() {
            return title + " (" + url + ")";
        }
    }

    /**
     * Adds a new history record for a visited webpage.
     *
     * @param title Page title
     * @param url   Page URL
     */
    public static void addHistory(String title, String url) {
        if (url == null || url.trim().isEmpty() || url.startsWith("data:") || url.equals("about:blank")) {
            return;
        }

        String sql = "INSERT INTO history (title, url, visited_at) VALUES (?, ?, datetime('now', 'localtime'));";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String sanitizedTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : url;
            pstmt.setString(1, sanitizedTitle);
            pstmt.setString(2, url.trim());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving browsing history: " + e.getMessage());
        }
    }

    /**
     * Retrieves all browsing history entries ordered by most recent first.
     *
     * @return List of HistoryItems
     */
    public static List<HistoryItem> getHistory() {
        List<HistoryItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, visited_at FROM history ORDER BY id DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new HistoryItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getString("visited_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error reading browsing history: " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches history entries by title or URL keyword.
     *
     * @param keyword Search term
     * @return List of matching HistoryItems
     */
    public static List<HistoryItem> searchHistory(String keyword) {
        List<HistoryItem> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getHistory();
        }

        String sql = "SELECT id, title, url, visited_at FROM history WHERE title LIKE ? OR url LIKE ? ORDER BY id DESC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword.trim() + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HistoryItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getString("visited_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching browsing history: " + e.getMessage());
        }
        return list;
    }

    /**
     * Deletes a specific history record by ID.
     *
     * @param id History record ID
     */
    public static void deleteHistory(int id) {
        String sql = "DELETE FROM history WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting history item: " + e.getMessage());
        }
    }

    /**
     * Clears all history records from the database.
     */
    public static void clearHistory() {
        String sql = "DELETE FROM history;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing browsing history: " + e.getMessage());
        }
    }
}
