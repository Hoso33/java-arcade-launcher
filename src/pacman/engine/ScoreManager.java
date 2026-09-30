package pacman.engine;

/**
 * Handles score tracking, combo multipliers, high score tracking, and bonus lives.
 */
public class ScoreManager {
    private int score;
    private int highScore;
    private int ghostsEatenInPowerPellet;
    private boolean extraLifeAwarded;

    public ScoreManager() {
        this.score = 0;
        this.highScore = 10000;
        this.ghostsEatenInPowerPellet = 0;
        this.extraLifeAwarded = false;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public void addScore(int points) {
        this.score += points;
        if (this.score > highScore) {
            highScore = score;
        }
    }

    public boolean checkAndAwardExtraLife() {
        if (!extraLifeAwarded && score >= 10000) {
            extraLifeAwarded = true;
            return true;
        }
        return false;
    }

    public int addGhostEatenScore() {
        ghostsEatenInPowerPellet++;
        int points = switch (ghostsEatenInPowerPellet) {
            case 1 -> 200;
            case 2 -> 400;
            case 3 -> 800;
            default -> 1600;
        };
        addScore(points);
        return points;
    }

    public void resetPowerPelletMultiplier() {
        this.ghostsEatenInPowerPellet = 0;
    }

    public void reset() {
        this.score = 0;
        this.ghostsEatenInPowerPellet = 0;
        this.extraLifeAwarded = false;
    }
}
