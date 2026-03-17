package com.aerofighters.entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public abstract class PowerUp {

    protected float x, y;
    protected boolean active = true;
    protected static final int SIZE = 40;

    protected BufferedImage sprite;

    public PowerUp(float x, float y) {
        this.x = x;
        this.y = y;
        loadSprite();
    }

    protected abstract void loadSprite();
    public abstract void applyTo(Player player);

    public void update() {
        y += 2;
        if (y > 800) active = false;
    }

    public void render(Graphics g) {
        if (!active) return;

        Graphics2D g2 = (Graphics2D) g;

        // animação flutuante
        float floatOffset = (float) Math.sin(System.nanoTime() * 0.000000004) * 4;

        // rotação suave
        float angle = (float) (System.nanoTime() * 0.000000001);

        // pulso do brilho
        float pulse = (float) (Math.sin(System.nanoTime() * 0.000000003) * 0.5 + 0.5);

        int glowSize = SIZE + 6;

        int cx = (int) (x + SIZE / 2);
        int cy = (int) (y + SIZE / 2);

        // glow suave (não é círculo sólido)
        g2.setColor(new Color(0, 255, 255, (int)(60 * pulse)));
        g2.fillOval(
                (int)x - 3,
                (int)(y + floatOffset) - 3,
                glowSize,
                glowSize
        );

        // salva estado
        AffineTransform old = g2.getTransform();

        // aplica rotação
        g2.rotate(angle, cx, cy + floatOffset);

        if (sprite != null) {
            g.drawImage(sprite, (int)x, (int)y, SIZE, SIZE, null);
        } else {
            // fallback (caso imagem não carregue)
            g.setColor(Color.MAGENTA);
            g.fillRect((int)x, (int)y, SIZE, SIZE);
        }
        g2.setTransform(old);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, SIZE, SIZE);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}