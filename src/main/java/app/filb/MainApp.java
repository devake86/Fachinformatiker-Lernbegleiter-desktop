package app.filb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Einstiegspunkt der JavaFX-Desktop-App.
// Erbt von Application, um JavaFX-Lifecycle, Stage, Scene und den UI-Start zu nutzen.
public class MainApp extends Application {
    private static final String FONT_JETBRAINS_MONO_BOLD = "/font/jetbrainsmono_bold.ttf";
    private static final String FONT_JETBRAINS_MONO_BOLD_ITALIC = "/font/jetbrainsmono_bolditalic.ttf";
    private static final String FONT_JETBRAINS_MONO_ITALIC = "/font/jetbrainsmono_italic.ttf";
    private static final String FONT_JETBRAINS_MONO_REGULAR = "/font/jetbrainsmono_regular.ttf";

    private static final int FONT_LOAD_SIZE = 16;

    private static final String MENU_VIEW_FXML = "/fxml/menu_view.fxml";
    private static final int APP_WIDTH = 560;
    private static final int APP_HEIGHT = 860;

    private static final String APP_TITLE = "Fachinformatiker Lernbegleiter";

    @Override
    public void start(Stage stage) throws Exception {
        // JetBrains Mono für die gesamte App laden.
        Font.loadFont(getClass().getResourceAsStream(FONT_JETBRAINS_MONO_BOLD), FONT_LOAD_SIZE);
        Font.loadFont(getClass().getResourceAsStream(FONT_JETBRAINS_MONO_BOLD_ITALIC), FONT_LOAD_SIZE);
        Font.loadFont(getClass().getResourceAsStream(FONT_JETBRAINS_MONO_ITALIC), FONT_LOAD_SIZE);
        Font.loadFont(getClass().getResourceAsStream(FONT_JETBRAINS_MONO_REGULAR), FONT_LOAD_SIZE);

        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource(MENU_VIEW_FXML));
        Parent menuRoot = menuLoader.load();

        Scene scene = new Scene(menuRoot, APP_WIDTH, APP_HEIGHT);

        stage.setScene(scene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}