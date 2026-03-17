package com.aerofighters.entities;


import com.aerofighters.core.SoundManager;
import com.aerofighters.input.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class Player {

    //POSIÇÃO DO JOGADOR
    private int positionY = 400;
    private int positionX = 400;

    //TAMANHO DO JOGADOR
    private final int WIDTH = 40;
    private final int HEIGHT = 40;

    //VELOCIDADE DO JOGADOR
    private int  speed = 5;

    //VIDA DO JOGADOR
    private int lives = 3;
    private int invincibleTimer = 0;

    // POWER-UPS ATIVOS
    private int shotLevel = 1;        // 1 = normal, 2 = duplo, 3 = triplo
    private boolean speedBoost = false;

    // SHIELD
    private boolean shieldActive = false;
    private int shieldHealth = 100;
    private int shieldTimer = 0;

    // LASER
    private boolean hasLaser = false;
    private boolean laserFiring = false;

    private float laserCharge = 0;
    private final int MAX_LASER = 100;

    private int laserRechargeLevel = 0;   // quantos powerups pegou
    private float laserRechargeSpeed = 0.1f; // começa MUITO lento

    private Laser laser;

    // CONSTANTES
    private static final int SHIELD_DURATION = 360;  // 10 segundos a 60fps
    private static final int BASE_SPEED = 5;
    private static final int BOOST_SPEED = 9;

    //OBJETOS
    private KeyHandler keyH;
    private BufferedImage sprite;

    //LISTA DE BALAS
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private int shootCooldown = 0; // evita spam de tiro

    //SONS
    private SoundManager soundManager;

    public Player(KeyHandler keyH, SoundManager soundManager){
        this.keyH = keyH;
        this.soundManager = soundManager;
        laserCharge = 0;
        hasLaser = false;
        laserRechargeLevel = 0;
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/resources/PlayerShip.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //INVENCIBILIDADE
    public boolean isInvincible() {
        return invincibleTimer > 0;
    }

    //SOFRER DANO
    public void hit() {
        if (invincibleTimer > 0) return;

        if (shieldActive) {
            shieldHealth -= 25;
            invincibleTimer = 60;
            if (shieldHealth <= 0) {
                shieldActive = false;
            }
            return;
        }

        //PERDE OS POWER UPS
        lives--;
        invincibleTimer = 120;
        shotLevel = 1;
        speedBoost = false;
        laserCharge = 0;
        laserFiring = false;
    }

    public int getLives() { return lives; }
    public boolean isAlive() { return lives > 0; }

    public List<Bullet> getBullets() {
        return bullets;
    }
    public Laser getLaser() {
        return laser;
    }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, WIDTH, HEIGHT);
    }

    //POWER UPS
    public void activateShield() {
        shieldActive = true;
        shieldHealth = 100;
        shieldTimer = SHIELD_DURATION;
    }
    public void upgradeShotLevel() { if (shotLevel < 3) shotLevel++; }
    public void activateSpeedBoost() { speedBoost = true; }
    public void addLaserCharge() {
        hasLaser = true;

        laserRechargeLevel++;

        laserRechargeSpeed = Math.min(1.5f, laserRechargeSpeed + 0.2f);

        // dá um pouco de carga inicial
        laserCharge = Math.min(MAX_LASER, laserCharge + 15);
    }
    public boolean isShieldActive() { return shieldActive; }
    public float getLaserCharge() { return laserCharge; }

    public void update(){

        if (invincibleTimer > 0) invincibleTimer--;
        int currentSpeed = speedBoost ? BOOST_SPEED : BASE_SPEED;
        if(keyH.upPressed) positionY -= currentSpeed;
        if(keyH.downPressed) positionY += currentSpeed;
        if(keyH.leftPressed) positionX -= currentSpeed;
        if(keyH.rightPressed) positionX += currentSpeed;

        if (positionX < 0) positionX = 0;
        if (positionY < 0) positionY = 0;
        if (positionX + 40 > 800) positionX = 800 - 40;
        if (positionY + 40 > 800) positionY = 800 - 40;
        // escudo
        if (shieldActive) {
            shieldTimer--;
            if (shieldTimer <= 0) shieldActive = false;
        }

        // ===== LASER SYSTEM =====

// recarrega sempre (se tiver laser desbloqueado)
        if (hasLaser && !laserFiring && laserCharge < MAX_LASER) {
            laserCharge += laserRechargeSpeed;
            if (laserCharge > MAX_LASER) laserCharge = MAX_LASER;
        }

// dispara laser
        if (hasLaser && keyH.laserPressed && laserCharge > 0) {

            laserFiring = true;
            laserCharge -= 2; // consome rápido

            int laserX = positionX + WIDTH / 2 - 3;
            int laserY = positionY;

            laser = new Laser(laserX, laserY);

        } else {
            laserFiring = false;
            laser = null;
        }

        if (keyH.spacePressed && shootCooldown == 0) {
            if (shotLevel == 1) {
                bullets.add(new Bullet(positionX + 17, positionY, 0));
            } else if (shotLevel == 2) {
                bullets.add(new Bullet(positionX + 7,  positionY, 0));
                bullets.add(new Bullet(positionX + 27, positionY, 0));
            } else {
                bullets.add(new Bullet(positionX + 17, positionY, 0));      // centro
                bullets.add(new Bullet(positionX + 5,  positionY, -3));     // diagonal esq
                bullets.add(new Bullet(positionX + 29, positionY, 3));      // diagonal dir
            }
            soundManager.playSound("BulletSound.wav");
            shootCooldown = 20;
        }
        if (shootCooldown > 0) shootCooldown--;

        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isActive());
    }

    public void render(Graphics g){
        //EFEITO DE PISCAR AO SOFRER DANO
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        //CRIAR AS BALAS
        for (Bullet b : bullets) b.render(g);

        if (laser != null) {
            laser.render(g);
        }
        if (shieldActive) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(0, 200, 255, 80));
            g2.fillOval(positionX - 8, positionY - 8, WIDTH + 16, HEIGHT + 16);

            g2.setColor(Color.CYAN);
            g2.drawOval(positionX - 8, positionY - 8, WIDTH + 16, HEIGHT + 16);
        }

        if (shieldActive && shieldTimer < 120) {
            if ((System.currentTimeMillis() / 150) % 2 == 0) return;
        }

        //EXECUTAR O PROCESSAMENTO DAS IMAGENS
        if (sprite != null) {
            g.drawImage(sprite, positionX, positionY, WIDTH, HEIGHT, null);
        } else {
            // fallback caso a imagem não carregue
            g.setColor(Color.WHITE);
            g.fillRect(positionX, positionY, WIDTH, HEIGHT);
        }
    }


}
