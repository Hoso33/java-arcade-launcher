package snake.engine;

import snake.audio.NokiaBeeper;
import snake.model.Direction;
import snake.model.GameState;
import snake.model.Point;
import snake.model.Snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Core Game Engine for Snake managing state, physics, collisions, and speed scaling.
 */
public class SnakeEngine {

    public static final int DEFAULT_GRID_WIDTH = 24;
    public static final int DEFAULT_GRID_HEIGHT = 16;

    private final int gridWidth;
    private final int gridHeight;
    private final Snake snake;
    private final NokiaBeeper beeper;
    private final Random random;

    private Point food;
    private GameState gameState;
    private int score;
    private int highScore;
    private int foodsEaten;
    private boolean isNewHighScore;

    public SnakeEngine() {
        this(DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT, new NokiaBeeper());
    }

    public SnakeEngine(int gridWidth, int gridHeight, NokiaBeeper beeper) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.beeper = beeper;
        this.random = new Random();
        this.snake = new Snake(gridWidth / 2, gridHeight / 2, 4, Direction.RIGHT);
        this.gameState = GameState.TITLE;
        this.score = 0;
        this.highScore = 0;
        this.foodsEaten = 0;
        this.isNewHighScore = false;
        spawnFood();
    }

    public void startNewGame() {
        this.snake.reset(gridWidth / 2, gridHeight / 2, 4, Direction.RIGHT);
        this.score = 0;
        this.foodsEaten = 0;
        this.isNewHighScore = false;
        this.gameState = GameState.PLAYING;
        spawnFood();
        if (beeper != null) {
            beeper.playStartTone();
        }
    }

    public void tick() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        Point nextHead = snake.getHead().translate(snake.getNextDirection());

        // 1. Check Wall Collision
        if (nextHead.getX() < 0 || nextHead.getX() >= gridWidth ||
            nextHead.getY() < 0 || nextHead.getY() >= gridHeight) {
            triggerGameOver();
            return;
        }

        // 2. Check Self Collision (unless moving into the tail cell that will be vacated)
        boolean eatingFood = nextHead.equals(food);
        boolean selfCollision = eatingFood ? snake.occupies(nextHead) : snake.occupiesExceptHead(nextHead);
        if (selfCollision) {
            triggerGameOver();
            return;
        }

        // 3. Move Snake
        snake.advance(eatingFood);

        // 4. Handle Eating Food
        if (eatingFood) {
            foodsEaten++;
            int pointsEarned = 10 + (foodsEaten / 5) * 5; // Scaling points
            score += pointsEarned;

            if (score > highScore) {
                if (!isNewHighScore && highScore > 0 && beeper != null) {
                    beeper.playHighScoreTone();
                }
                highScore = score;
                isNewHighScore = true;
            }

            if (beeper != null) {
                beeper.playEatTone();
            }

            spawnFood();
        }
    }

    public void spawnFood() {
        List<Point> freeCells = new ArrayList<>();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                Point p = new Point(x, y);
                if (!snake.occupies(p)) {
                    freeCells.add(p);
                }
            }
        }

        if (freeCells.isEmpty()) {
            // Screen fully filled - Win Game!
            triggerGameOver();
            return;
        }

        this.food = freeCells.get(random.nextInt(freeCells.size()));
    }

    private void triggerGameOver() {
        snake.setAlive(false);
        gameState = GameState.GAME_OVER;
        if (beeper != null) {
            beeper.playCrashTone();
        }
    }

    public void setRequestedDirection(Direction direction) {
        if (gameState == GameState.TITLE || gameState == GameState.GAME_OVER) {
            startNewGame();
            return;
        }
        if (gameState == GameState.PLAYING) {
            snake.setNextDirection(direction);
        }
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    /**
     * Computes dynamic tick delay (ms) based on score to increase difficulty.
     */
    public int getCurrentTickDelayMs() {
        int baseDelay = 125; // Base speed
        int minDelay = 55;   // Max speed limit
        int reduction = Math.min(70, (foodsEaten / 2) * 3);
        return Math.max(minDelay, baseDelay - reduction);
    }

    // Getters
    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public Snake getSnake() {
        return snake;
    }

    public Point getFood() {
        return food;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public NokiaBeeper getBeeper() {
        return beeper;
    }
}
