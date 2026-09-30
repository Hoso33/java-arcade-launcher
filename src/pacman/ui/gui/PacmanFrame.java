package pacman.ui.gui;

import pacman.audio.SoundSynthesizer;
import pacman.engine.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Main Swing Window Frame for Pac-Man GUI mode.
 */
public class PacmanFrame extends JFrame {

    private final PacmanPanel panel;
    private final SoundSynthesizer soundSynthesizer;

    public PacmanFrame(GameEngine engine, SoundSynthesizer soundSynthesizer) {
        this.soundSynthesizer = soundSynthesizer;

        setTitle("Pac-Man (Pure Java SE)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // Procedural App Icon (Pac-Man)
        setIconImage(createAppIcon());

        this.panel = new PacmanPanel(engine, soundSynthesizer);
        this.panel.setPreferredSize(new Dimension(560, 700));
        this.setContentPane(panel);

        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel.requestFocusInWindow();
                panel.startGameLoop();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                panel.stopGameLoop();
                if (soundSynthesizer != null) {
                    soundSynthesizer.shutdown();
                }
            }
        });
    }

    private Image createAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.YELLOW);
        g2.fillArc(4, 4, 56, 56, 35, 290);
        g2.dispose();
        return img;
    }
}
