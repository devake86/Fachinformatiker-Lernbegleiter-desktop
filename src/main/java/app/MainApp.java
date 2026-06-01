// MainApp
// Einstiegspunkt für die App der das Main Menu lädt

package app;

// Application ist Basisklasse für JavaFX-Apps; ermöglicht launch() und beschreibt start / stop Lebenszyklus.
// Regelt die Instanz der App.
import javafx.application.Application;

// Font
import javafx.scene.text.Font;

// FXML
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

// Stage dient zum Erzeugen des Fensters; vergleichbar mit Android Activity oder Swing JFrame; Stage hat Scene.
// Scene legt den Inhaltsbereich des Fensters fest; enthält Layouts und Controls; Scene hat Nodes.
import javafx.scene.Scene;
import javafx.stage.Stage;

// CSS einbinden
import java.util.Objects;

public class MainApp extends Application {

    // @Override sagt dem Compiler „Diese Methode überschreibt eine Methode der Oberklasse“.
    // Die Methode start(Stage) von Application muss implementiert werden.
    // Kurz: Erkennt Fehler bzw. hilft bei deren Erkennung. Verwendung wird immer empfohlen.
    @Override

    // Stage = Fenster.
    public void start(Stage stage) throws Exception {

        // JetBrainsMono-Regular -Italic für die ganze App initialisieren
        Font.loadFont(getClass().getResourceAsStream("/font/jetbrainsmono_regular.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/font/jetbrainsmono_italic.ttf"), 16);

        // Main View FXML laden für UI Aufbau und als Parent setzen
        FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/fxml/menu_view.fxml"));
        Parent menuRoot = menuLoader.load();

        // Scene erzeugen mit Breite und Höhe
        Scene scene = new Scene(menuRoot, 520, 920);

        // Stage Titel und Scene zuweisen und anzeigen
        stage.setScene(scene);
        stage.setTitle("projectFIQA");
        stage.show();

        // "Dark-Theme" darkMode.css einbinden
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/darkMode.css")).toExternalForm());

    }

    // App starten "launch(args)" ruft intern "start(Start stage)" auf. main steht konventionell ganz unten.
    public static void main(String[] args) {
        launch(args);
    }

}
