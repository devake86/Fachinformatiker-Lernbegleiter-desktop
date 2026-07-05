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

public class QuizMenuController {
    private static final String QUIZ_MODE_AP1 = "AP1";
    private static final String QUIZ_MODE_LF = "LF";

    private static final String CONTENT_MENU_VIEW_FXML = "/fxml/content_menu_view.fxml";
    private static final String MENU_VIEW_FXML = "/fxml/menu_view.fxml";

    private static final String TITLE_AP1 = "Abschlussprüfung";
    private static final String DESCRIPTION_AP1 = "\n\nEine Fragenmischung über mehrere Lernfelder.";

    private static final String TITLE_LF = "Lernfeld-Vertiefung";
    private static final String DESCRIPTION_LF = "\n\nEine gezielte Vertiefung eines einzelnen Lernfeldes.";

    private static final String QUIZ_MENU_TEXT_SPACER = "\n\n\n";
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
    private Button start20QuestionsButton;

    // FXML: Action Button
    @FXML
    private Button actionButton;

    @FXML
    public void initialize() {
        initUI();
        initButtonBehavior();
    }

    // Quiz-Auswahlmenü anzeigen.
    private void initUI() {
        statusBarLabel.setVisible(true);

        lfTitleLabel.setVisible(true);
        flavorLabel.setVisible(true);

        setTextFormatForTwoTitleAndDescription(
                TITLE_AP1,
                DESCRIPTION_AP1,
                TITLE_LF,
                DESCRIPTION_LF
        );

        ap1Button.setVisible(true);
        ap1Button.setManaged(true);

        start20QuestionsButton.setVisible(true);
        start20QuestionsButton.setManaged(true);

        actionButton.setVisible(true);
    }

    private void initButtonBehavior() {
        ap1Button.setOnAction(onClick -> loadContentMenu(QUIZ_MODE_AP1));
        start20QuestionsButton.setOnAction(onClick -> loadContentMenu(QUIZ_MODE_LF));

        actionButton.setOnAction(onClick -> backToMainMenu());
    }

    // ContentMenu laden und gewählten Hauptmodus weitergeben.
    private void loadContentMenu(String quizMode) {
        try {
            FXMLLoader contentMenuLoader = new FXMLLoader(getClass().getResource(CONTENT_MENU_VIEW_FXML));
            Parent contentMenuRoot = contentMenuLoader.load();

            ContentMenuController contentMenuController = contentMenuLoader.getController();
            contentMenuController.startContentMenuWithQuizMode(quizMode);

            rootLayout.getScene().setRoot(contentMenuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Zurück ins Hauptmenü wechseln.
    private void backToMainMenu() {
        try {
            FXMLLoader menuLoader = new FXMLLoader(getClass().getResource(MENU_VIEW_FXML));
            Parent menuRoot = menuLoader.load();

            rootLayout.getScene().setRoot(menuRoot);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    // Titel und Beschreibung als TextFlow formatieren.
    // REFACTOR PRIO 2: Ähnliche Textformat-Methoden in 3 Controllern -> später in eine gemeinsame Hilfsklasse auslagern.
    private void setTextFormatForTwoTitleAndDescription(
            String titleText1,
            String descriptionText1,
            String titleText2,
            String descriptionText2
    ) {
        Text title1 = new Text(titleText1);
        title1.getStyleClass().add("content-title-text");

        Text description1 = new Text(descriptionText1);
        description1.getStyleClass().add("content-description-text");

        Text spacer = new Text(QUIZ_MENU_TEXT_SPACER);

        Text title2 = new Text(titleText2);
        title2.getStyleClass().add("content-title-text");

        Text description2 = new Text(descriptionText2);
        description2.getStyleClass().add("content-description-text");

        TextFlow textFlow = new TextFlow(
                title1,
                description1,
                spacer,
                title2,
                description2
        );
        textFlow.setMaxWidth(TEXT_FLOW_MAX_WIDTH);
        lfTitleLabel.setText("");
        lfTitleLabel.setGraphic(textFlow);
        lfTitleLabel.setMaxWidth(TEXT_FLOW_MAX_WIDTH);
        lfTitleLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    }

}
