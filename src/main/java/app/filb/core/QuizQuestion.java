package app.filb.core;

import java.util.List;

// JSON-Modellklasse: Eine Frage enthält mehrere QuizAnswer-Objekte.
public class QuizQuestion {

    private String id;
    private String question;
    private List<QuizAnswer> answers;
    private String explanation;

    public QuizQuestion(String id, String question, List<QuizAnswer> answers, String explanation) {
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

}
