package app.io;

import app.core.QuizQuestion;

// Gson um JSON in Java-Objekte umzuwandeln.
import com.google.gson.Gson;

// InputStream(Reader) um Text aus dem Datenstrom zu lesen.
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// utils für Arrays und Listen.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizLoader {

    // Liste aus Fragen der JSON-Datei erstellen.
    public List<QuizQuestion> load(String filename) {

        // Datenstrom auslesen.
        InputStream dataStream = QuizLoader.class.getClassLoader().getResourceAsStream(filename);

        // Gson Objekt zum Umwandeln erzeugen.
        Gson gson = new Gson();

        // Text aus Datenstrom erzeugen.
        // JSON ist Liste von QuizQuestion.
        // Gson erstellt automatisch die Objekte und ruft die Konstruktoren indirekt auf.
        QuizQuestion[] quizArray = gson.fromJson(new InputStreamReader(dataStream), QuizQuestion[].class);

        // Erzeugtes Array als Liste (Interface) zurückgeben und als ArrayList (Objekt) implementieren.
        // Zum Shufflen der Fragen wird eine veränderbare ArrayList (mutable) benötigt da eine normale Liste unveränderbar ist (immutable).
        // Umweg von array über Liste zu ArrayList, da der ArrayList Konstruktor keine Liste erzeugen kann,
        // sondern eine Collection erwartet.
        return new ArrayList<>(Arrays.asList(quizArray));

    }

}
