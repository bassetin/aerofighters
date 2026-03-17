package com.aerofighters.core;

import javax.swing.JFrame;

public class GameWindow {


    public GameWindow() {
        JFrame window = new JFrame("AeroFighters da Shopee");
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gamePanel.startGameThread();
    }
}
