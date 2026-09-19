package org.example;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.browser.BrowserManager;
import org.example.browser.BrowserTab;

public class App extends Application {

    private final BrowserManager browserManager = new BrowserManager();

    @Override
    public void start(Stage stage) {

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

        Button goButton = new Button("Go");
        goButton.getStyleClass().add("go-btn");

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

        // Navigation Bar Layout
        HBox navBar = new HBox(
                8,
                backButton,
                forwardButton,
                reloadButton,
                urlField,
                goButton
        );
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(8, 14, 8, 14));

        // Top Container: Tab Bar + Navigation Bar + Progress Bar
        VBox topContainer = new VBox(browserManager.getTabBar(), navBar, progressBar);

        // Main Layout
        BorderPane root = new BorderPane();
        root.setTop(topContainer);

        // Change listeners to dynamically sync window title and URL bar with the active tab
        ChangeListener<String> titleListener = (obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                stage.setTitle(newTitle + " - StudySphere Browser");
            } else {
                stage.setTitle("StudySphere Browser");
            }
        };

        ChangeListener<String> urlListener = (obs, oldUrl, newUrl) -> {
            urlField.setText(newUrl != null ? newUrl : "");
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
                // Update center view to the active tab's WebView
                root.setCenter(newTab.getWebView());

                // Update URL field and window title to match new active tab
                urlField.setText(newTab.getUrl());
                titleListener.changed(newTab.titleProperty(), null, newTab.getTitle());

                // Attach listeners for live updates during navigation
                newTab.titleProperty().addListener(titleListener);
                newTab.urlProperty().addListener(urlListener);

                // Bind progress bar to active tab's WebEngine load worker
                progressBar.progressProperty().bind(newTab.getWebEngine().getLoadWorker().progressProperty());
                progressBar.visibleProperty().bind(newTab.getWebEngine().getLoadWorker().runningProperty());
            }
        });

        // Create the initial default tab
        browserManager.createNewTab();

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
    }

    /**
     * Provides access to the BrowserManager for future modules (Bookmarks, History, AI Assistant, etc.)
     */
    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    public static void main(String[] args) {
        launch();
    }
}