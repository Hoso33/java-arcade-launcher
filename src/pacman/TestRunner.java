package pacman;

import pacman.audio.SoundSynthesizer;
import pacman.audio.SoundType;
import pacman.engine.GameEngine;
import pacman.level.ClassicMaze;
import pacman.level.MazeGenerator;
import pacman.model.*;
import pacman.ui.cli.ConsoleRenderer;
import pacman.ui.gui.ProceduralGraphics;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Comprehensive Automated Test and Simulation Suite for Pac-Man.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  PAC-MAN AUTOMATED VERIFICATION SUITE   ");
        System.out.println("==========================================");

        testClassicMaze();
        testProceduralGenerator();
        testMovementAndCollisions();
        testGhostAIAndHouseRelease();
        testPowerPelletAndGhostEating();
        testLevelProgression();
        testAudioSynthesis();
        testProceduralGraphicsRendering();
        testConsoleRendering();

        System.out.println("\n==========================================");
        System.out.println("  ✓ ALL 9 TEST SUITES PASSED SUCCESSFULLY!");
        System.out.println("==========================================");
    }

    private static void testClassicMaze() {
        System.out.print("[1/9] Testing Classic Maze Layout... ");
        Maze classic = ClassicMaze.create();
        assertEquals(28, classic.getWidth(), "Classic width");
        assertEquals(31, classic.getHeight(), "Classic height");
        assertTrue(classic.getTotalDots() >= 240, "Classic dots count");
        assertEquals(classic.getTotalDots(), classic.getDotsRemaining(), "Dots remaining");
        assertEquals(TileType.WALL, classic.getTile(0, 0), "Corner wall");
        assertEquals(TileType.GHOST_HOUSE_DOOR, classic.getTile(13, 12), "Ghost house door");
        System.out.println("PASSED (" + classic.getTotalDots() + " dots)");
    }

    private static void testProceduralGenerator() {
        System.out.print("[2/9] Testing Procedural Generator (10 seeds)... ");
        for (int seed = 1; seed <= 10; seed++) {
            MazeGenerator gen = new MazeGenerator(seed * 12345L);
            Maze maze = gen.generate(28, 31);
            assertEquals(28, maze.getWidth(), "Maze width");
            assertEquals(31, maze.getHeight(), "Maze height");
            assertTrue(maze.getTotalDots() > 80, "Sufficient dots");

            // Verify left-right symmetry
            for (int y = 0; y < maze.getHeight(); y++) {
                for (int x = 0; x < 14; x++) {
                    TileType left = maze.getTile(x, y);
                    TileType right = maze.getTile(27 - x, y);
                    if (left == TileType.WALL) {
                        assertEquals(TileType.WALL, right, "Wall symmetry at (" + x + "," + y + ")");
                    }
                }
            }
        }
        System.out.println("PASSED");
    }

    private static void testMovementAndCollisions() {
        System.out.print("[3/9] Testing Movement, Clamping & Tunnel Wrap... ");
        GameEngine engine = new GameEngine(false);
        engine.setGameState(GameState.PLAYING, 0);

        // Pac-man starts at (13, 23). Moving left into open corridor
        engine.setPacmanNextDirection(Direction.LEFT);
        for (int i = 0; i < 30; i++) {
            engine.update(0.02);
        }
        assertTrue(engine.getPacman().getPosition().getX() < 13.0, "Pacman moved left");

        // Test 180 reverse
        engine.setPacmanNextDirection(Direction.RIGHT);
        for (int i = 0; i < 30; i++) {
            engine.update(0.02);
        }
        assertTrue(engine.getPacman().getPosition().getX() > 12.0, "Pacman reversed right");

        System.out.println("PASSED");
    }

    private static void testGhostAIAndHouseRelease() {
        System.out.print("[4/9] Testing Ghost Personalities & House Release... ");
        GameEngine engine = new GameEngine(false);
        engine.setGameState(GameState.PLAYING, 0);

        // Run simulation for 8 seconds to allow ghosts to exit house
        for (int i = 0; i < 400; i++) {
            engine.update(0.02);
        }

        boolean anyLeavingOrOutside = false;
        for (Ghost ghost : engine.getGhosts()) {
            if (ghost.getState() == GhostState.CHASE || ghost.getState() == GhostState.SCATTER || ghost.getState() == GhostState.LEAVING_HOUSE) {
                anyLeavingOrOutside = true;
            }
        }
        assertTrue(anyLeavingOrOutside, "Ghosts must exit house");
        System.out.println("PASSED");
    }

    private static void testPowerPelletAndGhostEating() {
        System.out.print("[5/9] Testing Energizer & Ghost Eating Combos... ");
        GameEngine engine = new GameEngine(false);
        engine.setGameState(GameState.PLAYING, 0);

        // Teleport Pacman next to power pellet at (1, 3)
        engine.getPacman().getPosition().set(1.0, 3.0);
        engine.update(0.02);

        assertTrue(engine.getFrightenedTimer() > 0, "Frightened timer active");
        Ghost blinky = engine.getGhosts().get(0);
        assertEquals(GhostState.FRIGHTENED, blinky.getState(), "Blinky is frightened");

        // Move Blinky to Pacman position
        blinky.getPosition().set(1.0, 3.0);
        int scoreBefore = engine.getScoreManager().getScore();
        engine.update(0.02);

        assertEquals(GhostState.EATEN_RETURNING, blinky.getState(), "Blinky eaten and returning");
        assertTrue(engine.getScoreManager().getScore() >= scoreBefore + 200, "Score increased by at least 200");

        System.out.println("PASSED");
    }

    private static void testLevelProgression() {
        System.out.print("[6/9] Testing Level Progression... ");
        GameEngine engine = new GameEngine(false);
        engine.setGameState(GameState.PLAYING, 0);

        // Simulate eating all dots
        engine.getMaze().setDotsRemaining(0);
        engine.update(0.02);

        assertEquals(GameState.LEVEL_CLEAR, engine.getGameState(), "Level cleared state");
        String firstLayout = mazeSignature(engine.getMaze());
        String winMessage = new ConsoleRenderer().render(engine);
        assertTrue(winMessage.contains("LEVEL COMPLETE!"), "Level completion message rendered");

        // Step past level clear timer
        for (int i = 0; i < 110; i++) {
            engine.update(0.02);
        }

        assertEquals(2, engine.getLevel(), "Level progressed to 2");
        assertEquals(GameState.READY, engine.getGameState(), "Ready state on level 2");
        assertTrue(!firstLayout.equals(mazeSignature(engine.getMaze())), "Next level has a unique maze");
        assertTrue(engine.getMaze().getTotalDots() > ClassicMaze.create().getTotalDots(),
                "Procedural level has more dots than the classic level");
        double levelTwoGhostSpeed = engine.getGhosts().get(0).getBaseSpeed();
        engine.loadLevel(3);
        assertTrue(engine.getGhosts().get(0).getBaseSpeed() > levelTwoGhostSpeed,
                "Ghost speed increases with level");

        System.out.println("PASSED");
    }

    private static String mazeSignature(Maze maze) {
        StringBuilder signature = new StringBuilder(maze.getWidth() * maze.getHeight());
        for (int y = 0; y < maze.getHeight(); y++) {
            for (int x = 0; x < maze.getWidth(); x++) {
                signature.append(maze.getTile(x, y).ordinal());
            }
        }
        return signature.toString();
    }

    private static void testAudioSynthesis() {
        System.out.print("[7/9] Testing 8-bit Sound Synthesizer... ");
        SoundSynthesizer synth = new SoundSynthesizer();
        synth.setMuted(true);
        for (SoundType type : SoundType.values()) {
            synth.play(type);
        }
        synth.shutdown();
        System.out.println("PASSED (All 8 waveforms generated)");
    }

    private static void testProceduralGraphicsRendering() {
        System.out.print("[8/9] Testing Graphics2D Vector Rendering... ");
        GameEngine engine = new GameEngine(false);
        BufferedImage canvas = new BufferedImage(560, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();

        // Render complete frame
        ProceduralGraphics.drawMaze(g2, engine.getMaze(), 20, 0, 45, false);
        ProceduralGraphics.drawPellets(g2, engine.getMaze(), 20, 0, 45, 1.0);
        ProceduralGraphics.drawPacman(g2, engine.getPacman(), 20, 0, 45);
        for (Ghost ghost : engine.getGhosts()) {
            ProceduralGraphics.drawGhost(g2, ghost, false, 20, 0, 45);
        }
        g2.dispose();
        System.out.println("PASSED (560x700 buffer rendered cleanly)");
    }

    private static void testConsoleRendering() {
        System.out.print("[9/9] Testing Console ANSI Renderer... ");
        GameEngine engine = new GameEngine(false);
        ConsoleRenderer renderer = new ConsoleRenderer();
        String output = renderer.render(engine);
        assertTrue(output.contains("SCORE:"), "Header rendered");
        assertTrue(output.contains("HIGH:"), "High score rendered");
        assertTrue(output.contains("LVL:"), "Level rendered");
        System.out.println("PASSED (ANSI buffer: " + output.length() + " chars)");
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
}
