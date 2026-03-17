package com.aerofighters.entities;

import java.awt.*;

public class EnemyBullet {
    private int x, y;
    private int speed = 6;
    private boolean active = true;

    public EnemyBullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        y += speed;
        if (y > 800) active = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 6, 12);
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
    public void render(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, 6, 12);
    }
}