package pacman.level;

import pacman.model.Maze;
import pacman.model.TileType;

import java.util.*;

/**
 * Procedural Pac-Man Maze Generator.
 * Creates horizontally symmetric, playable mazes with corridors, ghost house, tunnels, and pellets.
 */
public class MazeGenerator {

    private final Random random;

    public MazeGenerator() {
        this.random = new Random();
    }

    public MazeGenerator(long seed) {
        this.random = new Random(seed);
    }

    public Maze generate(int width, int height) {
        return generate(width, height, 1);
    }

    public Maze generate(int width, int height, int level) {
        // Force width to be even for exact left-right symmetry
        if (width % 2 != 0) width++;
        if (width < 20) width = 28;
        if (height < 25) height = 31;

        int halfWidth = width / 2;
        TileType[][] grid = new TileType[height][width];

        // 1. Initialize entire maze with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = TileType.WALL;
            }
        }

        // 2. Define Ghost House area in the center
        int ghX = halfWidth - 4;
        int ghY = height / 2 - 3;
        int ghW = 8;
        int ghH = 5;

        // Ghost house boundaries
        for (int y = ghY; y < ghY + ghH; y++) {
            for (int x = ghX; x < ghX + ghW; x++) {
                if (y == ghY || y == ghY + ghH - 1 || x == ghX || x == ghX + ghW - 1) {
                    grid[y][x] = TileType.WALL;
                } else {
                    grid[y][x] = TileType.GHOST_HOUSE;
                }
            }
        }
        // Ghost House Door at the top center
        grid[ghY][halfWidth - 1] = TileType.GHOST_HOUSE_DOOR;
        grid[ghY][halfWidth] = TileType.GHOST_HOUSE_DOOR;

        // 3. Carve primary horizontal and vertical corridors on the left half
        List<Integer> vLanes = new ArrayList<>();
        vLanes.add(1);
        vLanes.add(halfWidth - 2);
        int midV = 1 + (halfWidth - 3) / 2;
        if (midV > 1 && midV < halfWidth - 2) {
            vLanes.add(midV);
        }
        Collections.sort(vLanes);

        List<Integer> hLanes = new ArrayList<>();
        hLanes.add(1);
        hLanes.add(height - 2);
        hLanes.add(ghY - 2);
        hLanes.add(ghY + ghH + 1);
        hLanes.add(height - 7);
        hLanes.add(5);

        // Carve lanes
        for (int vx : vLanes) {
            for (int y = 1; y < height - 1; y++) {
                if (!isInsideGhostHouse(vx, y, ghX, ghY, ghW, ghH)) {
                    grid[y][vx] = TileType.EMPTY;
                }
            }
        }

        for (int hy : hLanes) {
            if (hy > 0 && hy < height - 1) {
                for (int x = 1; x < halfWidth; x++) {
                    if (!isInsideGhostHouse(x, hy, ghX, ghY, ghW, ghH)) {
                        grid[hy][x] = TileType.EMPTY;
                    }
                }
            }
        }

        // 4. Carve random additional interconnecting branches/loops to create varied maze layouts
        int numRandomPaths = 8 + Math.min(24, Math.max(0, level - 1) * 2) + random.nextInt(3);
        for (int i = 0; i < numRandomPaths; i++) {
            int rx = 1 + random.nextInt(halfWidth - 2);
            int ry = 1 + random.nextInt(height - 2);
            int len = 3 + random.nextInt(5);
            boolean horizontal = random.nextBoolean();

            for (int step = 0; step < len; step++) {
                int cx = horizontal ? rx + step : rx;
                int cy = horizontal ? ry : ry + step;

                if (cx >= 1 && cx < halfWidth && cy >= 1 && cy < height - 1) {
                    if (!isInsideGhostHouse(cx, cy, ghX, ghY, ghW, ghH)) {
                        grid[cy][cx] = TileType.EMPTY;
                    }
                }
            }
        }

        // Ensure path around ghost house
        for (int x = ghX - 1; x < halfWidth; x++) {
            if (x >= 1) {
                grid[ghY - 1][x] = TileType.EMPTY; // Top corridor
                grid[ghY + ghH][x] = TileType.EMPTY; // Bottom corridor
            }
        }
        for (int y = ghY - 1; y <= ghY + ghH; y++) {
            if (ghX - 1 >= 1) {
                grid[y][ghX - 1] = TileType.EMPTY; // Left corridor
            }
        }

        // 5. Tunnel on side
        int tunnelY = ghY + ghH / 2;
        for (int x = 0; x < ghX - 1; x++) {
            grid[tunnelY][x] = (x < 3) ? TileType.TUNNEL : TileType.EMPTY;
        }

        // 6. Mirror left half to right half for perfect symmetry
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < halfWidth; x++) {
                int mirrorX = width - 1 - x;
                TileType t = grid[y][x];
                if (t != TileType.GHOST_HOUSE && t != TileType.GHOST_HOUSE_DOOR) {
                    grid[y][mirrorX] = t;
                }
            }
        }

        // Ensure tunnel is mirrored on right
        for (int x = 0; x < 3; x++) {
            grid[tunnelY][width - 1 - x] = TileType.TUNNEL;
        }

        // 7. Cleanup dead ends / ensure solid blocks (clean 2x2 wall clusters)
        cleanIsolatedWalls(grid, width, height);

        // 8. Find valid Spawns
        int pacmanSpawnY = height - 7;
        while (pacmanSpawnY > height / 2 + 3 && grid[pacmanSpawnY][halfWidth - 1] == TileType.WALL) {
            pacmanSpawnY--;
        }
        grid[pacmanSpawnY][halfWidth - 1] = TileType.EMPTY;
        grid[pacmanSpawnY][halfWidth] = TileType.EMPTY;

        // BFS connectivity check from pacman spawn to reach all corridors
        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{halfWidth - 1, pacmanSpawnY});
        visited[pacmanSpawnY][halfWidth - 1] = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0];
            int cy = curr[1];

            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (nx < 0) nx = width - 1;
                if (nx >= width) nx = 0;

                if (ny >= 0 && ny < height) {
                    TileType nt = grid[ny][nx];
                    if (nt != TileType.WALL && nt != TileType.GHOST_HOUSE && nt != TileType.GHOST_HOUSE_DOOR && !visited[ny][nx]) {
                        visited[ny][nx] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        // Turn unreachable corridors into walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x] == TileType.EMPTY && !visited[y][x]) {
                    grid[y][x] = TileType.WALL;
                }
            }
        }

        // 9. Fill valid reachable empty paths with Dots
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == TileType.EMPTY) {
                    // Do not place dots immediately around ghost door or tunnel
                    if (y == tunnelY && (x < 5 || x >= width - 5)) continue;
                    if (y == ghY - 1 && (x == halfWidth - 1 || x == halfWidth)) continue;
                    if (y == pacmanSpawnY && (x == halfWidth - 1 || x == halfWidth)) continue;

                    grid[y][x] = TileType.DOT;
                }
            }
        }

        // 10. Place Power Pellets in 4 corners
        placePowerPelletNear(grid, 1, 3, width, height);
        placePowerPelletNear(grid, width - 2, 3, width, height);
        placePowerPelletNear(grid, 1, height - 6, width, height);
        placePowerPelletNear(grid, width - 2, height - 6, width, height);

        // Build Maze object
        Maze maze = new Maze(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze.setTile(x, y, grid[y][x]);
            }
        }

        // Set spawns
        maze.setPacmanSpawn(halfWidth - 1, pacmanSpawnY);
        maze.setBlinkySpawn(halfWidth - 1, ghY - 1);
        maze.setPinkySpawn(halfWidth - 1, ghY + 2);
        maze.setInkySpawn(halfWidth - 3, ghY + 2);
        maze.setClydeSpawn(halfWidth + 2, ghY + 2);
        maze.setGhostHouseDoor(halfWidth - 1, ghY);
        maze.setFruitSpawn(halfWidth - 1, ghY + ghH + 1);

        maze.recalculateDots();
        return maze;
    }

    private boolean isInsideGhostHouse(int x, int y, int ghX, int ghY, int ghW, int ghH) {
        return x >= ghX - 1 && x <= ghX + ghW && y >= ghY - 1 && y <= ghY + ghH;
    }

    private void placePowerPelletNear(TileType[][] grid, int targetX, int targetY, int width, int height) {
        int bestDistSq = Integer.MAX_VALUE;
        int bestX = -1, bestY = -1;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == TileType.DOT || grid[y][x] == TileType.EMPTY) {
                    int dx = x - targetX;
                    int dy = y - targetY;
                    int distSq = dx * dx + dy * dy;
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }

        if (bestX != -1 && bestY != -1) {
            grid[bestY][bestX] = TileType.POWER_PELLET;
        }
    }

    private void cleanIsolatedWalls(TileType[][] grid, int width, int height) {
        // Outer border must remain solid walls
        for (int x = 0; x < width; x++) {
            grid[0][x] = TileType.WALL;
            grid[height - 1][x] = TileType.WALL;
        }
        for (int y = 0; y < height; y++) {
            if (grid[y][0] != TileType.TUNNEL) grid[y][0] = TileType.WALL;
            if (grid[y][width - 1] != TileType.TUNNEL) grid[y][width - 1] = TileType.WALL;
        }
    }
}
