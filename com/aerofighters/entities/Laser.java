package com.aerofighters.entities;

import java.awt.*;

public class Laser {

    private int x;
    private int y;
    private int width = 6;
    private int height;

    public Laser(int x, int y) {
        this.x = x;
        this.y = y;
        this.height = y; // vai do player até o topo da tela
    }

    public void update() {
        // laser é instantâneo, não se move
    }

    public void render(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(x, 0, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, 0, width, height);
    }
}