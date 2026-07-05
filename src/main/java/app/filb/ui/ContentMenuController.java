package app.filb.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class ContentMenuController {
    private static final String QUIZ_MODE_AP1 = "AP1";
    private static final String QUIZ_MODE_LF = "LF";

    private static final String QUIZ_MODE_LF01 = "LF-01";
    private static final String QUIZ_MODE_LF02 = "LF-02";
    private static final String QUIZ_MODE_LF03 = "LF-03";
    private static final String QUIZ_MODE_LF04 = "LF-04";
    private static final String QUIZ_MODE_LF05 = "LF-05";
    private static final String QUIZ_MODE_LF06 = "LF-06";

    private static final String TITLE_AP1 = "Abschlussprüfung 1";
    private static final String DESCRIPTION_AP1 =
            "\n\nEine prüfungsnahe Mischung aus den Lernfeldern 01 bis 06 mit 36 Fragen.";

    private static final String TITLE_LF = "Lernfeld-Vertiefung";
    private static final String DESCRIPTION_LF =
            "\n\nEine gezielte 20-Fragen-Runde zur Wiederholung und Vertiefung eines einzelnen Lernfeldes.";

    private static final String CONTENT_SUBMENU_VIEW_FXML = "/fxml/content_submenu_view.fxml";
    private static final String QUIZ_MENU_VIEW_FXML = "/fxml/quiz_menu_view.fxml";

    private static final int TEXT_FLOW_MAX_WIDTH = 560;

    // FXML: Layout und Texte
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Label statusBarLabel;
    @FXML
    private Label lfTitleLabel;
    @FXML
    private Label flavorLabel;

    // FXML: Auswahlbuttons
    @FXML
    private Button ap1Button;
    @FXML
    private Button lf01Button;
    @FXML
    private Button lf02Button;
    @FXML
    private Button lf03Button;
    @FXML
    private Button lf04Button;
    @FXML
    private Button lf05Button;
    @FXML
    private Button lf06Button;

    // FXML: Action Button
    @FXML
    private Button actionButton;

    // Quiz-Vorbereitung
    private String quizMode;

    @FXML
    public void initialize() {
        initButtonBehavior();
    }

    public void startContentMenuWithQuizMode(String quizMode) {
        this.quizMode = quizMode;
        initUI();
    }

    // ContentMenu anhand des übergebenen quizMode aufbauen.
    private void initUI() {
        statusBarLabel.setVisible(true);

        lfTitleLabel.setVisible(true);
        flavorLabel.setVisible(true);

        if (QUIZ_MODE_AP1.equals(quizMode)) {
            setTextFormatForTitleAndDescription(
                    TITLE_AP1,
                    DESCRIPTION_AP1
            );

            ap1Button.setVisible(true);
            ap1Button.setManaged(true);
        } else if (QUIZ_MODE_LF.equals(quizMode)) {
            setTextFormatForTitleAndDescription(
                    TITLE_LF,
                    DESCRIPTION_LF
            );

            lf01Button.setVisible(true);
            lf01Button.setManaged(true);

            lf02Button.setVisible(true);
            lf02Button.setManaged(true);

            lf03Button.setVisible(true);
            lf03Button.setManaged(true);

            lf04Button.setVisible(true);
            lf04Button.setManaged(true);

            lf05Button.setVisible(true);
            lf05Button.setManaged(true);

            lf06Button.setVisible(true);
            lf06Button.setManaged(true);
        }

        actionButton.setVisible(true);
    }

    private void initButtonBehavior() {
        ap1Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_AP1));
        lf01Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF01));
        lf02Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF02));
        lf03Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF03));
        lf04Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF04));
        lf05Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF05));
        lf06Button.setOnAction(onClick -> loadContentSubmenu(QUIZ_MODE_LF06));

        actionButton.setOnAction(onClick -> backToQuizMenu());
    }

    // ContentSubmenu laden und ausgewählten Modus weitergeben.
    private void loadContentSubmenu(String quizMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CONTENT_SUBMENU_VIEW_FXML));
            Parent contentSubmenuRoot = loader.load();

            ContentSubmenuController contentSubmenuController = loader.getController();
            contentSubmenuController.startContentSubmenuWithQuizMode(quizMode);

            rootLayout.getScene().setRoot(contentSubmenuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Zurück ins Quiz-Auswahlmenü wechseln.
    private void backToQuizMenu() {
        try {
            FXMLLoader quizMenuLoader = new FXMLLoader(getClass().getResource(QUIZ_MENU_VIEW_FXML));
            Parent quizMenuRoot = quizMenuLoader.load();

            rootLayout.getScene().setRoot(quizMenuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Titel und Beschreibung als TextFlow formatieren.
    // REFACTOR PRIO 2: Ähnliche Textformat-Methoden in 3 Controllern -> später in eine gemeinsame Hilfsklasse auslagern.
    private void setTextFormatForTitleAndDescription(String titleText, String descriptionText) {
        Text title = new Text(titleText);
        title.getStyleClass().add("content-title-text");

        Text description = new Text(descriptionText);
        description.getStyleClass().add("content-description-text");

        TextFlow textFlow = new TextFlow(title, description);
        textFlow.setMaxWidth(TEXT_FLOW_MAX_WIDTH);

        lfTitleLabel.setText("");
        lfTitleLabel.setGraphic(textFlow);
        lfTitleLabel.setMaxWidth(TEXT_FLOW_MAX_WIDTH);
        lfTitleLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    }

}
