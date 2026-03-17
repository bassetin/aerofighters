package com.aerofighters.entities;

public class EnemyFast extends Enemy {

    public EnemyFast(int score) {
        super(score, "/resources/EnemyFast.png");
        speed += 3;
    }

    @Override
    public void update() {
        positionY += speed;
        positionX += Math.sin(positionY * 0.1) * 3;

        if (positionY > 800) active = false;
    }
}
