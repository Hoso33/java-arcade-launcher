package pacman.engine;

/**
 * Listener interface for subscribing to gameplay events.
 */
@FunctionalInterface
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
