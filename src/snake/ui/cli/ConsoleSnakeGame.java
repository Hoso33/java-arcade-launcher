package snake.ui.cli;

import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;
import snake.model.Direction;
import snake.model.GameState;

import java.io.InputStream;

/**
 * Terminal Runner for Nokia 3310 Snake in CLI mode.
 */
public class ConsoleSnakeGame {

    private final SnakeEngine engine;
    private final NokiaBeeper beeper;
    private final ConsoleSnakeRenderer renderer;
    private volatile boolean running;

    public ConsoleSnakeGame(SnakeEngine engine, NokiaBeeper beeper) {
        this.engine = engine;
        this.beeper = beeper;
        this.renderer = new ConsoleSnakeRenderer();
        this.running = false;
    }

    public void start() {
        this.running = true;

        setTerminalRawMode(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            setTerminalRawMode(false);
            System.out.print(ConsoleSnakeRenderer.ANSI_SHOW_CURSOR);
        }));

        System.out.print(ConsoleSnakeRenderer.ANSI_CLEAR_SCREEN);
        System.out.print(ConsoleSnakeRenderer.ANSI_HIDE_CURSOR);

        // Input reader thread
        Thread inputThread = new Thread(this::readInputLoop, "SnakeConsoleInputThread");
        inputThread.setDaemon(true);
        inputThread.start();

        // Game loop
        while (running) {
            long start = System.currentTimeMillis();

            engine.tick();
            String output = renderer.render(engine);
            System.out.print(output);
            System.out.flush();

            int delay = engine.getCurrentTickDelayMs();
            long elapsed = System.currentTimeMillis() - start;
            long sleepTime = delay - elapsed;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        setTerminalRawMode(false);
        System.out.print(ConsoleSnakeRenderer.ANSI_SHOW_CURSOR);
        System.out.println("\nThanks for playing Nokia 3310 Snake!");
    }

    public void stop() {
        this.running = false;
        if (beeper != null) {
            beeper.shutdown();
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
                        case 'w' -> engine.setRequestedDirection(Direction.UP);
                        case 's' -> engine.setRequestedDirection(Direction.DOWN);
                        case 'a' -> engine.setRequestedDirection(Direction.LEFT);
                        case 'd' -> engine.setRequestedDirection(Direction.RIGHT);
                        case 'p' -> engine.togglePause();
                        case ' ' -> {
                            if (engine.getGameState() == GameState.TITLE || engine.getGameState() == GameState.GAME_OVER) {
                                engine.startNewGame();
                            } else {
                                engine.togglePause();
                            }
                        }
                        case 'r' -> {
                            if (engine.getGameState() == GameState.GAME_OVER) {
                                engine.startNewGame();
                            }
                        }
                        case 'm' -> {
                            if (beeper != null) {
                                beeper.toggleMute();
                            }
                        }
                        case 'q' -> {
                            running = false;
                            return;
                        }
                        case 27 -> { // Escape / Arrow keys
                            Thread.sleep(10);
                            if (in.available() > 0) {
                                int next1 = in.read();
                                if (next1 == '[' || next1 == 'O') {
                                    if (in.available() > 0) {
                                        int next2 = in.read();
                                        switch (next2) {
                                            case 'A' -> engine.setRequestedDirection(Direction.UP);
                                            case 'B' -> engine.setRequestedDirection(Direction.DOWN);
                                            case 'C' -> engine.setRequestedDirection(Direction.RIGHT);
                                            case 'D' -> engine.setRequestedDirection(Direction.LEFT);
                                        }
                                    }
                                }
                            }
                        }
                        default -> {
                            if (engine.getGameState() == GameState.TITLE) {
                                engine.startNewGame();
                            }
                        }
                    }
                }
                Thread.sleep(15);
            }
        } catch (Exception ignored) {}
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
