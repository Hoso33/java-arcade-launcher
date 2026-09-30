package snake.ui.cli;

import snake.engine.SnakeEngine;
import snake.model.Direction;
import snake.model.GameState;
import snake.model.Point;
import snake.model.Snake;

import java.util.List;

/**
 * ANSI Terminal Renderer replicating the Nokia 3310 monochrome screen in the console.
 */
public class ConsoleSnakeRenderer {

    public static final String ANSI_RESET = "\033[0m";
    public static final String ANSI_HOME = "\033[H";
    public static final String ANSI_CLEAR_SCREEN = "\033[H\033[2J";
    public static final String ANSI_HIDE_CURSOR = "\033[?25l";
    public static final String ANSI_SHOW_CURSOR = "\033[?25h";

    // Nokia Green LCD styling in terminal
    public static final String LCD_GREEN_BG = "\033[48;2;155;188;15m";
    public static final String LCD_DARK_FG = "\033[38;2;15;56;15m";
    public static final String LCD_STYLE = LCD_GREEN_BG + LCD_DARK_FG;

    public String render(SnakeEngine engine) {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_HOME);

        int gw = engine.getGridWidth();
        int gh = engine.getGridHeight();
        int screenCharWidth = gw * 2 + 4;

        // 1. Nokia Phone Header
        sb.append(LCD_STYLE).append("╔").append("═".repeat(screenCharWidth)).append("╗").append(ANSI_RESET).append("\n");
        sb.append(LCD_STYLE).append("║  NOKIA 3310  [SNAKE]  ").append(" ".repeat(Math.max(0, screenCharWidth - 23))).append("║").append(ANSI_RESET).append("\n");

        // Status bar (Signal, Score, High Score, Battery)
        String scoreStr = String.format("SCORE:%04d  HI:%04d  [BAT:III]", engine.getScore(), engine.getHighScore());
        int pad = screenCharWidth - scoreStr.length() - 2;
        sb.append(LCD_STYLE).append("║ ").append(scoreStr).append(" ".repeat(Math.max(0, pad))).append(" ║").append(ANSI_RESET).append("\n");
        sb.append(LCD_STYLE).append("╠").append("═".repeat(screenCharWidth)).append("╣").append(ANSI_RESET).append("\n");

        // 2. Playfield Grid Buffer
        String[][] grid = new String[gh][gw];
        for (int y = 0; y < gh; y++) {
            for (int x = 0; x < gw; x++) {
                grid[y][x] = "  ";
            }
        }

        // Draw Food
        Point food = engine.getFood();
        if (food != null && food.getX() >= 0 && food.getX() < gw && food.getY() >= 0 && food.getY() < gh) {
            grid[food.getY()][food.getX()] = "● ";
        }

        // Draw Snake
        Snake snake = engine.getSnake();
        List<Point> body = snake.getBody();
        for (int i = 1; i < body.size(); i++) {
            Point p = body.get(i);
            if (p.getX() >= 0 && p.getX() < gw && p.getY() >= 0 && p.getY() < gh) {
                grid[p.getY()][p.getX()] = "■ ";
            }
        }

        // Draw Head
        if (!body.isEmpty()) {
            Point head = snake.getHead();
            if (head.getX() >= 0 && head.getX() < gw && head.getY() >= 0 && head.getY() < gh) {
                String headGlyph = switch (snake.getCurrentDirection()) {
                    case RIGHT -> "► ";
                    case LEFT -> "◄ ";
                    case UP -> "▲ ";
                    case DOWN -> "▼ ";
                };
                if (!snake.isAlive()) {
                    headGlyph = "X ";
                }
                grid[head.getY()][head.getX()] = headGlyph;
            }
        }

        // Output playfield rows
        for (int y = 0; y < gh; y++) {
            sb.append(LCD_STYLE).append("║ ");
            for (int x = 0; x < gw; x++) {
                sb.append(grid[y][x]);
            }
            sb.append(" ║").append(ANSI_RESET).append("\n");
        }

        // 3. Footer
        sb.append(LCD_STYLE).append("╚").append("═".repeat(screenCharWidth)).append("╝").append(ANSI_RESET).append("\n");

        GameState state = engine.getGameState();
        if (state == GameState.TITLE) {
            sb.append(" >> [PRESS SPACE / ANY KEY TO START] <<\n");
        } else if (state == GameState.PAUSED) {
            sb.append(" >> [PAUSED - Press 'P' to Resume] <<\n");
        } else if (state == GameState.GAME_OVER) {
            sb.append(" >> GAME OVER! Press [SPACE] or 'R' to Restart, 'Q' to Quit <<\n");
        } else {
            sb.append(" Controls: [W/A/S/D] Move | [P] Pause | [M] Mute | [Q] Quit\n");
        }

        return sb.toString();
    }
}
