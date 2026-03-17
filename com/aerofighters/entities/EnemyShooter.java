package com.aerofighters.entities;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnemyShooter extends Enemy {

    private List<EnemyBullet> bullets = new CopyOnWriteArrayList<>();
    private int shootCooldown = 0;

    public EnemyShooter(int score) {
        super(score, "/resources/EnemyShooter.png");
    }

    @Override
    public void update() {
        super.update();

        shootCooldown--;
        if (shootCooldown <= 0) {
            bullets.add(new EnemyBullet(getCenterX(), getBottomY()));
            shootCooldown = 120;
        }

        for (EnemyBullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isActive());
    }

    public List<EnemyBullet> getBullets() {
        return bullets;
    }

    @Override
    public void render(Graphics g) {
        super.render(g);
        for (EnemyBullet b : bullets) b.render(g);
    }
}