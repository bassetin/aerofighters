package com.aerofighters.entities;

import com.aerofighters.input.KeyHandler;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Bullet {

    private int positionX;
    private int positionY;
    private int velocityX;
    private final int WIDTH = 15;
    private final int HEIGHT = 25;
    private int speed = 10;
    private boolean active = true;
    private BufferedImage sprite;

    public Bullet(int positionX, int positionY, int velocityX) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.velocityX = velocityX;
        this.active = true;

        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/resources/Bullet.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, WIDTH, HEIGHT);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void update(){
        positionX += velocityX;
        positionY -= speed;
        if (positionY < 0 || positionX < 0 || positionX > 800) active = false;
    }

    protected void render(Graphics g){
        if (sprite != null) {
            g.drawImage(sprite, positionX, positionY, WIDTH, HEIGHT, null);
        } else {
            // fallback caso a imagem não carregue
            g.setColor(Color.WHITE);
            g.fillRect(positionX, positionY, WIDTH, HEIGHT);
        }
    }
}
