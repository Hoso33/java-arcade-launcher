package pacman.ui.cli;

import pacman.engine.GameEngine;
import pacman.model.*;

/**
 * High-fidelity ANSI colorized terminal renderer for Pac-Man.
 */
public class ConsoleRenderer {

    // ANSI Escape Codes
    public static final String ANSI_RESET = "\033[0m";
    public static final String ANSI_CLEAR_SCREEN = "\033[H\033[2J";
    public static final String ANSI_HOME = "\033[H";
    public static final String ANSI_HIDE_CURSOR = "\033[?25l";
    public static final String ANSI_SHOW_CURSOR = "\033[?25h";

    // Colors
    public static final String ANSI_WALL = "\033[34m"; // Blue
    public static final String ANSI_DOT = "\033[37m"; // Dim white
    public static final String ANSI_POWER_PELLET = "\033[1;93m"; // Bright yellow
    public static final String ANSI_PACMAN = "\033[1;93m"; // Bright yellow
    public static final String ANSI_BLINKY = "\033[1;91m"; // Bright red
    public static final String ANSI_PINKY = "\033[1;95m"; // Bright magenta/pink
    public static final String ANSI_INKY = "\033[1;96m"; // Bright cyan
    public static final String ANSI_CLYDE = "\033[38;5;214m"; // Orange
    public static final String ANSI_FRIGHTENED = "\033[1;94m"; // Blue
    public static final String ANSI_FLASH = "\033[1;97m"; // White
    public static final String ANSI_EYES = "\033[1;97m"; // White
    public static final String ANSI_DOOR = "\033[1;35m"; // Magenta
    public static final String ANSI_FRUIT = "\033[1;91m"; // Red

    public String render(GameEngine engine) {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_HOME);

        Maze maze = engine.getMaze();
        if (maze == null) return "";

        int w = maze.getWidth();
        int h = maze.getHeight();

        // 1. Header
        sb.append(String.format(" %sSCORE: %-8d  %sHIGH: %-8d  %sLVL: %-2d  %sLIVES: %s%s\n",
                ANSI_RESET, engine.getScoreManager().getScore(),
                "\033[1;36m", engine.getScoreManager().getHighScore(),
                "\033[1;32m", engine.getLevel(),
                "\033[1;33m", "C ".repeat(Math.max(0, engine.getPacman().getLives())),
                ANSI_RESET
        ));
        sb.append(" " + "=".repeat(w * 2) + "\n");

        // 2. Build character grid buffer
        String[][] charGrid = new String[h][w];

        // Draw maze tiles
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType tile = maze.getTile(x, y);
                switch (tile) {
                    case WALL -> charGrid[y][x] = ANSI_WALL + "██" + ANSI_RESET;
                    case DOT -> charGrid[y][x] = ANSI_DOT + " ·" + ANSI_RESET;
                    case POWER_PELLET -> charGrid[y][x] = ANSI_POWER_PELLET + " ●" + ANSI_RESET;
                    case GHOST_HOUSE_DOOR -> charGrid[y][x] = ANSI_DOOR + "==" + ANSI_RESET;
                    case GHOST_HOUSE -> charGrid[y][x] = "  ";
                    default -> charGrid[y][x] = "  ";
                }
            }
        }

        // Draw Fruit
        Fruit fruit = engine.getCurrentFruit();
        if (fruit != null && fruit.isActive()) {
            int fx = fruit.getTileX();
            int fy = fruit.getTileY();
            if (fx >= 0 && fx < w && fy >= 0 && fy < h) {
                charGrid[fy][fx] = ANSI_FRUIT + " 🍒" + ANSI_RESET;
            }
        }

        // Draw Ghosts
        boolean isFlashing = engine.isFrightenedFlashing();
        for (Ghost ghost : engine.getGhosts()) {
            int gx = ghost.getTileX();
            int gy = ghost.getTileY();
            if (gx >= 0 && gx < w && gy >= 0 && gy < h) {
                String ghostGlyph;
                if (ghost.getState() == GhostState.EATEN_RETURNING) {
                    ghostGlyph = ANSI_EYES + " \"\"" + ANSI_RESET;
                } else if (ghost.getState() == GhostState.FRIGHTENED) {
                    ghostGlyph = (isFlashing ? ANSI_FLASH : ANSI_FRIGHTENED) + " ᗣ" + ANSI_RESET;
                } else {
                    String color = switch (ghost.getType()) {
                        case BLINKY -> ANSI_BLINKY;
                        case PINKY -> ANSI_PINKY;
                        case INKY -> ANSI_INKY;
                        case CLYDE -> ANSI_CLYDE;
                    };
                    ghostGlyph = color + " ᗣ" + ANSI_RESET;
                }
                charGrid[gy][gx] = ghostGlyph;
            }
        }

        // Draw Pacman
        Pacman pacman = engine.getPacman();
        int px = pacman.getTileX();
        int py = pacman.getTileY();
        if (px >= 0 && px < w && py >= 0 && py < h) {
            String pacGlyph = switch (pacman.getCurrentDirection()) {
                case LEFT -> ANSI_PACMAN + " ᗤ" + ANSI_RESET;
                case RIGHT -> ANSI_PACMAN + " ᗧ" + ANSI_RESET;
                case UP -> ANSI_PACMAN + " V" + ANSI_RESET;
                case DOWN -> ANSI_PACMAN + " Λ" + ANSI_RESET;
                case NONE -> ANSI_PACMAN + " C" + ANSI_RESET;
            };
            if (pacman.isDead()) {
                pacGlyph = "\033[1;31m X" + ANSI_RESET;
            }
            charGrid[py][px] = pacGlyph;
        }

        // Convert grid to output string
        for (int y = 0; y < h; y++) {
            sb.append(" ");
            for (int x = 0; x < w; x++) {
                sb.append(charGrid[y][x]);
            }
            sb.append("\n");
        }

        // 3. Footer & Controls
        sb.append(" " + "=".repeat(w * 2) + "\n");
        GameState state = engine.getGameState();
        if (state == GameState.READY) {
            sb.append(String.format(" %s>> READY! <<%s\n", "\033[1;33m", ANSI_RESET));
        } else if (state == GameState.LEVEL_CLEAR) {
            sb.append(String.format(" %s>> LEVEL COMPLETE! Preparing level %d... <<%s\n",
                    "\033[1;93m", engine.getLevel() + 1, ANSI_RESET));
        } else if (state == GameState.GAME_OVER) {
            sb.append(String.format(" %s>> GAME OVER! Press 'R' to Restart or 'Q' to Quit <<%s\n", "\033[1;31m", ANSI_RESET));
        } else if (state == GameState.PAUSED) {
            sb.append(String.format(" %s>> PAUSED (Press 'P' to Resume) <<%s\n", "\033[1;36m", ANSI_RESET));
        } else {
            sb.append(String.format(" Controls: [W/A/S/D] Move | [P] Pause | [M] Mute | [Q] Quit\n"));
        }

        return sb.toString();
    }
}
