package pacman.model;

/**
 * Ghost entity with state, AI target calculations, and animation properties.
 */
public class Ghost extends Entity {
    private final GhostType type;
    private GhostState state;
    private GhostState previousState; // State before frightened
    private int scatterTileX;
    private int scatterTileY;
    private int targetTileX;
    private int targetTileY;

    private double stateTimer; // For house exit delays or state transitions
    private double skirtAnimationTime;
    private boolean isInsideHouse;

    public Ghost(GhostType type, double spawnX, double spawnY, int scatterX, int scatterY) {
        super(spawnX, spawnY, Direction.UP, 10.0);
        this.type = type;
        this.scatterTileX = scatterX;
        this.scatterTileY = scatterY;
        this.state = (type == GhostType.BLINKY) ? GhostState.SCATTER : GhostState.IN_HOUSE;
        this.previousState = GhostState.SCATTER;
        this.isInsideHouse = (type != GhostType.BLINKY);
        this.stateTimer = 0.0;
        this.skirtAnimationTime = 0.0;
    }

    public GhostType getType() {
        return type;
    }

    public GhostState getState() {
        return state;
    }

    public void setState(GhostState state) {
        if (this.state != GhostState.FRIGHTENED && state == GhostState.FRIGHTENED) {
            this.previousState = this.state;
        }
        this.state = state;
    }

    public GhostState getPreviousState() {
        return previousState;
    }

    public void setPreviousState(GhostState previousState) {
        this.previousState = previousState;
    }

    public int getScatterTileX() {
        return scatterTileX;
    }

    public void setScatterTileX(int scatterTileX) {
        this.scatterTileX = scatterTileX;
    }

    public int getScatterTileY() {
        return scatterTileY;
    }

    public void setScatterTileY(int scatterTileY) {
        this.scatterTileY = scatterTileY;
    }

    public int getTargetTileX() {
        return targetTileX;
    }

    public void setTargetTileX(int targetTileX) {
        this.targetTileX = targetTileX;
    }

    public int getTargetTileY() {
        return targetTileY;
    }

    public void setTargetTileY(int targetTileY) {
        this.targetTileY = targetTileY;
    }

    public double getStateTimer() {
        return stateTimer;
    }

    public void setStateTimer(double stateTimer) {
        this.stateTimer = stateTimer;
    }

    public double getSkirtAnimationTime() {
        return skirtAnimationTime;
    }

    public void updateSkirtAnimation(double deltaSeconds) {
        skirtAnimationTime += deltaSeconds * 10.0;
    }

    public boolean isInsideHouse() {
        return isInsideHouse;
    }

    public void setInsideHouse(boolean insideHouse) {
        isInsideHouse = insideHouse;
    }

    @Override
    public void resetToSpawn() {
        super.resetToSpawn();
        this.state = (type == GhostType.BLINKY) ? GhostState.SCATTER : GhostState.IN_HOUSE;
        this.previousState = GhostState.SCATTER;
        this.isInsideHouse = (type != GhostType.BLINKY);
        this.currentDirection = (type == GhostType.BLINKY) ? Direction.LEFT : Direction.UP;
        this.nextDirection = this.currentDirection;
        this.stateTimer = 0.0;
        this.skirtAnimationTime = 0.0;
    }
}
