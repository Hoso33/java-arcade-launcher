package pacman.engine;

import pacman.level.ClassicMaze;
import pacman.level.MazeGenerator;
import pacman.model.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Shared Core Game Engine executing physics, AI, collisions, scoring, and level progression.
 */
public class GameEngine {

    private Maze maze;
    private final boolean alwaysProcedural;
    private final MazeGenerator mazeGenerator;
    private int level;
    private final Set<String> generatedMazeLayouts = new HashSet<>();

    private Pacman pacman;
    private List<Ghost> ghosts;
    private Fruit currentFruit;

    private final ScoreManager scoreManager;
    private GameState gameState;
    private final List<GameEventListener> eventListeners;

    // Timers
    private double stateTimer;
    private double frightenedTimer;
    private double frightenedTotalDuration;
    private double globalTimer;
    private int dotsEatenThisLevel;
    private boolean fruitSpawned1;
    private boolean fruitSpawned2;

    // Wave management (Scatter / Chase schedule)
    private int waveIndex;
    private double waveTimer;
    private boolean isChaseWave;

    private final Random random = new Random();

    public GameEngine(boolean alwaysProcedural) {
        this.alwaysProcedural = alwaysProcedural;
        this.mazeGenerator = new MazeGenerator();
        this.scoreManager = new ScoreManager();
        this.eventListeners = new CopyOnWriteArrayList<>();
        this.level = 1;
        initNewGame();
    }

    public void addEventListener(GameEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }

    private void fireEvent(GameEvent event) {
        for (GameEventListener listener : eventListeners) {
            listener.onGameEvent(event);
        }
    }

    public void initNewGame() {
        this.level = 1;
        generatedMazeLayouts.clear();
        this.scoreManager.reset();
        loadLevel(this.level);
        this.pacman.setLives(3);
        setGameState(GameState.READY, 2.2);
        fireEvent(new GameEvent(GameEvent.Type.GAME_START));
    }

    public void loadLevel(int lvl) {
        this.level = lvl;
        if (!alwaysProcedural && lvl == 1) {
            this.maze = ClassicMaze.create();
            generatedMazeLayouts.add(layoutSignature(maze));
        } else {
            Maze candidate;
            String signature;
            do {
                candidate = new MazeGenerator(random.nextLong()).generate(28, 31, lvl);
                signature = layoutSignature(candidate);
            } while (!generatedMazeLayouts.add(signature));
            this.maze = candidate;
        }

        this.dotsEatenThisLevel = 0;
        this.fruitSpawned1 = false;
        this.fruitSpawned2 = false;
        this.currentFruit = null;
        this.waveIndex = 0;
        this.waveTimer = 0.0;
        this.isChaseWave = false;
        this.frightenedTimer = 0.0;

        initEntities();
    }

    private void initEntities() {
        int px = maze.getPacmanSpawnX();
        int py = maze.getPacmanSpawnY();
        if (pacman == null) {
            pacman = new Pacman(px, py);
        } else {
            pacman.setSpawnPosition(px, py);
            pacman.resetToSpawn();
        }

        // Setup 4 Ghosts
        ghosts = new ArrayList<>(4);
        int w = maze.getWidth();
        int h = maze.getHeight();

        Ghost blinky = new Ghost(GhostType.BLINKY, maze.getBlinkySpawnX(), maze.getBlinkySpawnY(), w - 3, 0);
        Ghost pinky = new Ghost(GhostType.PINKY, maze.getPinkySpawnX(), maze.getPinkySpawnY(), 2, 0);
        Ghost inky = new Ghost(GhostType.INKY, maze.getInkySpawnX(), maze.getInkySpawnY(), w - 1, h - 1);
        Ghost clyde = new Ghost(GhostType.CLYDE, maze.getClydeSpawnX(), maze.getClydeSpawnY(), 0, h - 1);

        // Adjust entity speeds with level
        double pacmanSpeedMult = 1.0 + Math.min(0.4, (level - 1) * 0.05);
        double ghostSpeedMult = 1.0 + Math.min(0.75, (level - 1) * 0.05);
        pacman.setBaseSpeed(10.5 * pacmanSpeedMult);
        blinky.setBaseSpeed(10.0 * ghostSpeedMult);
        pinky.setBaseSpeed(9.5 * ghostSpeedMult);
        inky.setBaseSpeed(9.5 * ghostSpeedMult);
        clyde.setBaseSpeed(9.5 * ghostSpeedMult);

        ghosts.add(blinky);
        ghosts.add(pinky);
        ghosts.add(inky);
        ghosts.add(clyde);
    }

    private String layoutSignature(Maze layout) {
        StringBuilder signature = new StringBuilder(layout.getWidth() * layout.getHeight());
        for (int y = 0; y < layout.getHeight(); y++) {
            for (int x = 0; x < layout.getWidth(); x++) {
                signature.append((char) ('A' + layout.getTile(x, y).ordinal()));
            }
        }
        return signature.toString();
    }

    public void update(double deltaSeconds) {
        if (gameState == GameState.PAUSED) {
            return;
        }

        globalTimer += deltaSeconds;

        if (gameState == GameState.READY) {
            stateTimer -= deltaSeconds;
            if (stateTimer <= 0) {
                setGameState(GameState.PLAYING, 0);
            }
            return;
        }

        if (gameState == GameState.PACMAN_DYING) {
            stateTimer -= deltaSeconds;
            pacman.setDeathProgress(Math.min(1.0, 1.0 - (stateTimer / 1.5)));
            if (stateTimer <= 0) {
                pacman.decrementLives();
                if (pacman.getLives() <= 0) {
                    setGameState(GameState.GAME_OVER, 0);
                    fireEvent(new GameEvent(GameEvent.Type.GAME_OVER));
                } else {
                    resetEntityPositions();
                    setGameState(GameState.READY, 1.8);
                    fireEvent(new GameEvent(GameEvent.Type.PACMAN_RESPAWNED));
                }
            }
            return;
        }

        if (gameState == GameState.LEVEL_CLEAR) {
            stateTimer -= deltaSeconds;
            if (stateTimer <= 0) {
                loadLevel(level + 1);
                setGameState(GameState.READY, 2.0);
            }
            return;
        }

        if (gameState == GameState.GAME_OVER) {
            return;
        }

        // Active Playing State
        updateWaveSchedule(deltaSeconds);
        updateFrightenedState(deltaSeconds);
        updateFruit(deltaSeconds);
        updatePacman(deltaSeconds);
        updateGhosts(deltaSeconds);
        checkCollisions();
    }

    private void updateWaveSchedule(double deltaSeconds) {
        if (frightenedTimer > 0) {
            return; // Waves paused while frightened
        }

        waveTimer += deltaSeconds;
        double threshold;
        if (!isChaseWave) {
            threshold = (waveIndex < 2) ? 7.0 : 5.0;
        } else {
            threshold = 20.0;
        }

        if (waveIndex < 4 && waveTimer >= threshold) {
            waveTimer = 0;
            if (!isChaseWave) {
                isChaseWave = true;
            } else {
                isChaseWave = false;
                waveIndex++;
            }

            // Switch ghost states if active
            for (Ghost ghost : ghosts) {
                if (ghost.getState() == GhostState.CHASE || ghost.getState() == GhostState.SCATTER) {
                    ghost.setState(isChaseWave ? GhostState.CHASE : GhostState.SCATTER);
                    ghost.setCurrentDirection(ghost.getCurrentDirection().opposite());
                }
            }
        }
    }

    private void updateFrightenedState(double deltaSeconds) {
        if (frightenedTimer > 0) {
            frightenedTimer -= deltaSeconds;
            if (frightenedTimer <= 0) {
                frightenedTimer = 0;
                scoreManager.resetPowerPelletMultiplier();
                for (Ghost ghost : ghosts) {
                    if (ghost.getState() == GhostState.FRIGHTENED) {
                        ghost.setState(isChaseWave ? GhostState.CHASE : GhostState.SCATTER);
                    }
                }
                fireEvent(new GameEvent(GameEvent.Type.GHOST_FRIGHTENED_END));
            }
        }
    }

    private void updateFruit(double deltaSeconds) {
        if (!fruitSpawned1 && dotsEatenThisLevel >= 70) {
            fruitSpawned1 = true;
            spawnFruit();
        }
        if (!fruitSpawned2 && dotsEatenThisLevel >= 170) {
            fruitSpawned2 = true;
            spawnFruit();
        }

        if (currentFruit != null && currentFruit.isActive()) {
            currentFruit.update(deltaSeconds);
            if (!currentFruit.isActive()) {
                currentFruit = null;
            }
        }
    }

    private void spawnFruit() {
        FruitType type = FruitType.forLevel(level);
        currentFruit = new Fruit(type, maze.getFruitSpawnX(), maze.getFruitSpawnY(), 9.5);
        fireEvent(new GameEvent(GameEvent.Type.FRUIT_SPAWNED, 0, null, type));
    }

    private void updatePacman(double deltaSeconds) {
        pacman.updateMouthAnimation(deltaSeconds);
        movePacman(deltaSeconds);

        int tx = pacman.getTileX();
        int ty = pacman.getTileY();

        if (isNearTileCenter(pacman.getPosition())) {
            TileType currentTile = maze.getTile(tx, ty);
            if (currentTile == TileType.DOT) {
                maze.setTile(tx, ty, TileType.EMPTY);
                maze.decrementDotsRemaining();
                dotsEatenThisLevel++;
                scoreManager.addScore(10);
                checkExtraLife();
                fireEvent(new GameEvent(GameEvent.Type.DOT_EATEN, 10));

                checkLevelCompletion();
            } else if (currentTile == TileType.POWER_PELLET) {
                maze.setTile(tx, ty, TileType.EMPTY);
                maze.decrementDotsRemaining();
                dotsEatenThisLevel++;
                scoreManager.addScore(50);
                checkExtraLife();
                triggerPowerPellet();
                fireEvent(new GameEvent(GameEvent.Type.POWER_PELLET_EATEN, 50));

                checkLevelCompletion();
            }

            if (currentFruit != null && currentFruit.isActive()) {
                if (currentFruit.getTileX() == tx && currentFruit.getTileY() == ty) {
                    int pts = currentFruit.getType().getPoints();
                    scoreManager.addScore(pts);
                    checkExtraLife();
                    currentFruit.setActive(false);
                    fireEvent(new GameEvent(GameEvent.Type.FRUIT_EATEN, pts, null, currentFruit.getType()));
                }
            }
        }
    }

    private void triggerPowerPellet() {
        scoreManager.resetPowerPelletMultiplier();
        frightenedTotalDuration = Math.max(2.0, 7.0 - (level - 1) * 0.8);
        frightenedTimer = frightenedTotalDuration;

        for (Ghost ghost : ghosts) {
            if (ghost.getState() != GhostState.EATEN_RETURNING && ghost.getState() != GhostState.IN_HOUSE) {
                ghost.setState(GhostState.FRIGHTENED);
                ghost.setCurrentDirection(ghost.getCurrentDirection().opposite());
            }
        }
        fireEvent(new GameEvent(GameEvent.Type.GHOST_FRIGHTENED_START));
    }

    private void checkExtraLife() {
        if (scoreManager.checkAndAwardExtraLife()) {
            pacman.addLife();
            fireEvent(new GameEvent(GameEvent.Type.EXTRA_LIFE_EARNED));
        }
    }

    private void checkLevelCompletion() {
        if (maze.getDotsRemaining() <= 0) {
            setGameState(GameState.LEVEL_CLEAR, 2.0);
            fireEvent(new GameEvent(GameEvent.Type.LEVEL_CLEARED));
        }
    }

    private void updateGhosts(double deltaSeconds) {
        for (Ghost ghost : ghosts) {
            ghost.updateSkirtAnimation(deltaSeconds);
            updateGhostHouseRelease(ghost, deltaSeconds);

            double speed = ghost.getBaseSpeed();
            if (ghost.getState() == GhostState.EATEN_RETURNING) {
                speed *= 1.8;
            } else if (ghost.getState() == GhostState.FRIGHTENED) {
                speed *= 0.6;
            } else if (maze.getTile(ghost.getTileX(), ghost.getTileY()) == TileType.TUNNEL) {
                speed *= 0.55;
            } else if (ghost.getType() == GhostType.BLINKY && maze.getDotsRemaining() < 20) {
                speed *= 1.1;
            }
            ghost.setCurrentSpeed(speed);

            computeGhostTarget(ghost);

            boolean inHouse = ghost.getState() == GhostState.IN_HOUSE || ghost.getState() == GhostState.LEAVING_HOUSE;
            moveGhost(ghost, deltaSeconds, inHouse);

            if (ghost.getState() == GhostState.EATEN_RETURNING) {
                if (ghost.getTileX() == maze.getGhostHouseDoorX() && Math.abs(ghost.getPosition().getY() - (maze.getGhostHouseDoorY() + 1.5)) < 0.5) {
                    ghost.setState(GhostState.LEAVING_HOUSE);
                }
            }
        }
    }

    private void updateGhostHouseRelease(Ghost ghost, double deltaSeconds) {
        if (ghost.getState() == GhostState.IN_HOUSE) {
            ghost.setStateTimer(ghost.getStateTimer() + deltaSeconds);
            boolean shouldRelease = switch (ghost.getType()) {
                case BLINKY -> true;
                case PINKY -> ghost.getStateTimer() > 1.5 || dotsEatenThisLevel >= 0;
                case INKY -> ghost.getStateTimer() > 4.0 || dotsEatenThisLevel >= 30;
                case CLYDE -> ghost.getStateTimer() > 7.0 || dotsEatenThisLevel >= 60;
            };
            if (shouldRelease) {
                ghost.setState(GhostState.LEAVING_HOUSE);
            }
        } else if (ghost.getState() == GhostState.LEAVING_HOUSE) {
            double doorX = maze.getGhostHouseDoorX();
            double doorY = maze.getGhostHouseDoorY() - 0.5;

            Position pos = ghost.getPosition();
            if (Math.abs(pos.getX() - doorX) > 0.1) {
                pos.setX(pos.getX() + (doorX > pos.getX() ? 1 : -1) * ghost.getCurrentSpeed() * deltaSeconds * 0.5);
            } else {
                pos.setX(doorX);
                if (pos.getY() > doorY) {
                    pos.setY(pos.getY() - ghost.getCurrentSpeed() * deltaSeconds);
                } else {
                    ghost.setState(frightenedTimer > 0 ? GhostState.FRIGHTENED : (isChaseWave ? GhostState.CHASE : GhostState.SCATTER));
                    ghost.setCurrentDirection(Direction.LEFT);
                    ghost.setNextDirection(Direction.LEFT);
                }
            }
        }
    }

    private void computeGhostTarget(Ghost ghost) {
        int pacX = pacman.getTileX();
        int pacY = pacman.getTileY();
        Direction pacDir = pacman.getCurrentDirection();

        switch (ghost.getState()) {
            case SCATTER -> {
                ghost.setTargetTileX(ghost.getScatterTileX());
                ghost.setTargetTileY(ghost.getScatterTileY());
            }
            case CHASE -> {
                switch (ghost.getType()) {
                    case BLINKY -> {
                        ghost.setTargetTileX(pacX);
                        ghost.setTargetTileY(pacY);
                    }
                    case PINKY -> {
                        int tx = pacX + 4 * pacDir.getDx();
                        int ty = pacY + 4 * pacDir.getDy();
                        ghost.setTargetTileX(tx);
                        ghost.setTargetTileY(ty);
                    }
                    case INKY -> {
                        Ghost blinky = ghosts.get(0);
                        int midX = pacX + 2 * pacDir.getDx();
                        int midY = pacY + 2 * pacDir.getDy();
                        int tx = midX + (midX - blinky.getTileX());
                        int ty = midY + (midY - blinky.getTileY());
                        ghost.setTargetTileX(tx);
                        ghost.setTargetTileY(ty);
                    }
                    case CLYDE -> {
                        double distSq = ghost.getPosition().distanceSquaredToTile(pacX, pacY);
                        if (distSq >= 64) {
                            ghost.setTargetTileX(pacX);
                            ghost.setTargetTileY(pacY);
                        } else {
                            ghost.setTargetTileX(ghost.getScatterTileX());
                            ghost.setTargetTileY(ghost.getScatterTileY());
                        }
                    }
                }
            }
            case EATEN_RETURNING -> {
                ghost.setTargetTileX(maze.getGhostHouseDoorX());
                ghost.setTargetTileY(maze.getGhostHouseDoorY() + 1);
            }
            case FRIGHTENED -> {
                ghost.setTargetTileX(random.nextInt(maze.getWidth()));
                ghost.setTargetTileY(random.nextInt(maze.getHeight()));
            }
            default -> {}
        }
    }

    /**
     * Precision cornering and smooth sub-tile movement for Pac-Man.
     */
    private void movePacman(double deltaSeconds) {
        Position pos = pacman.getPosition();
        Direction curr = pacman.getCurrentDirection();
        Direction next = pacman.getNextDirection();

        // 1. Instant 180-degree turnaround
        if (next != Direction.NONE && next.isOpposite(curr)) {
            pacman.setCurrentDirection(next);
            curr = next;
        }

        int currentTileX = (int) Math.round(pos.getX());
        int currentTileY = (int) Math.round(pos.getY());

        // 2. If stopped or near center, test turning into nextDirection
        if (next != Direction.NONE && next != curr) {
            boolean canTurn = false;
            if (curr == Direction.NONE) {
                int targetX = currentTileX + next.getDx();
                int targetY = currentTileY + next.getDy();
                if (maze.isPassable(targetX, targetY, false, false)) {
                    pos.set(currentTileX, currentTileY);
                    pacman.setCurrentDirection(next);
                    curr = next;
                    canTurn = true;
                }
            } else {
                double distToCenter = Math.hypot(pos.getX() - currentTileX, pos.getY() - currentTileY);
                if (distToCenter < 0.35) {
                    int targetX = currentTileX + next.getDx();
                    int targetY = currentTileY + next.getDy();
                    if (maze.isPassable(targetX, targetY, false, false)) {
                        pos.set(currentTileX, currentTileY);
                        pacman.setCurrentDirection(next);
                        curr = next;
                        canTurn = true;
                    }
                }
            }
        }

        // 3. Move forward along current direction
        if (curr != Direction.NONE) {
            double step = pacman.getCurrentSpeed() * deltaSeconds;
            double nextX = pos.getX() + curr.getDx() * step;
            double nextY = pos.getY() + curr.getDy() * step;

            int aheadTileX = currentTileX + curr.getDx();
            int aheadTileY = currentTileY + curr.getDy();

            boolean aheadPassable = maze.isPassable(aheadTileX, aheadTileY, false, false);

            if (aheadPassable) {
                pos.set(nextX, nextY);
            } else {
                // Moving towards blocked wall: clamp at center of current tile
                if (curr == Direction.RIGHT && nextX >= currentTileX) {
                    pos.setX(currentTileX);
                    pacman.setCurrentDirection(Direction.NONE);
                } else if (curr == Direction.LEFT && nextX <= currentTileX) {
                    pos.setX(currentTileX);
                    pacman.setCurrentDirection(Direction.NONE);
                } else if (curr == Direction.DOWN && nextY >= currentTileY) {
                    pos.setY(currentTileY);
                    pacman.setCurrentDirection(Direction.NONE);
                } else if (curr == Direction.UP && nextY <= currentTileY) {
                    pos.setY(currentTileY);
                    pacman.setCurrentDirection(Direction.NONE);
                } else {
                    pos.set(nextX, nextY);
                }
            }
        }

        // 4. Horizontal Tunnel Wrap-Around
        int mazeW = maze.getWidth();
        if (pos.getX() < -0.5) {
            pos.setX(mazeW - 0.5);
        } else if (pos.getX() > mazeW - 0.5) {
            pos.setX(-0.5);
        }
    }

    /**
     * Precision movement and decision making for Ghosts.
     */
    private void moveGhost(Ghost ghost, double deltaSeconds, boolean inHouse) {
        Position pos = ghost.getPosition();
        Direction curr = ghost.getCurrentDirection();

        int tileX = (int) Math.round(pos.getX());
        int tileY = (int) Math.round(pos.getY());
        double distToCenter = Math.hypot(pos.getX() - tileX, pos.getY() - tileY);

        if (!inHouse && distToCenter < 0.2) {
            chooseGhostNextDirection(ghost);
            curr = ghost.getCurrentDirection();
        }

        if (curr != Direction.NONE) {
            double step = ghost.getCurrentSpeed() * deltaSeconds;
            double nextX = pos.getX() + curr.getDx() * step;
            double nextY = pos.getY() + curr.getDy() * step;

            int aheadTileX = tileX + curr.getDx();
            int aheadTileY = tileY + curr.getDy();
            boolean aheadPassable = maze.isPassable(aheadTileX, aheadTileY, true, inHouse);

            if (aheadPassable) {
                pos.set(nextX, nextY);
            } else {
                if (curr.getDx() != 0) pos.setX(tileX);
                if (curr.getDy() != 0) pos.setY(tileY);
                chooseGhostNextDirection(ghost);
            }
        }

        // Tunnel Wrap-Around
        int mazeW = maze.getWidth();
        if (pos.getX() < -0.5) {
            pos.setX(mazeW - 0.5);
        } else if (pos.getX() > mazeW - 0.5) {
            pos.setX(-0.5);
        }
    }

    private void chooseGhostNextDirection(Ghost ghost) {
        int tx = ghost.getTileX();
        int ty = ghost.getTileY();
        Direction currDir = ghost.getCurrentDirection();

        Direction[] dirs = {Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT};
        List<Direction> validDirs = new ArrayList<>();

        for (Direction d : dirs) {
            if (currDir != Direction.NONE && d.isOpposite(currDir)) {
                continue;
            }
            int nx = tx + d.getDx();
            int ny = ty + d.getDy();
            boolean inHouse = ghost.getState() == GhostState.EATEN_RETURNING;
            if (maze.isPassable(nx, ny, true, inHouse)) {
                validDirs.add(d);
            }
        }

        if (validDirs.isEmpty()) {
            ghost.setCurrentDirection(currDir.opposite());
            return;
        }

        if (ghost.getState() == GhostState.FRIGHTENED) {
            Direction chosen = validDirs.get(random.nextInt(validDirs.size()));
            ghost.setCurrentDirection(chosen);
            return;
        }

        Direction bestDir = validDirs.get(0);
        double bestDistSq = Double.MAX_VALUE;

        for (Direction d : validDirs) {
            int nx = tx + d.getDx();
            int ny = ty + d.getDy();
            double distSq = Math.pow(nx - ghost.getTargetTileX(), 2) + Math.pow(ny - ghost.getTargetTileY(), 2);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestDir = d;
            }
        }

        ghost.setCurrentDirection(bestDir);
    }

    private void checkCollisions() {
        for (Ghost ghost : ghosts) {
            double dist = pacman.getPosition().distanceTo(ghost.getPosition());
            if (dist < 0.75) {
                if (ghost.getState() == GhostState.FRIGHTENED) {
                    ghost.setState(GhostState.EATEN_RETURNING);
                    int pts = scoreManager.addGhostEatenScore();
                    checkExtraLife();
                    fireEvent(new GameEvent(GameEvent.Type.GHOST_EATEN, ghost, pts));
                } else if (ghost.getState() == GhostState.CHASE || ghost.getState() == GhostState.SCATTER) {
                    setGameState(GameState.PACMAN_DYING, 1.5);
                    pacman.setDead(true);
                    fireEvent(new GameEvent(GameEvent.Type.PACMAN_DIED));
                    break;
                }
            }
        }
    }

    private boolean isNearTileCenter(Position pos) {
        int tx = (int) Math.round(pos.getX());
        int ty = (int) Math.round(pos.getY());
        return Math.hypot(pos.getX() - tx, pos.getY() - ty) < 0.25;
    }

    public void setPacmanNextDirection(Direction dir) {
        if (pacman != null) {
            pacman.setNextDirection(dir);
        }
    }

    public void resetEntityPositions() {
        pacman.resetToSpawn();
        for (Ghost ghost : ghosts) {
            ghost.resetToSpawn();
        }
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }

    public void setGameState(GameState state, double duration) {
        this.gameState = state;
        this.stateTimer = duration;
    }

    public Maze getMaze() {
        return maze;
    }

    public Pacman getPacman() {
        return pacman;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public Fruit getCurrentFruit() {
        return currentFruit;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getLevel() {
        return level;
    }

    public double getFrightenedTimer() {
        return frightenedTimer;
    }

    public double getFrightenedTotalDuration() {
        return frightenedTotalDuration;
    }

    public boolean isFrightenedFlashing() {
        return frightenedTimer > 0 && frightenedTimer <= 2.0 && ((int) (frightenedTimer * 6) % 2 == 0);
    }
}
