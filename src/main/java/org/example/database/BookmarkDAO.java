package org.example.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling bookmark CRUD operations in SQLite.
 */
public class BookmarkDAO {

    /**
     * Data model representing a saved bookmark entry.
     */
    public static class BookmarkItem {
        private final int id;
        private final String title;
        private final String url;
        private final String createdAt;

        public BookmarkItem(int id, String title, String url, String createdAt) {
            this.id = id;
            this.title = (title != null && !title.trim().isEmpty()) ? title.trim() : url;
            this.url = url;
            this.createdAt = createdAt != null ? createdAt : "";
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

        public String getCreatedAt() {
            return createdAt;
        }

        @Override
        public String toString() {
            return title + " (" + url + ")";
        }
    }

    /**
     * Adds a bookmark to SQLite. If the URL is already bookmarked, ignores the duplicate.
     *
     * @param title Page title
     * @param url   Page URL
     * @return true if inserted, false otherwise
     */
    public static boolean addBookmark(String title, String url) {
        if (url == null || url.trim().isEmpty() || url.startsWith("data:") || url.equals("about:blank")) {
            return false;
        }

        String sql = "INSERT OR IGNORE INTO bookmarks (title, url, created_at) VALUES (?, ?, datetime('now', 'localtime'));";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String sanitizedTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : url;
            pstmt.setString(1, sanitizedTitle);
            pstmt.setString(2, url.trim());
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving bookmark: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a given URL is already saved in bookmarks.
     *
     * @param url Page URL
     * @return true if bookmarked, false otherwise
     */
    public static boolean isBookmarked(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM bookmarks WHERE url = ? LIMIT 1;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, url.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking bookmark status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all bookmarks ordered by most recent first.
     *
     * @return List of BookmarkItems
     */
    public static List<BookmarkItem> getBookmarks() {
        List<BookmarkItem> list = new ArrayList<>();
        String sql = "SELECT id, title, url, created_at FROM bookmarks ORDER BY id DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new BookmarkItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error reading bookmarks: " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches bookmarks matching a keyword in title or URL.
     *
     * @param keyword Search query
     * @return List of matching BookmarkItems
     */
    public static List<BookmarkItem> searchBookmarks(String keyword) {
        List<BookmarkItem> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getBookmarks();
        }

        String sql = "SELECT id, title, url, created_at FROM bookmarks WHERE title LIKE ? OR url LIKE ? ORDER BY id DESC;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword.trim() + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookmarkItem(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("url"),
                            rs.getString("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching bookmarks: " + e.getMessage());
        }
        return list;
    }

    /**
     * Deletes a bookmark record by ID.
     *
     * @param id Bookmark record ID
     */
    public static void deleteBookmark(int id) {
        String sql = "DELETE FROM bookmarks WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting bookmark: " + e.getMessage());
        }
    }

    /**
     * Deletes a bookmark record by URL.
     *
     * @param url Page URL
     */
    public static void deleteBookmarkByUrl(String url) {
        if (url == null || url.trim().isEmpty()) return;

        String sql = "DELETE FROM bookmarks WHERE url = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, url.trim());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting bookmark by URL: " + e.getMessage());
        }
    }
}
