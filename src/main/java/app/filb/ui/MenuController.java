package app.filb.ui;

import app.filb.core.QuizQuestion;
import app.filb.io.QuestionProgressRepository;
import app.filb.io.QuizLoader;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

public class MenuController {
    private static final String SETTINGS_NODE = "FiLb/settings";

    private static final String THEME_SETTING_KEY = "themeSetting";
    private static final int LIGHT_THEME = 1;
    private static final int DARK_THEME = 2;
    private static final int THEME_NOT_SET = -1;

    private static final String HAS_LAST_QUIZ_DATA_KEY = "hasLastQuizData";
    private static final String LAST_QUIZ_MODE_KEY = "lastQuizMode";
    private static final String LAST_JSON_LIST_KEY = "lastJsonList";

    private static final String QUIZ_MODE_AP1 = "AP1";

    private static final int LF20_QUESTIONS_SIZE = 20;

    private static final int QUESTION_MULTIPLIER_AP1 = 3;
    private static final int TAKE_QUESTIONS_LF01 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF02 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF03 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF04 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF05 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF06 = 1 * QUESTION_MULTIPLIER_AP1;

    private static final String ERROR_JSON_PATH = "Keine JSON vorhanden oder falscher Pfad.";
    private static final String ERROR_JSON_SYNTAX = "Fehler in der JSON-Struktur.";

    private static final String QUIZ_MODE_ERROR_TRAINING = "Fehlertraining";
    private static final String LAST_SELECTION_ERROR_TEXT =
            "Letzte Auswahl konnte nicht geladen werden. Wähle bitte Modus und Fragenpaket neu.";

    private static final String QUIZ_MENU_VIEW_FXML = "/fxml/quiz_menu_view.fxml";
    private static final String QUIZ_VIEW_FXML = "/fxml/quiz_view.fxml";
    private static final String DIALOG_CONFIRMATION_FXML = "/fxml/dialog_confirmation.fxml";

    private static final String DARK_THEME_CSS = "/css/dark-theme.css";
    private static final String LIGHT_THEME_CSS = "/css/light-theme.css";

    private static final double LOWERED_BUTTON_OPACITY = 0.65;
    private static final double ENABLED_BUTTON_OPACITY = 1.0;

    // FXML: Texte
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Label versionIdLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label flavorLabel;

    // FXML: Hauptnavigation
    @FXML
    private Button quizChoiceButton;
    @FXML
    private Button startWithLastQuizDataButton;
    @FXML
    private Button errorTrainingButton;

    // FXML: Platzhalter / Action
    @FXML
    private Button optionsButton;
    @FXML
    private Button actionButton;

    // FXML: UI Settings
    @FXML
    private Button lightModeSelectButton;
    @FXML
    private Button darkModeSelectButton;

    // Einstellungen
    private Preferences preferences;

    // Quiz Vorbereitung
    private QuizController quizController;
    private QuizLoader quizLoader;
    private QuestionProgressRepository questionProgressRepository;

    private final List<List<QuizQuestion>> lf20QuestionsSessionPools = new ArrayList<>();

    // AP1-Fragenpools je Lernfeld
    private final List<QuizQuestion> lf01QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf02QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf03QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf04QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf05QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf06QuizSessionPool = new ArrayList<>();

    // Easter Egg
    private int creditClicks = 0;
    private final PauseTransition creditWindow = new PauseTransition(Duration.seconds(1));
    private boolean showingCredit = false;

    @FXML
    public void initialize() {
        initUISettings();

        quizLoader = new QuizLoader();
        questionProgressRepository = new QuestionProgressRepository();

        initUI();
        applyTheme(preferences);
        initButtonBehavior();

        creatorEasterEgg();
    }

    // Gespeicherte UI-Einstellungen laden und Standardwerte setzen.
    private void initUISettings() {
        preferences = Preferences.userRoot().node(SETTINGS_NODE);

        if (preferences.getInt(THEME_SETTING_KEY, THEME_NOT_SET) == THEME_NOT_SET) {
            preferences.putInt(THEME_SETTING_KEY, DARK_THEME);
        }
    }

    // Hauptmenü anzeigen und gespeicherte UI-Einstellungen darstellen.
    private void initUI() {
        versionIdLabel.setVisible(true);

        titleLabel.setVisible(true);
        flavorLabel.setVisible(true);

        quizChoiceButton.setVisible(true);

        startWithLastQuizDataButton.setVisible(preferences.getBoolean(HAS_LAST_QUIZ_DATA_KEY, false));

        // Fehlertraining Button mit Anzahl
        int wrongQuestionCount = questionProgressRepository.getWrongQuestionIds().size();

        if (wrongQuestionCount > 0) {
            errorTrainingButton.setVisible(true);
            errorTrainingButton.setManaged(true);
            errorTrainingButton.setText("Fehlertraining (" + wrongQuestionCount + ")");
        } else {
            errorTrainingButton.setVisible(false);
            errorTrainingButton.setManaged(true);
        }

        optionsButton.setVisible(false);
        optionsButton.setManaged(false);

        lightModeSelectButton.setVisible(true);
        darkModeSelectButton.setVisible(true);

        // Aktives Theme hervorheben.
        if (preferences.getInt(THEME_SETTING_KEY, DARK_THEME) == DARK_THEME) {
            lightModeSelectButton.setOpacity(LOWERED_BUTTON_OPACITY);
            darkModeSelectButton.setOpacity(ENABLED_BUTTON_OPACITY);
        } else {
            lightModeSelectButton.setOpacity(ENABLED_BUTTON_OPACITY);
            darkModeSelectButton.setOpacity(LOWERED_BUTTON_OPACITY);
        }

        actionButton.setVisible(true);
    }

    private void initButtonBehavior() {
        quizChoiceButton.setOnAction(onClick -> loadQuizMenu());

        startWithLastQuizDataButton.setOnAction(onClick -> startWithLastQuizData());

        errorTrainingButton.setOnAction(onClick -> startErrorTraining());

        lightModeSelectButton.setOnAction(v -> {
            preferences.putInt(THEME_SETTING_KEY, LIGHT_THEME);
            applyTheme(preferences);

            lightModeSelectButton.setOpacity(ENABLED_BUTTON_OPACITY);
            darkModeSelectButton.setOpacity(LOWERED_BUTTON_OPACITY);
        });

        darkModeSelectButton.setOnAction(v -> {
            preferences.putInt(THEME_SETTING_KEY, DARK_THEME);
            applyTheme(preferences);

            lightModeSelectButton.setOpacity(LOWERED_BUTTON_OPACITY);
            darkModeSelectButton.setOpacity(ENABLED_BUTTON_OPACITY);
        });

        actionButton.setOnAction(onClick -> {
            showDialogConfirmationMenu("Lernbegleiter wirklich beenden?");
        });
    }

    // Quiz-Auswahlmenü laden.
    private void loadQuizMenu() {
        try {
            FXMLLoader quizMenuLoader = new FXMLLoader(getClass().getResource(QUIZ_MENU_VIEW_FXML));
            Parent quizMenuRoot = quizMenuLoader.load();

            rootLayout.getScene().setRoot(quizMenuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Letzte geprüfte Quiz-Auswahl direkt starten und QuizMenu/ContentMenu/ContentSubmenu überspringen.
    private void startWithLastQuizData() {
        if (!preferences.getBoolean(HAS_LAST_QUIZ_DATA_KEY, false)) {
            preferences.putBoolean(HAS_LAST_QUIZ_DATA_KEY, false);

            startWithLastQuizDataButton.setVisible(false);
            flavorLabel.setText(LAST_SELECTION_ERROR_TEXT);

            return;
        }

        String lastQuizMode = preferences.get(LAST_QUIZ_MODE_KEY, null);
        String jsonListString = preferences.get(LAST_JSON_LIST_KEY, null);

        if (lastQuizMode == null || jsonListString == null) {
            preferences.putBoolean(HAS_LAST_QUIZ_DATA_KEY, false);

            startWithLastQuizDataButton.setVisible(false);
            flavorLabel.setText(LAST_SELECTION_ERROR_TEXT);

            return;
        }

        // JSON-String zurück in String[] umwandeln, da Preferences keine String-Arrays direkt speichert.
        String[] lastJsonList = new Gson().fromJson(jsonListString, String[].class);

        if (lastJsonList == null) {
            preferences.putBoolean(HAS_LAST_QUIZ_DATA_KEY, false);

            startWithLastQuizDataButton.setVisible(false);
            flavorLabel.setText(LAST_SELECTION_ERROR_TEXT);

            return;
        }

        prepareLastQuizData(lastQuizMode, lastJsonList);
    }

    // Schlüsselmeister: JSONs und interne Datenbank correct_answer prüfen, Quiz nur bei ausreichend Fragen starten.
    // REFACTOR PRIO 4: Ähnliche Prüfungen in mehreren Controllern -> Schlüsselmeister-/Torwächterlogik auslagern
    private void prepareLastQuizData(String quizMode, String[] jsonList) {
        List<String> selectedQuestionIds = new ArrayList<>();

        try {
            if (quizMode.contains(QUIZ_MODE_AP1)) {
                lf01QuizSessionPool.clear();
                lf02QuizSessionPool.clear();
                lf03QuizSessionPool.clear();
                lf04QuizSessionPool.clear();
                lf05QuizSessionPool.clear();
                lf06QuizSessionPool.clear();

                for (String json : jsonList) {
                    List<QuizQuestion> quizQuestions = quizLoader.load(json);

                    addQuestionIdsToSelectedQuestionIds(quizQuestions, selectedQuestionIds);

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
                    showDialogConfirmationResetCorrectQuestions(quizMode, jsonList, selectedQuestionIds);
                    return;
                }
            } else {
                lf20QuestionsSessionPools.clear();

                for (String json : jsonList) {
                    List<QuizQuestion> quizQuestions = quizLoader.load(json);

                    addQuestionIdsToSelectedQuestionIds(quizQuestions, selectedQuestionIds);

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
                    showDialogConfirmationResetCorrectQuestions(quizMode, jsonList, selectedQuestionIds);
                    return;
                }
            }

            startLastQuiz(quizMode, jsonList);
        } catch (IOException ioException) {
            flavorLabel.setText(ERROR_JSON_PATH);
            flavorLabel.setVisible(true);
        } catch (JsonSyntaxException jsonSyntaxException) {
            flavorLabel.setText(ERROR_JSON_SYNTAX);
            flavorLabel.setVisible(true);
        }
    }

    // Geprüften Quizmodus starten und Quiz-View anzeigen.
    private void startLastQuiz(String quizMode, String[] jsonList) {
        try {
            FXMLLoader quizViewLoader = new FXMLLoader(getClass().getResource(QUIZ_VIEW_FXML));
            Parent quizRoot = quizViewLoader.load();

            quizController = quizViewLoader.getController();

            quizController.setQuizMode(quizMode);
            quizController.setJsonList(jsonList);

            if (quizMode.contains(QUIZ_MODE_AP1)) {
                quizController.setupAP1();
            } else {
                quizController.setupLf20Questions();
            }

            rootLayout.getScene().setRoot(quizRoot);
        } catch (IOException ioException) {
            preferences.putBoolean(HAS_LAST_QUIZ_DATA_KEY, false);

            startWithLastQuizDataButton.setVisible(false);
            flavorLabel.setText(LAST_SELECTION_ERROR_TEXT);
        }
    }

    // Fehlertraining-Pool direkt als QuizController starten.
    private void startErrorTraining() {
        Set<String> wrongQuestionIds = questionProgressRepository.getWrongQuestionIds();

        if (wrongQuestionIds.isEmpty()) {
            return;
        }

        List<QuizQuestion> wrongQuestions = new ArrayList<>();

        try {
            for (String json : getAllKnownJsonPaths()) {
                List<QuizQuestion> quizQuestions = quizLoader.load(json);

                for (QuizQuestion quizQuestion : quizQuestions) {
                    if (quizQuestion == null || quizQuestion.getId() == null || quizQuestion.getId().isBlank()) {
                        continue;
                    }

                    if (wrongQuestionIds.contains(quizQuestion.getId())) {
                        wrongQuestions.add(quizQuestion);
                    }
                }
            }

            if (wrongQuestions.isEmpty()) {
                flavorLabel.setText("Fehlertraining konnte nicht geladen werden.");
                return;
            }

            FXMLLoader quizViewLoader = new FXMLLoader(getClass().getResource(QUIZ_VIEW_FXML));
            Parent quizRoot = quizViewLoader.load();

            QuizController quizController = quizViewLoader.getController();
            quizController.setQuizMode(QUIZ_MODE_ERROR_TRAINING);
            quizController.setupErrorTraining(wrongQuestions);

            rootLayout.getScene().setRoot(quizRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // REFACTOR PRIO 3: JSON Pfade kommen in mindestens 2 Controllern vor -> Auslagern
    private String[] getAllKnownJsonPaths() {
        return new String[]{
                // Sachfragen
                // LF-01
                "questions/lf-01/lf-01-sach-arbm.json",
                "questions/lf-01/lf-01-sach-bwl.json",
                "questions/lf-01/lf-01-sach-markt.json",
                "questions/lf-01/lf-01-sach-orga.json",
                "questions/lf-01/lf-01-sach-sozi.json",
                "questions/lf-01/lf-01-sach-ziel.json",

                // LF-02
                "questions/lf-02/lf-02-sach-besch.json",
                "questions/lf-02/lf-02-sach-hard.json",
                "questions/lf-02/lf-02-sach-kommun.json",
                "questions/lf-02/lf-02-sach-proj.json",
                "questions/lf-02/lf-02-sach-recht.json",
                "questions/lf-02/lf-02-sach-sich.json",

                // LF-03
                "questions/lf-03/lf-03-sach-adrs.json",
                "questions/lf-03/lf-03-sach-cloud.json",
                "questions/lf-03/lf-03-sach-integ.json",
                "questions/lf-03/lf-03-sach-komp.json",
                "questions/lf-03/lf-03-sach-modl.json",
                "questions/lf-03/lf-03-sach-secu.json",
                "questions/lf-03/lf-03-sach-strv.json",

                // LF-04
                "questions/lf-04/lf-04-sach-auth.json",
                "questions/lf-04/lf-04-sach-datg.json",
                "questions/lf-04/lf-04-sach-grds.json",
                "questions/lf-04/lf-04-sach-kryp.json",
                "questions/lf-04/lf-04-sach-lizn.json",
                "questions/lf-04/lf-04-sach-malw.json",
                "questions/lf-04/lf-04-sach-risi.json",
                "questions/lf-04/lf-04-sach-sba.json",
                "questions/lf-04/lf-04-sach-seng.json",
                "questions/lf-04/lf-04-sach-tom.json",

                // LF-05
                "questions/lf-05/lf-05-sach-data.json",
                "questions/lf-05/lf-05-sach-db.json",
                "questions/lf-05/lf-05-sach-dev.json",
                "questions/lf-05/lf-05-sach-modl.json",
                "questions/lf-05/lf-05-sach-prog.json",

                // LF-06
                "questions/lf-06/lf-06-sach-anal.json",
                "questions/lf-06/lf-06-sach-komm.json",
                "questions/lf-06/lf-06-sach-moni.json",
                "questions/lf-06/lf-06-sach-serv.json",
                "questions/lf-06/lf-06-sach-tick.json",
                "questions/lf-06/lf-06-sach-vert.json",
                "questions/lf-06/lf-06-sach-wart.json",

                // Fachbegriffe
                // LF-01
                "questions/lf-01/lf-01-fach-arbm.json",
                "questions/lf-01/lf-01-fach-bwl.json",
                "questions/lf-01/lf-01-fach-markt.json",
                "questions/lf-01/lf-01-fach-orga.json",
                "questions/lf-01/lf-01-fach-sozi.json",
                "questions/lf-01/lf-01-fach-ziel.json",

                // LF-02
                "questions/lf-02/lf-02-fach-besch.json",
                "questions/lf-02/lf-02-fach-hard.json",
                "questions/lf-02/lf-02-fach-kalk.json",
                "questions/lf-02/lf-02-fach-kommun.json",
                "questions/lf-02/lf-02-fach-proj.json",
                "questions/lf-02/lf-02-fach-recht.json",

                // LF-03
                "questions/lf-03/lf-03-fach-adrs.json",
                "questions/lf-03/lf-03-fach-cloud.json",
                "questions/lf-03/lf-03-fach-komp.json",
                "questions/lf-03/lf-03-fach-modl.json",
                "questions/lf-03/lf-03-fach-secu.json",
                "questions/lf-03/lf-03-fach-strv.json",

                // LF-04
                "questions/lf-04/lf-04-fach-auth.json",
                "questions/lf-04/lf-04-fach-datg.json",
                "questions/lf-04/lf-04-fach-grds.json",
                "questions/lf-04/lf-04-fach-kryp.json",
                "questions/lf-04/lf-04-fach-lizn.json",
                "questions/lf-04/lf-04-fach-malw.json",
                "questions/lf-04/lf-04-fach-risi.json",
                "questions/lf-04/lf-04-fach-sba.json",
                "questions/lf-04/lf-04-fach-seng.json",
                "questions/lf-04/lf-04-fach-tom.json",

                // LF-05
                "questions/lf-05/lf-05-fach-data.json",
                "questions/lf-05/lf-05-fach-db.json",
                "questions/lf-05/lf-05-fach-dev.json",
                "questions/lf-05/lf-05-fach-modl.json",
                "questions/lf-05/lf-05-fach-prog.json",

                // LF-06
                "questions/lf-06/lf-06-fach-anal.json",
                "questions/lf-06/lf-06-fach-komm.json",
                "questions/lf-06/lf-06-fach-moni.json",
                "questions/lf-06/lf-06-fach-serv.json",
                "questions/lf-06/lf-06-fach-tick.json",
                "questions/lf-06/lf-06-fach-vert.json",
                "questions/lf-06/lf-06-fach-wart.json",

                // Abkürzungen
                // LF-01
                "questions/lf-01/lf-01-abkz.json",

                // LF-02
                "questions/lf-02/lf-02-abkz.json",

                // LF-03
                "questions/lf-03/lf-03-abkz.json",

                // LF-04
                "questions/lf-04/lf-04-abkz.json",

                // LF-05
                "questions/lf-05/lf-05-abkz.json",

                // LF-06
                "questions/lf-06/lf-06-abkz.json",

                // Berechnungen
                // LF-02
                "questions/lf-02/lf-02-rech-finanz.json",
                "questions/lf-02/lf-02-rech-kalk.json",
                "questions/lf-02/lf-02-rech-wirt.json",

                // LF-03
                "questions/lf-03/lf-03-rech-netz.json",

                // LF-05
                "questions/lf-05/lf-05-rech-zahl.json",

                // Subnetting
                // LF-03
                "questions/lf-03/lf-03-subn-ipv4.json",

                // Codeausschnitte
                // LF-05
                "questions/lf-05/lf-05-code-py.json",
                "questions/lf-05/lf-05-code-sql.json",
        };
    }

    // REFACTOR PRIO 1: Dialog-Erzeugung ist ähnlich wie im QuizController und könnte später ausgelagert werden.
    private void showDialogConfirmationMenu(String popUpMessage) {
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

            leftConfirmationButton.setText("Beenden");
            rightConfirmationButton.setText("Weiterlernen");

            // Standardfokus auf „Weiterlernen“, damit Enter das Programm nicht versehentlich verlässt.
            leftConfirmationButton.setDefaultButton(false);
            rightConfirmationButton.setDefaultButton(true);
            rightConfirmationButton.requestFocus();

            leftConfirmationButton.setOnAction(onClick -> {
                Platform.exit();
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

    // Richtige Fragen zurücksetzen Dialog.
    // REFACTOR PRIO 1: Ähnlichkeiten mit den andren Dialogmenüs.
    private void showDialogConfirmationResetCorrectQuestions(String quizMode, String[] jsonList, List<String> selectedQuestionIds) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DIALOG_CONFIRMATION_FXML));
            Parent dialogConfirmationFXML = loader.load();

            Label dialogLabel = (Label) dialogConfirmationFXML.lookup("#dialogLabel");
            Button leftConfirmationButton = (Button) dialogConfirmationFXML.lookup("#leftConfirmationButton");
            Button rightConfirmationButton = (Button) dialogConfirmationFXML.lookup("#rightConfirmationButton");

            dialogLabel.setText("Auswahl bereits abgeschlossen.\n\nZurücksetzen und Quiz starten?");

            Stage dialogConfirmationWindow = new Stage();
            dialogConfirmationWindow.setTitle("");
            dialogConfirmationWindow.initOwner(rootLayout.getScene().getWindow());
            dialogConfirmationWindow.initModality(Modality.APPLICATION_MODAL);
            dialogConfirmationWindow.setResizable(false);

            leftConfirmationButton.setText("Abbrechen");
            rightConfirmationButton.setText("Quiz starten");

            // Sicherheit: Enter soll nicht versehentlich zurücksetzen.
            leftConfirmationButton.setDefaultButton(true);
            rightConfirmationButton.setDefaultButton(false);
            leftConfirmationButton.requestFocus();

            leftConfirmationButton.setOnAction(onClick -> dialogConfirmationWindow.close());

            rightConfirmationButton.setOnAction(onClick -> {
                questionProgressRepository.resetCorrectAnswersForQuestionIds(selectedQuestionIds);

                dialogConfirmationWindow.close();

                prepareLastQuizData(quizMode, jsonList);
            });

            Scene dialogScene = new Scene(dialogConfirmationFXML);

            // Enter-Schutz: Enter löst Abbrechen aus, nicht Zurücksetzen.
            dialogScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    leftConfirmationButton.fire();
                    event.consume();
                }
            });

            // Aktuelles Theme vom Hauptfenster übernehmen.
            dialogScene.getStylesheets().setAll(rootLayout.getScene().getStylesheets());

            dialogConfirmationWindow.setScene(dialogScene);

            // Dialog zentrieren.
            dialogConfirmationWindow.setOnShown(event -> {
                Stage ownerStage = (Stage) rootLayout.getScene().getWindow();

                double ownerCenterX = ownerStage.getX() + ownerStage.getWidth() / 2;
                double ownerCenterY = ownerStage.getY() + ownerStage.getHeight() / 2;

                dialogConfirmationWindow.setX(ownerCenterX - dialogConfirmationWindow.getWidth() / 2);
                dialogConfirmationWindow.setY(ownerCenterY - dialogConfirmationWindow.getHeight() / 2);

                leftConfirmationButton.requestFocus();
            });

            dialogConfirmationWindow.showAndWait();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Gespeichertes Theme auf die aktuelle Scene anwenden.
    private void applyTheme(Preferences preferences) {
        // Scene ist im initialize() ggf. noch nicht verfügbar; Theme dann später anwenden.
        if (rootLayout.getScene() == null) {
            Platform.runLater(() -> applyTheme(preferences));
            return;
        }

        rootLayout.getScene().getStylesheets().clear();

        if (preferences.getInt(THEME_SETTING_KEY, DARK_THEME) == DARK_THEME) {
            rootLayout.getScene().getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource(DARK_THEME_CSS)).toExternalForm()
            );
        } else {
            rootLayout.getScene().getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource(LIGHT_THEME_CSS)).toExternalForm()
            );
        }
    }

    // Hilfsmethode um alle gültigen IDs aus einer geladenen JSON in selectedQuestionIds zu sammeln.
    private void addQuestionIdsToSelectedQuestionIds(List<QuizQuestion> quizQuestions, List<String> selectedQuestionIds) {
        for (QuizQuestion quizQuestion : quizQuestions) {
            if (quizQuestion == null || quizQuestion.getId() == null || quizQuestion.getId().isBlank()) {
                continue;
            }

            selectedQuestionIds.add(quizQuestion.getId());
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

    // Easter Egg: 3 Klicks innerhalb von 1 Sekunde auf die Versionsanzeige.
    private void creatorEasterEgg() {
        creditWindow.setOnFinished(onClick -> creditClicks = 0);

        versionIdLabel.setOnMouseClicked(onClick -> {
            if (showingCredit) {
                return;
            }

            creditClicks++;

            creditWindow.stop();
            creditWindow.playFromStart();

            if (creditClicks >= 3) {
                creditClicks = 0;
                creditWindow.stop();

                showingCredit = true;

                String previousText = versionIdLabel.getText();
                versionIdLabel.setText("created by devake86");

                PauseTransition hide = new PauseTransition(Duration.seconds(3));

                hide.setOnFinished(ev -> {
                    versionIdLabel.setText(previousText);
                    showingCredit = false;
                });

                hide.play();
            }
        });
    }

}
