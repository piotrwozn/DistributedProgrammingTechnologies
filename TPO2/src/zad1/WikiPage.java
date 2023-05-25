package zad1;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WikiPage {
    public static void showWikiPage(String city) {
        try {
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle("Strona wikipedi dla " + city);
                BorderPane root = new BorderPane();
                Scene scene = new Scene(root, 1000, 800);
                WebView webView = new WebView();
                root.setCenter(webView);
                WebEngine webEngine = webView.getEngine();
                webEngine.load("https://en.wikipedia.org/wiki/" + city);
                stage.setScene(scene);
                stage.show();
            });
        } catch (Exception ignored) {
        }
    }
}