package common;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;
import pacman.ui.cli.ConsoleGame;
import pacman.ui.gui.PacmanFrame;
import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;
import snake.ui.SnakeFrame;
import snake.ui.cli.ConsoleSnakeGame;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;

/**
 * Unified Game Launcher with graphical Swing hub, terminal console menus, and CLI argument parsing.
 */
public class UnifiedLauncher {

    public enum GameChoice {
        PACMAN,
        SNAKE
    }

    public enum InterfaceChoice {
        GUI,
        CLI
    }

    public static void launch(String[] args) {
        GameChoice selectedGame = null;
        InterfaceChoice selectedInterface = null;
        boolean alwaysProcedural = false;
        boolean forceConsoleMenu = false;

        // 1. Parse command-line flags
        for (String arg : args) {
            String lower = arg.toLowerCase();
            switch (lower) {
                case "--pacman", "-p", "pacman" -> selectedGame = GameChoice.PACMAN;
                case "--snake", "-s", "snake" -> selectedGame = GameChoice.SNAKE;
                case "--gui", "-g", "gui" -> selectedInterface = InterfaceChoice.GUI;
                case "--cli", "-c", "cli" -> selectedInterface = InterfaceChoice.CLI;
                case "--menu", "--cli-menu" -> forceConsoleMenu = true;
                case "--procedural" -> alwaysProcedural = true;
                case "--help", "-h" -> {
                    printHelp();
                    return;
                }
            }
        }

        // 2. If both game and interface are specified directly via CLI flags, launch immediately
        if (selectedGame != null && selectedInterface != null) {
            if (selectedGame == GameChoice.PACMAN) {
                launchPacman(selectedInterface, alwaysProcedural);
            } else {
                launchSnake(selectedInterface);
            }
            return;
        }

        // 3. If force console menu requested or headless terminal
        if (forceConsoleMenu || GraphicsEnvironment.isHeadless()) {
            runConsoleMenu(selectedGame, selectedInterface, alwaysProcedural);
            return;
        }

        // 4. Default: Open Graphical User Interface (GUI) Launcher Hub
        SwingUtilities.invokeLater(() -> {
            try {
                GuiLauncherFrame guiLauncher = new GuiLauncherFrame();
                guiLauncher.setVisible(true);
            } catch (Throwable t) {
                // Fallback to console menu if graphical window fails to open
                runConsoleMenu(null, null, false);
            }
        });
    }

    private static void runConsoleMenu(GameChoice selectedGame, InterfaceChoice selectedInterface, boolean alwaysProcedural) {
        Scanner scanner = new Scanner(System.in);

        if (selectedGame == null) {
            selectedGame = promptGameMenu(scanner);
            if (selectedGame == null) {
                System.out.println("Exiting. Have a great day!");
                return;
            }
        }

        if (selectedInterface == null) {
            selectedInterface = promptInterfaceMenu(scanner, selectedGame);
            if (selectedInterface == null) {
                System.out.println("Exiting. Have a great day!");
                return;
            }
        }

        if (selectedGame == GameChoice.PACMAN) {
            launchPacman(selectedInterface, alwaysProcedural);
        } else {
            launchSnake(selectedInterface);
        }
    }

    private static GameChoice promptGameMenu(Scanner scanner) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║       JAVA SE RETRO ARCADE CENTER        ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  Select Game to Play:                    ║");
        System.out.println("║    [1] Pac-Man                           ║");
        System.out.println("║    [2] Snake (Nokia 3310 Classic)        ║");
        System.out.println("║    [3] Exit                              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("Enter game choice (1-3) [Default 1]: ");

        try {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equals("2")) return GameChoice.SNAKE;
                if (input.equals("3")) return null;
                return GameChoice.PACMAN;
            }
        } catch (Exception ignored) {}
        return GameChoice.PACMAN;
    }

    private static InterfaceChoice promptInterfaceMenu(Scanner scanner, GameChoice game) {
        String gameName = (game == GameChoice.PACMAN) ? "Pac-Man" : "Snake (Nokia 3310)";
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         SELECT DISPLAY INTERFACE         ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  Game: " + String.format("%-33s", gameName) + " ║");
        System.out.println("║    [1] Graphical User Interface (GUI)    ║");
        System.out.println("║    [2] Terminal Console Interface (CLI)  ║");
        System.out.println("║    [3] Exit                              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("Enter mode choice (1-3) [Default 1]: ");

        try {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equals("2")) return InterfaceChoice.CLI;
                if (input.equals("3")) return null;
                return InterfaceChoice.GUI;
            }
        } catch (Exception ignored) {}
        return InterfaceChoice.GUI;
    }

    public static void launchPacman(InterfaceChoice mode, boolean alwaysProcedural) {
        GameEngine engine = new GameEngine(alwaysProcedural);
        SoundSynthesizer soundSynthesizer = new SoundSynthesizer();
        engine.addEventListener(soundSynthesizer);

        if (mode == InterfaceChoice.CLI) {
            ConsoleGame consoleGame = new ConsoleGame(engine, soundSynthesizer);
            consoleGame.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                PacmanFrame frame = new PacmanFrame(engine, soundSynthesizer);
                frame.setVisible(true);
            });
        }
    }

    public static void launchSnake(InterfaceChoice mode) {
        NokiaBeeper beeper = new NokiaBeeper();
        SnakeEngine engine = new SnakeEngine(24, 16, beeper);

        if (mode == InterfaceChoice.CLI) {
            ConsoleSnakeGame consoleSnake = new ConsoleSnakeGame(engine, beeper);
            consoleSnake.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                SnakeFrame frame = new SnakeFrame(engine, beeper);
                frame.setVisible(true);
            });
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -cp bin Main [game] [mode] [options]");
        System.out.println("Games:");
        System.out.println("  --pacman, -p       Select Pac-Man");
        System.out.println("  --snake, -s        Select Nokia 3310 Snake");
        System.out.println("Modes:");
        System.out.println("  --gui, -g          Run in Java Swing Graphical Mode");
        System.out.println("  --cli, -c          Run in Terminal Console Mode");
        System.out.println("Options:");
        System.out.println("  --menu             Force interactive Terminal Console Menu");
        System.out.println("  --procedural       Generate procedural mazes for all Pac-Man levels");
        System.out.println("  --help, -h         Show this help message");
    }
}
