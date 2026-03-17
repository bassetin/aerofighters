package com.aerofighters.core;

import com.aerofighters.entities.*;
import com.aerofighters.input.KeyHandler;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;




public class GamePanel extends JPanel implements Runnable {

    //CONFIGURAÇÕES DE TELA
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final double FPS = 60.0;
    private static final double timePerFrame = 1000000000 / FPS;
    private boolean running = true;

    //PONTUAÇÃO
    private int score = 0;

    // MÚSICAS E SONS
    private SoundManager soundManager = new SoundManager();

    //INTANCIAÇÕES
    private KeyHandler keyH = new KeyHandler();
    private Player player = new Player(keyH, soundManager);
    private StarField starField = new StarField();
    private ScoreManager scoreManager = new ScoreManager();

    //GAMESTATE - MENU
    private GameState gameState = GameState.MENU;

    //CREDITOS
    private int creditsY = HEIGHT + 100;
    private final int creditsSpeed = 1; // lento e suave

    //LISTA DE INIMIGOS
    private List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private int spawnCooldown = 0;

    //LISTA DE ASTEROIDES
    private List<Asteroid> asteroids = new CopyOnWriteArrayList<>();
    private int asteroidCooldown = 0;


    //LISTA DE EXPLOSÕES
    private List<AnimatedEffect> effects = new CopyOnWriteArrayList<>();

    //LISTA DE POWER UPS
    private List<PowerUp> powerUps = new CopyOnWriteArrayList<>();

    //CAMERA SHAKE
    private int shakeDuration = 0;
    private int shakeIntensity = 5;

    //ANIMAÇÃO DO MENU
    private int blinkCounter = 0;
    private boolean blink = true;
    private float menuAlpha = 0f;
    private float laserPulse = 0f;

    // TRANSIÇÃO
    private boolean transitioning = false;
    private float transitionAlpha = 0f;
    private int menuOffsetY = 50;

    //MENU SELECIONAVEL
    private int menuIndex = 0;
    private int pauseIndex = 0;

    //TEMPO
    private int survivalTicks = 0;

    //ONDAS
    private int wave = 1;
    private int enemiesKilled = 0;
    private static final int ENEMIES_PER_WAVE = 10;
    private int waveMessageTimer = 0; // exibe mensagem por X frames

    //BOSS
    private Boss boss = null;

    //THREAD
    private Thread gameThread;


    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        soundManager.playMusic("Music.wav");
    }

    public void startGameThread() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void resetGame() {

        score = 0;
        survivalTicks = 0;

        enemies.clear();
        asteroids.clear();
        effects.clear();
        powerUps.clear();

        spawnCooldown = 0;
        asteroidCooldown = 0;

        wave = 1;
        enemiesKilled = 0;
        waveMessageTimer = 0;

        boss = null;

        shakeDuration = 0;

        player = new Player(keyH, soundManager);

        menuIndex = 0;
        pauseIndex = 0;

        transitionAlpha = 0f;
        menuAlpha = 0f;
        menuOffsetY = 50;

    }

    //OPÇÕES MENU
    private final String[] menuOptions = {
            "JOGAR",
            "CRÉDITOS",
            "SAIR"
    };

    //OPÇÕES PAUSE
    private final String[] pauseOptions = {"CONTINUAR", "MENU PRINCIPAL", "SAIR"};

    //FORMATAR O TEMPO
    private String formatTime(int ticks) {
        int totalSeconds = ticks / 60;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    //METODO PARA TREMER A TELA
    private void triggerShake(int duration, int intensity) {
        shakeDuration = duration;
        shakeIntensity = intensity;
    }

    private void spawnBoss() {
        enemies.clear(); // limpa inimigos normais
        boss = new Boss(wave);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running) {

            long now = System.nanoTime();
            delta += (now - lastTime) / timePerFrame;
            lastTime = now;
            if (delta >= 1) {
                if (gameState != GameState.GAME_OVER) {
                    update();
                } else if(keyH.restartPressed){
                    resetGame();
                    gameState = GameState.MENU;
                    keyH.restartPressed = false;
                }
                delta--;
            }
            repaint();
        }

    }

    public void update() {
        starField.update();

        if (gameState == GameState.MENU) {

            // anima entrada
            menuOffsetY += (0 - menuOffsetY) * 0.08f;
            menuAlpha += (1f - menuAlpha) * 0.08f;

            if (!transitioning) {

                if (keyH.upPressed) {
                    menuIndex = (menuIndex - 1 + menuOptions.length) % menuOptions.length;
                    soundManager.playSound("MenuMove.wav");
                    keyH.upPressed = false;
                }

                if (keyH.downPressed) {
                    menuIndex = (menuIndex + 1) % menuOptions.length;
                    soundManager.playSound("MenuMove.wav");
                    keyH.downPressed = false;
                }

                if (keyH.enterPressed) {
                    soundManager.playSound("MenuSelect.wav");
                    transitioning = true;
                    keyH.enterPressed = false;
                }

            } else {
                // fade out
                transitionAlpha += 0.03f;

                if (transitionAlpha >= 1f) {
                    transitionAlpha = 0f;
                    transitioning = false;

                    if (menuIndex == 0) {
                        gameState = GameState.PLAYING;
                        resetGame();
                    }else if (menuIndex == 1) {
                        gameState = GameState.CREDITS;
                        creditsY = HEIGHT + 100;
                    }
                    else if (menuIndex == 2) System.exit(0);

                    menuAlpha = 0f;
                    menuOffsetY = 50;
                }
            }

            return;
        }

        if (gameState == GameState.CREDITS) {
            updateCredits();
            if (keyH.escPressed) {
                gameState = GameState.MENU;
                keyH.escPressed = false;
            }
            return;
        }

        if (gameState == GameState.PAUSED) {
            if (keyH.upPressed) {
                pauseIndex--;
                if (pauseIndex < 0) pauseIndex = pauseOptions.length - 1;
                keyH.upPressed = false;
            }
            if (keyH.downPressed) {
                pauseIndex++;
                if (pauseIndex >= pauseOptions.length) pauseIndex = 0;
                keyH.downPressed = false;
            }
            if (keyH.enterPressed) {
                if (pauseIndex == 0) gameState = GameState.PLAYING;
                else if (pauseIndex == 1) {
                    resetGame();
                    gameState = GameState.MENU;
                } else System.exit(0);
                keyH.enterPressed = false;
            }
            return;
        }

        if (keyH.escPressed && gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            keyH.escPressed = false;
            return;
        }

        survivalTicks++;
        player.update();

        // SPAWN INIMIGOS
        if (boss == null) {
            spawnCooldown--;
            if (spawnCooldown <= 0) {
                double r = Math.random();

                double shooterChance = Math.min(0.5, 0.2 + wave * 0.05);
                double fastChance = Math.min(0.3, 0.1 + wave * 0.03);

                if (r < (1 - shooterChance - fastChance)) enemies.add(new EnemyNormal(score));
                else if (r < (1 - fastChance)) enemies.add(new EnemyShooter(score));
                else enemies.add(new EnemyFast(score));

                spawnCooldown = Math.max(20, 90 - wave * 5);
            }
        }

        // SPAWN ASTEROIDES
        asteroidCooldown--;
        if (asteroidCooldown <= 0) {
            asteroids.add(new Asteroid());
            asteroidCooldown = 240;
        }

        //ASTEROIDES
        for (Asteroid a : asteroids) a.update();
        asteroids.removeIf(a -> !a.isActive());

        //INIMIGOS
        for (Enemy e : enemies) e.update();
        enemies.removeIf(e -> !e.isActive());


        // COLISÕES BULLET x ENEMY
        for (Bullet b : player.getBullets()) {
            for (Enemy e : enemies) {
                if (b.getBounds().intersects(e.getBounds())) {
                    b.setActive(false);
                    e.setActive(false);
                    effects.add(new AnimatedEffect(
                            e.getCenterX(), e.getCenterY(),
                            "/ExplosionSheet.png",
                            512, 512, 2, 80, 13, 23
                    ));

                    enemiesKilled++;
                    if (enemiesKilled % ENEMIES_PER_WAVE == 0) {
                        wave++;
                        waveMessageTimer = 180;
                        if (wave % 5 == 0) spawnBoss();
                    }

                    soundManager.playSound("ExplosionSound.wav");
                    triggerShake(20, 15);
                    score++;
                }
            }
        }

        if (waveMessageTimer > 0) waveMessageTimer--;

        // ===== BOSS =====
        Boss currentBoss = boss;

        if (currentBoss != null) {

            currentBoss.update();

            if (!currentBoss.isActive()) {
                boss = null;
                return;
            }

            // balas do boss com player
            for (EnemyBullet b : currentBoss.getBullets()) {
                if (b.getBounds().intersects(player.getBounds())) {
                    player.hit();
                    b.setActive(false);
                    if (!player.isAlive()) {
                        gameState = GameState.GAME_OVER;
                        scoreManager.save(score);
                        return;
                    }
                }
            }

            // balas do player com boss
            for (Bullet b : player.getBullets()) {
                if (b.getBounds().intersects(currentBoss.getBounds())) {
                    b.setActive(false);
                    currentBoss.hit();

                    if (!currentBoss.isActive()) {
                        enemiesKilled += 10;
                        score += 20;

                        effects.add(new AnimatedEffect(
                                currentBoss.getCenterX(), currentBoss.getCenterY(),
                                "/ExplosionSheet.png", 512, 512, 2, 150, 13, 23
                        ));

                        soundManager.playSound("ExplosionSound.wav");
                        triggerShake(40, 20);

                        boss = null;
                        return;
                    }
                }
            }
        }

        // ENEMY x PLAYER
        for (Enemy e : enemies) {
            if (e.getBounds().intersects(player.getBounds())) {
                if (!player.isInvincible()) {
                    player.hit();
                    soundManager.playSound("ExplosionSound.wav");
                    if (!player.isAlive()) {
                        gameState = GameState.GAME_OVER;
                        scoreManager.save(score);
                        return;
                    }
                }
            }
        }

        // ASTEROID x PLAYER
        for (Asteroid a : asteroids) {
            if (a.getBounds().intersects(player.getBounds())) {
                if (!player.isInvincible()) {
                player.hit();
                triggerShake(20, 15);
                if (!player.isAlive()) {
                    gameState = GameState.GAME_OVER;
                    scoreManager.save(score);
                    return;
                }
                }
            }
        }

        // POWERUPS
        for (PowerUp p : powerUps) {
            if (p.getBounds().intersects(player.getBounds())) {
                p.applyTo(player);
                p.setActive(false);
            }
        }

        //COLISÃO BALA - INIMIGO
        for (Bullet b : player.getBullets()) {
            for (Enemy e : enemies) {
                if (b.getBounds().intersects(e.getBounds())) {
                    b.setActive(false);
                    e.setActive(false);

                    effects.add(new AnimatedEffect(
                            e.getCenterX(), e.getCenterY(),
                            "/ExplosionSheet.png",
                            512, 512, 2, 80, 13, 23
                    ));

                    enemiesKilled++;

                    if (enemiesKilled % ENEMIES_PER_WAVE == 0) {
                        wave++;
                        waveMessageTimer = 180;
                        if (wave % 5 == 0) spawnBoss();
                    }

                    soundManager.playSound("ExplosionSound.wav");
                    triggerShake(20, 15);
                    score++;
                }
            }
        }

        // ===== BULLET x ASTEROID =====
        for (Bullet b : player.getBullets()) {
            for (Asteroid a : asteroids) {

                if (b.getBounds().intersects(a.getBounds())) {

                    b.setActive(false);
                    a.setActive(false);

                    PowerUp drop = a.dropPowerUp();
                    if (drop != null) powerUps.add(drop);

                    effects.add(new AnimatedEffect(
                            a.getCenterX(), a.getCenterY(),
                            "/DustAnimation.png",
                            128, 128, 1, 100, 0, 35
                    ));

                    triggerShake(20, 15);
                    soundManager.playSound("ExplosionSound.wav");
                    score += 2;
                }
            }
        }


        // LASER
        if (player.getLaser() != null) {
            Rectangle laserBounds = player.getLaser().getBounds();

            if (boss != null) {
                Boss laserBoss = boss;
                if (laserBounds.intersects(laserBoss.getBounds())) {
                    laserBoss.hit();
                    if (!laserBoss.isActive()) {
                        enemiesKilled += 10;
                        score += 20;

                        effects.add(new AnimatedEffect(
                                laserBoss.getCenterX(), laserBoss.getCenterY(),
                                "/ExplosionSheet.png", 512, 512, 2, 150, 13, 23
                        ));

                        soundManager.playSound("ExplosionSound.wav");
                        triggerShake(40, 20);

                        boss = null;
                        return;
                    }
                }
            }


            //COLISÃO LASER - INIMIGO
            for (Enemy e : enemies) {
                if (laserBounds.intersects(e.getBounds())) {
                    e.setActive(false);

                    effects.add(new AnimatedEffect(
                            e.getCenterX(), e.getCenterY(),
                            "/ExplosionSheet.png",
                            512, 512, 2, 80, 13, 23
                    ));

                    enemiesKilled++;
                    if (enemiesKilled % ENEMIES_PER_WAVE == 0) {
                        wave++;
                        waveMessageTimer = 180;
                        if (wave % 5 == 0) spawnBoss();
                    }

                    triggerShake(20, 15);
                    soundManager.playSound("ExplosionSound.wav");
                    score++;
                }
            }

            // ===== LASER x ASTEROID (DROP POWERUP) =====
            for (Asteroid a : asteroids) {
                if (laserBounds.intersects(a.getBounds())) {

                    a.setActive(false);

                    PowerUp drop = a.dropPowerUp();
                    if (drop != null) powerUps.add(drop);

                    effects.add(new AnimatedEffect(
                            a.getCenterX(), a.getCenterY(),
                            "/DustAnimation.png",
                            128, 128, 1, 100, 0, 35
                    ));

                    triggerShake(20, 15);
                    soundManager.playSound("ExplosionSound.wav");
                    score += 2;
                }
            }
        }


        // EFFECTS
        for (AnimatedEffect e : effects) e.update();
        effects.removeIf(AnimatedEffect::isFinished);

        // POWERUPS UPDATE
        for (PowerUp p : powerUps) p.update();
        powerUps.removeIf(p -> !p.isActive());
    }

                    private void updateCredits() {
                        creditsY -= creditsSpeed;

                        if (creditsY < -600) {
                            creditsY = HEIGHT + 100;
                        }
                    }

                    private void drawCentered(Graphics g, String text, int centerX, int y) {
                        FontMetrics fm = g.getFontMetrics();
                        int x = centerX - fm.stringWidth(text) / 2;
                        g.drawString(text, x, y);
                    }
                    private void drawShadowText(Graphics2D g, String text, int x, int y, Font font, Color color) {
                        g.setFont(font);

                        g.setColor(Color.BLACK);
                        g.drawString(text, x + 2, y + 2);

                        g.setColor(color);
                        g.drawString(text, x, y);
                    }

                    private void drawKey(Graphics2D g, String text, int x, int y) {
                        int w = 45;
                        int h = 45;

                        // glow
                        g.setColor(new Color(0, 255, 255, 80));
                        g.fillRoundRect(x - 3, y - 3, w + 6, h + 6, 12, 12);

                        // fundo tecla
                        g.setColor(new Color(20, 20, 40));
                        g.fillRoundRect(x, y, w, h, 12, 12);

                        // borda
                        g.setColor(Color.CYAN);
                        g.drawRoundRect(x, y, w, h, 12, 12);

                        // texto
                        g.setFont(new Font("Arial", Font.BOLD, 18));
                        g.setColor(Color.WHITE);
                        FontMetrics fm = g.getFontMetrics();
                        int tx = x + (w - fm.stringWidth(text)) / 2;
                        int ty = y + (h + fm.getAscent()) / 2 - 4;
                        g.drawString(text, tx, ty);
                    }

                    private void drawWaveMessage(Graphics2D g) {
                        float alpha = Math.min(1f, waveMessageTimer / 60f); // fade out
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g.setFont(new Font("Arial", Font.BOLD, 42));
                        g.setColor(Color.YELLOW);
                        drawCentered(g, "ONDA " + wave, WIDTH / 2, HEIGHT / 2 - 20);
                        g.setFont(new Font("Arial", Font.PLAIN, 22));
                        g.setColor(Color.WHITE);
                        drawCentered(g, "Inimigos ficaram mais fortes!", WIDTH / 2, HEIGHT / 2 + 20);
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    }


                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        Graphics2D g2 = (Graphics2D) g;


                        int offsetX = 0;
                        int offsetY = 0;

                        // APLICA SCREEN SHAKE
                        if (shakeDuration > 0) {
                            offsetX = (int)(Math.random() * shakeIntensity * 2 - shakeIntensity);
                            offsetY = (int)(Math.random() * shakeIntensity * 2 - shakeIntensity);
                            shakeDuration--;
                        }

                        // move a "câmera"
                        g2.translate(offsetX, offsetY);

                        // DESENHA O MUNDO
                        starField.render(g2);

                        if (gameState == GameState.MENU) {
                            drawMenu(g2);

                        } else if (gameState == GameState.PLAYING) {


                            player.render(g2);
                            for (PowerUp p : powerUps) p.render(g);
                            for (Asteroid a : asteroids) a.render(g2);
                            for (Enemy e : enemies) e.render(g2);
                            for (AnimatedEffect e : effects) e.render(g2);

                            if (waveMessageTimer > 0) {
                                drawWaveMessage(g2);
                            }
                            if (boss != null) boss.render(g2);

                        } else if (gameState == GameState.GAME_OVER) {
                            drawGameOver(g2);
                        } else if (gameState == GameState.CREDITS) {
                            drawCredits(g2);
                        }else if (gameState == GameState.PAUSED) {
                            // desenha o jogo congelado embaixo
                            player.render(g2);
                            for (Asteroid a : asteroids) a.render(g2);
                            for (Enemy e : enemies) e.render(g2);
                            drawPause(g2);
                        }

                        // VOLTA A CÂMERA PARA O NORMAL
                        g2.translate(-offsetX, -offsetY);

                        // HUD NÃO TREME
                        if (gameState == GameState.PLAYING) {
                            drawHUD(g2);
                        }

                        if (transitioning) {
                            int h = (int)(HEIGHT * transitionAlpha);
                            g2.setColor(Color.BLACK);
                            g2.fillRect(0, 0, WIDTH, h);
                            g2.fillRect(0, HEIGHT - h, WIDTH, h);
                        }
                    }


                    private void drawMenu(Graphics g) {

                        Graphics2D g2 = (Graphics2D) g;

                        // fade in
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, menuAlpha));

                        int centerX = WIDTH / 2;
                        int baseY = 220 + menuOffsetY;

                        // ===== TÍTULO =====
                        g2.setColor(Color.YELLOW);
                        g2.setFont(new Font("Arial", Font.BOLD, 56));
                        drawCentered(g2, "AERO FIGHTERS", centerX, baseY);

                        // SUBTÍTULO
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.setFont(new Font("Arial", Font.PLAIN, 22));
                        drawCentered(g2, "2D Space Shooter", centerX, 260);

                        // linha decorativa
                        g2.setColor(Color.GRAY);
                        g2.drawLine(centerX - 180, 280, centerX + 180, 280);

                        // ===== MENU =====
                        int startY = 330;
                        g2.setFont(new Font("Arial", Font.BOLD, 26));

                        for (int i = 0; i < menuOptions.length; i++) {

                            if (i == menuIndex) {
                                float glow = (float)(Math.sin(System.nanoTime() * 0.000000005) * 50 + 200);
                                g2.setColor(new Color(255, (int)glow, 0));
                                drawCentered(g2, "➤ " + menuOptions[i], centerX, startY + i * 45);
                            } else {
                                g2.setColor(Color.LIGHT_GRAY);
                                drawCentered(g2, menuOptions[i], centerX, startY + i * 45);
                            }
                        }

                        // ===== CAIXA NEON DO TUTORIAL =====
                        int boxX = WIDTH / 2 - 250;
                        int boxY = 500;
                        int boxW = 500;
                        int boxH = 230;

// glow externo
                        g2.setColor(new Color(0, 200, 255, 60));
                        g2.fillRoundRect(boxX - 5, boxY - 5, boxW + 10, boxH + 10, 30, 30);

// fundo
                        g2.setColor(new Color(10, 10, 30, 200));
                        g2.fillRoundRect(boxX, boxY, boxW, boxH, 25, 25);

// borda neon
                        g2.setColor(Color.CYAN);
                        g2.drawRoundRect(boxX, boxY, boxW, boxH, 25, 25);

// título
                        g2.setFont(new Font("Arial", Font.BOLD, 24));
                        g2.setColor(Color.CYAN);
                        drawCentered(g2, "CONTROLES", WIDTH / 2, boxY + 35);

// ===== MOVIMENTO (WASD) =====
                        int keyY = boxY + 60;

                        drawKey(g2, "W", centerX - 25, keyY);
                        drawKey(g2, "A", centerX - 75, keyY + 50);
                        drawKey(g2, "S", centerX - 25, keyY + 50);
                        drawKey(g2, "D", centerX + 25, keyY + 50);

                        g2.setFont(new Font("Arial", Font.PLAIN, 16));
                        g2.setColor(Color.LIGHT_GRAY);
                        drawCentered(g2, "Mover a nave", centerX, keyY + 120);

// ===== TIRO (SPACE) =====
                        int shootY = boxY + 150;

                        int spaceX = centerX - 200;
                        drawKey(g2, "SPACE", spaceX, shootY);

                        drawCentered(g2, "Atirar", spaceX + 30, shootY + 70);

// ===== LASER (SHIFT) =====
                        int shiftX = centerX + 100;
                        drawKey(g2, "SHIFT", shiftX, shootY);

                        drawCentered(g2, "Laser especial", shiftX + 35, shootY + 70);
                        // ===== RODAPÉ =====
                        int footerY = boxY + boxH + 30;
                        g2.setFont(new Font("Arial", Font.ITALIC, 16));
                        g2.setColor(Color.GRAY);
                        drawCentered(g2, "Desenvolvido por Lucas Rodrigo Basseto de Sousa", centerX, footerY);

                        // reset alpha
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    }

                    private void drawHUD(Graphics2D g) {

                        int panelHeight = 80;

                        // painel fundo
                        g.setColor(new Color(0, 0, 0, 180));
                        g.fillRoundRect(10, 10, WIDTH - 20, panelHeight, 25, 25);

                        // borda
                        g.setColor(new Color(100, 100, 100));
                        g.drawRoundRect(10, 10, WIDTH - 20, panelHeight, 25, 25);

                        Font titleFont = new Font("Arial", Font.BOLD, 16);
                        Font valueFont = new Font("Arial", Font.BOLD, 22);

                        // SCORE
                        drawShadowText(g, "SCORE", 30, 35, titleFont, Color.LIGHT_GRAY);
                        drawShadowText(g, String.valueOf(score), 30, 60, valueFont, Color.WHITE);

                        // HIGH SCORE
                        drawShadowText(g, "HIGH", 130, 35, titleFont, Color.LIGHT_GRAY);
                        drawShadowText(g, String.valueOf(scoreManager.getHighScore()), 130, 60, valueFont, Color.YELLOW);

                        // LIVES
                        g.setColor(Color.LIGHT_GRAY);
                        g.setFont(titleFont);
                        g.drawString("LIVES", 240, 35);

                        g.setColor(Color.RED);
                        for (int i = 0; i < player.getLives(); i++) {
                            g.fillOval(240 + i * 22, 42, 16, 16);
                        }

                        // TEMPO
                        drawShadowText(g, "TEMPO", 340, 35, titleFont, Color.LIGHT_GRAY);
                        drawShadowText(g, formatTime(survivalTicks), 340, 60, valueFont, Color.WHITE);

                        // ONDA
                        drawShadowText(g, "ONDA", 450, 35, titleFont, Color.LIGHT_GRAY);
                        drawShadowText(g, String.valueOf(wave), 450, 60, valueFont, Color.CYAN);

                        if (boss != null) {
                            drawBossBar(g);
                        } else {
                            drawLaserBar(g, titleFont);
                        }
                    }

                    private void drawBossBar(Graphics2D g) {

                        int barW = 400;
                        int barX = WIDTH / 2 - barW / 2;
                        int barY = 100;

                        // fundo painel
                        g.setColor(new Color(0, 0, 0, 180));
                        g.fillRoundRect(barX - 10, barY - 20, barW + 20, 40, 15, 15);

                        // nome boss
                        g.setFont(new Font("Arial", Font.BOLD, 14));
                        g.setColor(Color.RED);
                        drawCentered(g, "★ BOSS ★  FASE " + boss.getPhase(), WIDTH / 2, barY - 5);

                        // barra fundo
                        g.setColor(Color.DARK_GRAY);
                        g.fillRoundRect(barX, barY, barW, 14, 10, 10);

                        float healthPercent = (float) boss.getCurrentHealth() / boss.getMaxHealth();

                        Color barColor = healthPercent > 0.5f ? Color.GREEN :
                                healthPercent > 0.25f ? Color.ORANGE : Color.RED;

                        // barra vida
                        g.setColor(barColor);
                        g.fillRoundRect(barX, barY, (int)(barW * healthPercent), 14, 10, 10);

                        // glow
                        g.setColor(new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 80));
                        g.fillRoundRect(barX - 2, barY - 2, (int)(barW * healthPercent) + 4, 18, 12, 12);

                        // borda
                        g.setColor(Color.WHITE);
                        g.drawRoundRect(barX, barY, barW, 14, 10, 10);
                    }

                    private void drawLaserBar(Graphics2D g, Font titleFont) {

                        int barX = WIDTH - 210;
                        int barY = 40;
                        int barWidth = 200;
                        int barHeight = 18;

                        drawShadowText(g, "LASER", barX, barY - 10, titleFont, Color.LIGHT_GRAY);

                        // fundo
                        g.setColor(Color.DARK_GRAY);
                        g.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                        laserPulse += 0.05f;
                        float pulse = (float)(Math.sin(laserPulse) * 5);

                        int laserWidth = (int)(barWidth * (player.getLaserCharge() / 100.0));

                        Color laserColor = Color.CYAN;
                        if (player.getLives() == 1) laserColor = Color.RED;
                        else if (player.getLives() == 2) laserColor = Color.ORANGE;

                        // barra
                        g.setColor(laserColor);
                        g.fillRoundRect(barX, barY, laserWidth, barHeight, 12, 12);

                        // glow
                        g.setColor(new Color(laserColor.getRed(), laserColor.getGreen(), laserColor.getBlue(), 120));
                        g.fillRoundRect(barX - 2, barY - 2, laserWidth + 4, barHeight + 4, 14, 14);

                        // moldura
                        g.setColor(Color.WHITE);
                        g.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    }

                    private void drawGameOver(Graphics g) {
                        g.setColor(Color.RED);
                        g.setFont(new Font("Arial", Font.BOLD, 48));
                        g.drawString("GAME OVER", 270, 280);
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.PLAIN, 24));
                        g.drawString("Score: " + score, 330, 330);
                        g.drawString("Tempo: " + formatTime(survivalTicks), 330, 360);
                        g.drawString("High Score: " + scoreManager.getHighScore(), 290, 390);
                        g.drawString("Pressione R para reiniciar", 240, 430);
                    }

                    private void drawCredits(Graphics g) {

                        g.setColor(Color.WHITE);

                        int centerX = WIDTH / 2;

                        g.setFont(new Font("Arial", Font.BOLD, 42));
                        drawCentered(g, "AERO FIGHTERS", centerX, creditsY);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "A 2D Space Shooter Game", centerX, creditsY + 50);

                        int y = creditsY + 150;

                        g.setFont(new Font("Arial", Font.BOLD, 26));
                        drawCentered(g, "DESENVOLVIMENTO", centerX, y);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "Programação & Design", centerX, y + 40);
                        drawCentered(g, "Lucas Rodrigo Basseto de Sousa", centerX, y + 70);

                        y += 150;

                        g.setFont(new Font("Arial", Font.BOLD, 26));
                        drawCentered(g, "TECNOLOGIAS", centerX, y);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "Java", centerX, y + 40);
                        drawCentered(g, "Java Swing (Graphics2D)", centerX, y + 70);

                        y += 150;

                        g.setFont(new Font("Arial", Font.BOLD, 26));
                        drawCentered(g, "ARTE & ÁUDIO", centerX, y);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "Sprites e efeitos: Free Assets", centerX, y + 40);
                        drawCentered(g, "Música e sons: FreeSound.org", centerX, y + 70);

                        y += 150;

                        g.setFont(new Font("Arial", Font.BOLD, 26));
                        drawCentered(g, "PORTFÓLIO", centerX, y);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "GitHub: github.com/Bassetin", centerX, y + 40);
                        drawCentered(g, "LinkedIn: linkedin.com/in/lucasrodrigodev", centerX, y + 70);

                        y += 150;

                        g.setFont(new Font("Arial", Font.BOLD, 26));
                        drawCentered(g, "AGRADECIMENTOS", centerX, y);

                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        drawCentered(g, "Obrigado por jogar!", centerX, y + 40);
                        drawCentered(g, "Projeto desenvolvido para fins educacionais", centerX, y + 70);

                        y += 150;

                        g.setFont(new Font("Arial", Font.ITALIC, 18));
                        drawCentered(g, "Pressione ESC para voltar ao menu", centerX, y + 120);
                    }

                    private void drawPause(Graphics2D g) {
                        // overlay escuro translucido
                        g.setColor(new Color(0, 0, 0, 150));
                        g.fillRect(0, 0, WIDTH, HEIGHT);

                        int centerX = WIDTH / 2;

                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 48));
                        drawCentered(g, "PAUSADO", centerX, 300);

                        g.setFont(new Font("Arial", Font.BOLD, 28));
                        for (int i = 0; i < pauseOptions.length; i++) {
                            if (i == pauseIndex) {
                                g.setColor(Color.YELLOW);
                                drawCentered(g, "> " + pauseOptions[i], centerX, 380 + i * 50);
                            } else {
                                g.setColor(Color.LIGHT_GRAY);
                                drawCentered(g, pauseOptions[i], centerX, 380 + i * 50);
                            }
                        }
                    }
                }




