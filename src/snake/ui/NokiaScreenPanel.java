package snake.ui;

import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;
import snake.model.Direction;
import snake.model.GameState;
import snake.model.Point;
import snake.model.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Authentic Nokia 3310 Monochrome LCD Game Panel with dot-matrix pixel aesthetics.
 */
public class NokiaScreenPanel extends JPanel {

    // Authentic Nokia / Game Boy LCD Palette
    public static final Color LCD_BG = new Color(155, 188, 15);      // #9BBC0F
    public static final Color LCD_PIXEL = new Color(15, 56, 15);     // #0F380F
    public static final Color LCD_GRID_LINE = new Color(145, 178, 12);// Subtle pixel gap
    public static final Color PHONE_BEZEL = new Color(38, 50, 56);   // Dark casing

    private final SnakeEngine engine;
    private final NokiaBeeper beeper;
    private final Timer gameLoopTimer;

    private long animationFrame;
    private int gameOverFlashTimer;

    public NokiaScreenPanel(SnakeEngine engine, NokiaBeeper beeper) {
        this.engine = engine;
        this.beeper = beeper;
        this.animationFrame = 0;
        this.gameOverFlashTimer = 0;

        setBackground(PHONE_BEZEL);
        setFocusable(true);

        setupKeyListeners();

        this.gameLoopTimer = new Timer(engine.getCurrentTickDelayMs(), null);
        this.gameLoopTimer.addActionListener(e -> {
            animationFrame++;
            if (engine.getGameState() == GameState.GAME_OVER) {
                gameOverFlashTimer++;
            }
            engine.tick();
            gameLoopTimer.setDelay(engine.getCurrentTickDelayMs());
            repaint();
        });
    }

    public void startLoop() {
        gameLoopTimer.start();
    }

    public void stopLoop() {
        gameLoopTimer.stop();
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> engine.setRequestedDirection(Direction.UP);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> engine.setRequestedDirection(Direction.DOWN);
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> engine.setRequestedDirection(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> engine.setRequestedDirection(Direction.RIGHT);
                    case KeyEvent.VK_P -> engine.togglePause();
                    case KeyEvent.VK_SPACE, KeyEvent.VK_ENTER -> {
                        if (engine.getGameState() == GameState.TITLE || engine.getGameState() == GameState.GAME_OVER) {
                            engine.startNewGame();
                        } else {
                            engine.togglePause();
                        }
                    }
                    case KeyEvent.VK_M -> {
                        if (beeper != null) {
                            beeper.toggleMute();
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> System.exit(0);
                    default -> {
                        if (engine.getGameState() == GameState.TITLE) {
                            engine.startNewGame();
                        }
                    }
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int panelW = getWidth();
        int panelH = getHeight();

        // 1. Outer Nokia Phone Frame & Screen Bezel
        int bezelPadding = 24;
        int screenX = bezelPadding;
        int screenY = bezelPadding + 20;
        int screenW = panelW - bezelPadding * 2;
        int screenH = panelH - bezelPadding * 2 - 30;

        // Draw speaker slot on phone top
        g2.setColor(new Color(25, 33, 38));
        g2.fillRoundRect(panelW / 2 - 35, 12, 70, 8, 4, 4);

        // Draw Nokia Brand logo
        g2.setColor(new Color(176, 190, 197));
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        String brand = "NOKIA";
        FontMetrics bfm = g2.getFontMetrics();
        g2.drawString(brand, (panelW - bfm.stringWidth(brand)) / 2, screenY - 6);

        // Draw LCD Screen Outer Border
        g2.setColor(new Color(60, 75, 70));
        g2.fillRoundRect(screenX - 4, screenY - 4, screenW + 8, screenH + 8, 8, 8);

        // Fill LCD Background
        g2.setColor(LCD_BG);
        g2.fillRect(screenX, screenY, screenW, screenH);

        // 2. Playfield Dimensions
        int hudH = 26;
        int playX = screenX + 6;
        int playY = screenY + hudH + 2;
        int playW = screenW - 12;
        int playH = screenH - hudH - 8;

        int gridCols = engine.getGridWidth();
        int gridRows = engine.getGridHeight();

        double cellW = (double) playW / gridCols;
        double cellH = (double) playH / gridRows;

        // Draw HUD Header (Signal, Score, High Score, Battery)
        drawNokiaHud(g2, screenX, screenY, screenW, hudH);

        // Draw Playfield Perimeter Wall
        g2.setColor(LCD_PIXEL);
        g2.drawRect(playX - 2, playY - 2, playW + 3, playH + 3);
        g2.drawRect(playX - 3, playY - 3, playW + 5, playH + 5);

        // 3. Draw Game Elements
        if (engine.getGameState() == GameState.TITLE) {
            drawTitleScreen(g2, playX, playY, playW, playH);
        } else {
            // Draw Food (Pixel Apple with stem)
            Point food = engine.getFood();
            if (food != null) {
                drawNokiaFood(g2, playX, playY, food.getX(), food.getY(), cellW, cellH);
            }

            // Draw Snake
            drawNokiaSnake(g2, playX, playY, cellW, cellH);

            // Draw State Overlays
            if (engine.getGameState() == GameState.PAUSED) {
                drawPausedOverlay(g2, playX, playY, playW, playH);
            } else if (engine.getGameState() == GameState.GAME_OVER) {
                drawGameOverOverlay(g2, playX, playY, playW, playH);
            }
        }
    }

    private void drawNokiaHud(Graphics2D g2, int sx, int sy, int sw, int sh) {
        g2.setColor(LCD_PIXEL);
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));

        // Signal bars (Left)
        for (int i = 0; i < 4; i++) {
            g2.fillRect(sx + 8 + i * 4, sy + 18 - (i + 1) * 3, 2, (i + 1) * 3);
        }

        // Score & High Score (Center)
        String scoreStr = String.format("%04d", engine.getScore());
        String highStr = String.format("HI:%04d", engine.getHighScore());
        g2.drawString(scoreStr, sx + 40, sy + 18);
        g2.drawString(highStr, sx + sw / 2 - 25, sy + 18);

        // Sound Mute Icon
        if (beeper != null && beeper.isMuted()) {
            g2.drawString("✕♪", sx + sw - 60, sy + 18);
        }

        // Battery bars (Right)
        int bx = sx + sw - 28;
        g2.drawRect(bx, sy + 7, 18, 11);
        g2.fillRect(bx + 18, sy + 10, 2, 5);
        for (int i = 0; i < 3; i++) {
            g2.fillRect(bx + 2 + i * 5, sy + 9, 3, 7);
        }

        // Divider Line
        g2.drawLine(sx + 2, sy + sh - 1, sx + sw - 2, sy + sh - 1);
    }

    private void drawNokiaSnake(Graphics2D g2, int px, int py, double cw, double ch) {
        Snake snake = engine.getSnake();
        List<Point> body = snake.getBody();
        if (body.isEmpty()) return;

        g2.setColor(LCD_PIXEL);

        // Draw Body Segments
        for (int i = 1; i < body.size(); i++) {
            Point p = body.get(i);
            int x = (int) (px + p.getX() * cw);
            int y = (int) (py + p.getY() * ch);
            int w = (int) cw;
            int h = (int) ch;

            g2.fillRect(x + 1, y + 1, w - 2, h - 2);

            Point prev = body.get(i - 1);
            int midX = (int) (px + (p.getX() + prev.getX()) * 0.5 * cw);
            int midY = (int) (py + (p.getY() + prev.getY()) * 0.5 * ch);
            g2.fillRect(midX + 1, midY + 1, w - 2, h - 2);
        }

        // Draw Head with Eyes
        Point head = snake.getHead();
        int hx = (int) (px + head.getX() * cw);
        int hy = (int) (py + head.getY() * ch);
        int hw = (int) cw;
        int hh = (int) ch;

        g2.setColor(LCD_PIXEL);
        g2.fillRect(hx, hy, hw, hh);

        // Eyes (LCD_BG cutout dots looking in current direction)
        g2.setColor(LCD_BG);
        Direction dir = snake.getCurrentDirection();
        int eyeSize = Math.max(2, (int) (hw * 0.22));

        if (dir == Direction.RIGHT) {
            g2.fillRect(hx + hw - eyeSize - 2, hy + 2, eyeSize, eyeSize);
            g2.fillRect(hx + hw - eyeSize - 2, hy + hh - eyeSize - 2, eyeSize, eyeSize);
        } else if (dir == Direction.LEFT) {
            g2.fillRect(hx + 2, hy + 2, eyeSize, eyeSize);
            g2.fillRect(hx + 2, hy + hh - eyeSize - 2, eyeSize, eyeSize);
        } else if (dir == Direction.UP) {
            g2.fillRect(hx + 2, hy + 2, eyeSize, eyeSize);
            g2.fillRect(hx + hw - eyeSize - 2, hy + 2, eyeSize, eyeSize);
        } else if (dir == Direction.DOWN) {
            g2.fillRect(hx + 2, hy + hh - eyeSize - 2, eyeSize, eyeSize);
            g2.fillRect(hx + hw - eyeSize - 2, hy + hh - eyeSize - 2, eyeSize, eyeSize);
        }
    }

    private void drawNokiaFood(Graphics2D g2, int px, int py, int fx, int fy, double cw, double ch) {
        int x = (int) (px + fx * cw);
        int y = (int) (py + fy * ch);
        int w = (int) cw;
        int h = (int) ch;

        g2.setColor(LCD_PIXEL);

        // Classic Nokia pixel apple: 3x3 block with top stem pixel
        int inset = Math.max(2, (int) (w * 0.18));
        g2.fillRect(x + inset, y + inset + 1, w - inset * 2, h - inset * 2);

        // Top stem pixel
        g2.fillRect(x + w / 2, y + 1, Math.max(2, w / 4), 2);

        // Blinking inner dot
        if ((animationFrame / 3) % 2 == 0) {
            g2.setColor(LCD_BG);
            g2.fillRect(x + w / 2 - 1, y + h / 2, 2, 2);
        }
    }

    private void drawTitleScreen(Graphics2D g2, int px, int py, int pw, int ph) {
        g2.setColor(LCD_PIXEL);

        // Title text
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        String title = "S N A K E";
        g2.drawString(title, px + (pw - fm.stringWidth(title)) / 2, py + ph / 3);

        // Subtitle
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics sfm = g2.getFontMetrics();
        String sub = "NOKIA 3310 EDITION";
        g2.drawString(sub, px + (pw - sfm.stringWidth(sub)) / 2, py + ph / 3 + 22);

        // Blinking Press Key text
        if ((animationFrame / 4) % 2 == 0) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 13));
            FontMetrics pfm = g2.getFontMetrics();
            String press = "> PRESS SPACE <";
            g2.drawString(press, px + (pw - pfm.stringWidth(press)) / 2, py + ph * 3 / 4);
        }
    }

    private void drawPausedOverlay(Graphics2D g2, int px, int py, int pw, int ph) {
        int bw = 130;
        int bh = 34;
        int bx = px + (pw - bw) / 2;
        int by = py + (ph - bh) / 2;

        g2.setColor(LCD_PIXEL);
        g2.fillRect(bx, by, bw, bh);
        g2.setColor(LCD_BG);
        g2.drawRect(bx + 2, by + 2, bw - 5, bh - 5);

        g2.setFont(new Font("Monospaced", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String text = "PAUSED";
        g2.drawString(text, bx + (bw - fm.stringWidth(text)) / 2, by + 22);
    }

    private void drawGameOverOverlay(Graphics2D g2, int px, int py, int pw, int ph) {
        int bw = 190;
        int bh = 68;
        int bx = px + (pw - bw) / 2;
        int by = py + (ph - bh) / 2;

        g2.setColor(LCD_PIXEL);
        g2.fillRect(bx, by, bw, bh);

        g2.setColor(LCD_BG);
        g2.drawRect(bx + 2, by + 2, bw - 5, bh - 5);

        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        String title = "GAME OVER";
        g2.drawString(title, bx + (bw - fm.stringWidth(title)) / 2, by + 22);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        FontMetrics sfm = g2.getFontMetrics();
        String scoreText = "SCORE: " + engine.getScore();
        g2.drawString(scoreText, bx + (bw - sfm.stringWidth(scoreText)) / 2, by + 40);

        if ((gameOverFlashTimer / 4) % 2 == 0) {
            String restartText = "[SPACE] RESTART";
            g2.drawString(restartText, bx + (bw - sfm.stringWidth(restartText)) / 2, by + 56);
        }
    }
}
