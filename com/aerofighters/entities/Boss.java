package com.aerofighters.entities;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Boss extends Enemy {
    private int maxHealth;
    private int currentHealth;
    private int phase = 1; // muda comportamento com a vida
    private int shootCooldown = 0;
    private List<EnemyBullet> bullets = new CopyOnWriteArrayList<>();

    public Boss(int wave) {
        super(0, "/resources/Boss.png");
        this.positionX = 340; // centro
        this.positionY = -150;
        this.maxHealth = 20 + wave * 5;
        this.currentHealth = maxHealth;
        this.speed = 2;
    }

    public int getPhase() { return phase; }

    public Rectangle getBounds() {
        return new Rectangle(positionX, positionY, 120, 120); // ✅ tamanho maior
    }

    @Override
    public void update() {
        // desce até posição fixa no topo
        if (positionY < 50) {
            positionY += speed;
            return;
        }

        // patrulha horizontal
        positionX += speed;
        if (positionX > 720 || positionX < 40) speed = -speed;

        // muda de fase com metade da vida
        if (currentHealth <= maxHealth / 2) phase = 2;

        // atira
        shootCooldown--;
        int cooldown = phase == 1 ? 60 : 30; // fase 2 atira mais rápido
        if (shootCooldown <= 0) {
            if (phase == 1) {
                bullets.add(new EnemyBullet(positionX + 40, positionY + 80));
            } else {
                // fase 2: 3 balas em spread
                bullets.add(new EnemyBullet(positionX + 20, positionY + 80));
                bullets.add(new EnemyBullet(positionX + 40, positionY + 80));
                bullets.add(new EnemyBullet(positionX + 60, positionY + 80));
            }
            shootCooldown = cooldown;
        }

        for (EnemyBullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isActive());
    }

    public void hit() {
        currentHealth--;
        if (currentHealth <= 0) active = false;
    }

    public List<EnemyBullet> getBullets() { return bullets; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }

    @Override
    public void render(Graphics g) {
        super.render(g);
        for (EnemyBullet b : bullets) b.render(g);

    }
}
