package pacman.ui.cli;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;
import pacman.model.Direction;
import pacman.model.GameState;

import java.io.InputStream;

/**
 * CLI Terminal runner with non-blocking keyboard input loop and ANSI screen updates.
 */
public class ConsoleGame {

    private final GameEngine engine;
    private final SoundSynthesizer soundSynthesizer;
    private final ConsoleRenderer renderer;
    private volatile boolean running;

    public ConsoleGame(GameEngine engine, SoundSynthesizer soundSynthesizer) {
        this.engine = engine;
        this.soundSynthesizer = soundSynthesizer;
        this.renderer = new ConsoleRenderer();
        this.running = false;
    }

    public void start() {
        this.running = true;

        // Set terminal to raw mode if on Unix/Mac
        setTerminalRawMode(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            setTerminalRawMode(false);
            System.out.print(ConsoleRenderer.ANSI_SHOW_CURSOR);
        }));

        System.out.print(ConsoleRenderer.ANSI_CLEAR_SCREEN);
        System.out.print(ConsoleRenderer.ANSI_HIDE_CURSOR);

        // Start Keyboard input thread
        Thread inputThread = new Thread(this::readInputLoop, "ConsoleInputThread");
        inputThread.setDaemon(true);
        inputThread.start();

        // Main game loop (~25 FPS for smooth CLI action)
        long lastTime = System.nanoTime();
        final long frameTimeNs = 40_000_000L; // 40ms per frame = 25 FPS

        while (running) {
            long now = System.nanoTime();
            double deltaSeconds = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            if (deltaSeconds > 0.1) deltaSeconds = 0.1;

            engine.update(deltaSeconds);
            String output = renderer.render(engine);
            System.out.print(output);
            System.out.flush();

            long elapsed = System.nanoTime() - now;
            long sleepTime = (frameTimeNs - elapsed) / 1_000_000L;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        setTerminalRawMode(false);
        System.out.print(ConsoleRenderer.ANSI_SHOW_CURSOR);
        System.out.println("\nThanks for playing Pac-Man!");
    }

    public void stop() {
        this.running = false;
        if (soundSynthesizer != null) {
            soundSynthesizer.shutdown();
        }
    }

    private void readInputLoop() {
        InputStream in = System.in;
        try {
            while (running) {
                if (in.available() > 0) {
                    int c = in.read();
                    if (c == -1) break;

                    switch (Character.toLowerCase((char) c)) {
                        case 'w' -> engine.setPacmanNextDirection(Direction.UP);
                        case 's' -> engine.setPacmanNextDirection(Direction.DOWN);
                        case 'a' -> engine.setPacmanNextDirection(Direction.LEFT);
                        case 'd' -> engine.setPacmanNextDirection(Direction.RIGHT);
                        case 'p' -> engine.togglePause();
                        case 'm' -> {
                            if (soundSynthesizer != null) {
                                soundSynthesizer.toggleMute();
                            }
                        }
                        case 'r' -> {
                            if (engine.getGameState() == GameState.GAME_OVER) {
                                engine.initNewGame();
                            }
                        }
                        case 'q' -> {
                            running = false;
                            return;
                        }
                        case 27 -> { // Escape sequence handling for Arrow Keys (\033[A, \033OA, etc.)
                            Thread.sleep(10);
                            if (in.available() > 0) {
                                int next1 = in.read();
                                if (next1 == '[' || next1 == 'O') {
                                    if (in.available() > 0) {
                                        int next2 = in.read();
                                        switch (next2) {
                                            case 'A' -> engine.setPacmanNextDirection(Direction.UP);
                                            case 'B' -> engine.setPacmanNextDirection(Direction.DOWN);
                                            case 'C' -> engine.setPacmanNextDirection(Direction.RIGHT);
                                            case 'D' -> engine.setPacmanNextDirection(Direction.LEFT);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(15);
            }
        } catch (Exception ignored) {
        }
    }

    private void setTerminalRawMode(boolean enable) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            try {
                if (enable) {
                    Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty raw -echo < /dev/tty 2>/dev/null"}).waitFor();
                } else {
                    Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty cooked echo < /dev/tty 2>/dev/null"}).waitFor();
                }
            } catch (Exception ignored) {}
        }
    }
}
