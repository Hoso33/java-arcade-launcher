package common;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;
import pacman.ui.cli.ConsoleGame;
import pacman.ui.gui.PacmanFrame;
import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;
import snake.ui.SnakeFrame;
import snake.ui.cli.ConsoleSnakeGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Modernized Retro Launcher Hub with dark contrast cards, prominent CLI/GUI choice, and custom buttons.
 */
public class GuiLauncherFrame extends JFrame {

    private UnifiedLauncher.GameChoice selectedGame = UnifiedLauncher.GameChoice.PACMAN;
    private UnifiedLauncher.InterfaceChoice selectedInterface = UnifiedLauncher.InterfaceChoice.GUI;
    private boolean proceduralMazes = false;

    private GameCardPanel pacmanCard;
    private GameCardPanel snakeCard;
    private JRadioButton guiRadio;
    private JRadioButton cliRadio;
    private JCheckBox proceduralCheck;

    public GuiLauncherFrame() {
        setTitle("Retro Arcade Game Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(createHubIcon());

        // Deep dark background palette
        Color bgDark = new Color(12, 14, 22);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 16));
        rootPanel.setBackground(bgDark);
        rootPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Section
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(bgDark);

        JLabel titleLabel = new JLabel("RETRO ARCADE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0)); // Arcade Gold

        JLabel subtitleLabel = new JLabel("Select Game & Interface Mode", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(160, 175, 200));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Section: Vertical Stack of Dark Game Cards & Interface Selector
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(bgDark);

        // Vertical Dark Game Selection Cards
        JPanel cardsStack = new JPanel(new GridLayout(2, 1, 0, 12));
        cardsStack.setBackground(bgDark);
        cardsStack.setMaximumSize(new Dimension(480, 200));

        pacmanCard = new GameCardPanel(
                "PAC-MAN",
                "Classic arcade maze, 4 ghost AIs, power pellets & procedural levels.",
                new Color(255, 204, 0),    // Bright Pac-Man Yellow
                UnifiedLauncher.GameChoice.PACMAN
        );

        snakeCard = new GameCardPanel(
                "SNAKE 3310",
                "Authentic monochrome Nokia LCD display with 8-bit piezo sound.",
                new Color(155, 188, 15),   // Authentic Nokia LCD Green
                UnifiedLauncher.GameChoice.SNAKE
        );

        cardsStack.add(pacmanCard);
        cardsStack.add(snakeCard);
        centerPanel.add(cardsStack);

        centerPanel.add(Box.createVerticalStrut(16));

        // Interface Choice Box (GUI vs CLI)
        JPanel modePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        modePanel.setBackground(new Color(22, 27, 40));
        modePanel.setMaximumSize(new Dimension(480, 72));
        modePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 72, 100), 1),
                new EmptyBorder(2, 10, 2, 10)
        ));

        guiRadio = createStyledRadioButton("GUI Mode (Swing Window)", true);
        guiRadio.addActionListener(e -> selectedInterface = UnifiedLauncher.InterfaceChoice.GUI);

        cliRadio = createStyledRadioButton("Terminal Console (CLI Mode)", false);
        cliRadio.addActionListener(e -> selectedInterface = UnifiedLauncher.InterfaceChoice.CLI);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(guiRadio);
        modeGroup.add(cliRadio);

        modePanel.add(guiRadio);
        modePanel.add(cliRadio);
        centerPanel.add(modePanel);

        centerPanel.add(Box.createVerticalStrut(10));

        // Procedural Mazes Option
        proceduralCheck = new JCheckBox("Procedural Mazes for all Pac-Man levels");
        proceduralCheck.setFont(new Font("Monospaced", Font.PLAIN, 12));
        proceduralCheck.setForeground(new Color(180, 195, 220));
        proceduralCheck.setOpaque(false);
        proceduralCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        proceduralCheck.addActionListener(e -> proceduralMazes = proceduralCheck.isSelected());
        centerPanel.add(proceduralCheck);

        rootPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Buttons (START & EXIT)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottomPanel.setBackground(bgDark);

        JButton startButton = createStyledButton(
                "START GAME",
                new Color(0, 160, 75),       // Dark green
                new Color(0, 200, 95),       // Hover bright green
                new Color(0, 130, 60),       // Pressed green
                Color.WHITE,
                new Dimension(210, 44)
        );
        startButton.addActionListener(e -> launchSelectedGame());

        JButton exitButton = createStyledButton(
                "EXIT",
                new Color(180, 40, 40),      // Dark red
                new Color(220, 50, 50),      // Hover bright red
                new Color(140, 30, 30),      // Pressed red
                Color.WHITE,
                new Dimension(130, 44)
        );
        exitButton.addActionListener(e -> System.exit(0));

        bottomPanel.add(startButton);
        bottomPanel.add(exitButton);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);
        updateCardHighlights();
    }

    private JRadioButton createStyledRadioButton(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setFont(new Font("Monospaced", Font.BOLD, 13));
        rb.setForeground(Color.WHITE);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private JButton createStyledButton(String text, Color bg, Color hoverBg, Color pressedBg, Color fg, Dimension prefSize) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ButtonModel model = getModel();
                Color currentBg = bg;
                if (model.isPressed()) {
                    currentBg = pressedBg;
                } else if (model.isRollover()) {
                    currentBg = hoverBg;
                }

                // Rounded colored background
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Subtle border outline
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Text
                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);

                g2.dispose();
            }
        };

        btn.setFont(new Font("Monospaced", Font.BOLD, 15));
        btn.setPreferredSize(prefSize);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * High-contrast Dark Game Card component with prominent title typography and selection outline.
     */
    private class GameCardPanel extends JPanel {
        private final String title;
        private final String description;
        private final Color accentColor;
        private final UnifiedLauncher.GameChoice gameChoice;
        private boolean isHovered = false;

        public GameCardPanel(String title, String description, Color accentColor, UnifiedLauncher.GameChoice gameChoice) {
            this.title = title;
            this.description = description;
            this.accentColor = accentColor;
            this.gameChoice = gameChoice;

            setLayout(new BorderLayout(14, 0));
            setBorder(new EmptyBorder(12, 18, 12, 18));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Inner Info Panel
            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
            infoPanel.setOpaque(false);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(new Font("Monospaced", Font.BOLD, 18));
            titleLbl.setForeground(accentColor); // Bright contrast title color

            JLabel descLbl = new JLabel(description);
            descLbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
            descLbl.setForeground(new Color(200, 215, 235)); // Clean legible text

            infoPanel.add(titleLbl);
            infoPanel.add(descLbl);
            add(infoPanel, BorderLayout.CENTER);

            // Click & Hover Listeners
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedGame = gameChoice;
                    updateCardHighlights();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean isSelected = (selectedGame == gameChoice);

            // Dark Blue/Gray Card Backgrounds (#1E2430 & #252D3C)
            Color cardBg = isSelected ? new Color(37, 45, 62) : (isHovered ? new Color(30, 36, 50) : new Color(24, 29, 40));
            g2.setColor(cardBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            // Left Pill Accent Bar
            g2.setColor(isSelected ? accentColor : new Color(70, 82, 110));
            g2.fillRoundRect(6, 10, 6, getHeight() - 20, 4, 4);

            // Selection Outline Highlight
            if (isSelected) {
                g2.setColor(new Color(255, 215, 0)); // Gold outline for selected card
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            } else {
                g2.setColor(new Color(55, 66, 90));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private void updateCardHighlights() {
        if (selectedGame == UnifiedLauncher.GameChoice.PACMAN) {
            proceduralCheck.setEnabled(true);
        } else {
            proceduralCheck.setEnabled(false);
        }
        pacmanCard.repaint();
        snakeCard.repaint();
    }

    private void launchSelectedGame() {
        dispose(); // Close Launcher window

        if (selectedGame == UnifiedLauncher.GameChoice.PACMAN) {
            GameEngine engine = new GameEngine(proceduralMazes);
            SoundSynthesizer soundSynthesizer = new SoundSynthesizer();
            engine.addEventListener(soundSynthesizer);

            if (selectedInterface == UnifiedLauncher.InterfaceChoice.CLI) {
                ConsoleGame consoleGame = new ConsoleGame(engine, soundSynthesizer);
                Thread cliThread = new Thread(consoleGame::start, "PacManConsoleGame");
                cliThread.start();
            } else {
                SwingUtilities.invokeLater(() -> {
                    PacmanFrame frame = new PacmanFrame(engine, soundSynthesizer);
                    frame.setVisible(true);
                });
            }
        } else {
            NokiaBeeper beeper = new NokiaBeeper();
            SnakeEngine engine = new SnakeEngine(24, 16, beeper);

            if (selectedInterface == UnifiedLauncher.InterfaceChoice.CLI) {
                ConsoleSnakeGame consoleSnake = new ConsoleSnakeGame(engine, beeper);
                Thread cliThread = new Thread(consoleSnake::start, "SnakeConsoleGame");
                cliThread.start();
            } else {
                SwingUtilities.invokeLater(() -> {
                    SnakeFrame frame = new SnakeFrame(engine, beeper);
                    frame.setVisible(true);
                });
            }
        }
    }

    private Image createHubIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(12, 14, 22));
        g2.fillRoundRect(0, 0, 64, 64, 16, 16);
        g2.setColor(Color.YELLOW);
        g2.fillArc(6, 16, 26, 26, 35, 290);
        g2.setColor(new Color(155, 188, 15));
        g2.fillRect(36, 16, 22, 26);
        g2.setColor(new Color(15, 56, 15));
        g2.fillRect(38, 18, 8, 8);
        g2.fillRect(46, 18, 8, 8);
        g2.fillRect(46, 26, 8, 8);
        g2.dispose();
        return img;
    }
}
