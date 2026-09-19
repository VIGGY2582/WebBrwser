package org.example.browser;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Represents a single browser tab encapsulating its own WebView, WebEngine,
 * and navigation/state properties.
 */
public class BrowserTab {

    public static final String DEFAULT_HOME_HTML =
            "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<title>StudySphere - Web Browser</title>" +
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
            "  <div class='logo'>StudySphere</div>" +
            "  <p class='subtitle'>Smart, lightweight browser for students & researchers.</p>" +
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
            "  <div class='footer'>Type any URL or search query in the address bar above.</div>" +
            "</div>" +
            "</body>" +
            "</html>";

    private final WebView webView;
    private final WebEngine webEngine;
    private final StringProperty title = new SimpleStringProperty("StudySphere");
    private final StringProperty url = new SimpleStringProperty("");

    public BrowserTab() {
        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        initListeners();
        loadHome();
    }

    public BrowserTab(String initialUrl) {
        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        initListeners();
        if (initialUrl == null || initialUrl.trim().isEmpty()) {
            loadHome();
        } else {
            load(initialUrl);
        }
    }

    private void initListeners() {
        // Dynamic title synchronization
        webEngine.titleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                title.set(newVal.trim());
            } else {
                title.set("StudySphere");
            }
        });

        // Dynamic URL synchronization
        webEngine.locationProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.startsWith("data:")) {
                url.set(newVal);
            } else {
                url.set("");
            }
        });
    }

    public void load(String targetUrl) {
        if (targetUrl == null) return;
        String trimmed = targetUrl.trim();
        if (trimmed.isEmpty()) return;

        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }
        webEngine.load(trimmed);
    }

    public void reload() {
        webEngine.reload();
    }

    public void goBack() {
        try {
            webEngine.getHistory().go(-1);
        } catch (Exception ignored) {
        }
    }

    public void goForward() {
        try {
            webEngine.getHistory().go(1);
        } catch (Exception ignored) {
        }
    }

    public void loadHome() {
        webEngine.loadContent(DEFAULT_HOME_HTML);
        title.set("StudySphere");
        url.set("");
    }

    // --- Getters for core components and properties ---

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getUrl() {
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }
}
