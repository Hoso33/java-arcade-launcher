package pacman.model;

/**
 * Base abstract class for movable game entities (Pacman, Ghosts).
 */
public abstract class Entity {
    protected Position position;
    protected Position spawnPosition;
    protected Direction currentDirection;
    protected Direction nextDirection;
    protected double baseSpeed; // in tiles per second
    protected double currentSpeed;

    public Entity(double spawnX, double spawnY, Direction initialDirection, double baseSpeed) {
        this.position = new Position(spawnX, spawnY);
        this.spawnPosition = new Position(spawnX, spawnY);
        this.currentDirection = initialDirection;
        this.nextDirection = initialDirection;
        this.baseSpeed = baseSpeed;
        this.currentSpeed = baseSpeed;
    }

    public Position getPosition() {
        return position;
    }

    public int getTileX() {
        return position.getTileX();
    }

    public int getTileY() {
        return position.getTileY();
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Direction getNextDirection() {
        return nextDirection;
    }

    public void setNextDirection(Direction nextDirection) {
        this.nextDirection = nextDirection;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public void resetToSpawn() {
        this.position.set(spawnPosition.getX(), spawnPosition.getY());
        this.currentDirection = Direction.NONE;
        this.nextDirection = Direction.NONE;
        this.currentSpeed = baseSpeed;
    }

    public void setSpawnPosition(double x, double y) {
        this.spawnPosition.set(x, y);
    }
}
