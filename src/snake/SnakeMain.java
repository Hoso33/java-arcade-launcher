package snake;

import common.UnifiedLauncher;

/**
 * Dedicated Entry point for the Nokia 3310 Snake game.
 */
public class SnakeMain {

    public static void main(String[] args) {
        UnifiedLauncher.InterfaceChoice mode = UnifiedLauncher.InterfaceChoice.GUI;
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--cli", "-c" -> mode = UnifiedLauncher.InterfaceChoice.CLI;
                case "--gui", "-g" -> mode = UnifiedLauncher.InterfaceChoice.GUI;
                case "--help", "-h" -> {
                    System.out.println("Usage: java -cp bin snake.SnakeMain [--gui|--cli]");
                    return;
                }
                default -> {
                    System.err.println("Unknown option: " + arg);
                    System.err.println("Usage: java -cp bin snake.SnakeMain [--gui|--cli]");
                    return;
                }
            }
        }
        UnifiedLauncher.launchSnake(mode);
    }
}
