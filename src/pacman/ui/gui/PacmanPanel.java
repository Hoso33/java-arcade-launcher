package pacman.ui.gui;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;
import pacman.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Swing Canvas Panel with 60 FPS game loop and procedural rendering.
 */
public class PacmanPanel extends JPanel {

    private final GameEngine engine;
    private final SoundSynthesizer soundSynthesizer;
    private final Timer gameLoopTimer;
    private long lastTimeNanos;
    private double globalTimer;

    public PacmanPanel(GameEngine engine, SoundSynthesizer soundSynthesizer) {
        this.engine = engine;
        this.soundSynthesizer = soundSynthesizer;
        this.globalTimer = 0.0;
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        setupKeyBindings();

        this.lastTimeNanos = System.nanoTime();
        this.gameLoopTimer = new Timer(16, e -> updateAndRepaint());
    }

    public void startGameLoop() {
        lastTimeNanos = System.nanoTime();
        gameLoopTimer.start();
    }

    public void stopGameLoop() {
        gameLoopTimer.stop();
    }

    private void updateAndRepaint() {
        long now = System.nanoTime();
        double deltaSeconds = (now - lastTimeNanos) / 1_000_000_000.0;
        lastTimeNanos = now;

        // Cap delta time to prevent physics explosions if window moved/lagged
        if (deltaSeconds > 0.05) {
            deltaSeconds = 0.05;
        }

        globalTimer += deltaSeconds;
        engine.update(deltaSeconds);
        repaint();
    }

    private void setupKeyBindings() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> engine.setPacmanNextDirection(Direction.UP);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> engine.setPacmanNextDirection(Direction.DOWN);
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> engine.setPacmanNextDirection(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> engine.setPacmanNextDirection(Direction.RIGHT);
                    case KeyEvent.VK_P -> engine.togglePause();
                    case KeyEvent.VK_M -> {
                        if (soundSynthesizer != null) {
                            soundSynthesizer.toggleMute();
                        }
                    }
                    case KeyEvent.VK_R -> {
                        if (engine.getGameState() == GameState.GAME_OVER) {
                            engine.initNewGame();
                        }
                    }
                    case KeyEvent.VK_G -> {
                        // Generate new procedural maze
                        engine.loadLevel(engine.getLevel());
                        engine.setGameState(GameState.READY, 1.5);
                    }
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                    default -> {}
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Maze maze = engine.getMaze();
        if (maze == null) return;

        int panelW = getWidth();
        int panelH = getHeight();

        // Top and bottom HUD margins
        int hudTopHeight = 45;
        int hudBottomHeight = 35;
        int playableH = panelH - hudTopHeight - hudBottomHeight;

        double cellW = (double) panelW / maze.getWidth();
        double cellH = (double) playableH / maze.getHeight();
        double cellSize = Math.min(cellW, cellH);

        double mazeTotalW = maze.getWidth() * cellSize;
        double mazeTotalH = maze.getHeight() * cellSize;
        double offsetX = (panelW - mazeTotalW) / 2.0;
        double offsetY = hudTopHeight + (playableH - mazeTotalH) / 2.0;

        // 1. Draw Top HUD (Scores & High Score)
        drawTopHud(g2, panelW);

        // 2. Draw Maze Walls
        boolean flashWhite = (engine.getGameState() == GameState.LEVEL_CLEAR) && ((int) (globalTimer * 8) % 2 == 0);
        ProceduralGraphics.drawMaze(g2, maze, cellSize, offsetX, offsetY, flashWhite);

        // 3. Draw Dots & Power Pellets
        ProceduralGraphics.drawPellets(g2, maze, cellSize, offsetX, offsetY, globalTimer);

        // 4. Draw Fruit if active
        Fruit fruit = engine.getCurrentFruit();
        if (fruit != null && fruit.isActive()) {
            ProceduralGraphics.drawFruit(g2, fruit, cellSize, offsetX, offsetY);
        }

        // 5. Draw Pacman
        ProceduralGraphics.drawPacman(g2, engine.getPacman(), cellSize, offsetX, offsetY);

        // 6. Draw Ghosts
        boolean isFlashing = engine.isFrightenedFlashing();
        for (Ghost ghost : engine.getGhosts()) {
            ProceduralGraphics.drawGhost(g2, ghost, isFlashing, cellSize, offsetX, offsetY);
        }

        // 7. Draw Bottom HUD (Lives, Level, Sound Status)
        drawBottomHud(g2, panelW, panelH, cellSize);

        // 8. Draw State Overlays (READY, GAME OVER, PAUSED)
        drawStateOverlays(g2, panelW, panelH);
    }

    private void drawTopHud(Graphics2D g2, int panelW) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));

        // 1UP & Score
        g2.setColor(Color.WHITE);
        g2.drawString("1UP", 30, 20);
        g2.setColor(Color.YELLOW);
        g2.drawString(String.format("%06d", engine.getScoreManager().getScore()), 30, 38);

        // HIGH SCORE
        g2.setColor(Color.WHITE);
        int hsX = panelW / 2 - 50;
        g2.drawString("HIGH SCORE", hsX, 20);
        g2.setColor(Color.CYAN);
        g2.drawString(String.format("%06d", engine.getScoreManager().getHighScore()), hsX + 15, 38);

        // LEVEL
        g2.setColor(Color.WHITE);
        g2.drawString("LVL", panelW - 80, 20);
        g2.setColor(Color.GREEN);
        g2.drawString(String.format("%02d", engine.getLevel()), panelW - 75, 38);
    }

    private void drawBottomHud(Graphics2D g2, int panelW, int panelH, double cellSize) {
        int y = panelH - 15;

        // Lives mini-icons
        g2.setColor(ProceduralGraphics.PACMAN_COLOR);
        int lives = engine.getPacman().getLives();
        for (int i = 0; i < lives - 1; i++) {
            int iconX = 30 + i * 22;
            g2.fillArc(iconX, y - 16, 16, 16, 210, 300);
        }

        // Fruit indicator for current level
        FruitType fruitType = FruitType.forLevel(engine.getLevel());
        g2.setColor(new Color(fruitType.getColorRgb()));
        g2.fillOval(panelW - 60, y - 16, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.drawString(fruitType.getDisplayName(), panelW - 130, y - 3);

        // Sound indicator
        if (soundSynthesizer != null && soundSynthesizer.isMuted()) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("[MUTE: M]", panelW / 2 - 30, y - 3);
        }
    }

    private void drawStateOverlays(Graphics2D g2, int panelW, int panelH) {
        GameState state = engine.getGameState();
        if (state == GameState.READY) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 26));
            g2.setColor(Color.YELLOW);
            drawCenteredString(g2, "READY!", panelW, panelH / 2 + 30);
        } else if (state == GameState.LEVEL_CLEAR) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 30));
            g2.setColor(((int) (globalTimer * 4) % 2 == 0) ? Color.YELLOW : Color.WHITE);
            drawCenteredString(g2, "LEVEL COMPLETE!", panelW, panelH / 2 + 10);
            g2.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            drawCenteredString(g2, "Preparing level " + (engine.getLevel() + 1), panelW, panelH / 2 + 40);
        } else if (state == GameState.GAME_OVER) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 28));
            g2.setColor(Color.RED);
            drawCenteredString(g2, "GAME OVER", panelW, panelH / 2 - 10);
            g2.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2.setColor(Color.WHITE);
            drawCenteredString(g2, "Press 'R' to Restart or 'ESC' to Quit", panelW, panelH / 2 + 25);
        } else if (state == GameState.PAUSED) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 28));
            g2.setColor(Color.CYAN);
            drawCenteredString(g2, "PAUSED", panelW, panelH / 2 + 10);
        }
    }

    private void drawCenteredString(Graphics2D g2, String text, int width, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}
