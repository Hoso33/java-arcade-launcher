package pacman.model;

/**
 * Bonus fruit item appearing at specific dot counts for extra score.
 */
public class Fruit {
    private final FruitType type;
    private final int tileX;
    private final int tileY;
    private double timer; // Remaining active seconds
    private boolean active;

    public Fruit(FruitType type, int tileX, int tileY, double durationSeconds) {
        this.type = type;
        this.tileX = tileX;
        this.tileY = tileY;
        this.timer = durationSeconds;
        this.active = true;
    }

    public FruitType getType() {
        return type;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public double getTimer() {
        return timer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void update(double deltaSeconds) {
        if (active) {
            timer -= deltaSeconds;
            if (timer <= 0) {
                active = false;
            }
        }
    }
}
