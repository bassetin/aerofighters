package com.aerofighters.entities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimatedEffect {

    private int x, y;
    private BufferedImage[] frames;
    private int frameIndex;
    private int frameCounter = 0;
    private int frameDelay;
    private boolean finished = false;
    private int size;
    private int startFrame;
    private int endFrame;

    public AnimatedEffect(int x, int y,
                          String spritePath,
                          int frameWidth,
                          int frameHeight,
                          int frameDelay,
                          int size,
                          int startFrame,
                          int endFrame) {

        this.x = x;
        this.y = y;
        this.frameDelay = frameDelay;
        this.size = size;

        loadSprite(spritePath, frameWidth, frameHeight);

        // garante limites válidos
        this.startFrame = Math.max(0, startFrame);
        this.endFrame = Math.min(endFrame, frames.length - 1);

        this.frameIndex = this.startFrame;
    }

    private void loadSprite(String path, int w, int h) {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResource(path));

            int cols = sheet.getWidth() / w;
            int rows = sheet.getHeight() / h;

            frames = new BufferedImage[cols * rows];

            int count = 0;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    frames[count++] = sheet.getSubimage(col * w, row * h, w, h);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (finished) return;

        frameCounter++;

        if (frameCounter >= frameDelay) {
            frameCounter = 0;
            frameIndex++;

            if (frameIndex > endFrame || frameIndex >= frames.length) {
                finished = true;
                frameIndex = endFrame; // trava no último frame válido
            }
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void render(Graphics g) {
        if (finished) return;

        if (frameIndex < 0 || frameIndex >= frames.length) return;

        g.drawImage(frames[frameIndex],
                x - size / 2,
                y - size / 2,
                size,
                size,
                null);
    }
}