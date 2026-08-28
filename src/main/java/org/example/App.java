package org.example;

import javafx.application.Application;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        // Browser View
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // URL Field
        TextField urlField = new TextField();
        urlField.setPromptText("Search or enter web address...");
        urlField.getStyleClass().add("url-field");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        // Buttons
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
        progressBar.progressProperty().bind(engine.getLoadWorker().progressProperty());
        progressBar.visibleProperty().bind(engine.getLoadWorker().runningProperty());

        // Go Button Action
        goButton.setOnAction(e -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) return;

            if (!url.startsWith("http://") &&
                    !url.startsWith("https://")) {
                url = "https://" + url;
            }

            engine.load(url);
        });

        // Enter Key Support
        urlField.setOnAction(e -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) return;

            if (!url.startsWith("http://") &&
                    !url.startsWith("https://")) {
                url = "https://" + url;
            }

            engine.load(url);
        });

        // Back Button
        backButton.setOnAction(e -> {
            try {
                engine.getHistory().go(-1);
            } catch (Exception ignored) {
            }
        });

        // Forward Button
        forwardButton.setOnAction(e -> {
            try {
                engine.getHistory().go(1);
            } catch (Exception ignored) {
            }
        });

        // Reload Button
        reloadButton.setOnAction(e -> engine.reload());

        // Update URL Bar when page changes
        engine.locationProperty().addListener(
                (obs, oldLocation, newLocation) -> {
                    if (newLocation != null && !newLocation.startsWith("data:")) {
                        urlField.setText(newLocation);
                    }
                }
        );

        // Update Window Title
        engine.titleProperty().addListener(
                (obs, oldTitle, newTitle) -> {
                    if (newTitle != null && !newTitle.trim().isEmpty()) {
                        stage.setTitle(newTitle + " - Basic Web Browser");
                    } else {
                        stage.setTitle("Basic Web Browser");
                    }
                }
        );

        // Navigation Bar
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

        // Top Container with Navigation Bar and Loading Progress
        VBox topContainer = new VBox(navBar, progressBar);

        // Main Layout
        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(webView);

        // Scene
        Scene scene = new Scene(root, 1200, 800);

        // Load external stylesheet if present
        try {
            var cssUrl = getClass().getResource("/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }

        stage.setScene(scene);
        stage.setTitle("Basic Web Browser");
        stage.show();

        // Default Home Page with modern styling & quick shortcuts
        engine.loadContent(
                "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Welcome to Web Browser</title>" +
                "<style>" +
                "  * { box-sizing: border-box; margin: 0; padding: 0; }" +
                "  body {" +
                "    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;" +
                "    background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);" +
                "    color: #f8fafc;" +
                "    min-height: 100vh;" +
                "    display: flex;" +
                "    flex-direction: column;" +
                "    align-items: center;" +
                "    justify-content: center;" +
                "    padding: 40px 20px;" +
                "  }" +
                "  .container {" +
                "    max-width: 800px;" +
                "    width: 100%;" +
                "    text-align: center;" +
                "    animation: fadeIn 0.8s ease-out;" +
                "  }" +
                "  .logo {" +
                "    font-size: 48px;" +
                "    margin-bottom: 12px;" +
                "    display: inline-block;" +
                "    background: linear-gradient(135deg, #38bdf8, #818cf8, #c084fc);" +
                "    -webkit-background-clip: text;" +
                "    -webkit-text-fill-color: transparent;" +
                "    font-weight: 800;" +
                "    letter-spacing: -0.5px;" +
                "  }" +
                "  .subtitle {" +
                "    font-size: 18px;" +
                "    color: #94a3b8;" +
                "    margin-bottom: 40px;" +
                "  }" +
                "  .grid {" +
                "    display: grid;" +
                "    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));" +
                "    gap: 16px;" +
                "    margin-top: 20px;" +
                "  }" +
                "  .card {" +
                "    background: rgba(255, 255, 255, 0.05);" +
                "    border: 1px solid rgba(255, 255, 255, 0.1);" +
                "    border-radius: 16px;" +
                "    padding: 20px 16px;" +
                "    text-decoration: none;" +
                "    color: #f8fafc;" +
                "    display: flex;" +
                "    flex-direction: column;" +
                "    align-items: center;" +
                "    transition: all 0.25s ease;" +
                "    backdrop-filter: blur(10px);" +
                "  }" +
                "  .card:hover {" +
                "    transform: translateY(-4px);" +
                "    background: rgba(255, 255, 255, 0.1);" +
                "    border-color: rgba(96, 165, 250, 0.4);" +
                "    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 0 15px rgba(56, 189, 248, 0.2);" +
                "  }" +
                "  .card .icon {" +
                "    font-size: 28px;" +
                "    margin-bottom: 10px;" +
                "  }" +
                "  .card .title {" +
                "    font-size: 14px;" +
                "    font-weight: 600;" +
                "  }" +
                "  .card .desc {" +
                "    font-size: 11px;" +
                "    color: #94a3b8;" +
                "    margin-top: 4px;" +
                "  }" +
                "  .footer {" +
                "    margin-top: 50px;" +
                "    font-size: 13px;" +
                "    color: #64748b;" +
                "  }" +
                "  @keyframes fadeIn {" +
                "    from { opacity: 0; transform: translateY(15px); }" +
                "    to { opacity: 1; transform: translateY(0); }" +
                "  }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "  <div class='logo'>Web Browser</div>" +
                "  <p class='subtitle'>Fast, lightweight, and ready for your search.</p>" +
                "  <div class='grid'>" +
                "    <a class='card' href='https://www.google.com'>" +
                "      <div class='icon'>🔍</div>" +
                "      <div class='title'>Google</div>" +
                "      <div class='desc'>Search the web</div>" +
                "    </a>" +
                "    <a class='card' href='https://github.com'>" +
                "      <div class='icon'>💻</div>" +
                "      <div class='title'>GitHub</div>" +
                "      <div class='desc'>Build software</div>" +
                "    </a>" +
                "    <a class='card' href='https://en.wikipedia.org'>" +
                "      <div class='icon'>📚</div>" +
                "      <div class='title'>Wikipedia</div>" +
                "      <div class='desc'>Free encyclopedia</div>" +
                "    </a>" +
                "    <a class='card' href='https://news.ycombinator.com'>" +
                "      <div class='icon'>🚀</div>" +
                "      <div class='title'>Hacker News</div>" +
                "      <div class='desc'>Tech & startups</div>" +
                "    </a>" +
                "  </div>" +
                "  <div class='footer'>Type any URL or search term in the address bar above to begin.</div>" +
                "</div>" +
                "</body>" +
                "</html>"
        );
    }

    public static void main(String[] args) {
        launch();
    }
}