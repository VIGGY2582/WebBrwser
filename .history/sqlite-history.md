# Module Documentation: SQLite Database + Browsing History

---

## 1. Module Name
**SQLite Database + Browsing History** for StudySphere / BasicWebBrowser

---

## 2. Objective
The purpose of this module is to introduce a lightweight, embedded local SQLite database and persistent browsing history for StudySphere. This allows the browser to automatically record all visited webpages across multiple tabs, store their URLs, titles, and visit timestamps locally, and provide students with a fast search and history management panel that persists across application restarts. It also establishes the database foundation for future modules (Bookmarks, Notes, Settings, AI Study Assistant).

---

## 3. Previous State
Prior to this module, the browser supported session-based tab navigation (Back, Forward, Reload) via the in-memory `WebEngine` history stack. However, visited URLs were lost as soon as a tab was closed or the application exited. There was no persistent database or history view.

---

## 4. Technology
- **Language / Platform**: Java 21 LTS
- **UI Toolkit**: JavaFX 21 (`javafx-controls`, `javafx-web`)
- **Database**: SQLite (embedded, zero-configuration local file storage)
- **Database Driver**: `org.xerial:sqlite-jdbc` (3.45.1.0)
- **Data Access API**: Java Database Connectivity (JDBC) with `PreparedStatement`
- **Build System**: Maven

---

## 5. Database Structure

Database Location: `data/studysphere.db` (auto-created relative to application root)

### `history` Table Schema:
```sql
CREATE TABLE IF NOT EXISTS history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    url TEXT,
    visited_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | `INTEGER` | Auto-incrementing primary key |
| `title` | `TEXT` | Webpage title captured from `WebEngine.getTitle()` |
| `url` | `TEXT` | Full destination URL (`https://...`) |
| `visited_at` | `DATETIME` | Timestamp of page visit (local time) |

---

## 6. Files Created
1. [`src/main/java/org/example/database/DatabaseManager.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/database/DatabaseManager.java): Manages the SQLite database connection, creates the `data/` directory, and executes schema creation statements.
2. [`src/main/java/org/example/database/HistoryDAO.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/database/HistoryDAO.java): Data Access Object containing CRUD and search operations (`addHistory`, `getHistory`, `searchHistory`, `deleteHistory`, `clearHistory`) and the `HistoryItem` model.

---

## 7. Files Modified
1. [`pom.xml`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/pom.xml): Added `org.xerial:sqlite-jdbc` dependency.
2. [`src/main/java/org/example/browser/BrowserTab.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/browser/BrowserTab.java): Added listener to `WebEngine.getLoadWorker().stateProperty()` to automatically record successful page visits into `HistoryDAO`.
3. [`src/main/java/org/example/App.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/App.java): Initialized `DatabaseManager.initDatabase()`, added the `🕒 History` toggle button, and built the collapsible History side drawer with real-time search, item deletion, and clear history.
4. [`src/main/resources/styles.css`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/resources/styles.css): Added styling for `.history-btn`, `.history-panel`, `.history-header-title`, `.history-clear-btn`, `.history-close-btn`, `.history-search-field`, `.history-list-view`, `.history-row`, and item text elements.

---

## 8. Architecture

```text
JavaFX Browser Application (App.java)
      │
      ├── BrowserManager / BrowserTab
      │        │
      │        ▼
      │    WebEngine (Page Load SUCCEEDED)
      │        │
      │        ▼
      │    HistoryDAO (addHistory / search / clear)
      │        │
      │        ▼
      │    DatabaseManager (getConnection)
      │        │
      │        ▼
      └─── SQLite Database (data/studysphere.db)
```

---

## 9. Data Flow
1. **User Navigation**: The user inputs a URL or clicks a link inside any `BrowserTab`.
2. **Page Loading**: `WebEngine` starts fetching the webpage.
3. **Completion Detection**: The worker state transitions to `Worker.State.SUCCEEDED`.
4. **Filtering**: `BrowserTab` retrieves `webEngine.getLocation()` and `webEngine.getTitle()`. Internal pages (`data:`, `about:blank`) are excluded.
5. **Persistence**: `HistoryDAO.addHistory(title, url)` executes a parameterized `INSERT` query via `PreparedStatement`.
6. **SQLite Storage**: The record is committed to `data/studysphere.db` with the current local datetime.

---

## 10. History UI
- **View History**: Clicking the `🕒 History` button in the navigation bar toggles the right-hand slide-over drawer displaying recent visits ordered newest first.
- **Search History**: Typing in the search field executes `HistoryDAO.searchHistory()` live, filtering by page title or URL keywords.
- **Open History Entry**: Clicking any item in the history list loads that URL immediately into the **currently active tab**.
- **Delete Single Item**: Clicking the `✕` delete button beside any entry removes only that record from the database.
- **Clear All History**: Clicking **Clear All** prompts a confirmation alert and wipes all records via `HistoryDAO.clearHistory()`.

---

## 11. Future Integration
The modular DAO pattern and `DatabaseManager` architecture make it straightforward to add upcoming student modules to the same database:

```text
data/studysphere.db
 ├── history (Implemented)
 ├── bookmarks (Future BookmarkDAO)
 ├── notes (Future NotesDAO)
 └── settings (Future SettingsDAO)
```

- **Bookmarks**: Can create a `bookmarks` table and `BookmarkDAO` using the same `DatabaseManager.getConnection()`.
- **Student Notes**: Can associate saved notes with specific URLs from the `history` or `bookmarks` table.
- **AI Study Assistant**: Can retrieve past visited topic URLs from `HistoryDAO` to generate study summaries or quizzes.

---

## 12. Testing & Verification
The following test scenarios were verified:
- **Database Creation**: Verified `data/studysphere.db` is created automatically on initial run.
- **Table Creation**: Verified `history` table schema is generated via `IF NOT EXISTS`.
- **Page Visit Insertion**: Verified visiting web pages inserts title, URL, and timestamp.
- **Multiple Page Visits**: Verified navigating multiple pages creates individual history entries.
- **History Persistence**: Verified history entries remain stored and viewable after restarting the application.
- **History Search**: Verified typing title or URL keywords filters the results in real time.
- **Opening History Entries**: Verified clicking a history entry navigates the active tab to that URL.
- **Clear History**: Verified clearing history removes all records from the database and updates the UI with the placeholder.
- **Multi-Tab Independence**: Verified visits in Tab 1 and Tab 2 are both captured into history correctly.
- **Core Browser Features**: Verified tabs, Back, Forward, Reload, URL bar, Go button, home page, and progress bar remain fully operational.

---

## 13. Known Limitations
- **History Grouping by Date**: History items are displayed in a flat list ordered by most recent (grouping by "Today", "Yesterday", "Last Week" can be added in a future update).
- **Pagination**: Loads recent history records directly into the list view without page offsets (adequate for typical desktop usage).
