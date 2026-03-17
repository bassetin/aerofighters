package com.aerofighters.entities;

import javax.imageio.ImageIO;
import java.io.IOException;

public class MultiShotPowerUp extends PowerUp {

    public MultiShotPowerUp(float x, float y) {
        super(x, y);
    }

    @Override
    protected void loadSprite() {
        try {
            sprite = ImageIO.read(
                    getClass().getResource("/powerups/MultiShotPowerUp.png")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void applyTo(Player player) {
        player.upgradeShotLevel();
    }
}