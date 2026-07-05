package app.filb.core;

public class QuizAnswer {

    private String text;
    private boolean correct;

    public QuizAnswer(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }

}
