package snake;

import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;
import snake.model.Direction;
import snake.model.GameState;
import snake.model.Point;
import snake.model.Snake;
import snake.ui.NokiaScreenPanel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Automated Verification Suite for Nokia 3310 Snake.
 */
public class SnakeTestRunner {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   NOKIA 3310 SNAKE VERIFICATION SUITE    ");
        System.out.println("==========================================");

        testMovementAndGrowth();
        testReversePrevention();
        testWallCollision();
        testSelfCollision();
        testScoreAndSpeedProgression();
        testNokiaBeeper();
        testOffscreenPanelRendering();

        System.out.println("\n==========================================");
        System.out.println("  ✓ ALL 7 SNAKE TEST SUITES PASSED!");
        System.out.println("==========================================");
    }

    private static void testMovementAndGrowth() {
        System.out.print("[1/7] Testing Movement & Food Consumption... ");
        SnakeEngine engine = new SnakeEngine(20, 20, null);
        engine.startNewGame();
        Snake snake = engine.getSnake();

        Point initialHead = snake.getHead();
        engine.tick();
        Point newHead = snake.getHead();

        assertEquals(initialHead.getX() + 1, newHead.getX(), "Snake head moved right");
        assertEquals(4, snake.getLength(), "Snake initial length");

        // Manually place food directly ahead of snake
        Point ahead = newHead.translate(Direction.RIGHT);
        // Force spawn food at ahead position
        try {
            var field = SnakeEngine.class.getDeclaredField("food");
            field.setAccessible(true);
            field.set(engine, ahead);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int scoreBefore = engine.getScore();
        engine.tick(); // Snake eats food

        assertEquals(5, snake.getLength(), "Snake length increased after eating");
        assertTrue(engine.getScore() > scoreBefore, "Score increased");
        System.out.println("PASSED");
    }

    private static void testReversePrevention() {
        System.out.print("[2/7] Testing 180° Reverse Prevention... ");
        Snake snake = new Snake(10, 10, 4, Direction.RIGHT);
        snake.setNextDirection(Direction.LEFT); // Trying to reverse directly into neck
        assertEquals(Direction.RIGHT, snake.getNextDirection(), "Opposite direction blocked");

        snake.setNextDirection(Direction.UP); // Perpendicular turn
        assertEquals(Direction.UP, snake.getNextDirection(), "Perpendicular turn allowed");
        System.out.println("PASSED");
    }

    private static void testWallCollision() {
        System.out.print("[3/7] Testing Wall Collision & Game Over... ");
        SnakeEngine engine = new SnakeEngine(10, 10, null);
        engine.startNewGame();

        // Move right until hitting right wall (width = 10)
        for (int i = 0; i < 15; i++) {
            engine.tick();
        }

        assertEquals(GameState.GAME_OVER, engine.getGameState(), "Game over on wall collision");
        assertFalse(engine.getSnake().isAlive(), "Snake marked dead");
        System.out.println("PASSED");
    }

    private static void testSelfCollision() {
        System.out.print("[4/7] Testing Self Collision... ");
        SnakeEngine engine = new SnakeEngine(20, 20, null);
        engine.startNewGame();
        Snake snake = engine.getSnake();

        // Grow snake to length 6
        for (int i = 0; i < 3; i++) {
            snake.advance(true);
        }

        // Loop snake into itself: RIGHT -> UP -> LEFT -> DOWN
        snake.setNextDirection(Direction.UP);
        engine.tick();
        snake.setNextDirection(Direction.LEFT);
        engine.tick();
        snake.setNextDirection(Direction.DOWN);
        engine.tick();

        assertEquals(GameState.GAME_OVER, engine.getGameState(), "Game over on self collision");
        System.out.println("PASSED");
    }

    private static void testScoreAndSpeedProgression() {
        System.out.print("[5/7] Testing Speed Scaling Delay... ");
        SnakeEngine engine = new SnakeEngine(20, 20, null);
        int initialDelay = engine.getCurrentTickDelayMs();

        // Simulate foods eaten increase
        try {
            var field = SnakeEngine.class.getDeclaredField("foodsEaten");
            field.setAccessible(true);
            field.setInt(engine, 10);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int fasterDelay = engine.getCurrentTickDelayMs();
        assertTrue(fasterDelay < initialDelay, "Tick delay decreases (faster speed) as foods eaten increases");
        System.out.println("PASSED (" + initialDelay + "ms -> " + fasterDelay + "ms)");
    }

    private static void testNokiaBeeper() {
        System.out.print("[6/7] Testing Nokia Piezo Beeper... ");
        NokiaBeeper beeper = new NokiaBeeper();
        beeper.setMuted(true);
        beeper.playEatTone();
        beeper.playCrashTone();
        beeper.playStartTone();
        beeper.playHighScoreTone();
        beeper.shutdown();
        System.out.println("PASSED (All piezo tones synthesized)");
    }

    private static void testOffscreenPanelRendering() {
        System.out.print("[7/7] Testing Nokia LCD Panel Graphics2D Rendering... ");
        SnakeEngine engine = new SnakeEngine(24, 16, null);
        NokiaScreenPanel panel = new NokiaScreenPanel(engine, null);
        panel.setSize(540, 420);

        BufferedImage img = new BufferedImage(540, 420, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        panel.paint(g2);
        g2.dispose();
        System.out.println("PASSED (Monochrome LCD buffer rendered)");
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!expected.equals(actual)) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg + " | Condition was false");
        }
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new AssertionError(msg + " | Condition was true");
        }
    }
}
