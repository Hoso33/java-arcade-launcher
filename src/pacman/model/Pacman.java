package pacman.model;

/**
 * Pac-Man player entity with mouth animation, life count, and state.
 */
public class Pacman extends Entity {
    private int lives;
    private double mouthAngle; // 0.0 to 1.0 (closed to wide open)
    private boolean mouthOpening;
    private double deathProgress; // 0.0 to 1.0 for death animation
    private boolean isDead;

    public Pacman(double spawnX, double spawnY) {
        super(spawnX, spawnY, Direction.LEFT, 11.0); // 11 tiles/sec base speed
        this.lives = 3;
        this.mouthAngle = 0.5;
        this.mouthOpening = true;
        this.deathProgress = 0.0;
        this.isDead = false;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void decrementLives() {
        if (this.lives > 0) {
            this.lives--;
        }
    }

    public void addLife() {
        this.lives++;
    }

    public double getMouthAngle() {
        return mouthAngle;
    }

    public void setMouthAngle(double mouthAngle) {
        this.mouthAngle = mouthAngle;
    }

    public void updateMouthAnimation(double deltaSeconds) {
        if (currentDirection == Direction.NONE) {
            return;
        }
        double speed = 5.0; // mouth cycle speed
        if (mouthOpening) {
            mouthAngle += deltaSeconds * speed;
            if (mouthAngle >= 1.0) {
                mouthAngle = 1.0;
                mouthOpening = false;
            }
        } else {
            mouthAngle -= deltaSeconds * speed;
            if (mouthAngle <= 0.0) {
                mouthAngle = 0.0;
                mouthOpening = true;
            }
        }
    }

    public double getDeathProgress() {
        return deathProgress;
    }

    public void setDeathProgress(double deathProgress) {
        this.deathProgress = deathProgress;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    @Override
    public void resetToSpawn() {
        super.resetToSpawn();
        this.currentDirection = Direction.LEFT;
        this.nextDirection = Direction.LEFT;
        this.mouthAngle = 0.5;
        this.mouthOpening = true;
        this.deathProgress = 0.0;
        this.isDead = false;
    }
}
