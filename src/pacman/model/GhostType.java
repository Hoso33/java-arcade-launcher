package pacman.model;

/**
 * The 4 ghost types with their names and primary colors.
 */
public enum GhostType {
    BLINKY("Blinky", 0xFF0000), // Red
    PINKY("Pinky", 0xFFB8FF),   // Pink
    INKY("Inky", 0x00FFFF),     // Cyan
    CLYDE("Clyde", 0xFFB852);   // Orange

    private final String ghostName;
    private final int rgb;

    GhostType(String ghostName, int rgb) {
        this.ghostName = ghostName;
        this.rgb = rgb;
    }

    public String getGhostName() {
        return ghostName;
    }

    public int getRgb() {
        return rgb;
    }
}
