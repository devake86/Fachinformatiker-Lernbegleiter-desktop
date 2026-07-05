package app.filb.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class QuestionProgressRepository {
    private static final String DB_FOLDER_NAME = ".filb";
    private static final String DB_FILE_NAME = "filb-progress.db";

    private final String databaseUrl;

    public QuestionProgressRepository() {
        try {
            Path databaseFolder = Path.of(System.getProperty("user.home"), DB_FOLDER_NAME);
            Files.createDirectories(databaseFolder);

            Path databaseFile = databaseFolder.resolve(DB_FILE_NAME);
            databaseUrl = "jdbc:sqlite:" + databaseFile.toAbsolutePath();

            String sql =
                    "CREATE TABLE IF NOT EXISTS question_progress (" +
                            "question_id TEXT PRIMARY KEY, " +
                            "correct_answer INTEGER NOT NULL DEFAULT 0, " +
                            "wrong_answer INTEGER NOT NULL DEFAULT 0" +
                            ");";

            try (Connection connection = connect();
                 Statement statement = connection.createStatement()) {

                statement.execute(sql);
            } catch (SQLException exception) {
                throw new RuntimeException("Tabelle question_progress konnte nicht erstellt werden.", exception);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Fortschrittsdatenbank konnte nicht initialisiert werden.", exception);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    public void markCorrect(String questionId) {
        saveQuestionStatus(questionId, true);
    }

    public void markWrong(String questionId) {
        saveQuestionStatus(questionId, false);
    }

    private void saveQuestionStatus(String questionId, boolean correctAnswer) {
        if (questionId == null || questionId.trim().isEmpty()) {
            return;
        }

        int correctAnswerValue = correctAnswer ? 1 : 0;
        int wrongAnswerValue = correctAnswer ? 0 : 1;

        String insertSql =
                "INSERT OR IGNORE INTO question_progress (question_id, correct_answer, wrong_answer) " +
                        "VALUES (?, ?, ?);";

        String updateSql =
                "UPDATE question_progress " +
                        "SET correct_answer = ?, wrong_answer = ? " +
                        "WHERE question_id = ?;";

        try (Connection connection = connect();
             PreparedStatement insertStatement = connection.prepareStatement(insertSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            connection.setAutoCommit(false);

            insertStatement.setString(1, questionId);
            insertStatement.setInt(2, correctAnswerValue);
            insertStatement.setInt(3, wrongAnswerValue);
            insertStatement.executeUpdate();

            updateStatement.setInt(1, correctAnswerValue);
            updateStatement.setInt(2, wrongAnswerValue);
            updateStatement.setString(3, questionId);
            updateStatement.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new RuntimeException("Fragenstatus konnte nicht gespeichert werden.", exception);
        }
    }

    public Set<String> getCorrectQuestionIds() {
        Set<String> questionIds = new HashSet<>();

        String sql =
                "SELECT question_id " +
                        "FROM question_progress " +
                        "WHERE correct_answer = 1;";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                questionIds.add(resultSet.getString("question_id"));
            }

            return questionIds;
        } catch (SQLException exception) {
            throw new RuntimeException("Richtige Fragen konnten nicht geladen werden.", exception);
        }
    }

    public Set<String> getWrongQuestionIds() {
        Set<String> questionIds = new HashSet<>();

        String sql =
                "SELECT question_id " +
                        "FROM question_progress " +
                        "WHERE wrong_answer = 1;";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                questionIds.add(resultSet.getString("question_id"));
            }

            return questionIds;
        } catch (SQLException exception) {
            throw new RuntimeException("Falsche Fragen konnten nicht geladen werden.", exception);
        }
    }

    public void resetCorrectAnswersForQuestionIds(Collection<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }

        String placeholders = placeholderForSqlInQuery(questionIds.size());

        String sql =
                "UPDATE question_progress " +
                        "SET correct_answer = 0 " +
                        "WHERE correct_answer = 1 " +
                        "AND question_id IN (" + placeholders + ");";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int parameterIndex = 1;

            for (String questionId : questionIds) {
                statement.setString(parameterIndex, questionId);
                parameterIndex++;
            }

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Richtige Fragen konnten nicht zurückgesetzt werden.", exception);
        }
    }

    private String placeholderForSqlInQuery(int count) {
        List<String> placeholders = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            placeholders.add("?");
        }

        return String.join(", ", placeholders);
    }

}