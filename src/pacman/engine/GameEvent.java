package pacman.engine;

import pacman.model.FruitType;
import pacman.model.Ghost;

/**
 * Game events dispatched by the GameEngine to UI and Audio subsystems.
 */
public class GameEvent {
    public enum Type {
        GAME_START,
        DOT_EATEN,
        POWER_PELLET_EATEN,
        GHOST_EATEN,
        GHOST_FRIGHTENED_START,
        GHOST_FRIGHTENED_END,
        PACMAN_DIED,
        PACMAN_RESPAWNED,
        LEVEL_CLEARED,
        GAME_OVER,
        FRUIT_SPAWNED,
        FRUIT_EATEN,
        EXTRA_LIFE_EARNED
    }

    private final Type type;
    private final int scoreAdded;
    private final Ghost ghost;
    private final FruitType fruitType;

    public GameEvent(Type type) {
        this(type, 0, null, null);
    }

    public GameEvent(Type type, int scoreAdded) {
        this(type, scoreAdded, null, null);
    }

    public GameEvent(Type type, Ghost ghost, int scoreAdded) {
        this(type, scoreAdded, ghost, null);
    }

    public GameEvent(Type type, FruitType fruitType, int scoreAdded) {
        this(type, scoreAdded, null, fruitType);
    }

    public GameEvent(Type type, int scoreAdded, Ghost ghost, FruitType fruitType) {
        this.type = type;
        this.scoreAdded = scoreAdded;
        this.ghost = ghost;
        this.fruitType = fruitType;
    }

    public Type getType() {
        return type;
    }

    public int getScoreAdded() {
        return scoreAdded;
    }

    public Ghost getGhost() {
        return ghost;
    }

    public FruitType getFruitType() {
        return fruitType;
    }
}
