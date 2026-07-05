package app.filb.ui;

import app.filb.core.QuizAnswer;
import app.filb.core.QuizEngine;
import app.filb.core.QuizQuestion;
import app.filb.io.QuestionProgressRepository;
import app.filb.io.QuizLoader;
import com.google.gson.JsonSyntaxException;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QuizController {
    private static final String QUIZ_MODE_AP1 = "AP1";

    private static final String DIALOG_CONFIRMATION_FXML = "/fxml/dialog_confirmation.fxml";
    private static final String MENU_VIEW_FXML = "/fxml/menu_view.fxml";

    private static final String RESULT_TEXT_PREFIX = "Ergebnis: ";
    private static final String QUESTION_ID_PREFIX = "⧉ ID: ";
    private static final String QUESTION_ID_COPIED_TEXT = "Fragen-ID kopiert";
    private static final String ALL_QUESTIONS_SOLVED_TEXT = "Alle vorhandenen Fragen für dieses Fragenpaket richtig beantwortet.";
    private static final String ERROR_NOT_ENOUGH_QUESTIONS = "Zu wenig Fragen für diese Auswahl vorhanden.";
    private static final String ERROR_JSON_PATH = "Keine JSON vorhanden oder falscher Pfad.";
    private static final String ERROR_JSON_SYNTAX = "Fehler in der JSON-Struktur.";

    private static final String MARKER_CORRECT = "✓";
    private static final String MARKER_WRONG = "✕";

    private static final double LOWERED_BUTTON_OPACITY = 0.65;
    private static final double ENABLED_BUTTON_OPACITY = 1.0;
    
    // 20-Fragen-Modus: Poolgröße
    private static final int LF20_QUESTIONS_SIZE = 20;
    private static final int MAX_ANSWER_COUNT = 4;
    private static final int FALLBACK_CORRECT_INDEX = -1;
    private static final int COPY_FEEDBACK_SECONDS = 1;

    // AP1 Gewichtung LF01-LF06: 1:3:3:1:3:1, Multiplikator 3 ergibt 36 Fragen.
    private static final int QUESTION_MULTIPLIER_AP1 = 3;
    private static final int TAKE_QUESTIONS_LF01 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF02 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF03 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF04 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF05 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF06 = 1 * QUESTION_MULTIPLIER_AP1;

    // FXML: Statusbar
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Label questionCounterLabel;
    @FXML
    private Label quizModeLabel;
    @FXML
    private Label questionIdLabel;

    // FXML: Frage und Ergebnis
    @FXML
    private Label questionLabel;
    @FXML
    private Label resultLabel;
    @FXML
    private Label explanationLabel;

    // FXML: Resultscreen Buttons
    @FXML
    private Button newRoundButton;
    @FXML
    private Button repeatSessionWrongQuestionsButton;

    // FXML: Antwortreihen, Antwortbuttons und Marker
    @FXML
    private HBox answerRow1;
    @FXML
    private HBox answerRow2;
    @FXML
    private HBox answerRow3;
    @FXML
    private HBox answerRow4;

    @FXML
    private Button answerButton1;
    @FXML
    private Button answerButton2;
    @FXML
    private Button answerButton3;
    @FXML
    private Button answerButton4;

    @FXML
    private Label answerLeftMarker1;
    @FXML
    private Label answerLeftMarker2;
    @FXML
    private Label answerLeftMarker3;
    @FXML
    private Label answerLeftMarker4;

    @FXML
    private Label answerRightMarker1;
    @FXML
    private Label answerRightMarker2;
    @FXML
    private Label answerRightMarker3;
    @FXML
    private Label answerRightMarker4;

    // FXML: Action Bar
    @FXML
    private Button actionButton;
    @FXML
    private Button leftHandedExitButton;
    @FXML
    private Button rightHandedExitButton;

    // UI-Hilfslisten für einfachere Verarbeitung der festen FXML-Elemente
    private List<HBox> answerRows;
    private List<Button> answerButtons = new ArrayList<>();
    private List<Label> leftAnswerMarkers;
    private List<Label> rightAnswerMarkers;

    // Quiz-Zustand
    private QuizEngine quizEngine;
    private QuizLoader quizLoader;
    private QuestionProgressRepository questionProgressRepository;

    private String quizMode;
    private String[] jsonList;

    private boolean confirmed = false;          // Antwort wurde bestätigt?
    private boolean isResultScreen = false;     // Action Button verhält sich im Resultscreen anders

    private Button selectedAnswerButton;
    private Button correctAnswerButton;

    private final List<List<QuizQuestion>> lf20QuestionsSessionPools = new ArrayList<>();

    // AP1-Fragenpools je Lernfeld
    private final List<QuizQuestion> lf01QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf02QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf03QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf04QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf05QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf06QuizSessionPool = new ArrayList<>();

    private final List<QuizQuestion> ap1QuizRound = new ArrayList<>();

    // Falsch beantwortete Fragen bleiben innerhalb der aktuellen Quizsession für optionale Wiederholung erhalten.
    private final List<QuizQuestion> wrongQuestionsSessionPool = new ArrayList<>();

    // Resultscreen Buttonzustände
    private boolean newRoundQuestionsAvailable = false;
    private boolean wrongQuestionsAvailable = false;

    // Aktuelle Fragen-ID für Kopierfunktion
    private String currentQuestionId = "";

    @FXML
    public void initialize() {
        quizLoader = new QuizLoader();
        questionProgressRepository = new QuestionProgressRepository();

        initAnswerButtonsLists();
        initUI();
        initButtonBehavior();
    }

    public void setQuizMode(String quizMode) {
        this.quizMode = quizMode;
    }

    public void setJsonList(String[] jsonList) {
        this.jsonList = jsonList;
    }

    private void initAnswerButtonsLists() {
        answerRows = List.of(answerRow1, answerRow2, answerRow3, answerRow4);

        answerButtons = new ArrayList<>(List.of(
                answerButton1,
                answerButton2,
                answerButton3,
                answerButton4
        ));

        leftAnswerMarkers = List.of(
                answerLeftMarker1,
                answerLeftMarker2,
                answerLeftMarker3,
                answerLeftMarker4
        );

        rightAnswerMarkers = List.of(
                answerRightMarker1,
                answerRightMarker2,
                answerRightMarker3,
                answerRightMarker4
        );
    }

    private void initUI() {
        // Startzustand: Quizdetails sind erst sichtbar, sobald eine Frage geladen wurde.
        questionCounterLabel.setVisible(false);
        quizModeLabel.setVisible(false);
        questionIdLabel.setVisible(false);

        questionLabel.setVisible(false);
        explanationLabel.setVisible(false);

        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        newRoundButton.setVisible(false);
        newRoundButton.setManaged(false);

        repeatSessionWrongQuestionsButton.setVisible(false);
        repeatSessionWrongQuestionsButton.setManaged(false);

        actionButton.setVisible(false);

        rightHandedExitButton.setVisible(true);
        leftHandedExitButton.setVisible(false);
    }

    private void initButtonBehavior() {
        questionIdLabel.setOnMouseClicked(onClick -> copyCurrentQuestionId());

        actionButton.setOnAction(onClick -> actionButtonBehavior());
        newRoundButton.setOnAction(onClick -> newRoundButtonBehavior());
        repeatSessionWrongQuestionsButton.setOnAction(onClick -> repeatSessionWrongQuestionsButtonBehavior());
        rightHandedExitButton.setOnAction(onClick -> showDialogConfirmationQuiz("Quiz wirklich verlassen?"));

        // Enter auf Exit blockieren, damit das Quiz nicht versehentlich per Tastatur verlassen wird.
        rightHandedExitButton.setFocusTraversable(false);
        rightHandedExitButton.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
            }
        });

        answerButtonBehavior();
    }

    private void answerButtonBehavior() {
        for (Button answerButton : answerButtons) {
            answerButton.setOnAction(onClick -> {
                selectedAnswerButton(answerButton);

                actionButton.setVisible(true);
                actionButton.setManaged(true);
            });
        }
    }

    // AP1-Modus vorbereiten: JSONs LF01-LF06 laden, je Lernfeld poolen und mischen.
    public void setupAP1() {
        quizLoader = new QuizLoader();

        // Torwächter: Wenn ein AP1-Lernfeldpool nicht die Mindestanzahl Fragen hat, wird kein Quiz gestartet.
        // REFACTOR PRIO 4: Ähnliche Prüfungen in mehreren Controllern -> Schlüsselmeister-/Torwächterlogik auslagern
        try {
            lf01QuizSessionPool.clear();
            lf02QuizSessionPool.clear();
            lf03QuizSessionPool.clear();
            lf04QuizSessionPool.clear();
            lf05QuizSessionPool.clear();
            lf06QuizSessionPool.clear();

            for (String json : jsonList) {
                List<QuizQuestion> quizQuestions = quizLoader.load(json);

                List<QuizQuestion> filteredQuizQuestions = filterCorrectQuestions(quizQuestions);

                // Fragen anhand des JSON-Pfads dem passenden Lernfeldpool zuordnen.
                if (json.contains("lf-01")) {
                    lf01QuizSessionPool.addAll(filteredQuizQuestions);
                } else if (json.contains("lf-02")) {
                    lf02QuizSessionPool.addAll(filteredQuizQuestions);
                } else if (json.contains("lf-03")) {
                    lf03QuizSessionPool.addAll(filteredQuizQuestions);
                } else if (json.contains("lf-04")) {
                    lf04QuizSessionPool.addAll(filteredQuizQuestions);
                } else if (json.contains("lf-05")) {
                    lf05QuizSessionPool.addAll(filteredQuizQuestions);
                } else if (json.contains("lf-06")) {
                    lf06QuizSessionPool.addAll(filteredQuizQuestions);
                }
            }

            // AP1 benötigt pro Lernfeld genug Fragen für die gewichtete Runde.
            if (lf01QuizSessionPool.size() < TAKE_QUESTIONS_LF01
                    || lf02QuizSessionPool.size() < TAKE_QUESTIONS_LF02
                    || lf03QuizSessionPool.size() < TAKE_QUESTIONS_LF03
                    || lf04QuizSessionPool.size() < TAKE_QUESTIONS_LF04
                    || lf05QuizSessionPool.size() < TAKE_QUESTIONS_LF05
                    || lf06QuizSessionPool.size() < TAKE_QUESTIONS_LF06) {
                explanationLabel.setText(ERROR_NOT_ENOUGH_QUESTIONS);
                explanationLabel.setVisible(true);

                for (HBox answerRow : answerRows) {
                    answerRow.setVisible(false);
                }

                return;
            }
        } catch (IOException ioException) {
            explanationLabel.setText(ERROR_JSON_PATH);
            explanationLabel.setVisible(true);

            for (HBox answerRow : answerRows) {
                answerRow.setVisible(false);
            }

            return;
        } catch (JsonSyntaxException jsonSyntaxException) {
            explanationLabel.setText(ERROR_JSON_SYNTAX);
            explanationLabel.setVisible(true);

            for (HBox answerRow : answerRows) {
                answerRow.setVisible(false);
            }

            return;
        }

        Collections.shuffle(lf01QuizSessionPool);
        Collections.shuffle(lf02QuizSessionPool);
        Collections.shuffle(lf03QuizSessionPool);
        Collections.shuffle(lf04QuizSessionPool);
        Collections.shuffle(lf05QuizSessionPool);
        Collections.shuffle(lf06QuizSessionPool);

        startAP1Round();
    }

    // AP1-Runde erstellen: LF01-LF06 im Verhältnis 1:3:3:1:3:1 ziehen und anschließend mischen.
    private void startAP1Round() {
        isResultScreen = false;
        ap1QuizRound.clear();

        if (lf01QuizSessionPool.size() >= TAKE_QUESTIONS_LF01) {
            ap1QuizRound.addAll(lf01QuizSessionPool.subList(0, TAKE_QUESTIONS_LF01));
            lf01QuizSessionPool.subList(0, TAKE_QUESTIONS_LF01).clear();
        }

        if (lf02QuizSessionPool.size() >= TAKE_QUESTIONS_LF02) {
            ap1QuizRound.addAll(lf02QuizSessionPool.subList(0, TAKE_QUESTIONS_LF02));
            lf02QuizSessionPool.subList(0, TAKE_QUESTIONS_LF02).clear();
        }

        if (lf03QuizSessionPool.size() >= TAKE_QUESTIONS_LF03) {
            ap1QuizRound.addAll(lf03QuizSessionPool.subList(0, TAKE_QUESTIONS_LF03));
            lf03QuizSessionPool.subList(0, TAKE_QUESTIONS_LF03).clear();
        }

        if (lf04QuizSessionPool.size() >= TAKE_QUESTIONS_LF04) {
            ap1QuizRound.addAll(lf04QuizSessionPool.subList(0, TAKE_QUESTIONS_LF04));
            lf04QuizSessionPool.subList(0, TAKE_QUESTIONS_LF04).clear();
        }

        if (lf05QuizSessionPool.size() >= TAKE_QUESTIONS_LF05) {
            ap1QuizRound.addAll(lf05QuizSessionPool.subList(0, TAKE_QUESTIONS_LF05));
            lf05QuizSessionPool.subList(0, TAKE_QUESTIONS_LF05).clear();
        }

        if (lf06QuizSessionPool.size() >= TAKE_QUESTIONS_LF06) {
            ap1QuizRound.addAll(lf06QuizSessionPool.subList(0, TAKE_QUESTIONS_LF06));
            lf06QuizSessionPool.subList(0, TAKE_QUESTIONS_LF06).clear();
        }

        Collections.shuffle(ap1QuizRound);

        quizEngine = new QuizEngine(ap1QuizRound);
        showCurrentQuestion();
    }

    // LF-20-Fragen-Modus vorbereiten: JSONs laden, je Unterthema mischen und als Poolliste speichern.
    public void setupLf20Questions() {
        quizLoader = new QuizLoader();

        // Torwächter: Wenn kein Fragenpool geladen werden konnte, wird kein Quiz gestartet.
        // REFACTOR PRIO 4: Ähnliche Prüfungen in mehreren Controllern -> Schlüsselmeister-/Torwächterlogik auslagern
        try {
            lf20QuestionsSessionPools.clear();

            for (String json : jsonList) {
                List<QuizQuestion> quizQuestions = quizLoader.load(json);

                List<QuizQuestion> filteredQuizQuestions = filterCorrectQuestions(quizQuestions);

                if (!filteredQuizQuestions.isEmpty()) {
                    Collections.shuffle(filteredQuizQuestions);
                    lf20QuestionsSessionPools.add(new ArrayList<>(filteredQuizQuestions));
                }
            }

            int remainingQuestions = 0;

            for (List<QuizQuestion> lf20QuestionSessionPool : lf20QuestionsSessionPools) {
                remainingQuestions += lf20QuestionSessionPool.size();
            }

            if (remainingQuestions < LF20_QUESTIONS_SIZE) {
                explanationLabel.setText(ERROR_NOT_ENOUGH_QUESTIONS);
                explanationLabel.setVisible(true);
                for (HBox answerRow : answerRows) {
                    answerRow.setVisible(false);
                }
                return;
            }
        } catch (IOException ioException) {
            explanationLabel.setText(ERROR_JSON_PATH);
            explanationLabel.setVisible(true);
            for (HBox answerRow : answerRows) {
                answerRow.setVisible(false);
            }
            return;
        } catch (JsonSyntaxException jsonSyntaxException) {
            explanationLabel.setText(ERROR_JSON_SYNTAX);
            explanationLabel.setVisible(true);
            for (HBox answerRow : answerRows) {
                answerRow.setVisible(false);
            }
            return;
        }

        // Poolliste zusätzlich mischen, damit nicht immer dasselbe Unterthema zuerst startet.
        Collections.shuffle(lf20QuestionsSessionPools);

        startLf20QuestionsRound();
    }

    // 20-Fragen-Runde per Round-Robin aus allen verfügbaren Unterthemen bilden.
    private void startLf20QuestionsRound() {
        isResultScreen = false;

        List<QuizQuestion> lf20QuestionsQuizRound = new ArrayList<>();

        while (lf20QuestionsQuizRound.size() < LF20_QUESTIONS_SIZE) {
            boolean stillHasQuestions = false;

            for (List<QuizQuestion> lf20QuestionsSessionPool : lf20QuestionsSessionPools) {
                if (lf20QuestionsQuizRound.size() >= LF20_QUESTIONS_SIZE) {
                    break;
                }

                if (!lf20QuestionsSessionPool.isEmpty()) {
                    lf20QuestionsQuizRound.add(lf20QuestionsSessionPool.remove(0));
                    stillHasQuestions = true;
                }
            }

            // Abbruch, wenn kein Unterthemenpool mehr Fragen liefern konnte.
            if (!stillHasQuestions) {
                break;
            }
        }

        Collections.shuffle(lf20QuestionsQuizRound);

        quizEngine = new QuizEngine(lf20QuestionsQuizRound);
        showCurrentQuestion();
    }

    // Fehlertraining Fragenpool direkt als eigene Wiederholungsrunde starten.
    public void setupErrorTraining(List<QuizQuestion> wrongQuestions) {
        if (wrongQuestions == null || wrongQuestions.isEmpty()) {
            explanationLabel.setText(ERROR_NOT_ENOUGH_QUESTIONS);
            explanationLabel.setVisible(true);
            return;
        }

        Collections.shuffle(wrongQuestions);

        quizEngine = new QuizEngine(wrongQuestions);
        showCurrentQuestion();
    }

    // Aktuelle Frage anzeigen und Quiz-UI für die Antwortphase zurücksetzen.
    private void showCurrentQuestion() {
        isResultScreen = false;

        // UI-Zustand für aktive Frage
        rightHandedExitButton.setVisible(true);
        rightHandedExitButton.setManaged(true);

        leftHandedExitButton.setVisible(false);
        leftHandedExitButton.setManaged(true);

        questionCounterLabel.setVisible(true);
        quizModeLabel.setVisible(true);
        questionIdLabel.setVisible(true);

        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        newRoundButton.setVisible(false);
        newRoundButton.setManaged(false);

        repeatSessionWrongQuestionsButton.setVisible(false);
        repeatSessionWrongQuestionsButton.setManaged(false);

        questionLabel.setVisible(true);

        explanationLabel.setVisible(false);

        selectedAnswerButton = null;
        correctAnswerButton = null;
        confirmed = false;

        actionButton.setVisible(false);
        actionButton.setManaged(true);
        actionButton.setText("Bestätigen");

        // Statusbar aktualisieren
        int currentIndex = quizEngine.getCurrentIndex() + 1;
        int totalQuestions = quizEngine.getQuestionCount();

        questionCounterLabel.setText("Frage " + twoDigit(currentIndex) + "/" + twoDigit(totalQuestions));
        quizModeLabel.setText(quizMode);

        currentQuestionId = quizEngine.getCurrentQuestion().getId();
        questionIdLabel.setText(QUESTION_ID_PREFIX + currentQuestionId);

        // Frage und Erklärung setzen
        QuizQuestion question = quizEngine.getCurrentQuestion();

        questionLabel.setText(question.getQuestion());
        explanationLabel.setText(question.getExplanation());

        List<QuizAnswer> answers = question.getAnswers();

        // Wahr/Falsch-Fragen sind vorbereitet; aktuell werden hauptsächlich 1-aus-4-Fragen genutzt.
        if (question.getAnswers().size() > 2) {
            Collections.shuffle(question.getAnswers());
        }

        // Feste vier Antwortreihen zurücksetzen und mit vorhandenen Antworten befüllen.
        for (int answerIndex = 0; answerIndex < MAX_ANSWER_COUNT; answerIndex++) {
            Button answerButton = answerButtons.get(answerIndex);
            HBox answerRow = answerRows.get(answerIndex);

            Label leftMarker = leftAnswerMarkers.get(answerIndex);
            Label rightMarker = rightAnswerMarkers.get(answerIndex);

            leftMarker.setText("");
            rightMarker.setText("");

            leftMarker.setVisible(false);
            rightMarker.setVisible(false);

            leftMarker.getStyleClass().removeAll("marker-correct", "marker-wrong");
            rightMarker.getStyleClass().removeAll("marker-correct", "marker-wrong");

            answerButton.getStyleClass().removeAll("button-selected", "button-correct", "button-wrong");
            answerButton.setOpacity(ENABLED_BUTTON_OPACITY);
            answerButton.setMouseTransparent(false);

            if (answerIndex < answers.size()) {
                QuizAnswer answer = answers.get(answerIndex);

                answerRow.setVisible(true);
                answerRow.setManaged(true);

                answerButton.setText(answer.getText());
            } else {
                answerRow.setVisible(false);
                answerRow.setManaged(false);
            }
        }
    }

    // Gewählte Antwort optisch markieren und als aktuelle Auswahl speichern.
    private void selectedAnswerButton(Button selectedButton) {
        for (Button button : answerButtons) {
            button.setOpacity(ENABLED_BUTTON_OPACITY);
            button.getStyleClass().remove("button-selected");
        }

        selectedButton.setOpacity(ENABLED_BUTTON_OPACITY);
        selectedButton.getStyleClass().add("button-selected");

        selectedAnswerButton = selectedButton;
    }

    // Bestätigen-/Weiter-Button: Antwort prüfen, nächste Frage laden oder Resultscreen öffnen.
    private void actionButtonBehavior() {
        if (quizEngine == null) {
            return;
        }

        if (isResultScreen) {
            showDialogConfirmationQuiz("Quiz wirklich verlassen?");
            return;
        }

        // Nach bestätigter Antwort führt der Button zur nächsten Frage oder zur Auswertung.
        if (confirmed) {
            if (quizEngine.isLastQuestion()) {
                showResult();
            } else {
                quizEngine.nextQuestion();
                showCurrentQuestion();
            }

            return;
        }

        if (selectedAnswerButton == null) {
            return;
        }

        int selectedIndex = answerButtons.indexOf(selectedAnswerButton);
        boolean correct = quizEngine.checkAnswer(selectedIndex);

        quizEngine.answerScore(correct);

        QuizQuestion currentQuestion = quizEngine.getCurrentQuestion();

        if (correct) {
            // Markieren in der internen DB zum Filtern der richtigen Fragen vor Quiz Runden.
            // Wird somit auch gegebenenfalls aus "Fehlertraining" Modus entfernt.
            questionProgressRepository.markCorrect(currentQuestion.getId());
        } else {
            // Markieren in der internen DB für "Fehlertraining" Modus Schnellzugriff über Hauptmenü
            questionProgressRepository.markWrong(currentQuestion.getId());

            // Lokaler Fehlerpool für "Falsche wiederholen" am Ende der aktuellen Runde.
            wrongQuestionsSessionPool.add(currentQuestion);
        }

        // Antwortphase beenden: alle Buttons abdunkeln und aktive Zustände entfernen.
        for (Button answerButton : answerButtons) {
            answerButton.setOpacity(LOWERED_BUTTON_OPACITY);
            answerButton.getStyleClass().removeAll("button-selected", "button-correct", "button-wrong");
        }

        // Richtige Antwort suchen; -1 dient als Fallback bei fehlerhaft gepflegten Antworten.
        int correctIndex = FALLBACK_CORRECT_INDEX;
        List<QuizAnswer> answers = quizEngine.getCurrentQuestion().getAnswers();
        for (int answerIndex = 0; answerIndex < answers.size(); answerIndex++) {
            if (answers.get(answerIndex).isCorrect()) {
                correctIndex = answerIndex;
                break;
            }
        }

        if (correctIndex != FALLBACK_CORRECT_INDEX) {
            correctAnswerButton = answerButtons.get(correctIndex);
            correctAnswerButton.getStyleClass().add("button-correct");
            correctAnswerButton.setOpacity(ENABLED_BUTTON_OPACITY);

            showAnswerMarker(correctIndex, MARKER_CORRECT, true);
        }

        if (!correct) {
            selectedAnswerButton.getStyleClass().add("button-wrong");
            selectedAnswerButton.setOpacity(ENABLED_BUTTON_OPACITY);

            showAnswerMarker(selectedIndex, MARKER_WRONG, false);
        }

        // Antwortbuttons nach Auswertung blockieren, bis die nächste Frage geladen wird.
        for (Button answerButton : answerButtons) {
            answerButton.setMouseTransparent(true);
        }

        explanationLabel.setVisible(true);

        if (quizEngine.isLastQuestion()) {
            actionButton.setText("Quiz auswerten");
        } else {
            actionButton.setText("Nächste Frage");
        }

        confirmed = true;
    }

    // Antwortmarker anzeigen und passend als richtig/falsch markieren.
    private void showAnswerMarker(int markerIndex, String markerSymbol, boolean correctMarker) {
        Label marker = leftAnswerMarkers.get(markerIndex);

        marker.setText(markerSymbol);
        marker.setVisible(true);

        marker.getStyleClass().removeAll("marker-correct", "marker-wrong");

        if (correctMarker) {
            marker.getStyleClass().add("marker-correct");
        } else {
            marker.getStyleClass().add("marker-wrong");
        }
    }

    // Gesamtergebnis anzeigen und Resultscreen-Buttons vorbereiten.
    private void showResult() {
        isResultScreen = true;

        actionButton.setText("Hauptmenü");

        rightHandedExitButton.setVisible(false);
        leftHandedExitButton.setVisible(false);

        questionCounterLabel.setVisible(false);
        questionIdLabel.setVisible(false);

        questionLabel.setVisible(false);

        explanationLabel.setVisible(false);

        resultLabel.setVisible(true);
        resultLabel.setManaged(true);
        resultLabel.setText(RESULT_TEXT_PREFIX + twoDigit(quizEngine.getScore()) + "/" + twoDigit(quizEngine.getQuestionCount()));

        for (HBox answerRow : answerRows) {
            answerRow.setVisible(false);
            answerRow.setManaged(false);
        }

        showResultScreenButtons();
    }

    // Resultscreen-Buttons abhängig von verfügbaren Fragen anzeigen.
    private void showResultScreenButtons() {
        showNewRoundButton();
        showRepeatSessionWrongQuestionsButton();

        if (!newRoundQuestionsAvailable && !wrongQuestionsAvailable) {
            explanationLabel.setText(ALL_QUESTIONS_SOLVED_TEXT);
            explanationLabel.setVisible(true);
        }
    }

    // Neue-Runde-Button nur anzeigen, wenn noch genug Fragen für den aktuellen Modus vorhanden sind.
    private void showNewRoundButton() {
        newRoundButton.setVisible(false);
        newRoundButton.setManaged(false);
        newRoundQuestionsAvailable = false;

        if (quizMode.contains(QUIZ_MODE_AP1)) {
            boolean hasEnoughQuestions = true;

            if (lf01QuizSessionPool.size() < TAKE_QUESTIONS_LF01) {
                hasEnoughQuestions = false;
            } else if (lf02QuizSessionPool.size() < TAKE_QUESTIONS_LF02) {
                hasEnoughQuestions = false;
            } else if (lf03QuizSessionPool.size() < TAKE_QUESTIONS_LF03) {
                hasEnoughQuestions = false;
            } else if (lf04QuizSessionPool.size() < TAKE_QUESTIONS_LF04) {
                hasEnoughQuestions = false;
            } else if (lf05QuizSessionPool.size() < TAKE_QUESTIONS_LF05) {
                hasEnoughQuestions = false;
            } else if (lf06QuizSessionPool.size() < TAKE_QUESTIONS_LF06) {
                hasEnoughQuestions = false;
            }

            if (hasEnoughQuestions) {
                newRoundButton.setVisible(true);
                newRoundButton.setManaged(true);
                newRoundQuestionsAvailable = true;
            }

            return;
        }

        int remainingQuestions = 0;

        for (List<QuizQuestion> lf20QuestionSessionPool : lf20QuestionsSessionPools) {
            remainingQuestions += lf20QuestionSessionPool.size();
        }

        if (remainingQuestions >= LF20_QUESTIONS_SIZE) {
            newRoundButton.setVisible(true);
            newRoundButton.setManaged(true);
            newRoundQuestionsAvailable = true;
        }
    }

    // Falsche-wiederholen-Button nur anzeigen, wenn falsche Fragen in der Session vorhanden sind.
    private void showRepeatSessionWrongQuestionsButton() {
        if (wrongQuestionsSessionPool.isEmpty()) {
            repeatSessionWrongQuestionsButton.setVisible(false);
            repeatSessionWrongQuestionsButton.setManaged(false);
            wrongQuestionsAvailable = false;
            return;
        }

        repeatSessionWrongQuestionsButton.setText("Falsche wiederholen (" + wrongQuestionsSessionPool.size() + ")");
        repeatSessionWrongQuestionsButton.setVisible(true);
        repeatSessionWrongQuestionsButton.setManaged(true);
        wrongQuestionsAvailable = true;
    }

    // Neue Runde im aktuellen Quizmodus starten.
    private void newRoundButtonBehavior() {
        if (quizMode.contains(QUIZ_MODE_AP1)) {
            startAP1Round();
            return;
        }

        startLf20QuestionsRound();
    }

    // Falsch beantwortete Fragen der aktuellen Quizsession als eigene Wiederholungsrunde starten.
    private void repeatSessionWrongQuestionsButtonBehavior() {
        List<QuizQuestion> repeatWrongQuestionsSessionRound = new ArrayList<>(wrongQuestionsSessionPool);

        wrongQuestionsSessionPool.clear();

        Collections.shuffle(repeatWrongQuestionsSessionRound);

        quizEngine = new QuizEngine(repeatWrongQuestionsSessionRound);
        showCurrentQuestion();
    }

    // REFACTOR PRIO 1: Dialog-Erzeugung ist ähnlich wie im MenuController und könnte später ausgelagert werden.
    private void showDialogConfirmationQuiz(String popUpMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DIALOG_CONFIRMATION_FXML));
            Parent dialogConfirmationFXML = loader.load();

            Label dialogLabel = (Label) dialogConfirmationFXML.lookup("#dialogLabel");
            Button leftConfirmationButton = (Button) dialogConfirmationFXML.lookup("#leftConfirmationButton");
            Button rightConfirmationButton = (Button) dialogConfirmationFXML.lookup("#rightConfirmationButton");

            dialogLabel.setText(popUpMessage);

            Stage dialogConfirmationWindow = new Stage();
            dialogConfirmationWindow.setTitle("");
            dialogConfirmationWindow.initOwner(rootLayout.getScene().getWindow());
            dialogConfirmationWindow.initModality(Modality.APPLICATION_MODAL);
            dialogConfirmationWindow.setResizable(false);

            leftConfirmationButton.setText("Hauptmenü");
            rightConfirmationButton.setText("Weiterlernen");

            // Standardfokus auf „Weiterlernen“, damit Enter das Quiz nicht versehentlich verlässt.
            leftConfirmationButton.setDefaultButton(false);
            rightConfirmationButton.setDefaultButton(true);
            rightConfirmationButton.requestFocus();

            leftConfirmationButton.setOnAction(onClick -> {
                // Resultscreen-Zustand zurücksetzen, bevor ins Hauptmenü gewechselt wird.
                if (isResultScreen) {
                    isResultScreen = false;

                    quizEngine = null;
                    selectedAnswerButton = null;

                    resultLabel.setVisible(false);
                    resultLabel.setManaged(false);

                    lf01QuizSessionPool.clear();
                    lf02QuizSessionPool.clear();
                    lf03QuizSessionPool.clear();
                    lf04QuizSessionPool.clear();
                    lf05QuizSessionPool.clear();
                    lf06QuizSessionPool.clear();

                    lf20QuestionsSessionPools.clear();
                }

                try {
                    FXMLLoader menuLoader = new FXMLLoader(getClass().getResource(MENU_VIEW_FXML));
                    Parent menuRoot = menuLoader.load();

                    rootLayout.getScene().setRoot(menuRoot);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }

                dialogConfirmationWindow.close();
            });

            rightConfirmationButton.setOnAction(onClick -> dialogConfirmationWindow.close());

            Scene dialogScene = new Scene(dialogConfirmationFXML);

            // Enter-Schutz: Im Bestätigungsdialog soll Enter nicht abbrechen, sondern „Weiterlernen“ auslösen.
            dialogScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    rightConfirmationButton.fire();
                    event.consume();
                }
            });

            // Aktuelles Theme vom Hauptfenster übernehmen.
            dialogScene.getStylesheets().setAll(rootLayout.getScene().getStylesheets());

            dialogConfirmationWindow.setScene(dialogScene);

            // Dialog erst nach dem Anzeigen zentrieren, da Breite/Höhe dann zuverlässig verfügbar sind.
            dialogConfirmationWindow.setOnShown(event -> {
                Stage ownerStage = (Stage) rootLayout.getScene().getWindow();

                double ownerCenterX = ownerStage.getX() + ownerStage.getWidth() / 2;
                double ownerCenterY = ownerStage.getY() + ownerStage.getHeight() / 2;

                dialogConfirmationWindow.setX(ownerCenterX - dialogConfirmationWindow.getWidth() / 2);
                dialogConfirmationWindow.setY(ownerCenterY - dialogConfirmationWindow.getHeight() / 2);

                rightConfirmationButton.requestFocus();
            });

            dialogConfirmationWindow.showAndWait();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Hilfsmethode um bereits richtig beantwortete Fragen beim Quiz-Start zu filtern
    private List<QuizQuestion> filterCorrectQuestions(List<QuizQuestion> quizQuestions) {
        List<QuizQuestion> filteredQuizQuestions = new ArrayList<>();

        Set<String> correctQuestionIds = questionProgressRepository.getCorrectQuestionIds();

        for (QuizQuestion quizQuestion : quizQuestions) {
            if (quizQuestion == null || quizQuestion.getId() == null || quizQuestion.getId().isBlank()) {
                continue;
            }

            if (!correctQuestionIds.contains(quizQuestion.getId())) {
                filteredQuizQuestions.add(quizQuestion);
            }
        }

        return filteredQuizQuestions;
    }

    // Aktuelle Fragen-ID ins Clipboard kopieren und kurzzeitig UI-Feedback anzeigen.
    private void copyCurrentQuestionId() {
        if (currentQuestionId == null || currentQuestionId.isEmpty()) {
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();

        content.putString(currentQuestionId);
        clipboard.setContent(content);

        questionIdLabel.setText(QUESTION_ID_COPIED_TEXT);

        String copiedId = currentQuestionId;

        PauseTransition resetText = new PauseTransition(Duration.seconds(COPY_FEEDBACK_SECONDS));

        resetText.setOnFinished(event -> {
            // Nur zurücksetzen, wenn währenddessen keine neue Frage geladen wurde.
            if (copiedId.equals(currentQuestionId)) {
                questionIdLabel.setText(QUESTION_ID_PREFIX + currentQuestionId);
            }
        });

        resetText.play();
    }

    // Zahlen zweistellig formatieren, z. B. 1 -> 01.
    private String twoDigit(int value) {
        return String.format("%02d", value);
    }

}
