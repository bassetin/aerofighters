package com.aerofighters.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ScoreManager {
    private static final String FILE_PATH = "highscore.txt";
    private int highScore = 0;

    public ScoreManager() { load(); }

    public int getHighScore() { return highScore; }

    public void save(int score) {
        if (score > highScore) {
            highScore = score;
            try (FileWriter fw = new FileWriter(FILE_PATH)) {
                fw.write(String.valueOf(highScore));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void load() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            highScore = Integer.parseInt(br.readLine());
        } catch (Exception e) { highScore = 0; }
    }
}
