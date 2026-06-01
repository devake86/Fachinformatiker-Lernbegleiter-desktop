package app.core;

// List um Listen auszugeben.
import java.util.List;

public class QuizQuestion {

    private String id;
    private String question;

    // Das Feld answers der Klasse QuizQuestion HAT eine Liste von Objekten der Klasse QuizAnswer (Komposition).
    // QuizQuestion kennt nur die QuizAnswer Objekte, nicht die JSON-Datei.
    // Die Liste (Container) wird später beim Laden der JSON-Datei mit QuizAnswer-Objekten befüllt.
    // Diese enthalten die Daten der JSON.
    // Realisiert mit List<> aus java.util.List.
    // final verhindert neue Zuweisungen zu answers. Die Liste der Fragen kann verändert werden, aber answers ist immer answers.
    private List<QuizAnswer> answers;
    private String explanation;

    public QuizQuestion (String id, String question, List<QuizAnswer> answers, String explanation) {
        this.id = id;
        this.question = question;
        this.answers = answers;
        this.explanation = explanation;

    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getCorrectAnswerText(){

        // Für jede Antwort in Antworten
        for (QuizAnswer answer : this.answers) {

            // Wenn Antwort richtig
            if (answer.isCorrect()) {

                // Gib Text zurück
                return answer.getText();
            }

        }

        // Rückgabe wenn keine Antwort der Antwortliste richtig (true) ist
        return "Richtige Antwort wurde nicht gefunden. Frage mit ID " + this.id + " scheint fehlerhaft zu sein. ";
    }

}
