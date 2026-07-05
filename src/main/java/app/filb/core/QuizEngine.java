package app.filb.core;

import java.util.List;

// Verwaltet Fortschritt, Punktezahl und aktuelle Frage einer Quizrunde.
public class QuizEngine {

    private int score = 0;
    private int currentIndex = 0;

    private final List<QuizQuestion> questions;

    public QuizEngine(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public QuizQuestion getCurrentQuestion() {
        return questions.get(currentIndex);
    }

    public boolean checkAnswer(int answerIndex) {
        QuizAnswer selectedAnswer = questions.get(currentIndex).getAnswers().get(answerIndex);
        return selectedAnswer.isCorrect();
    }

    public void answerScore(boolean correct) {
        if (correct) {
            score++;
        }
    }

    public void nextQuestion() {
        currentIndex++;
    }

    public int getScore() {
        return score;
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isLastQuestion() {
        return currentIndex == questions.size() - 1;
    }

}
