// JavaFX GUI; App wird hier aufgerufen nicht mehr in Main.
// Festlegung der verschiedenen UI-Zustände und deren jeweilige Layouts.
// JavaFX gewählt für modernere Designmöglichkeiten im Vergleich zu Swing und Android App Parität.
// !Layouts für Desktop App sollen, wenn möglich, Parität mit Android App aufweisen!

// TODO: FXML Refactor

package app.ui;

import app.core.*;
import app.io.*;


// FXML
import javafx.fxml.FXML;

// Label ist für Textanzeigen; hier für Titel, Fragen, Erklärung.
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;

// Button zum erstellen von (klickbaren) Buttons.
import javafx.scene.control.Button;

// VBox / Hbox / Region / Priority
import javafx.scene.layout.*;

// Benötigt für Padding

// Text Format Optionen

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Klasse erbt von importierter Klasse Application
public class QuizController {

    // FXML Refactor
    @FXML private Label questionCounterLabel;
    @FXML private Label quizModeLabel;
    @FXML private Label questionIdLabel;

    @FXML private VBox questionPane;
    @FXML private VBox questionTextBox;
    @FXML private Label questionLabel;
    @FXML private Label resultLabel;
    @FXML private VBox explanationTextBox;
    @FXML private Label explanationLabel;
    @FXML private VBox interactionPane;
    @FXML private Button newRoundButton;
    @FXML private Button menuButton;

    @FXML private Button actionButton;


    // Klasse als Feld zur späteren Verwendung in Methoden.
    private QuizEngine quizEngine;
    private QuizLoader quizLoader;

    // Bestätigen gedrückt?
    private boolean confirmed = false;

    // Ausgewählter Antwort-Button
    private Button selectedAnswerButton;

    // Richtige Antwort Button
    private Button correctAnswerButton;

    // Alle Antwort Buttons der aktuellen Frage (zum gleichzeitigen abdunkeln bei Auswahl)
    private List<Button> answerButtons = new ArrayList<>();

    // Fragenpoolgröße für 20 Questions Modus
    private int lf20QuestionsSize = 20;

    // 20 Questions Liste von Session-Fragenpools
    private List<List<QuizQuestion>> lf20QuestionsSessionPools = new ArrayList<>();

    // Quiz Pools zum Zusammenführen von einzelnen Lernfeld JSONs für AP1
    List<QuizQuestion> lf01quizSessionPool = new ArrayList<>();
    List<QuizQuestion> lf02quizSessionPool = new ArrayList<>();
    List<QuizQuestion> lf03quizSessionPool = new ArrayList<>();
    List<QuizQuestion> lf04quizSessionPool = new ArrayList<>();
    List<QuizQuestion> lf05quizSessionPool = new ArrayList<>();
    List<QuizQuestion> lf06quizSessionPool = new ArrayList<>();

    // AP1 Quiz Runde
    List<QuizQuestion> ap1QuizRound = new ArrayList<>();

    // AP1 Gewichtung
    private int questionMultiplierAP1 = 3;

    private int takeQuestionsLF01 = 1 * questionMultiplierAP1;
    private int takeQuestionsLF02 = 3 * questionMultiplierAP1;
    private int takeQuestionsLF03 = 3 * questionMultiplierAP1;
    private int takeQuestionsLF04 = 1 * questionMultiplierAP1;
    private int takeQuestionsLF05 = 3 * questionMultiplierAP1;
    private int takeQuestionsLF06 = 1 * questionMultiplierAP1;

    // Quiz String + Setter
    private String[] jsonList = {"questions/lf-test.json"};

    public void setJsonList(String[] jsonList) {
        this.jsonList = jsonList;
    }

    // Quiz Mode + Setter
    private String quizMode = "20 Fragen";

    public void setQuizMode(String quizMode) {
        this.quizMode = quizMode;
    }

    // Hilfmethode um auf 2 Zeichen zu erhöhen, sprich z.B Frage 01/20 nicht 1/20
    private String twoDigit(int value) {
        return String.format("%02d", value);
    }



    @FXML
    public void initialize() {

        // Statusbar
        questionCounterLabel.setVisible(false);
        quizModeLabel.setVisible(false);
        questionIdLabel.setVisible(false);

        // Frage und Erklärung
        questionLabel.setVisible(false);
        explanationLabel.setVisible(false);

        // Endergebnis Screen
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);
        newRoundButton.setVisible(false);
        newRoundButton.setManaged(false);
        newRoundButton.setOnAction(onClick -> newRoundButtonBehavior());
        menuButton.setVisible(false);
        menuButton.setManaged(false);
        menuButton.setOnAction(onClick -> menuButtonBehavior());

        // Action Button
        actionButton.setVisible(false);
        actionButton.setManaged(true);
        actionButton.setOnAction(onClick -> actionButtonBehavior());

    }

    // Action Button Verhalten (Falsche Antwort: Rot, Richtige Antwort: Grün und Textwechsel mit entsprechender Weiterleitung)
    private void actionButtonBehavior() {

        // Beim Drücken von Quiz auswerten oder Nächste Frage und confirmed true
        if (confirmed) {
            // Übergang zur nächsten Frage oder Gesamtauswertung
            if (quizEngine.isLastQuestion()) {
                showResult();

            } else {
                quizEngine.nextQuestion();
                showCurrentQuestion();

            }
            return;
        }

        // Wenn nichts ausgewählt
        if (selectedAnswerButton == null) {
            return;

        }

        // Antwort prüfen
        int selectedIndex = answerButtons.indexOf(selectedAnswerButton);
        boolean correct = quizEngine.checkAnswer(selectedIndex);

        // Wenn richtig erhöhe Punktzahl
        quizEngine.answerScore(correct);

        // Erstmal alle Button auf halbe Transparenz und ggf. Farben entfernen
        for (Button answerButton : answerButtons) {
            answerButton.setOpacity(0.5);
            answerButton.getStyleClass().removeAll("button-correct", "button-wrong");
        }


        // Richtige Antwort raussuchen
        // Bei -1 starten (OutOfBounds) nicht 0 damit index 0 beim Suchen der richtigen Antwort auch wirklich die richtige ist
        // Und Absicherung gegen Crashes falls Antworten falsch gepflegt
        int correctIndex = -1;

        List<QuizAnswer> answers = quizEngine.getCurrentQuestion().getAnswers();

        for (int answerIndex = 0; answerIndex < answers.size(); answerIndex++) {
            if (answers.get(answerIndex).isCorrect()) {
                correctIndex = answerIndex;
                break;
            }
        }

        // Wenn Index von richter Antwort gefunden färbe grün
        if (correctIndex != -1) {
            correctAnswerButton = answerButtons.get(correctIndex);

            correctAnswerButton.getStyleClass().add("button-correct");
            correctAnswerButton.setOpacity(1.0);
        }

        // Gewählte Antwort falsch: Rot oder richtig: Grün
        if (correct) {
            // Richtige Antwort: grün
            selectedAnswerButton.getStyleClass().add("button-correct");

        } else {
            // Falsche Antwort: rot
            selectedAnswerButton.getStyleClass().add("button-wrong");
        }

        selectedAnswerButton.setOpacity(1.0);

        // Antwortbuttons blockieren
        for (Button answerButton : answerButtons) {
            answerButton.setDisable(true);
        }

        explanationTextBox.setVisible(true);
        explanationTextBox.setManaged(true);
        explanationLabel.setVisible(true);

        // Button Quiz auswerten, wenn letzte Frage; ansonsten Nächste Frage
        if (quizEngine.isLastQuestion()) {
            actionButton.setText("Quiz auswerten");
        } else {
            actionButton.setText("Nächste Frage");
        }

        confirmed = true;

    }

    // Neue Runde Button Verhalten
    private void newRoundButtonBehavior() {

        if ("AP1 PoC".equals(quizMode)) {

            boolean hasEnoughQuestions = true;

            // Falls nicht genug Fragen für neue Runde
            if (lf01quizSessionPool.size() < takeQuestionsLF01) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            } else if (lf02quizSessionPool.size() < takeQuestionsLF02) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            } else if (lf03quizSessionPool.size() < takeQuestionsLF03) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            } else if (lf04quizSessionPool.size() < takeQuestionsLF04) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            } else if (lf05quizSessionPool.size() < takeQuestionsLF05) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            } else if (lf06quizSessionPool.size() < takeQuestionsLF06) {
                // Nicht genug Fragen setzen
                hasEnoughQuestions = false;
            }


            // Wenn nicht genug Fragen für Neue Runde
            if (!hasEnoughQuestions) {

                // Setze Text auf fertig und mache nichts
                newRoundButton.setText("v Fertig v");
                return;
            }

            // Ansonsten neue Runde AP1PoC starten
            startAP1Round();
            return;
        }

        int remainingQuestions = 0;

        for (List<QuizQuestion> lf20QuestionSessionPool : lf20QuestionsSessionPools) {
            remainingQuestions += lf20QuestionSessionPool.size();
        }

        // Falls Fragenpoolsgröße unter gewünschter Fragenpoolgröße
        if (remainingQuestions < lf20QuestionsSize) {

            // Setze Text auf fertig und mache nichts
            newRoundButton.setText("v Fertig v");
            return;
        }

        // Ansonsten neue Runde AP1PoC starten
        startLf20QuestionsRound();

    }

    // Hauptmenü Button Verhalten
    private void menuButtonBehavior() {

        // Quiz Zustand zurücksetzen
        quizEngine = null;
        selectedAnswerButton = null;
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);
        // Fragenpools leeren
        lf01quizSessionPool.clear();
        lf02quizSessionPool.clear();
        lf03quizSessionPool.clear();
        lf04quizSessionPool.clear();
        lf05quizSessionPool.clear();
        lf06quizSessionPool.clear();

        try {
            FXMLLoader menuLoader = new FXMLLoader(getClass().getResource("/fxml/menu_view.fxml"));
            Parent menuRoot = menuLoader.load();

            // Scene aus dem Node holen, der gerade angezeigt wird
            menuButton.getScene().setRoot(menuRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Button Transparenz festlegen
    private void selectedAnswerButton(Button selectedButton) {

        // Alle Buttons abdunkeln
        for (Button button : answerButtons) {
            button.setOpacity(0.5);

        }

        // Ausgewählten Button hervorheben
        selectedButton.setOpacity(1.0);

        // Referenz merken
        selectedAnswerButton = selectedButton;

    }

    // AP1 Modus (JSONs Lernfeld 1-6)
    public void setupAP1(){




        // QuizLoader erzeugen um JSON einzulesen
        quizLoader = new QuizLoader();

        // Fragenpools leeren
        lf01quizSessionPool.clear();
        lf02quizSessionPool.clear();
        lf03quizSessionPool.clear();
        lf04quizSessionPool.clear();
        lf05quizSessionPool.clear();
        lf06quizSessionPool.clear();

        // Für jede JSON in der JSON Liste
        for (String json : jsonList) {

            // Lade JSON und Liste Fragen
            List<QuizQuestion> quizQuestions = quizLoader.load(json);

            // Unterthemen von Lernfeldern zusammenführen und in jeweiligen Lernfeldpool speichern.
            if (json.contains("lf-01")) {
                lf01quizSessionPool.addAll(quizQuestions);
            } else if (json.contains("lf-02")) {
                lf02quizSessionPool.addAll(quizQuestions);
            } else if (json.contains("lf-03")) {
                lf03quizSessionPool.addAll(quizQuestions);
            } else if (json.contains("lf-04")) {
                lf04quizSessionPool.addAll(quizQuestions);
            } else if (json.contains("lf-05")) {
                lf05quizSessionPool.addAll(quizQuestions);
            } else if (json.contains("lf-06")) {
                lf06quizSessionPool.addAll(quizQuestions);
            }

        }

        // Lernfeld Fragenpools mischen
        Collections.shuffle(lf01quizSessionPool);
        Collections.shuffle(lf02quizSessionPool);
        Collections.shuffle(lf03quizSessionPool);
        Collections.shuffle(lf04quizSessionPool);
        Collections.shuffle(lf05quizSessionPool);
        Collections.shuffle(lf06quizSessionPool);

        // Erste AP1 PoC Runde aus Session-Fragenpool starten
        startAP1Round();

    }

    // AP1 Gewichtung LF1-6 1:3:3:1:3:1 (Grundlage: Lernfeldwochen) *3 für 36 Fragen (vergleichbar mit großer LZK)
    private void startAP1Round() {

        ap1QuizRound.clear();

        // Check ob Fragenpool groß genug
        if (lf01quizSessionPool.size() >= takeQuestionsLF01) {
            // X Anzahl Fragen aus Quiz Pool der AP1 Quiz Runde zufügen
            ap1QuizRound.addAll(lf01quizSessionPool.subList(0, takeQuestionsLF01));
            // X Anzahl der zugefügten Fragen aus Pool entfernen
            lf01quizSessionPool.subList(0, takeQuestionsLF01).clear();
        }
        if (lf02quizSessionPool.size() >= takeQuestionsLF02) {
            ap1QuizRound.addAll(lf02quizSessionPool.subList(0, takeQuestionsLF02));
            lf02quizSessionPool.subList(0, takeQuestionsLF02).clear();
        }
        if (lf03quizSessionPool.size() >= takeQuestionsLF03) {
            ap1QuizRound.addAll(lf03quizSessionPool.subList(0, takeQuestionsLF03));
            lf03quizSessionPool.subList(0, takeQuestionsLF03).clear();
        }
        if (lf04quizSessionPool.size() >= takeQuestionsLF04) {
            ap1QuizRound.addAll(lf04quizSessionPool.subList(0, takeQuestionsLF04));
            lf04quizSessionPool.subList(0, takeQuestionsLF04).clear();
        }
        if (lf05quizSessionPool.size() >= takeQuestionsLF05) {
            ap1QuizRound.addAll(lf05quizSessionPool.subList(0, takeQuestionsLF05));
            lf05quizSessionPool.subList(0, takeQuestionsLF05).clear();
        }
        if (lf06quizSessionPool.size() >= takeQuestionsLF06) {
            ap1QuizRound.addAll(lf06quizSessionPool.subList(0, takeQuestionsLF06));
            lf06quizSessionPool.subList(0, takeQuestionsLF06).clear();
        }

        // Gezogene Rundenfragen mischen
        Collections.shuffle(ap1QuizRound);

        quizEngine = new QuizEngine(ap1QuizRound);


        showCurrentQuestion();

    }

    // LF 20 Questions Modus Fragenpool vorbereiten
    public void setupLf20Questions(){


        // QuizLoader erzeugen um JSON einzulesen
        quizLoader = new QuizLoader();

        // Fragenpool leeren
        lf20QuestionsSessionPools.clear();

        // Für jede JSON in der JSON Liste
        for (String json : jsonList) {

            // Lade JSON und Liste Fragen
            List<QuizQuestion> quizQuestions = quizLoader.load(json);

            // Solange es noch Fragen gibt
            if (!quizQuestions.isEmpty()) {

                // Mische die Liste
                Collections.shuffle(quizQuestions);

                // Packe die gemischte Liste in den Fragenpool
                lf20QuestionsSessionPools.add(new ArrayList<>(quizQuestions));

            }

        }

        // Fragenlistenpool nochmal mischen damit die erste nicht immer an erster Stelle
        Collections.shuffle(lf20QuestionsSessionPools);

        // Erste AP1 PoC Runde aus Session-Fragenpool starten
        startLf20QuestionsRound();

    }

    private void startLf20QuestionsRound() {

        List<QuizQuestion> lf20QuestionsQuizRound = new ArrayList<>();

        // Solang Größe der Fragenrunde kleiner gewünschter Fragenpoolgröße
        while (lf20QuestionsQuizRound.size() < lf20QuestionsSize) {


            boolean stillHasQuestions = false;

            // Quizrunde befüllen "Round Robin"-Style bis Fragepool voll
            for (List<QuizQuestion> lf20QuestionsSessionPool : lf20QuestionsSessionPools) {

                // Wenn die Runde voll ist, ziehe keine weiteren Fragen mehr
                if (lf20QuestionsQuizRound.size() >= lf20QuestionsSize) {
                    break;
                }

                // Wenn Fragenpool nicht leer
                if (!lf20QuestionsSessionPool.isEmpty()) {
                    // Ziehe Frage aus index 0 des Fragenpools, entferne diese aus dem Fragenpool und packe sie in Quizrunde
                    lf20QuestionsQuizRound.add(lf20QuestionsSessionPool.remove(0));
                    stillHasQuestions = true;
                }
            }

            // Wenn nicht genug Fragen für Runde dann abbrechen
            if (!stillHasQuestions) {
                break;
            }

        }

        // Gezogene Rundenfragen mischen
        Collections.shuffle(lf20QuestionsQuizRound);

        quizEngine = new QuizEngine(lf20QuestionsQuizRound);


        showCurrentQuestion();

    }

    private void showCurrentQuestion() {

        // Statusbartext anzeigen
        questionCounterLabel.setVisible(true);
        quizModeLabel.setVisible(true);
        questionIdLabel.setVisible(true);

        // Ergebnistext ausblenden
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        // Ergebnis Screen Buttons ausschalten
        newRoundButton.setVisible(false);
        newRoundButton.setManaged(false);
        menuButton.setVisible(false);
        menuButton.setManaged(false);

        // Frage anzeigen
        questionTextBox.setVisible(true);
        questionLabel.setVisible(true);

        // Erklärung ausblenden aber Platz reservieren
        explanationTextBox.setVisible(false);
        explanationTextBox.setManaged(true);
        explanationLabel.setVisible(false);


        // Antwort Buttons zurücksetzen
        interactionPane.getChildren().clear();
        answerButtons.clear();

        // Rücksetzen auf Antwortphase.
        selectedAnswerButton = null;
        correctAnswerButton = null;
        confirmed = false;

        // Action Button verstecken und benennen
        actionButton.setVisible(false);
        actionButton.setManaged(true);
        actionButton.setText("Bestätigen");

        // Aktueller Fragen Index und Gesamtzahl Fragenpool für Statusbar Anzeige
        int currentIndex = quizEngine.getCurrentIndex() + 1;
        int totalQuestions = quizEngine.getQuestionCount();

        // Index und Gesamtzahl zusammensetzen Format z.B. Frage 01/20
        questionCounterLabel.setText("Frage " + twoDigit(currentIndex) + "/" + twoDigit(totalQuestions));

        // Quiz Mode Anzeige
        quizModeLabel.setText(quizMode);

        // Fragen ID holen
        questionIdLabel.setText(quizEngine.getCurrentQuestion().getId());

        // Aktuelle Frage holen.
        QuizQuestion question = quizEngine.getCurrentQuestion();

        // Fragetext erzeugen.
        questionLabel.setText(question.getQuestion());

        // Erklärungstext erzeugen.
        explanationLabel.setText(question.getExplanation());

        if (question.getAnswers().size() > 2) {
            Collections.shuffle(question.getAnswers());
        }

        // Antwort-Buttons dynamisch (2-4) erzeugen
        // Für Antworten aus Antwortenpool
        for (QuizAnswer answer : question.getAnswers()) {

            // Erzeuge Button pro Antwort
            Button answerButton = new Button(answer.getText());
            answerButton.setMaxWidth(Double.MAX_VALUE);
            answerButton.setStyle("-fx-font-size: 14px;");

            answerButtons.add(answerButton);

            answerButton.setOnAction(onClick -> {

                // Methode für Button Transparenz aufrufen
                selectedAnswerButton(answerButton);

                // Action Button anzeigen
                actionButton.setVisible(true);
                actionButton.setManaged(true);
            });

            interactionPane.getChildren().add(answerButton);

        }

        // Alle Buttons abdunkeln damit keiner vorausgewählt erscheint
        for (Button button : answerButtons) {
            button.setOpacity(1.0);

        }

    }

    // Gesamtergebnis Bildschirm
    private void showResult() {

        //Statusbartext ausblenden
        questionCounterLabel.setVisible(false);
        questionIdLabel.setVisible(false);

        // Action Button ausblenden
        actionButton.setVisible(false);

        // Fragen ausblenden
        questionLabel.setVisible(false);

        // Erklärung ausblenden
        explanationTextBox.setVisible(false);
        explanationTextBox.setManaged(true);
        explanationLabel.setVisible(false);

        // Gesamtergebnis Text anzeigen und festlegen
        resultLabel.setVisible(true);
        resultLabel.setManaged(true);
        resultLabel.setText("Ergebnis: " + twoDigit(quizEngine.getScore()) + "/" + twoDigit(quizEngine.getQuestionCount()));

        // Neue Runde Button
        newRoundButton.setVisible(true);
        newRoundButton.setManaged(true);

        menuButton.setVisible(true);
        menuButton.setManaged(true);

        interactionPane.getChildren().setAll(newRoundButton, menuButton);

    }



}
