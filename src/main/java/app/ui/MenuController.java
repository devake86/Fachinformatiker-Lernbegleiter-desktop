package app.ui;

// Kontrolliert Lebenszyklus
import javafx.animation.PauseTransition;
import javafx.application.Platform;

// FXML und UI
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.util.Duration;
import javafx.scene.control.Label;

// Exceptions
import java.io.IOException;

public class MenuController {

    // FXML
    @FXML private BorderPane rootLayout;
    @FXML private Button ap1Button;
    @FXML private Button start20QuestionsButton;
    @FXML private Button exitButton;
    @FXML private Label titleLabel;

    // Extra Modi für beta2
    @FXML private Button startABKZButton;
    @FXML private Button startFBEGButton;

    // Easter Egg - Created by devake86 (3x Klick in 1 Sekunde auf quizModeLabel)
    @FXML private Label versionIdLabel;
    // Klickzähler
    private int creditClicks = 0;
    // Klick-Zeitfenster
    private final PauseTransition creditWindow = new PauseTransition(Duration.seconds(1));
    // Easter Egg (in-)aktiv setzen
    private boolean showingCredit = false;

    @FXML
    public void initialize() {

        // Version Id anzeigen
        versionIdLabel.setVisible(true);

        titleLabel.setVisible(true);

        // EasterEgg
        creatorEasterEgg();

        // QuizMode Weitergabe an loadQuiz
        ap1Button.setOnAction(onClick -> loadQuiz("AP1 PoC"));
        start20QuestionsButton.setOnAction(onClick -> loadQuiz("20 Fragen"));

        // Extra Modi für beta2
        startABKZButton.setOnAction(onClick -> loadQuiz("20 Fragen - ABKZ"));
        startFBEGButton.setOnAction(onClick -> loadQuiz("20 Fragen - FBEG"));

        // Beenden der App wenn Beenden-Button gedrückt.
        // setOnAction gibt an, was passieren soll wenn Button gedrückt wird.
        // Lambda-Ausdruck bei onclick (bei Klick) -> (führe folgenden Code aus) {...} (Codeblock der bei Klick ausgeführt wird).
        // normalerweise kurze Schreibweise für Ausdruck (e -> {}).
        exitButton.setOnAction(onClick -> {
            // Beenden der UI-Event-Schleife.
            // Ruft intern Application.stop() auf.
            // Alle Fenster (Stages) werden geschlossen und Java Prozess wird sauber beendet.
            Platform.exit();
        });

    }

    private void loadQuiz(String quizMode) {

        try {
            // Quiz View FXML laden für UI Aufbau und als Parent setzen
            FXMLLoader quizLoader = new FXMLLoader(getClass().getResource("/fxml/quiz_view.fxml"));
            Parent quizRoot = quizLoader.load();

            // Quiz Controller Feld setzen
            // Feld zur Verbindung zum QuizController Spielablauf
            QuizController quizController = quizLoader.getController();

            if (quizMode.equals("AP1 PoC")) {

                // Quiz Modus und JSON String setzen
                quizController.setQuizMode("AP1 PoC");
                quizController.setJsonList(new String[]{
                        "questions/lf-01.json",
                        "questions/lf-02.json",
                        "questions/lf-03.json",
                        "questions/lf-04.json",
                        "questions/lf-05.json",
                        "questions/lf-06.json",

                });

                // Quiz Modus starten
                quizController.setupAP1();

            } else if (quizMode.equals("20 Fragen")) {

                // Quiz Modus und JSON String setzen
                quizController.setQuizMode("20 Fragen - LF-VT1");
                quizController.setJsonList(new String[]{
                        "questions/lf-vt1/lf-vt1-abkz.json",
                        "questions/lf-vt1/lf-vt1-apwb.json",
                        "questions/lf-vt1/lf-vt1-bumi.json",
                        "questions/lf-vt1/lf-vt1-edak.json",
                        "questions/lf-vt1/lf-vt1-fbeg.json",
                        "questions/lf-vt1/lf-vt1-ksdb.json",
                        "questions/lf-vt1/lf-vt1-navw.json",
                        "questions/lf-vt1/lf-vt1-netz.json",
                        "questions/lf-vt1/lf-vt1-spdv.json",
                        "questions/lf-vt1/lf-vt1-trup.json",

                });

                // Quiz Modus starten
                quizController.setupLf20Questions();




            // Extra Modi für beta2
            } else if (quizMode.equals("20 Fragen - ABKZ")) {

                // Quiz Modus und JSON String setzen
                quizController.setQuizMode("20 Fragen - ABKZ");
                quizController.setJsonList(new String[]{
                        "questions/lf-vt1/lf-vt1-abkz.json",

                });

                // Quiz Modus starten
                quizController.setupLf20Questions();

            } else if (quizMode.equals("20 Fragen - FBEG")) {

                // Quiz Modus und JSON String setzen
                quizController.setQuizMode("20 Fragen - FBEG");
                quizController.setJsonList(new String[]{
                        "questions/lf-vt1/lf-vt1-fbeg.json",

                });

                // Quiz Modus starten
                quizController.setupLf20Questions();



            }




            // Altes Layout raus neues Layout rein
            rootLayout.getScene().setRoot(quizRoot);

        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }

    }

    // Easter Egg - Created by devake86 (3x Klick in 1 Sekunde auf quizModeLabel)
    private void creatorEasterEgg() {
        // Nach Zeitfenster setze Klicks zurück
        creditWindow.setOnFinished(onClick -> creditClicks = 0);
        // Was passiert bei Mausklick
        versionIdLabel.setOnMouseClicked(onClick -> {
            // Wenn Easter Egg schon angezeigt wird - abbrechen
            if (showingCredit) return;
            // Klickzähler um 1 erhöhen
            creditClicks++;
            // Stoppe alten Timer
            creditWindow.stop();
            // Starte 1 Sekunden Fenster neu
            creditWindow.playFromStart();
            // Bei mind. 3 Klicks in einer Sekunde
            if (creditClicks >= 3) {
                // Klicks zurücksetzen
                creditClicks = 0;
                // Stoppe alten Timer
                creditWindow.stop();
                // Easter Egg auf aktiv setzen
                showingCredit = true;
                // Bisherigen Text speichern
                String previousText = versionIdLabel.getText();
                // Easter Egg Text anzeigen
                versionIdLabel.setText("created by devake86");
                // Anzeige für 3 Sekunden
                PauseTransition hide = new PauseTransition(Duration.seconds(3));
                // Easter Egg wieder verschwinden lassen und
                hide.setOnFinished(ev -> {
                    // Letzten Text wieder anzeigen
                    versionIdLabel.setText(previousText);
                    // Easter Egg auf inaktiv setzen
                    showingCredit = false;
                });
                // Starte Timer
                hide.play();
            }
        });

    }

}
