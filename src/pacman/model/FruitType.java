package pacman.model;

/**
 * Fruit types with point values and colors.
 */
public enum FruitType {
    CHERRY(100, "Cherry", 0xFF0033),
    STRAWBERRY(300, "Strawberry", 0xFF2255),
    ORANGE(500, "Orange", 0xFF8800),
    APPLE(700, "Apple", 0xEE1111),
    MELON(1000, "Melon", 0x33DD33),
    GALAXIAN(2000, "Galaxian", 0xFFFF00),
    BELL(3000, "Bell", 0xFFEE22),
    KEY(5000, "Key", 0x00EEEE);

    private final int points;
    private final String displayName;
    private final int colorRgb;

    FruitType(int points, String displayName, int colorRgb) {
        this.points = points;
        this.displayName = displayName;
        this.colorRgb = colorRgb;
    }

    public int getPoints() {
        return points;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRgb() {
        return colorRgb;
    }

    public static FruitType forLevel(int level) {
        if (level <= 1) return CHERRY;
        if (level == 2) return STRAWBERRY;
        if (level <= 4) return ORANGE;
        if (level <= 6) return APPLE;
        if (level <= 8) return MELON;
        if (level <= 10) return GALAXIAN;
        if (level <= 12) return BELL;
        return KEY;
    }
}
