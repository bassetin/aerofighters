package com.aerofighters.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    public boolean upPressed,downPressed,leftPressed,rightPressed,spacePressed,restartPressed,enterPressed,cPressed,escPressed,laserPressed,pausePressed;


    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = true;
        if (key == KeyEvent.VK_S) downPressed = true;
        if (key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_D) rightPressed = true;
        if (key == KeyEvent.VK_SPACE) spacePressed = true;
        if (key == KeyEvent.VK_SHIFT) laserPressed = true;
        if (key == KeyEvent.VK_R) restartPressed = true;
        if (key == KeyEvent.VK_C) cPressed = true;
        if (key == KeyEvent.VK_ESCAPE) pausePressed = true;
        if (key == KeyEvent.VK_ESCAPE) escPressed = true;
        if (key == KeyEvent.VK_ENTER) enterPressed = true;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W) upPressed = false;
        if (key == KeyEvent.VK_S) downPressed = false;
        if (key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_D) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) spacePressed = false;
        if (key == KeyEvent.VK_SHIFT) laserPressed = false;
        if (key == KeyEvent.VK_R) restartPressed = false;
        if (key == KeyEvent.VK_C) cPressed = false;
        if (key == KeyEvent.VK_ESCAPE) pausePressed = false;
        if (key == KeyEvent.VK_ESCAPE) escPressed = false;
        if (key == KeyEvent.VK_ENTER) enterPressed = false;
    }

}
