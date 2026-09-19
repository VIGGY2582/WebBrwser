package org.example.browser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the collection of BrowserTabs, tab switching, tab closure,
 * and renders the Tab Bar UI component.
 */
public class BrowserManager {

    private final ObservableList<BrowserTab> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<BrowserTab> activeTab = new SimpleObjectProperty<>();

    // UI Components for the Tab Bar
    private final HBox tabContainer = new HBox(4);
    private final HBox tabBar = new HBox(6);
    private final Map<BrowserTab, HBox> tabUiMap = new HashMap<>();

    public BrowserManager() {
        initTabBarUi();
    }

    private void initTabBarUi() {
        tabContainer.setAlignment(Pos.CENTER_LEFT);

        ScrollPane scrollPane = new ScrollPane(tabContainer);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("tab-scroll-pane");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        Button newTabBtn = new Button("+");
        newTabBtn.getStyleClass().add("new-tab-btn");
        newTabBtn.setTooltip(new Tooltip("Open New Tab"));
        newTabBtn.setOnAction(e -> createNewTab());

        tabBar.setAlignment(Pos.CENTER_LEFT);
        tabBar.getStyleClass().add("tab-bar-container");
        tabBar.getChildren().addAll(scrollPane, newTabBtn);

        // Update UI styling whenever active tab changes
        activeTab.addListener((obs, oldTab, newTab) -> updateTabStyles());
    }

    public BrowserTab createNewTab() {
        return createNewTab(null);
    }

    public BrowserTab createNewTab(String initialUrl) {
        BrowserTab tab = new BrowserTab(initialUrl);
        tabs.add(tab);

        HBox tabUi = createTabUi(tab);
        tabUiMap.put(tab, tabUi);
        tabContainer.getChildren().add(tabUi);

        setActiveTab(tab);
        return tab;
    }

    public void closeTab(BrowserTab tab) {
        if (tab == null || !tabs.contains(tab)) return;

        int index = tabs.indexOf(tab);
        boolean wasActive = (tab == getActiveTab());

        // Remove from list & UI
        tabs.remove(tab);
        HBox ui = tabUiMap.remove(tab);
        if (ui != null) {
            tabContainer.getChildren().remove(ui);
        }

        // If the closed tab was active, switch to an adjacent tab
        if (wasActive) {
            if (!tabs.isEmpty()) {
                int nextIndex = Math.min(index, tabs.size() - 1);
                setActiveTab(tabs.get(nextIndex));
            } else {
                // Keep the browser usable: create a fresh home tab if the last tab was closed
                createNewTab();
            }
        } else if (tabs.isEmpty()) {
            createNewTab();
        }
    }

    private HBox createTabUi(BrowserTab tab) {
        HBox tabBox = new HBox(6);
        tabBox.setAlignment(Pos.CENTER_LEFT);
        tabBox.getStyleClass().add("tab-item");

        Label titleLabel = new Label(tab.getTitle());
        titleLabel.getStyleClass().add("tab-title");
        titleLabel.setMaxWidth(140);
        titleLabel.setEllipsisString("...");

        Tooltip tooltip = new Tooltip(tab.getTitle());
        Tooltip.install(tabBox, tooltip);

        // Update tab title and tooltip dynamically
        tab.titleProperty().addListener((obs, oldVal, newVal) -> {
            titleLabel.setText(newVal);
            tooltip.setText(newVal);
        });

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("tab-close-btn");
        closeBtn.setOnAction(e -> {
            e.consume();
            closeTab(tab);
        });

        tabBox.getChildren().addAll(titleLabel, closeBtn);

        // Switch to this tab on click
        tabBox.setOnMouseClicked(e -> setActiveTab(tab));

        return tabBox;
    }

    private void updateTabStyles() {
        BrowserTab current = getActiveTab();
        for (Map.Entry<BrowserTab, HBox> entry : tabUiMap.entrySet()) {
            HBox ui = entry.getValue();
            if (entry.getKey() == current) {
                if (!ui.getStyleClass().contains("active-tab")) {
                    ui.getStyleClass().add("active-tab");
                }
            } else {
                ui.getStyleClass().remove("active-tab");
            }
        }
    }

    public void setActiveTab(BrowserTab tab) {
        if (tab != null && tabs.contains(tab)) {
            activeTab.set(tab);
        }
    }

    public BrowserTab getActiveTab() {
        return activeTab.get();
    }

    public ObjectProperty<BrowserTab> activeTabProperty() {
        return activeTab;
    }

    public ObservableList<BrowserTab> getTabs() {
        return tabs;
    }

    public Node getTabBar() {
        return tabBar;
    }
}
