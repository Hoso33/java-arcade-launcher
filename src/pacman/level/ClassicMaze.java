package pacman.level;

import pacman.model.Maze;
import pacman.model.TileType;

/**
 * Standard authentic arcade 28x31 Pac-Man maze layout.
 */
public class ClassicMaze {

    // 28 columns x 31 rows
    // Legend:
    // W = Wall
    // . = Dot
    // o = Power Pellet
    // ' ' = Empty path (no dot)
    // - = Ghost house door
    // H = Ghost house inside
    // T = Tunnel
    private static final String[] CLASSIC_MAP = {
        "WWWWWWWWWWWWWWWWWWWWWWWWWWWW",
        "W............WW............W",
        "W.WWWW.WWWWW.WW.WWWWW.WWWW.W",
        "WoWWWW.WWWWW.WW.WWWWW.WWWWoW",
        "W.WWWW.WWWWW.WW.WWWWW.WWWW.W",
        "W..........................W",
        "W.WWWW.WW.WWWWWWWW.WW.WWWW.W",
        "W.WWWW.WW.WWWWWWWW.WW.WWWW.W",
        "W......WW....WW....WW......W",
        "WWWWWW.WWWWW WW WWWWW.WWWWWW",
        "     W.WWWWW WW WWWWW.W     ",
        "     W.WW          WW.W     ",
        "     W.WW WWW--WWW WW.W     ",
        "WWWWWW.WW W HHHH W WW.WWWWWW",
        "TTTTTT.   W HHHH W   .TTTTTT",
        "WWWWWW.WW W HHHH W WW.WWWWWW",
        "     W.WW WWWWWWWW WW.W     ",
        "     W.WW          WW.W     ",
        "     W.WW WWWWWWWW WW.W     ",
        "WWWWWW.WW WWWWWWWW WW.WWWWWW",
        "W............WW............W",
        "W.WWWW.WWWWW.WW.WWWWW.WWWW.W",
        "W.WWWW.WWWWW.WW.WWWWW.WWWW.W",
        "Wo..WW................WW..oW",
        "WWW.WW.WW.WWWWWWWW.WW.WW.WWW",
        "WWW.WW.WW.WWWWWWWW.WW.WW.WWW",
        "W......WW....WW....WW......W",
        "W.WWWWWWWWWW.WW.WWWWWWWWWW.W",
        "W.WWWWWWWWWW.WW.WWWWWWWWWW.W",
        "W..........................W",
        "WWWWWWWWWWWWWWWWWWWWWWWWWWWW"
    };

    public static Maze create() {
        int height = CLASSIC_MAP.length;
        int width = CLASSIC_MAP[0].length();
        Maze maze = new Maze(width, height);

        for (int y = 0; y < height; y++) {
            String row = CLASSIC_MAP[y];
            for (int x = 0; x < width; x++) {
                char c = (x < row.length()) ? row.charAt(x) : ' ';
                switch (c) {
                    case 'W' -> maze.setTile(x, y, TileType.WALL);
                    case '.' -> maze.setTile(x, y, TileType.DOT);
                    case 'o' -> maze.setTile(x, y, TileType.POWER_PELLET);
                    case '-' -> maze.setTile(x, y, TileType.GHOST_HOUSE_DOOR);
                    case 'H' -> maze.setTile(x, y, TileType.GHOST_HOUSE);
                    case 'T' -> maze.setTile(x, y, TileType.TUNNEL);
                    default -> maze.setTile(x, y, TileType.EMPTY);
                }
            }
        }

        // Standard Arcade Spawns
        maze.setPacmanSpawn(13, 23);
        maze.setBlinkySpawn(13, 11);
        maze.setPinkySpawn(13, 14);
        maze.setInkySpawn(11, 14);
        maze.setClydeSpawn(15, 14);
        maze.setGhostHouseDoor(13, 12);
        maze.setFruitSpawn(13, 17);

        maze.recalculateDots();
        return maze;
    }
}
