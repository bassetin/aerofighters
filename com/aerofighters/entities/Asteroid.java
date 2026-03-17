package com.aerofighters.entities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class Asteroid {

    private float x, y;
    private float speedY;
    private float driftX;
    private int size;
    private boolean active = true;
    private int health = 2;

    // animação
    private BufferedImage[] frames;
    private int frameIndex = 0;
    private int frameCounter = 0;
    private int frameDelay = 5;

    private double rotation = 0;
    private double rotationSpeed;

    private static final int FRAME_WIDTH = 128;   // ajuste conforme seu sprite
    private static final int FRAME_HEIGHT = 128;
    private static final int TOTAL_FRAMES = 64;   // quantos frames tem seu sheet

    public Asteroid() {
        Random r = new Random();

        x = r.nextInt(760);
        y = -100;

        speedY = 2 + r.nextFloat() * 2;
        driftX = (r.nextFloat() - 0.5f) * 0.5f;

        size = 40 + r.nextInt(40);
        rotationSpeed = (r.nextFloat() - 0.5f) * 0.05;

        loadSpriteSheet();
    }

    public int getCenterX() {
        return getBounds().x + getBounds().width / 2;
    }

    public int getCenterY() {
        return getBounds().y + getBounds().height / 2;
    }

    private void loadSpriteSheet() {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResource("/AsteroidAnimation.png"));

            frames = new BufferedImage[TOTAL_FRAMES];

            int cols = sheet.getWidth() / FRAME_WIDTH;

            for (int i = 0; i < TOTAL_FRAMES; i++) {
                int col = i % cols;
                int row = i / cols;

                frames[i] = sheet.getSubimage(
                        col * FRAME_WIDTH,
                        row * FRAME_HEIGHT,
                        FRAME_WIDTH,
                        FRAME_HEIGHT
                );
            }

            // começa em frame aleatório
            frameIndex = new Random().nextInt(TOTAL_FRAMES);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hit() {
        health--;
        if (health <= 0) active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }

    public PowerUp dropPowerUp() {
        Random r = new Random();
        int chance = r.nextInt(100);
        float cx = getCenterX();
        float cy = getCenterY();

        if (chance < 25) return new ShieldPowerUp(cx, cy);
        if (chance < 50) return new MultiShotPowerUp(cx, cy);
        if (chance < 75) return new SpeedPowerUp(cx, cy);
        if (chance < 90) return new LaserPowerUp(cx, cy);
        return null; // 10% de chance de não dropar nada
    }

    public void update() {
        y += speedY;
        x += driftX;

        rotation += rotationSpeed;



        // animação
        if (frames != null) { //  só anima se carregou
            frameCounter++;
            if (frameCounter >= frameDelay) {
                frameIndex = (frameIndex + 1) % frames.length;
                frameCounter = 0;
            }
        }

        if (y > 820) active = false;
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform old = g2.getTransform(); // salva

        if (frames == null) {
            g.setColor(Color.GRAY);
            g.fillOval((int)x, (int)y, size, size);
            g2.setTransform(old); // restaura mesmo no fallback
            return;
        }

        int drawX = (int) x;
        int drawY = (int) y;

        g2.translate(drawX + size / 2, drawY + size / 2);
        g2.rotate(rotation);
        g2.drawImage(frames[frameIndex], -size/2, -size/2, size, size, null);

        g2.setTransform(old); // restaura direto —
    }
}