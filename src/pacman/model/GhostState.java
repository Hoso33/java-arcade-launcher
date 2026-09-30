package pacman.model;

/**
 * Lifecycle states of a Ghost.
 */
public enum GhostState {
    IN_HOUSE,
    LEAVING_HOUSE,
    SCATTER,
    CHASE,
    FRIGHTENED,
    EATEN_RETURNING;

    public boolean isVulnerable() {
        return this == FRIGHTENED;
    }

    public boolean isDangerous() {
        return this == CHASE || this == SCATTER;
    }

    public boolean isEyes() {
        return this == EATEN_RETURNING;
    }
}
