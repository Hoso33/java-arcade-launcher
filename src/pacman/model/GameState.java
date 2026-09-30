package pacman.model;

/**
 * Game execution state machine.
 */
public enum GameState {
    READY,
    PLAYING,
    PACMAN_DYING,
    LEVEL_CLEAR,
    GAME_OVER,
    PAUSED
}
