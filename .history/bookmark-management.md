# Module Documentation: Bookmark Management

---

## 1. Module Name
**Bookmark Management** for StudySphere / BasicWebBrowser

---

## 2. Objective
The **Bookmark Management** module enables students and researchers to quickly save, organize, and access frequently visited academic websites, reference materials, and documentation. Bookmarks persist across browser restarts in the local SQLite database, prevent duplicate entries, and feature real-time star icon synchronization that reflects whether the active webpage is bookmarked.

---

## 3. Previous State
Prior to this module, StudySphere supported:
- Multi-Tab Management
- Local SQLite Database (`DatabaseManager`)
- Persistent Browsing History (`HistoryDAO`)

However, there was no way to bookmark important pages for quick retrieval without searching through the full browsing history.

---

## 4. Database Changes

Database Location: `data/studysphere.db` (reuses the existing database file)

### `bookmarks` Table Schema:
```sql
CREATE TABLE IF NOT EXISTS bookmarks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    url TEXT UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | `INTEGER` | Primary key, auto-incrementing |
| `title` | `TEXT` | Webpage title captured from active `WebEngine` |
| `url` | `TEXT UNIQUE` | Webpage destination URL, enforced unique to prevent duplicates |
| `created_at` | `DATETIME` | Timestamp of when the bookmark was saved |

---

## 5. Files Created
1. [`src/main/java/org/example/database/BookmarkDAO.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/database/BookmarkDAO.java): Data Access Object containing CRUD and search operations (`addBookmark`, `isBookmarked`, `getBookmarks`, `searchBookmarks`, `deleteBookmark`, `deleteBookmarkByUrl`) and the `BookmarkItem` data model.

---

## 6. Files Modified
1. [`src/main/java/org/example/database/DatabaseManager.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/database/DatabaseManager.java): Updated `initDatabase()` to create the `bookmarks` table automatically on startup alongside `history`.
2. [`src/main/java/org/example/App.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/App.java): Added the `☆`/`★` Star bookmark toggle button in the navigation bar, dynamic star state synchronization on tab switch and URL navigation, `⭐ Bookmarks` toolbar button, and the Bookmarks drawer panel with search and deletion.
3. [`src/main/resources/styles.css`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/resources/styles.css): Added styling for `.star-btn`, `.star-btn.bookmarked`, and updated shared side-panel CSS.

---

## 7. Bookmark Data Flow

```text
User clicks Star button (☆ / ★) on Active Tab
                  │
                  ▼
         Get current URL & Title
                  │
                  ▼
         BookmarkDAO.isBookmarked(url)
           ┌──────┴──────┐
        (True)        (False)
           │             │
           ▼             ▼
  deleteBookmarkByUrl   addBookmark (INSERT OR IGNORE)
           │             │
           └──────┬──────┘
                  ▼
     Update Star State (☆ / ★)
                  │
                  ▼
         SQLite Database (data/studysphere.db)
```

---

## 8. Bookmark UI
- **Add / Remove Bookmark**: Clicking the star button (`☆` when unbookmarked, `★` when bookmarked) near the address bar toggles bookmarking for the active page.
- **Bookmark State Synchronization**: The star button automatically switches between outline (`☆`) and filled gold (`★`) when switching tabs or navigating to bookmarked URLs.
- **View Bookmarks**: Clicking `⭐ Bookmarks` in the toolbar toggles the right slide-over drawer displaying all saved bookmarks.
- **Search Bookmarks**: Real-time filtering search box searches across bookmark titles and URLs.
- **Open Bookmark**: Clicking any bookmark row loads the URL directly into the **currently active tab** without creating duplicate tabs.
- **Delete Bookmark**: Clicking the `✕` delete button removes the entry from the database and immediately updates the star button if the active tab has that URL open.

---

## 9. Duplicate Prevention
Duplicate bookmarks are prevented at two layers:
1. **Database Layer**: The `url` column has a `UNIQUE` constraint and queries use `INSERT OR IGNORE`.
2. **Application Layer**: `BookmarkDAO.isBookmarked(url)` verifies presence before insertion.

---

## 10. Future Integration
The modular SQLite DAO architecture seamlessly integrates with upcoming modules:

```text
data/studysphere.db
 ├── history (HistoryDAO - Implemented)
 ├── bookmarks (BookmarkDAO - Implemented)
 ├── notes (NotesDAO - Future student notes)
 └── ai_summaries (Future AI Study Assistant)
```

- **Student Notes Module**: Students can attach personal study notes directly to bookmarked URLs.
- **AI Study Assistant**: Can fetch bookmarked research links to generate study decks, summaries, and revision flashcards.
- **Student Dashboard**: Quick access widget listing favorite bookmarks on the StudySphere home dashboard.

---

## 11. Testing & Verification
The following test scenarios were executed and verified:
- **Add Bookmark**: Verified clicking `☆` saves the page and turns the icon to `★`.
- **Duplicate Prevention**: Verified clicking bookmark repeatedly does not produce duplicate rows in SQLite.
- **Delete Bookmark**: Verified deleting from the drawer removes the record and updates the star button to `☆`.
- **Open Bookmark**: Verified clicking a bookmark opens the URL in the active tab.
- **Search Bookmarks**: Verified real-time search filters bookmarks by title and URL keywords.
- **Tab Switching & Navigation**: Verified star icon dynamically reflects bookmark status when switching between tabs with different pages.
- **Persistence Across Restarts**: Verified all bookmarks remain saved in `data/studysphere.db` after restarting the app.
- **History & Navigation Integrity**: Verified History drawer, Tab management, Back, Forward, Reload, and URL bar remain 100% operational.

---

## 12. Known Limitations
- **Bookmark Folders / Tags**: Bookmarks are listed in a flat searchable list (folder categorization can be introduced in a future update).
- **Favicons**: Displays clean text/titles rather than downloaded website favicons to keep networking lightweight.
