package com.aerofighters.entities;


import javax.imageio.ImageIO;
import java.io.IOException;

public class SpeedPowerUp extends PowerUp {

    public SpeedPowerUp(float x, float y) {
        super(x, y);
    }

    @Override
    protected void loadSprite() {
        try {
            sprite = ImageIO.read(
                    getClass().getResource("/powerups/SpeedPowerUp.png")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void applyTo(Player player) {
        player.activateSpeedBoost();
    }
}

