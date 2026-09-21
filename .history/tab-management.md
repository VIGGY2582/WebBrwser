# Module Documentation: Tab Management

---

## 1. Module Name
**Tab Management** for StudySphere / BasicWebBrowser

---

## 2. Objective
The **Tab Management** module transitions StudySphere from a single-page browser into a multi-tab web browser. It allows users to open multiple independent browsing tabs, switch between them with synchronized navigation controls, close individual tabs, and automatically maintain a usable state when the active or last tab is closed. Furthermore, it establishes a clean interface for future academic modules (AI Assistant, Bookmarks, Notes, History, Reading Mode) to interact directly with the active tab and its underlying WebEngine.

---

## 3. Existing Code Before Implementation
Prior to this module, the application operated with a single, hardcoded `WebView` and `WebEngine` directly in [App.java](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/App.java):
- **Single Page View**: Only one webpage could be loaded at a time.
- **Direct UI Binding**: Back, forward, reload, and address bar controls were directly attached to a single `WebEngine` instance.
- **No Tab Representation**: There was no tab bar, tab switching, or tab lifecycle management.

---

## 4. Changes Made

### Files Created
- [`src/main/java/org/example/browser/BrowserTab.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/browser/BrowserTab.java): Encapsulates an individual tab, its own `WebView` and `WebEngine`, navigation actions, dispose cleanup, and dynamic title/URL properties.
- [`src/main/java/org/example/browser/BrowserManager.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/browser/BrowserManager.java): Manages the collection of open tabs, persistent `StackPane` WebView container, tab switching, tab creation/closure, and renders the Tab Bar UI component.

### Files Modified
- [`src/main/java/org/example/App.java`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/java/org/example/App.java): Refactored to delegate tab management and WebView hosting to `BrowserManager`. Bound browser navigation buttons, URL text field, and progress bar dynamically to the currently selected tab.
- [`src/main/resources/styles.css`](file:///c:/Users/T-rex/Desktop/BasicWebBrowser/src/main/resources/styles.css): Added styling for the tab bar container (`.tab-bar-container`), tab scroll pane (`.tab-scroll-pane`), individual tab buttons (`.tab-item`), active tab indicator (`.tab-item.active-tab`), tab title label (`.tab-title`), close button (`.tab-close-btn`), and new tab button (`.new-tab-btn`).

### Classes Added
- `org.example.browser.BrowserTab`
- `org.example.browser.BrowserManager`

### Important Methods Added
- `BrowserTab.load(String targetUrl)`: Auto-prepends `https://` if needed and loads target URL.
- `BrowserTab.reload()`: Reloads the active tab's WebEngine.
- `BrowserTab.goBack()` & `BrowserTab.goForward()`: Navigates back and forward with history boundary safeguards.
- `BrowserTab.loadHome()`: Loads the built-in StudySphere home dashboard.
- `BrowserTab.dispose()`: Cleans up WebEngine content when a tab is closed.
- `BrowserTab.getWebView()` & `BrowserTab.getWebEngine()`: Accessors for WebView and WebEngine.
- `BrowserTab.titleProperty()` & `BrowserTab.urlProperty()`: Observable properties for real-time synchronization.
- `BrowserManager.createNewTab()` / `createNewTab(String initialUrl)`: Creates and selects a new tab, adding its WebView to the persistent container.
- `BrowserManager.closeTab(BrowserTab tab)`: Closes tab, selects adjacent tab, or creates a new default tab if the last tab is closed.
- `BrowserManager.setActiveTab(BrowserTab tab)`: Switches active tab selection and toggles WebView visibility.
- `BrowserManager.getActiveTab()` & `BrowserManager.activeTabProperty()`: Accessors for active tab.
- `BrowserManager.getTabBar()`: Returns the Tab Bar JavaFX UI component.
- `BrowserManager.getWebViewContainer()`: Returns the persistent `StackPane` holding tab WebViews.

### UI & CSS Changes
- Integrated a sleek tab bar above the existing navigation bar.
- Added a `+` button at the right end of the tab bar to quickly create new tabs.
- Added dynamic title labels on each tab with ellipsis truncation and full-text hover tooltips.
- Added red-accent hover close buttons (`✕`) on each tab.
- Distinct active tab visual state with white card elevation over light-grey inactive tabs.

---

## 5. Architecture

```text
App (JavaFX Application)
 │
 ├── Top Container (VBox)
 │    ├── Tab Bar (BrowserManager.getTabBar())
 │    │    ├── Tab ScrollPane (HBox with Tab Items & Close Buttons)
 │    │    └── New Tab Button (+)
 │    ├── Navigation Bar (Back, Forward, Reload, URL Field, Go)
 │    └── Progress Bar (Bound to active tab's LoadWorker)
 │
 ├── Center (BorderPane.setCenter)
 │    └── Persistent StackPane (BrowserManager.getWebViewContainer())
 │         ├── Tab 1 WebView [ visible=true / toFront() ]
 │         ├── Tab 2 WebView [ visible=false ]
 │         └── Tab N WebView [ visible=false ]
 │
 └── BrowserManager
      ├── activeTab: ObjectProperty<BrowserTab>
      └── tabs: ObservableList<BrowserTab>
           ├── BrowserTab (Tab 1) ── [ WebView | WebEngine | Title & URL ]
           ├── BrowserTab (Tab 2) ── [ WebView | WebEngine | Title & URL ]
           └── BrowserTab (Tab N) ── [ WebView | WebEngine | Title & URL ]
```

---

## 6. How Tab Switching Works
1. **User Interaction**: Clicking any tab in the tab bar invokes `browserManager.setActiveTab(tab)`.
2. **State Propagation**: `activeTabProperty()` notifies `App.java`'s change listener and `BrowserManager.updateActiveTabDisplay()`.
3. **Viewport Switching**: In the persistent `StackPane`, the selected tab's `WebView` is marked visible (`setVisible(true)`) and brought to front (`toFront()`), while inactive WebViews are hidden. All WebViews remain attached to the scene graph to prevent native rendering texture drops.
4. **Control Synchronization**:
   - The address bar is updated to `newTab.getUrl()`.
   - The window title is updated to `newTab.getTitle() + " - StudySphere Browser"`.
   - The progress bar is unbound from the previous tab and bound to `newTab.getWebEngine().getLoadWorker()`.
   - Listeners are attached so that subsequent page changes in this tab update the address bar and window title live.
5. **UI Styling Update**: The active tab receives the `.active-tab` style class, while all other tabs revert to the inactive style.

---

## 7. How Active Tab Information Is Accessed
Future modules can retrieve all details about the currently active tab using standard getters:

```java
// 1. Get the current active tab
BrowserTab currentTab = browserManager.getActiveTab();

// 2. Get its WebEngine & WebView
WebEngine engine = currentTab.getWebEngine();
WebView view = currentTab.getWebView();

// 3. Get current URL & Title
String currentUrl = currentTab.getUrl();
String currentTitle = currentTab.getTitle();

// 4. Listen to tab switches
browserManager.activeTabProperty().addListener((obs, oldTab, newTab) -> {
    // React to user switching tabs
});
```

---

## 8. Future Integration

The clean encapsulation of `BrowserTab` and `BrowserManager` enables future student features:

- **History Module**: Listen to `BrowserTab.urlProperty()` or `WebEngine.locationProperty()` to automatically record visited URLs and timestamps into SQLite.
- **Bookmarks Module**: Save the output of `browserManager.getActiveTab().getUrl()` and `getTitle()` into SQLite bookmarks.
- **Student Notes Module**: Attach note entries to specific URLs fetched from `browserManager.getActiveTab().getUrl()`.
- **AI Study Assistant**: Extract document text via `browserManager.getActiveTab().getWebEngine().executeScript("document.body.innerText")` and send it to the Gemini API for summarization, key concepts, or Q&A.
- **Reading Mode**: Inject custom CSS or extract article nodes using the active tab's `WebEngine`.

---

## 9. Testing & Verification

The following verification tests were performed and confirmed:
- **Compilation**: Verified via `mvn clean compile` — 3 source files compiled with **BUILD SUCCESS** (0 errors).
- **Create Tab**: Verified clicking `+` creates a new tab with the StudySphere start dashboard.
- **Switch Tabs**: Verified clicking tabs switches the active `WebView` and updates the URL bar and window title.
- **Independent Navigation**: Verified that browsing `github.com` in Tab 1 and `wikipedia.org` in Tab 2 maintains distinct history and states.
- **Close Tab**: Verified clicking `✕` removes the tab from the tab bar and memory.
- **Close Active Tab**: Verified closing the currently active tab automatically selects an adjacent tab.
- **Close Last Tab**: Verified closing the final remaining tab automatically spawns a fresh StudySphere home tab, preventing an empty/broken window.
- **URL Synchronization**: Verified URL field updates on link clicks and redirects for the active tab.
- **Title Synchronization**: Verified document title updates the tab title and window title dynamically.
- **Back / Forward / Reload**: Verified controls act exclusively on the selected tab.
- **Progress Bar**: Verified gradient loading bar tracks only the active tab's loading worker.

---

## 10. Known Limitations
- **Tab Drag-and-Drop**: Tabs cannot be reordered by dragging (omitted to keep code simple and avoid unnecessary third-party dependencies).
- **Tab Duplication / Detach**: Tabs cannot be detached into separate windows.
- **Persistence Across Sessions**: Open tabs are not persisted between app launches (can be implemented later with SQLite).
