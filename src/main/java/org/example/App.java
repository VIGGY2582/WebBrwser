package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.browser.BrowserManager;
import org.example.browser.BrowserTab;
import org.example.database.BookmarkDAO;
import org.example.database.DatabaseManager;
import org.example.database.HistoryDAO;

import java.util.List;
import java.util.Optional;

public class App extends Application {

    private final BrowserManager browserManager = new BrowserManager();
    private BorderPane root;

    // History Side Panel Components
    private VBox historyPanel;
    private ListView<HistoryDAO.HistoryItem> historyListView;
    private TextField historySearchField;

    // Bookmarks Side Panel Components
    private VBox bookmarksPanel;
    private ListView<BookmarkDAO.BookmarkItem> bookmarksListView;
    private TextField bookmarksSearchField;
    private Button starButton;

    @Override
    public void start(Stage stage) {

        // Initialize SQLite local database (History & Bookmarks tables)
        DatabaseManager.initDatabase();

        // URL Field
        TextField urlField = new TextField();
        urlField.setPromptText("Search or enter web address...");
        urlField.getStyleClass().add("url-field");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        // Navigation Buttons
        Button backButton = new Button("←");
        backButton.getStyleClass().add("nav-btn");

        Button forwardButton = new Button("→");
        forwardButton.getStyleClass().add("nav-btn");

        Button reloadButton = new Button("⟳");
        reloadButton.getStyleClass().add("nav-btn");

        // Bookmark Star Toggle Button (near URL field)
        starButton = new Button("☆");
        starButton.getStyleClass().add("star-btn");
        starButton.setTooltip(new Tooltip("Bookmark this page"));

        Button goButton = new Button("Go");
        goButton.getStyleClass().add("go-btn");

        // Toolbar Action Buttons
        Button bookmarksButton = new Button("⭐ Bookmarks");
        bookmarksButton.getStyleClass().add("history-btn");

        Button historyButton = new Button("🕒 History");
        historyButton.getStyleClass().add("history-btn");

        // Loading Progress Bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        // Button and URL Action Handlers (operate on the currently active tab)
        goButton.setOnAction(e -> {
            BrowserTab active = browserManager.getActiveTab();
            if (active != null) {
                active.load(urlField.getText());
            }
        });

        urlField.setOnAction(e -> {
            BrowserTab active = browserManager.getActiveTab();
            if (active != null) {
                active.load(urlField.getText());
            }
        });

        backButton.setOnAction(e -> {
            BrowserTab active = browserManager.getActiveTab();
            if (active != null) {
                active.goBack();
            }
        });

        forwardButton.setOnAction(e -> {
            BrowserTab active = browserManager.getActiveTab();
            if (active != null) {
                active.goForward();
            }
        });

        reloadButton.setOnAction(e -> {
            BrowserTab active = browserManager.getActiveTab();
            if (active != null) {
                active.reload();
            }
        });

        // Bookmark Star Toggle Action
        starButton.setOnAction(e -> handleBookmarkToggle());

        // Side Panels Toggle Actions
        bookmarksButton.setOnAction(e -> toggleBookmarksPanel());
        historyButton.setOnAction(e -> toggleHistoryPanel());

        // Navigation Bar Layout
        HBox navBar = new HBox(
                8,
                backButton,
                forwardButton,
                reloadButton,
                urlField,
                starButton,
                goButton,
                bookmarksButton,
                historyButton
        );
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(8, 14, 8, 14));

        // Top Container: Tab Bar + Navigation Bar + Progress Bar
        VBox topContainer = new VBox(browserManager.getTabBar(), navBar, progressBar);

        // Main Layout with persistent webViewContainer in Center
        root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(browserManager.getWebViewContainer());

        // Build the Side Panels
        initHistoryPanel();
        initBookmarksPanel();

        // Change listeners to dynamically sync window title, URL bar, and Star button with the active tab
        ChangeListener<String> titleListener = (obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                stage.setTitle(newTitle + " - StudySphere Browser");
            } else {
                stage.setTitle("StudySphere Browser");
            }
        };

        ChangeListener<String> urlListener = (obs, oldUrl, newUrl) -> {
            urlField.setText(newUrl != null ? newUrl : "");
            updateStarButtonState();
        };

        // Active Tab Switch Listener
        browserManager.activeTabProperty().addListener((obs, oldTab, newTab) -> {
            if (oldTab != null) {
                oldTab.titleProperty().removeListener(titleListener);
                oldTab.urlProperty().removeListener(urlListener);
                progressBar.progressProperty().unbind();
                progressBar.visibleProperty().unbind();
            }

            if (newTab != null) {
                // Update URL field, star button, and window title to match new active tab
                urlField.setText(newTab.getUrl());
                updateStarButtonState();
                titleListener.changed(newTab.titleProperty(), null, newTab.getTitle());

                // Attach listeners for live updates during navigation
                newTab.titleProperty().addListener(titleListener);
                newTab.urlProperty().addListener(urlListener);

                // Bind progress bar to active tab's WebEngine load worker
                progressBar.progressProperty().bind(newTab.getWebEngine().getLoadWorker().progressProperty());
                progressBar.visibleProperty().bind(newTab.getWebEngine().getLoadWorker().runningProperty());
            }
        });

        // Scene & Window setup
        Scene scene = new Scene(root, 1200, 800);

        // Load external stylesheet
        try {
            var cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }

        stage.setScene(scene);
        stage.setTitle("StudySphere Browser");
        stage.show();

        // Create the initial default tab after stage is shown and sized
        Platform.runLater(browserManager::createNewTab);
    }

    /**
     * Toggles bookmarking for the currently active tab URL.
     */
    private void handleBookmarkToggle() {
        BrowserTab active = browserManager.getActiveTab();
        if (active == null) return;

        String url = active.getUrl();
        String title = active.getTitle();

        if (url == null || url.trim().isEmpty() || url.startsWith("data:") || url.equals("about:blank")) {
            return;
        }

        if (BookmarkDAO.isBookmarked(url)) {
            BookmarkDAO.deleteBookmarkByUrl(url);
        } else {
            BookmarkDAO.addBookmark(title, url);
        }

        updateStarButtonState();
        if (root.getRight() == bookmarksPanel) {
            loadBookmarksData(bookmarksSearchField.getText());
        }
    }

    /**
     * Updates the Star button icon and style based on the active tab's URL bookmark status.
     */
    private void updateStarButtonState() {
        BrowserTab active = browserManager.getActiveTab();
        if (active == null) {
            starButton.setText("☆");
            starButton.getStyleClass().remove("bookmarked");
            return;
        }

        String url = active.getUrl();
        if (url != null && !url.trim().isEmpty() && BookmarkDAO.isBookmarked(url)) {
            starButton.setText("★");
            if (!starButton.getStyleClass().contains("bookmarked")) {
                starButton.getStyleClass().add("bookmarked");
            }
            starButton.setTooltip(new Tooltip("Remove from Bookmarks"));
        } else {
            starButton.setText("☆");
            starButton.getStyleClass().remove("bookmarked");
            starButton.setTooltip(new Tooltip("Bookmark this page"));
        }
    }

    /**
     * Initializes the Bookmarks Side Panel UI.
     */
    private void initBookmarksPanel() {
        bookmarksPanel = new VBox(10);
        bookmarksPanel.setPrefWidth(340);
        bookmarksPanel.setMaxWidth(380);
        bookmarksPanel.getStyleClass().add("history-panel");
        bookmarksPanel.setPadding(new Insets(14));

        // Header
        Label headerTitle = new Label("Saved Bookmarks");
        headerTitle.getStyleClass().add("history-header-title");
        HBox.setHgrow(headerTitle, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("history-close-btn");
        closeBtn.setOnAction(e -> root.setRight(null));

        HBox header = new HBox(8, headerTitle, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // Search Field
        bookmarksSearchField = new TextField();
        bookmarksSearchField.setPromptText("Search bookmarks (title or URL)...");
        bookmarksSearchField.getStyleClass().add("history-search-field");
        bookmarksSearchField.textProperty().addListener((obs, oldV, newV) -> loadBookmarksData(newV));

        // Bookmarks List
        bookmarksListView = new ListView<>();
        bookmarksListView.getStyleClass().add("history-list-view");
        bookmarksListView.setPlaceholder(new Label("No bookmarks saved yet."));
        VBox.setVgrow(bookmarksListView, Priority.ALWAYS);

        // Custom Cell Factory for Bookmark Items
        bookmarksListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookmarkDAO.BookmarkItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox contentBox = new VBox(3);
                    HBox.setHgrow(contentBox, Priority.ALWAYS);

                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("history-item-title");

                    Label urlLabel = new Label(item.getUrl());
                    urlLabel.getStyleClass().add("history-item-url");

                    Label timeLabel = new Label(item.getCreatedAt());
                    timeLabel.getStyleClass().add("history-item-time");

                    contentBox.getChildren().addAll(titleLabel, urlLabel, timeLabel);

                    Button deleteBtn = new Button("✕");
                    deleteBtn.getStyleClass().add("history-delete-item-btn");
                    deleteBtn.setTooltip(new Tooltip("Delete bookmark"));
                    deleteBtn.setOnAction(ev -> {
                        ev.consume();
                        BookmarkDAO.deleteBookmark(item.getId());
                        loadBookmarksData(bookmarksSearchField.getText());
                        updateStarButtonState();
                    });

                    HBox row = new HBox(6, contentBox, deleteBtn);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("history-row");

                    // Clicking row opens URL in the currently active tab
                    row.setOnMouseClicked(ev -> {
                        if (ev.getTarget() != deleteBtn) {
                            BrowserTab active = browserManager.getActiveTab();
                            if (active != null) {
                                active.load(item.getUrl());
                            }
                        }
                    });

                    setGraphic(row);
                    setText(null);
                }
            }
        });

        bookmarksPanel.getChildren().addAll(header, bookmarksSearchField, bookmarksListView);
    }

    /**
     * Toggles the Bookmarks side panel on/off.
     */
    private void toggleBookmarksPanel() {
        if (root.getRight() == bookmarksPanel) {
            root.setRight(null);
        } else {
            loadBookmarksData(bookmarksSearchField.getText());
            root.setRight(bookmarksPanel);
            bookmarksSearchField.requestFocus();
        }
    }

    /**
     * Loads/filters bookmarks records from SQLite database into the list view.
     */
    private void loadBookmarksData(String keyword) {
        List<BookmarkDAO.BookmarkItem> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = BookmarkDAO.getBookmarks();
        } else {
            list = BookmarkDAO.searchBookmarks(keyword);
        }
        bookmarksListView.setItems(FXCollections.observableArrayList(list));
    }

    /**
     * Initializes the History Side Panel UI.
     */
    private void initHistoryPanel() {
        historyPanel = new VBox(10);
        historyPanel.setPrefWidth(340);
        historyPanel.setMaxWidth(380);
        historyPanel.getStyleClass().add("history-panel");
        historyPanel.setPadding(new Insets(14));

        // Header
        Label headerTitle = new Label("Browsing History");
        headerTitle.getStyleClass().add("history-header-title");
        HBox.setHgrow(headerTitle, Priority.ALWAYS);

        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("history-clear-btn");
        clearBtn.setOnAction(e -> handleClearHistory());

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("history-close-btn");
        closeBtn.setOnAction(e -> root.setRight(null));

        HBox header = new HBox(8, headerTitle, clearBtn, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        // Search Field
        historySearchField = new TextField();
        historySearchField.setPromptText("Search history (title or URL)...");
        historySearchField.getStyleClass().add("history-search-field");
        historySearchField.textProperty().addListener((obs, oldV, newV) -> loadHistoryData(newV));

        // History List
        historyListView = new ListView<>();
        historyListView.getStyleClass().add("history-list-view");
        historyListView.setPlaceholder(new Label("No browsing history found."));
        VBox.setVgrow(historyListView, Priority.ALWAYS);

        // Custom Cell Factory for History Items
        historyListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(HistoryDAO.HistoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox contentBox = new VBox(3);
                    HBox.setHgrow(contentBox, Priority.ALWAYS);

                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.getStyleClass().add("history-item-title");

                    Label urlLabel = new Label(item.getUrl());
                    urlLabel.getStyleClass().add("history-item-url");

                    Label timeLabel = new Label(item.getVisitedAt());
                    timeLabel.getStyleClass().add("history-item-time");

                    contentBox.getChildren().addAll(titleLabel, urlLabel, timeLabel);

                    Button deleteBtn = new Button("✕");
                    deleteBtn.getStyleClass().add("history-delete-item-btn");
                    deleteBtn.setTooltip(new Tooltip("Delete entry"));
                    deleteBtn.setOnAction(ev -> {
                        ev.consume();
                        HistoryDAO.deleteHistory(item.getId());
                        loadHistoryData(historySearchField.getText());
                    });

                    HBox row = new HBox(6, contentBox, deleteBtn);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.getStyleClass().add("history-row");

                    // Clicking row opens URL in the currently active tab
                    row.setOnMouseClicked(ev -> {
                        if (ev.getTarget() != deleteBtn) {
                            BrowserTab active = browserManager.getActiveTab();
                            if (active != null) {
                                active.load(item.getUrl());
                            }
                        }
                    });

                    setGraphic(row);
                    setText(null);
                }
            }
        });

        historyPanel.getChildren().addAll(header, historySearchField, historyListView);
    }

    /**
     * Toggles the History side panel on/off.
     */
    private void toggleHistoryPanel() {
        if (root.getRight() == historyPanel) {
            root.setRight(null);
        } else {
            loadHistoryData(historySearchField.getText());
            root.setRight(historyPanel);
            historySearchField.requestFocus();
        }
    }

    /**
     * Loads/filters history records from SQLite database into the list view.
     */
    private void loadHistoryData(String keyword) {
        List<HistoryDAO.HistoryItem> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = HistoryDAO.getHistory();
        } else {
            list = HistoryDAO.searchHistory(keyword);
        }
        historyListView.setItems(FXCollections.observableArrayList(list));
    }

    /**
     * Clears all history after a user confirmation dialog.
     */
    private void handleClearHistory() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear History");
        confirm.setHeaderText("Clear all browsing history?");
        confirm.setContentText("This action will permanently delete all history records from the local database.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HistoryDAO.clearHistory();
            loadHistoryData(null);
        }
    }

    /**
     * Provides access to the BrowserManager for future modules (Notes, AI Assistant, etc.)
     */
    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    public static void main(String[] args) {
        launch();
    }
}