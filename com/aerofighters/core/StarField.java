package com.aerofighters.core;

import java.awt.*;
import java.util.Random;

public class StarField {
    private int[] x, y, speed;
    private static final int STAR_COUNT = 100;


    public StarField() {
        // inicialize os arrays com posições e velocidades aleatórias
        Random random = new Random();
        this.x = new int[STAR_COUNT];
        this.y = new int[STAR_COUNT];
        this.speed = new int[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            x[i] = random.nextInt(800);
            y[i] = random.nextInt(800);
            speed[i] = 1 + random.nextInt(3); // velocidade entre 1 e 3
        }
    }

    public void update() {
        // mova cada estrela para baixo, se passar de 600 volta ao topo
        for (int i = 0; i < STAR_COUNT; i++) {
            y[i] += speed[i];
            if (y[i] > 800) y[i] = 0;
        }

    }

    public void render(Graphics g) {
        // desenhe cada estrela como um ponto branco pequeno
        g.setColor(Color.WHITE);
        for (int i = 0; i < STAR_COUNT; i++) {
            g.fillRect(x[i], y[i], 2, 2);
        }
    }
}
