package pacman.ui.gui;

import pacman.model.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * 100% Procedural Vector Graphics Renderer using Java 2D Graphics2D.
 * Programmatically renders Pac-Man, ghosts, animated skirts, directional eyes, walls, dots, and fruits.
 */
public class ProceduralGraphics {

    public static final Color MAZE_WALL_COLOR = new Color(33, 33, 222);
    public static final Color MAZE_WALL_GLOW = new Color(75, 75, 255);
    public static final Color MAZE_BG = new Color(0, 0, 0);
    public static final Color DOT_COLOR = new Color(255, 183, 174);
    public static final Color POWER_PELLET_COLOR = new Color(255, 183, 174);
    public static final Color PACMAN_COLOR = new Color(255, 255, 0);
    public static final Color FRIGHTENED_GHOST_COLOR = new Color(33, 33, 255);
    public static final Color FRIGHTENED_FLASH_COLOR = new Color(240, 240, 255);
    public static final Color GHOST_DOOR_COLOR = new Color(255, 184, 222);

    /**
     * Draws the Pac-Man character with directional mouth opening angle and dying animations.
     */
    public static void drawPacman(Graphics2D g2, Pacman pacman, double cellSize, double offsetX, double offsetY) {
        double px = offsetX + pacman.getPosition().getX() * cellSize;
        double py = offsetY + pacman.getPosition().getY() * cellSize;
        double radius = cellSize * 0.75;
        double cx = px + cellSize * 0.5;
        double cy = py + cellSize * 0.5;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (pacman.isDead()) {
            // Death animation: mouth expands from current angle all the way to 360 degrees
            double progress = pacman.getDeathProgress();
            if (progress >= 1.0) {
                // Popping particles
                g2.setColor(new Color(255, 255, 0, 180));
                g2.fill(new Ellipse2D.Double(cx - 2, cy - 2, 4, 4));
                return;
            }
            double startAngle = 90 + progress * 180;
            double arcExtent = 360 - progress * 360;

            g2.setColor(PACMAN_COLOR);
            g2.fill(new Arc2D.Double(cx - radius / 2, cy - radius / 2, radius, radius, startAngle, arcExtent, Arc2D.PIE));
            return;
        }

        double maxMouthAngle = 70.0;
        double mouthAngleDeg = pacman.getMouthAngle() * maxMouthAngle;

        double baseAngle = switch (pacman.getCurrentDirection()) {
            case RIGHT -> 0.0;
            case DOWN -> 270.0;
            case LEFT -> 180.0;
            case UP -> 90.0;
            case NONE -> 0.0;
        };

        double startAngle = baseAngle + mouthAngleDeg / 2.0;
        double arcExtent = 360.0 - mouthAngleDeg;

        g2.setColor(PACMAN_COLOR);
        g2.fill(new Arc2D.Double(cx - radius / 2, cy - radius / 2, radius, radius, startAngle, arcExtent, Arc2D.PIE));
    }

    /**
     * Draws a Ghost entity with dome head, animated wavy skirt, and directional eyes.
     */
    public static void drawGhost(Graphics2D g2, Ghost ghost, boolean isFlashing, double cellSize, double offsetX, double offsetY) {
        double px = offsetX + ghost.getPosition().getX() * cellSize;
        double py = offsetY + ghost.getPosition().getY() * cellSize;
        double size = cellSize * 0.85;
        double x = px + (cellSize - size) / 2.0;
        double y = py + (cellSize - size) / 2.0;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (ghost.getState() == GhostState.EATEN_RETURNING) {
            // Draw only floating eyeballs returning to house
            drawGhostEyes(g2, x, y, size, ghost.getCurrentDirection());
            return;
        }

        Color bodyColor;
        if (ghost.getState() == GhostState.FRIGHTENED) {
            bodyColor = isFlashing ? FRIGHTENED_FLASH_COLOR : FRIGHTENED_GHOST_COLOR;
        } else {
            bodyColor = new Color(ghost.getType().getRgb());
        }

        // 1. Draw Body & Head
        Path2D.Double path = new Path2D.Double();
        double headRadius = size / 2.0;
        double headCenterX = x + headRadius;
        double headCenterY = y + headRadius;

        // Top arc (head dome)
        path.append(new Arc2D.Double(x, y, size, size, 0, 180, Arc2D.OPEN), false);

        // Right side down
        path.lineTo(x + size, y + size);

        // Animated wavy skirt at bottom (3 wave feet)
        int tentacles = 3;
        double tentacleWidth = size / tentacles;
        double wobble = Math.sin(ghost.getSkirtAnimationTime()) * (size * 0.12);

        for (int i = tentacles; i > 0; i--) {
            double tx = x + (i - 0.5) * tentacleWidth;
            double bottomY = (i % 2 == 0) ? (y + size - wobble) : (y + size + wobble);
            path.quadTo(tx, bottomY, x + (i - 1) * tentacleWidth, y + size);
        }

        path.closePath();

        g2.setColor(bodyColor);
        g2.fill(path);

        // 2. Draw Eyes or Frightened Face
        if (ghost.getState() == GhostState.FRIGHTENED) {
            drawFrightenedFace(g2, x, y, size, isFlashing);
        } else {
            drawGhostEyes(g2, x, y, size, ghost.getCurrentDirection());
        }
    }

    private static void drawGhostEyes(Graphics2D g2, double x, double y, double size, Direction dir) {
        double eyeW = size * 0.28;
        double eyeH = size * 0.35;
        double eyeY = y + size * 0.22;

        double leftEyeX = x + size * 0.18;
        double rightEyeX = x + size * 0.54;

        // Sclera (white of eye)
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(leftEyeX, eyeY, eyeW, eyeH));
        g2.fill(new Ellipse2D.Double(rightEyeX, eyeY, eyeW, eyeH));

        // Pupils looking in direction
        double pupilSize = eyeW * 0.55;
        double pupilOffsetX = dir.getDx() * (eyeW * 0.22);
        double pupilOffsetY = dir.getDy() * (eyeH * 0.22);

        double leftPupilX = leftEyeX + (eyeW - pupilSize) / 2.0 + pupilOffsetX;
        double leftPupilY = eyeY + (eyeH - pupilSize) / 2.0 + pupilOffsetY;
        double rightPupilX = rightEyeX + (eyeW - pupilSize) / 2.0 + pupilOffsetX;
        double rightPupilY = eyeY + (eyeH - pupilSize) / 2.0 + pupilOffsetY;

        g2.setColor(new Color(0, 0, 200));
        g2.fill(new Ellipse2D.Double(leftPupilX, leftPupilY, pupilSize, pupilSize));
        g2.fill(new Ellipse2D.Double(rightPupilX, rightPupilY, pupilSize, pupilSize));
    }

    private static void drawFrightenedFace(Graphics2D g2, double x, double y, double size, boolean isFlashing) {
        Color featureColor = isFlashing ? new Color(200, 0, 0) : new Color(255, 184, 222);

        // Small frightened eyes
        double eyeSize = size * 0.12;
        g2.setColor(featureColor);
        g2.fill(new Ellipse2D.Double(x + size * 0.3, y + size * 0.35, eyeSize, eyeSize));
        g2.fill(new Ellipse2D.Double(x + size * 0.58, y + size * 0.35, eyeSize, eyeSize));

        // Squiggly mouth
        Path2D.Double mouth = new Path2D.Double();
        double my = y + size * 0.65;
        mouth.moveTo(x + size * 0.25, my);
        mouth.lineTo(x + size * 0.35, my - 3);
        mouth.lineTo(x + size * 0.45, my + 3);
        mouth.lineTo(x + size * 0.55, my - 3);
        mouth.lineTo(x + size * 0.65, my + 3);
        mouth.lineTo(x + size * 0.75, my);

        g2.setStroke(new BasicStroke((float) (size * 0.08), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(mouth);
    }

    /**
     * Draws vector fruit icons (Cherries, Strawberry, Orange, Apple, Key, etc.).
     */
    public static void drawFruit(Graphics2D g2, Fruit fruit, double cellSize, double offsetX, double offsetY) {
        double px = offsetX + fruit.getTileX() * cellSize;
        double py = offsetY + fruit.getTileY() * cellSize;
        double size = cellSize * 0.85;
        double cx = px + cellSize * 0.5;
        double cy = py + cellSize * 0.5;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (fruit.getType()) {
            case CHERRY -> {
                // Two red berries
                g2.setColor(new Color(230, 0, 30));
                g2.fill(new Ellipse2D.Double(cx - size * 0.4, cy + size * 0.05, size * 0.38, size * 0.38));
                g2.fill(new Ellipse2D.Double(cx + size * 0.05, cy + size * 0.1, size * 0.38, size * 0.38));

                // Green stems
                g2.setColor(new Color(34, 177, 76));
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Double stems = new Path2D.Double();
                stems.moveTo(cx - size * 0.2, cy + size * 0.1);
                stems.quadTo(cx - size * 0.1, cy - size * 0.3, cx + size * 0.2, cy - size * 0.35);
                stems.moveTo(cx + size * 0.25, cy + size * 0.15);
                stems.quadTo(cx + size * 0.1, cy - size * 0.2, cx + size * 0.2, cy - size * 0.35);
                g2.draw(stems);
            }
            case STRAWBERRY -> {
                // Strawberry body
                Path2D.Double straw = new Path2D.Double();
                straw.moveTo(cx - size * 0.3, cy - size * 0.1);
                straw.quadTo(cx - size * 0.35, cy + size * 0.2, cx, cy + size * 0.4);
                straw.quadTo(cx + size * 0.35, cy + size * 0.2, cx + size * 0.3, cy - size * 0.1);
                straw.closePath();
                g2.setColor(new Color(255, 30, 80));
                g2.fill(straw);

                // Leaves
                g2.setColor(new Color(34, 177, 76));
                g2.fill(new Polygon(
                        new int[]{(int)(cx - size * 0.3), (int)cx, (int)(cx + size * 0.3)},
                        new int[]{(int)(cy - size * 0.1), (int)(cy - size * 0.35), (int)(cy - size * 0.1)},
                        3
                ));
            }
            case ORANGE -> {
                // Orange sphere
                g2.setColor(new Color(255, 140, 0));
                g2.fill(new Ellipse2D.Double(cx - size * 0.35, cy - size * 0.25, size * 0.7, size * 0.7));
                // Green stem leaf
                g2.setColor(new Color(34, 177, 76));
                g2.fill(new Ellipse2D.Double(cx - 2, cy - size * 0.38, 4, 6));
            }
            case APPLE -> {
                g2.setColor(new Color(220, 20, 20));
                g2.fill(new Ellipse2D.Double(cx - size * 0.35, cy - size * 0.25, size * 0.7, size * 0.65));
                g2.setColor(new Color(139, 69, 19));
                g2.fillRect((int)cx - 1, (int)(cy - size * 0.4), 2, 6);
            }
            case MELON -> {
                g2.setColor(new Color(50, 205, 50));
                g2.fill(new Ellipse2D.Double(cx - size * 0.35, cy - size * 0.3, size * 0.7, size * 0.65));
                g2.setColor(new Color(0, 100, 0));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Arc2D.Double(cx - size * 0.3, cy - size * 0.25, size * 0.6, size * 0.55, 0, 180, Arc2D.OPEN));
            }
            default -> {
                // Bell / Key / default
                g2.setColor(new Color(fruit.getType().getColorRgb()));
                g2.fill(new RoundRectangle2D.Double(cx - size * 0.25, cy - size * 0.3, size * 0.5, size * 0.6, 6, 6));
            }
        }
    }

    /**
     * Draws the Maze Walls with neon arcade aesthetics.
     */
    public static void drawMaze(Graphics2D g2, Maze maze, double cellSize, double offsetX, double offsetY, boolean flashWhite) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = maze.getWidth();
        int h = maze.getHeight();

        Color wallColor = flashWhite ? Color.WHITE : MAZE_WALL_COLOR;
        Color wallGlow = flashWhite ? Color.WHITE : MAZE_WALL_GLOW;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType tile = maze.getTile(x, y);
                double tx = offsetX + x * cellSize;
                double ty = offsetY + y * cellSize;

                if (tile == TileType.WALL) {
                    g2.setColor(MAZE_BG);
                    g2.fillRect((int) tx, (int) ty, (int) Math.ceil(cellSize), (int) Math.ceil(cellSize));

                    // Draw inner neon border for walls facing corridors
                    drawWallCell(g2, maze, x, y, tx, ty, cellSize, wallColor, wallGlow);
                } else if (tile == TileType.GHOST_HOUSE_DOOR) {
                    // Pink barrier
                    g2.setColor(GHOST_DOOR_COLOR);
                    g2.fillRect((int) tx, (int) (ty + cellSize * 0.4), (int) Math.ceil(cellSize), (int) (cellSize * 0.2));
                }
            }
        }
    }

    private static void drawWallCell(Graphics2D g2, Maze maze, int x, int y, double tx, double ty, double size, Color color, Color glow) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f));

        boolean topOpen = isWallOpen(maze, x, y - 1);
        boolean bottomOpen = isWallOpen(maze, x, y + 1);
        boolean leftOpen = isWallOpen(maze, x - 1, y);
        boolean rightOpen = isWallOpen(maze, x + 1, y);

        // If surrounded entirely by walls, draw solid dark blue block
        if (!topOpen && !bottomOpen && !leftOpen && !rightOpen) {
            g2.setColor(new Color(15, 15, 70));
            g2.fillRect((int) tx + 1, (int) ty + 1, (int) size - 2, (int) size - 2);
            return;
        }

        // Draw edge lines where adjacent cell is empty/passable
        if (topOpen) {
            g2.drawLine((int) tx, (int) ty, (int) (tx + size), (int) ty);
        }
        if (bottomOpen) {
            g2.drawLine((int) tx, (int) (ty + size), (int) (tx + size), (int) (ty + size));
        }
        if (leftOpen) {
            g2.drawLine((int) tx, (int) ty, (int) tx, (int) (ty + size));
        }
        if (rightOpen) {
            g2.drawLine((int) (tx + size), (int) ty, (int) (tx + size), (int) (ty + size));
        }
    }

    private static boolean isWallOpen(Maze maze, int x, int y) {
        if (x < 0 || x >= maze.getWidth() || y < 0 || y >= maze.getHeight()) {
            return false;
        }
        TileType t = maze.getTile(x, y);
        return t != TileType.WALL && t != TileType.GHOST_HOUSE;
    }

    /**
     * Draws Dots and Power Pellets with pulsing glow animation.
     */
    public static void drawPellets(Graphics2D g2, Maze maze, double cellSize, double offsetX, double offsetY, double globalTimer) {
        int w = maze.getWidth();
        int h = maze.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType tile = maze.getTile(x, y);
                double cx = offsetX + (x + 0.5) * cellSize;
                double cy = offsetY + (y + 0.5) * cellSize;

                if (tile == TileType.DOT) {
                    double dotRadius = cellSize * 0.14;
                    g2.setColor(DOT_COLOR);
                    g2.fill(new Ellipse2D.Double(cx - dotRadius, cy - dotRadius, dotRadius * 2, dotRadius * 2));
                } else if (tile == TileType.POWER_PELLET) {
                    // Pulsing animation
                    double pulse = (Math.sin(globalTimer * 7.0) + 1.0) / 2.0; // 0.0 to 1.0
                    double pelletRadius = cellSize * (0.35 + pulse * 0.1);

                    g2.setColor(new Color(255, 183, 174, (int) (180 + pulse * 75)));
                    g2.fill(new Ellipse2D.Double(cx - pelletRadius, cy - pelletRadius, pelletRadius * 2, pelletRadius * 2));
                }
            }
        }
    }
}
