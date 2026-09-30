package pacman.model;

/**
 * Representation of the Pac-Man maze grid and its contents.
 */
public class Maze {
    private final int width;
    private final int height;
    private final TileType[][] tiles;
    private int totalDots;
    private int dotsRemaining;

    private int pacmanSpawnX;
    private int pacmanSpawnY;
    private int blinkySpawnX;
    private int blinkySpawnY;
    private int pinkySpawnX;
    private int pinkySpawnY;
    private int inkySpawnX;
    private int inkySpawnY;
    private int clydeSpawnX;
    private int clydeSpawnY;
    private int ghostHouseDoorX;
    private int ghostHouseDoorY;
    private int fruitSpawnX;
    private int fruitSpawnY;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new TileType[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = TileType.EMPTY;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TileType.WALL;
        }
        return tiles[y][x];
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[y][x] = type;
        }
    }

    public boolean isPassable(int x, int y, boolean isGhost, boolean inOrLeavingHouse) {
        if (y < 0 || y >= height) {
            return false;
        }
        // Horizontal tunnel wrap-around
        if (x < 0 || x >= width) {
            return true;
        }
        TileType tile = tiles[y][x];
        if (isGhost) {
            return tile.isPassableByGhost(inOrLeavingHouse);
        } else {
            return tile.isPassableByPacman();
        }
    }

    public void recalculateDots() {
        int dots = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (tiles[y][x] == TileType.DOT || tiles[y][x] == TileType.POWER_PELLET) {
                    dots++;
                }
            }
        }
        this.totalDots = dots;
        this.dotsRemaining = dots;
    }

    public int getTotalDots() {
        return totalDots;
    }

    public void setTotalDots(int totalDots) {
        this.totalDots = totalDots;
    }

    public int getDotsRemaining() {
        return dotsRemaining;
    }

    public void setDotsRemaining(int dotsRemaining) {
        this.dotsRemaining = dotsRemaining;
    }

    public void decrementDotsRemaining() {
        if (dotsRemaining > 0) {
            dotsRemaining--;
        }
    }

    public int getPacmanSpawnX() {
        return pacmanSpawnX;
    }

    public void setPacmanSpawn(int x, int y) {
        this.pacmanSpawnX = x;
        this.pacmanSpawnY = y;
    }

    public int getPacmanSpawnY() {
        return pacmanSpawnY;
    }

    public int getBlinkySpawnX() {
        return blinkySpawnX;
    }

    public int getBlinkySpawnY() {
        return blinkySpawnY;
    }

    public void setBlinkySpawn(int x, int y) {
        this.blinkySpawnX = x;
        this.blinkySpawnY = y;
    }

    public int getPinkySpawnX() {
        return pinkySpawnX;
    }

    public int getPinkySpawnY() {
        return pinkySpawnY;
    }

    public void setPinkySpawn(int x, int y) {
        this.pinkySpawnX = x;
        this.pinkySpawnY = y;
    }

    public int getInkySpawnX() {
        return inkySpawnX;
    }

    public int getInkySpawnY() {
        return inkySpawnY;
    }

    public void setInkySpawn(int x, int y) {
        this.inkySpawnX = x;
        this.inkySpawnY = y;
    }

    public int getClydeSpawnX() {
        return clydeSpawnX;
    }

    public int getClydeSpawnY() {
        return clydeSpawnY;
    }

    public void setClydeSpawn(int x, int y) {
        this.clydeSpawnX = x;
        this.clydeSpawnY = y;
    }

    public int getGhostHouseDoorX() {
        return ghostHouseDoorX;
    }

    public int getGhostHouseDoorY() {
        return ghostHouseDoorY;
    }

    public void setGhostHouseDoor(int x, int y) {
        this.ghostHouseDoorX = x;
        this.ghostHouseDoorY = y;
    }

    public int getFruitSpawnX() {
        return fruitSpawnX;
    }

    public int getFruitSpawnY() {
        return fruitSpawnY;
    }

    public void setFruitSpawn(int x, int y) {
        this.fruitSpawnX = x;
        this.fruitSpawnY = y;
    }

    public Maze copy() {
        Maze copy = new Maze(this.width, this.height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy.tiles[y][x] = this.tiles[y][x];
            }
        }
        copy.totalDots = this.totalDots;
        copy.dotsRemaining = this.dotsRemaining;
        copy.pacmanSpawnX = this.pacmanSpawnX;
        copy.pacmanSpawnY = this.pacmanSpawnY;
        copy.blinkySpawnX = this.blinkySpawnX;
        copy.blinkySpawnY = this.blinkySpawnY;
        copy.pinkySpawnX = this.pinkySpawnX;
        copy.pinkySpawnY = this.pinkySpawnY;
        copy.inkySpawnX = this.inkySpawnX;
        copy.inkySpawnY = this.inkySpawnY;
        copy.clydeSpawnX = this.clydeSpawnX;
        copy.clydeSpawnY = this.clydeSpawnY;
        copy.ghostHouseDoorX = this.ghostHouseDoorX;
        copy.ghostHouseDoorY = this.ghostHouseDoorY;
        copy.fruitSpawnX = this.fruitSpawnX;
        copy.fruitSpawnY = this.fruitSpawnY;
        return copy;
    }
}
