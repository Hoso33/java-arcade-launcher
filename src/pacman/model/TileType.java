package pacman.model;

/**
 * Represents the type of tile in the Pac-Man maze.
 */
public enum TileType {
    EMPTY,
    WALL,
    DOT,
    POWER_PELLET,
    GHOST_HOUSE_DOOR,
    GHOST_HOUSE,
    TUNNEL;

    public boolean isPassableByPacman() {
        return this != WALL && this != GHOST_HOUSE_DOOR && this != GHOST_HOUSE;
    }

    public boolean isPassableByGhost(boolean inOrLeavingHouse) {
        if (this == WALL) {
            return false;
        }
        if (this == GHOST_HOUSE_DOOR || this == GHOST_HOUSE) {
            return inOrLeavingHouse;
        }
        return true;
    }
}
