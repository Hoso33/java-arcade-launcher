package pacman.ui;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;
import pacman.ui.cli.ConsoleGame;
import pacman.ui.gui.PacmanFrame;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;

/**
 * Startup Launcher with CLI flag parsing and interactive console menu fallback.
 */
public class Launcher {

    public static void launch(String[] args) {
        boolean forceCli = false;
        boolean forceGui = false;
        boolean alwaysProcedural = false;

        for (String arg : args) {
            String lower = arg.toLowerCase();
            if (lower.equals("--cli") || lower.equals("-c")) {
                forceCli = true;
            } else if (lower.equals("--gui") || lower.equals("-g")) {
                forceGui = true;
            } else if (lower.equals("--procedural") || lower.equals("-p")) {
                alwaysProcedural = true;
            } else if (lower.equals("--help") || lower.equals("-h")) {
                printHelp();
                return;
            }
        }

        // If no mode specified via flags, show interactive terminal selection
        if (!forceCli && !forceGui) {
            if (GraphicsEnvironment.isHeadless()) {
                forceCli = true;
            } else {
                int selection = promptStartupMenu();
                if (selection == 2) {
                    forceCli = true;
                } else if (selection == 3) {
                    System.out.println("Goodbye!");
                    return;
                } else {
                    forceGui = true;
                }
            }
        }

        GameEngine engine = new GameEngine(alwaysProcedural);
        SoundSynthesizer soundSynthesizer = new SoundSynthesizer();
        engine.addEventListener(soundSynthesizer);

        if (forceCli) {
            ConsoleGame consoleGame = new ConsoleGame(engine, soundSynthesizer);
            consoleGame.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                PacmanFrame frame = new PacmanFrame(engine, soundSynthesizer);
                frame.setVisible(true);
            });
        }
    }

    private static int promptStartupMenu() {
        System.out.println("\n==========================================");
        System.out.println("        PAC-MAN (Java SE Edition)         ");
        System.out.println("==========================================");
        System.out.println(" Select Interface Mode:");
        System.out.println("   [1] Graphical User Interface (GUI Swing)");
        System.out.println("   [2] Terminal Console Interface (CLI)");
        System.out.println("   [3] Exit");
        System.out.println("==========================================");
        System.out.print("Enter choice (1-3) [Default 1]: ");

        try {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.equals("2")) return 2;
                if (input.equals("3")) return 3;
                return 1;
            }
        } catch (Exception ignored) {}

        return 1; // Default to GUI
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar pacman.jar [options]");
        System.out.println("Options:");
        System.out.println("  --gui, -g          Launch in Graphical User Interface (Swing) mode");
        System.out.println("  --cli, -c          Launch in Console/Terminal (CLI) mode");
        System.out.println("  --procedural, -p   Force procedural maze generation for all levels");
        System.out.println("  --help, -h         Display this help message");
    }
}
