package com.aerofighters.entities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class Enemy {

    protected int positionX;
    protected int positionY;

    protected static final int WIDTH = 40;
    protected static final int HEIGHT = 40;

    protected int speed;
    protected boolean active = true;

    protected BufferedImage sprite;

    public Enemy(int score, String spritePath) {
        Random random = new Random();
        this.positionX = random.nextInt(760);
        this.positionY = -40;

        speed = 3 + (score / 5);
        if (speed > 10) speed = 10;

        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(spritePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        positionY += speed;
        if (positionY > 800) active = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, WIDTH, HEIGHT);
    }

    public int getCenterY() {
        return positionY + HEIGHT / 2;
    }

    public int getCenterX() {
        return positionX + WIDTH / 2;
    }

    public int getBottomY() {
        return positionY + HEIGHT;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean a) { active = a; }

    public void render(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, positionX, positionY, WIDTH, HEIGHT, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(positionX, positionY, WIDTH, HEIGHT);
        }
    }
}