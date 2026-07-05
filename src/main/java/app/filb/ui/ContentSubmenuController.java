package app.filb.ui;

import app.filb.core.QuizQuestion;
import app.filb.io.QuestionProgressRepository;
import app.filb.io.QuizLoader;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

public class ContentSubmenuController {
    private static final String SETTINGS_NODE = "FiLb/settings";

    private static final String DIALOG_CONFIRMATION_FXML = "/fxml/dialog_confirmation.fxml";

    private static final String HAS_LAST_QUIZ_DATA_KEY = "hasLastQuizData";
    private static final String LAST_QUIZ_MODE_KEY = "lastQuizMode";
    private static final String LAST_JSON_LIST_KEY = "lastJsonList";

    private static final String QUIZ_MODE_AP1 = "AP1";
    private static final String QUIZ_MODE_LF = "LF";

    private static final String QUIZ_MODE_LF01 = "LF-01";
    private static final String QUIZ_MODE_LF02 = "LF-02";
    private static final String QUIZ_MODE_LF03 = "LF-03";
    private static final String QUIZ_MODE_LF04 = "LF-04";
    private static final String QUIZ_MODE_LF05 = "LF-05";
    private static final String QUIZ_MODE_LF06 = "LF-06";

    // Textanhänge für quizMode
    private static final String FACTUAL_QUESTIONS_TEXT = " - Sachfragen";
    private static final String TECHNICAL_TERMS_TEXT = " - Fachbegriffe";
    private static final String ABBREVIATIONS_TEXT = " - Abkürzungen";
    private static final String SUBNETTING_TEXT = " - Subnetting";
    private static final String CODE_SNIPPETS_TEXT = " - Codeausschnitte";
    private static final String CALCULATIONS_TEXT = " - Berechnungen";

    private static final String CONTENT_MENU_VIEW_FXML = "/fxml/content_menu_view.fxml";
    private static final String QUIZ_VIEW_FXML = "/fxml/quiz_view.fxml";

    private static final String TITLE_FACTS = "Sachfragen";
    private static final String DESCRIPTION_FACTS = "\n\nprüfen Anwendung, Verständnis und Einordnung von Themen.";

    private static final String TITLE_TERMS = "Fachbegriffe";
    private static final String DESCRIPTION_TERMS = "\n\nprüfen Definitionen und Bedeutungen zentraler Begriffe.";

    private static final String TITLE_ABBR = "Abkürzungen";
    private static final String DESCRIPTION_ABBR = "\n\nprüfen Langformen technischer Abkürzungen.";

    private static final String DESCRIPTION_EXTRA = "\n\n\nJe nach Lernfeld können zusätzliche Fragenpakete verfügbar sein.";

    private static final String QUIZ_MENU_TEXT_SPACER = "\n\n\n";
    private static final int TEXT_FLOW_MAX_WIDTH = 560;

    // 20-Fragen-Modus: Poolgröße
    private static final int LF20_QUESTIONS_SIZE = 20;

    // AP1 Gewichtung LF01-LF06: 1:3:3:1:3:1, Multiplikator 3 ergibt 36 Fragen.
    private static final int QUESTION_MULTIPLIER_AP1 = 3;
    private static final int TAKE_QUESTIONS_LF01 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF02 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF03 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF04 = 1 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF05 = 3 * QUESTION_MULTIPLIER_AP1;
    private static final int TAKE_QUESTIONS_LF06 = 1 * QUESTION_MULTIPLIER_AP1;

    private static final String ERROR_NOT_ENOUGH_QUESTIONS = "Zu wenig Fragen für diese Auswahl vorhanden.";
    private static final String ERROR_JSON_PATH = "Keine JSON vorhanden oder falscher Pfad.";
    private static final String ERROR_JSON_SYNTAX = "Fehler in der JSON-Struktur.";

    // FXML: Layout und Texte
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Label statusBarLabel;
    @FXML
    private Label lfTitleLabel;
    @FXML
    private Label flavorLabel;

    // FXML: Hauptbuttons
    @FXML
    private Button factualQuestionsButton;
    @FXML
    private Button technicalTermsButton;
    @FXML
    private Button abbreviationsButton;

    // FXML: optionale Buttons
    @FXML
    private Button subnettingButton;
    @FXML
    private Button codeSnippetsButton;
    @FXML
    private Button calculationsButton;

    // FXML: Action Button
    @FXML
    private Button actionButton;

    // Speicherung für „Letzte Auswahl“
    private Preferences preferences;

    // Quiz-Vorbereitung
    private QuizLoader quizLoader;
    private QuizController quizController;
    private String quizMode;
    private String[] jsonList;

    private QuestionProgressRepository questionProgressRepository;

    private final List<List<QuizQuestion>> lf20QuestionsSessionPools = new ArrayList<>();

    // AP1-Fragenpools je Lernfeld
    private final List<QuizQuestion> lf01QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf02QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf03QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf04QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf05QuizSessionPool = new ArrayList<>();
    private final List<QuizQuestion> lf06QuizSessionPool = new ArrayList<>();

    @FXML
    public void initialize() {
        preferences = Preferences.userRoot().node(SETTINGS_NODE);

        quizLoader = new QuizLoader();
        questionProgressRepository = new QuestionProgressRepository();

        initButtonBehavior();
    }

    public void startContentSubmenuWithQuizMode(String quizMode) {
        this.quizMode = quizMode;
        initUI();
    }

    // Submenu anhand des übergebenen quizMode aufbauen.
    private void initUI() {
        statusBarLabel.setVisible(true);

        lfTitleLabel.setVisible(true);
        flavorLabel.setVisible(false);

        factualQuestionsButton.setVisible(true);
        factualQuestionsButton.setManaged(true);

        technicalTermsButton.setVisible(true);
        technicalTermsButton.setManaged(true);

        abbreviationsButton.setVisible(true);
        abbreviationsButton.setManaged(true);

        subnettingButton.setVisible(false);
        subnettingButton.setManaged(false);

        codeSnippetsButton.setVisible(false);
        codeSnippetsButton.setManaged(false);

        calculationsButton.setVisible(false);
        calculationsButton.setManaged(false);

        if (QUIZ_MODE_LF02.equals(quizMode)) {
            calculationsButton.setVisible(true);
            calculationsButton.setManaged(true);
        }

        if (QUIZ_MODE_LF03.equals(quizMode)) {
            subnettingButton.setVisible(true);
            subnettingButton.setManaged(true);

            calculationsButton.setVisible(true);
            calculationsButton.setManaged(true);
        }

        if (QUIZ_MODE_LF05.equals(quizMode)) {
            codeSnippetsButton.setVisible(true);
            codeSnippetsButton.setManaged(true);

            calculationsButton.setVisible(true);
            calculationsButton.setManaged(true);
        }

        setTextFormatForThreeTitleAndDescription(
                TITLE_FACTS,
                DESCRIPTION_FACTS,
                TITLE_TERMS,
                DESCRIPTION_TERMS,
                TITLE_ABBR,
                DESCRIPTION_ABBR,
                DESCRIPTION_EXTRA
        );

        actionButton.setVisible(true);
    }

    private void initButtonBehavior() {
        factualQuestionsButton.setOnAction(onClick -> addStringToQuizMode(FACTUAL_QUESTIONS_TEXT));
        technicalTermsButton.setOnAction(onClick -> addStringToQuizMode(TECHNICAL_TERMS_TEXT));
        abbreviationsButton.setOnAction(onClick -> addStringToQuizMode(ABBREVIATIONS_TEXT));
        subnettingButton.setOnAction(onClick -> addStringToQuizMode(SUBNETTING_TEXT));
        codeSnippetsButton.setOnAction(onClick -> addStringToQuizMode(CODE_SNIPPETS_TEXT));
        calculationsButton.setOnAction(onClick -> addStringToQuizMode(CALCULATIONS_TEXT));

        actionButton.setOnAction(onClick -> backToContentMenu());
    }

    // Zurück ins ContentMenu wechseln und je nach aktuellem Modus AP1 oder LF anzeigen.
    private void backToContentMenu() {
        try {
            FXMLLoader contentMenuLoader = new FXMLLoader(getClass().getResource(CONTENT_MENU_VIEW_FXML));
            Parent contentMenuRoot = contentMenuLoader.load();

            ContentMenuController contentMenuController = contentMenuLoader.getController();

            if (QUIZ_MODE_AP1.equals(quizMode)) {
                contentMenuController.startContentMenuWithQuizMode(QUIZ_MODE_AP1);
            } else {
                contentMenuController.startContentMenuWithQuizMode(QUIZ_MODE_LF);
            }

            rootLayout.getScene().setRoot(contentMenuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Fragenpaket an den Lernfeld-/AP1-Modus anhängen und finale Quiz-Auswahl vorbereiten.
    private void addStringToQuizMode(String addedString) {
        if (quizMode == null) {
            quizMode = QUIZ_MODE_AP1;
        }

        switch (quizMode) {
            case QUIZ_MODE_AP1:
            case QUIZ_MODE_LF01:
            case QUIZ_MODE_LF02:
            case QUIZ_MODE_LF03:
            case QUIZ_MODE_LF04:
            case QUIZ_MODE_LF05:
            case QUIZ_MODE_LF06:
                setupQuizMode(quizMode + addedString);
                break;

            default:
                setupQuizMode(QUIZ_MODE_AP1 + FACTUAL_QUESTIONS_TEXT);
                break;
        }
    }

    // Finale Quiz-Auswahl auf konkrete JSON-Fragenpakete abbilden und Quiz-View vorbereiten.
    // REFACTOR PRIO 3: JSON Pfade kommen in mindestens 2 Controllern vor -> Auslagern
    private void setupQuizMode(String quizMode) {
        if ("AP1 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
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
            };
        } else if ("AP1 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
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
            };
        } else if ("AP1 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
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
            };
        } else if ("LF-01 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-01/lf-01-sach-arbm.json",
                    "questions/lf-01/lf-01-sach-bwl.json",
                    "questions/lf-01/lf-01-sach-markt.json",
                    "questions/lf-01/lf-01-sach-orga.json",
                    "questions/lf-01/lf-01-sach-sozi.json",
                    "questions/lf-01/lf-01-sach-ziel.json",
            };
        } else if ("LF-01 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-01/lf-01-fach-arbm.json",
                    "questions/lf-01/lf-01-fach-bwl.json",
                    "questions/lf-01/lf-01-fach-markt.json",
                    "questions/lf-01/lf-01-fach-orga.json",
                    "questions/lf-01/lf-01-fach-sozi.json",
                    "questions/lf-01/lf-01-fach-ziel.json",
            };
        } else if ("LF-01 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-01/lf-01-abkz.json",
            };
        } else if ("LF-02 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-02/lf-02-sach-besch.json",
                    "questions/lf-02/lf-02-sach-hard.json",
                    "questions/lf-02/lf-02-sach-kommun.json",
                    "questions/lf-02/lf-02-sach-proj.json",
                    "questions/lf-02/lf-02-sach-recht.json",
                    "questions/lf-02/lf-02-sach-sich.json",
            };
        } else if ("LF-02 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-02/lf-02-fach-besch.json",
                    "questions/lf-02/lf-02-fach-hard.json",
                    "questions/lf-02/lf-02-fach-kalk.json",
                    "questions/lf-02/lf-02-fach-kommun.json",
                    "questions/lf-02/lf-02-fach-proj.json",
                    "questions/lf-02/lf-02-fach-recht.json",
            };
        } else if ("LF-02 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-02/lf-02-abkz.json",
            };
        } else if ("LF-02 - Berechnungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-02/lf-02-rech-finanz.json",
                    "questions/lf-02/lf-02-rech-kalk.json",
                    "questions/lf-02/lf-02-rech-wirt.json",
            };
        } else if ("LF-03 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-03/lf-03-sach-adrs.json",
                    "questions/lf-03/lf-03-sach-cloud.json",
                    "questions/lf-03/lf-03-sach-integ.json",
                    "questions/lf-03/lf-03-sach-komp.json",
                    "questions/lf-03/lf-03-sach-modl.json",
                    "questions/lf-03/lf-03-sach-secu.json",
                    "questions/lf-03/lf-03-sach-strv.json",
            };
        } else if ("LF-03 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-03/lf-03-fach-adrs.json",
                    "questions/lf-03/lf-03-fach-cloud.json",
                    "questions/lf-03/lf-03-fach-komp.json",
                    "questions/lf-03/lf-03-fach-modl.json",
                    "questions/lf-03/lf-03-fach-secu.json",
                    "questions/lf-03/lf-03-fach-strv.json",
            };
        } else if ("LF-03 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-03/lf-03-abkz.json",
            };
        } else if ("LF-03 - Subnetting".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-03/lf-03-subn-ipv4.json",
            };
        } else if ("LF-03 - Berechnungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-03/lf-03-rech-netz.json",
            };
        } else if ("LF-04 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
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
            };
        } else if ("LF-04 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
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
            };
        } else if ("LF-04 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-04/lf-04-abkz.json",
            };
        } else if ("LF-05 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-05/lf-05-sach-data.json",
                    "questions/lf-05/lf-05-sach-db.json",
                    "questions/lf-05/lf-05-sach-dev.json",
                    "questions/lf-05/lf-05-sach-modl.json",
                    "questions/lf-05/lf-05-sach-prog.json",
            };
        } else if ("LF-05 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-05/lf-05-fach-data.json",
                    "questions/lf-05/lf-05-fach-db.json",
                    "questions/lf-05/lf-05-fach-dev.json",
                    "questions/lf-05/lf-05-fach-modl.json",
                    "questions/lf-05/lf-05-fach-prog.json",
            };
        } else if ("LF-05 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-05/lf-05-abkz.json",
            };
        } else if ("LF-05 - Codeausschnitte".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-05/lf-05-code-py.json",
                    "questions/lf-05/lf-05-code-sql.json",
            };
        } else if ("LF-05 - Berechnungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-05/lf-05-rech-zahl.json",
            };
        } else if ("LF-06 - Sachfragen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-06/lf-06-sach-anal.json",
                    "questions/lf-06/lf-06-sach-komm.json",
                    "questions/lf-06/lf-06-sach-moni.json",
                    "questions/lf-06/lf-06-sach-serv.json",
                    "questions/lf-06/lf-06-sach-tick.json",
                    "questions/lf-06/lf-06-sach-vert.json",
                    "questions/lf-06/lf-06-sach-wart.json",
            };
        } else if ("LF-06 - Fachbegriffe".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-06/lf-06-fach-anal.json",
                    "questions/lf-06/lf-06-fach-komm.json",
                    "questions/lf-06/lf-06-fach-moni.json",
                    "questions/lf-06/lf-06-fach-serv.json",
                    "questions/lf-06/lf-06-fach-tick.json",
                    "questions/lf-06/lf-06-fach-vert.json",
                    "questions/lf-06/lf-06-fach-wart.json",
            };
        } else if ("LF-06 - Abkürzungen".equals(quizMode)) {
            jsonList = new String[]{
                    "questions/lf-06/lf-06-abkz.json",
            };
        }

        prepareQuizContentData(quizMode, jsonList);
    }

    // Schlüsselmeister: JSONs und interne Datenbank correct_answer prüfen, Quiz nur bei ausreichend Fragen starten.
    // REFACTOR PRIO 4: Ähnliche Prüfungen in mehreren Controllern -> Schlüsselmeister-/Torwächterlogik auslagern
    private void prepareQuizContentData(String quizMode, String[] jsonList) {
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
        } catch (IOException ioException) {
            flavorLabel.setText(ERROR_JSON_PATH);
            flavorLabel.setVisible(true);
            return;
        } catch (JsonSyntaxException jsonSyntaxException) {
            flavorLabel.setText(ERROR_JSON_SYNTAX);
            flavorLabel.setVisible(true);
            return;
        }

        saveLastQuizData(quizMode, jsonList);
        startQuiz(quizMode, jsonList);
    }

    // Erfolgreich geprüfte Quiz-Auswahl für den Button „Letzte Auswahl“ speichern.
    private void saveLastQuizData(String quizMode, String[] jsonList) {
        // String[] wird als JSON-String gespeichert, da Preferences keine String-Arrays direkt speichert.
        String jsonListString = new Gson().toJson(jsonList);

        preferences.put(LAST_QUIZ_MODE_KEY, quizMode);
        preferences.put(LAST_JSON_LIST_KEY, jsonListString);
        preferences.putBoolean(HAS_LAST_QUIZ_DATA_KEY, true);
    }

    // Geprüften Quizmodus starten und Quiz-View anzeigen.
    private void startQuiz(String quizMode, String[] jsonList) {
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

            dialogLabel.setText("""                            
                    Auswahl bereits abgeschlossen.
                    
                    Zurücksetzen und Quiz starten?
                    """
            );

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

                prepareQuizContentData(quizMode, jsonList);
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

    // Titel und Beschreibung als TextFlow formatieren.
    // REFACTOR PRIO 2: Ähnliche Textformat-Methoden in 3 Controllern -> später in eine gemeinsame Hilfsklasse auslagern.
    private void setTextFormatForThreeTitleAndDescription(
            String titleText1,
            String descriptionText1,
            String titleText2,
            String descriptionText2,
            String titleText3,
            String descriptionText3,
            String extraDescription
    ) {
        Text boldTitle1 = new Text(titleText1);
        boldTitle1.getStyleClass().add("content-title-text");

        Text normalDescription1 = new Text(descriptionText1);
        normalDescription1.getStyleClass().add("content-description-text");

        Text spacer1 = new Text(QUIZ_MENU_TEXT_SPACER);

        Text boldTitle2 = new Text(titleText2);
        boldTitle2.getStyleClass().add("content-title-text");

        Text normalDescription2 = new Text(descriptionText2);
        normalDescription2.getStyleClass().add("content-description-text");

        Text spacer2 = new Text(QUIZ_MENU_TEXT_SPACER);

        Text boldTitle3 = new Text(titleText3);
        boldTitle3.getStyleClass().add("content-title-text");

        Text normalDescription3 = new Text(descriptionText3);
        normalDescription3.getStyleClass().add("content-description-text");

        Text extraNormalDescription = new Text(extraDescription);
        extraNormalDescription.getStyleClass().add("content-description-text");

        TextFlow textFlow = new TextFlow(
                boldTitle1,
                normalDescription1,
                spacer1,
                boldTitle2,
                normalDescription2,
                spacer2,
                boldTitle3,
                normalDescription3,
                extraNormalDescription
        );
        textFlow.setMaxWidth(TEXT_FLOW_MAX_WIDTH);

        lfTitleLabel.setText("");
        lfTitleLabel.setGraphic(textFlow);
        lfTitleLabel.setMaxWidth(TEXT_FLOW_MAX_WIDTH);
        lfTitleLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
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

}
