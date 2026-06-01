package app.core;

import java.util.List;

public class QuizEngine {

    private int score = 0;

    private List<QuizQuestion> questions;

    // currentIndex zum Bestimmen von aktuelle/nächste Frage und zum Prüfen ob noch Fragen vorhanden sind
    private int currentIndex = 0;

    // Erzeuge Liste aus allen Quizfragen und mische diese und begrenze Größe des Fragenpools
    public QuizEngine(List<QuizQuestion> questions) {
        this.questions = questions;

    }

    // Aktuelle Frage erhalten
    public QuizQuestion getCurrentQuestion() {
        return questions.get(currentIndex);
    }

    // Antwort (Index) bewerten
    public boolean checkAnswer(int answerIndex) {

        // Hole Antworten der aktuellen Frage und die ausgewählte Antwort
        QuizAnswer selectedAnswer = questions.get(currentIndex).getAnswers().get(answerIndex);

        // Gibt zurück ob richtig/falsch
        return selectedAnswer.isCorrect();
    }

    // Punktezahl erhöhen wenn Antwort richtig
    public void answerScore(boolean correct) {
        if (correct) {
            score++;
        }
    }

    // Fragenpool Index erhöhen
    public void nextQuestion() {
        currentIndex++;
    }

    // Prüfe ob offene Fragen vorhanden wenn currentIndex kleiner Fragenpool
    public boolean hasNextQuestion() {
        return currentIndex < questions.size();
    }

    // Punktezahl
    public int getScore() {
        return score;
    }

    // Fragenpoolgröße
    public int getQuestionCount(){
        return questions.size();
    }

    // Fragenpool Index
    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isLastQuestion() {
        return currentIndex == questions.size() - 1;
    }
}
