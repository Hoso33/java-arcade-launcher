# Retro Arcade Games in Pure Java SE

A collection of classic arcade games built **100% with standard Java SE libraries** (no external Maven/Gradle dependencies or external media files).

---

## 🐍 1. Nokia 3310 Snake

An authentic recreation of the legendary **Snake** from Nokia 3310:

- **Monochrome LCD Aesthetics**: Greenish LCD background (`#9BBC0F`), dark-green pixel elements (`#0F380F`), battery and signal indicators, retro Nokia font, and authentic pixel grid.
- **Classic Snake Mechanics**:
  - Grid-based movement with instantaneous direction buffering (WASD or Arrow keys).
  - 180° reverse-turn collision prevention.
  - Pixel apple generation in random free cells.
  - Wall collision & self-tail collision detection.
  - Dynamic speed scaling (increases as more food is eaten).
  - Score & High Score tracking.
- **Procedural Nokia Piezo Audio**: Authentic 8-bit monophonic square-wave piezo beeper for eating, crash, startup tone, and new high score fanfare (mute toggle with `M`).
- **Dual Interfaces**: Swing GUI and ANSI terminal console modes.

### Controls (Snake)
| Action | Key |
| :--- | :--- |
| **Move Up / Down / Left / Right** | `W`/`S`/`A`/`D` or Arrow Keys (`↑`,`↓`,`←`,`→`) |
| **Start / Pause / Restart** | `SPACE` or `ENTER` or `P` |
| **Mute / Unmute Audio** | `M` |
| **Quit** | `ESC` |

---

## 🕹️ 2. Pac-Man (Pure Java SE)

A complete, faithful, and procedurally rendered implementation of Pac-Man:

- **Dual Interfaces**: Java Swing GUI (60 FPS) and Terminal ANSI CLI.
- **Procedural Graphics & Audio**: 100% Java 2D vector drawing & 8-bit PCM audio synthesizer.
- **Level Generation**: Classic Arcade Map (Level 1) + increasingly intricate unique procedural mazes on each later level.
- **Difficulty Progression**: More maze corridors and faster ghosts as levels advance; a "LEVEL COMPLETE!" celebration appears before the next maze loads.
- **Arcade Ghost AI**: Blinky (Chaser), Pinky (Ambusher), Inky (Flanker), Clyde (Coward) with timed Scatter/Chase waves.

---

## 🚀 How to Compile & Run

### Compilation
```bash
mkdir -p bin
javac -d bin $(find src -name '*.java')
```

### Launch Nokia 3310 Snake Directly
```bash
java -cp bin snake.SnakeMain --gui
java -cp bin snake.SnakeMain --cli
```

In Snake CLI mode, use `W/A/S/D` to move, `P` to pause, `M` to mute, and `Q` to quit.

### Launch Pac-Man Directly
```bash
# Graphical User Interface (GUI)
java -cp bin pacman.Main --gui

# Terminal Console (CLI)
java -cp bin pacman.Main --cli
```

### Interactive Menu Launcher
```bash
java -cp bin Main
```

---

## 🧪 Automated Test Suites
```bash
# Run Snake Verification Tests (7 test suites)
java -ea -cp bin snake.SnakeTestRunner

# Run Pac-Man Verification Tests (9 test suites)
java -ea -cp bin pacman.TestRunner
```
