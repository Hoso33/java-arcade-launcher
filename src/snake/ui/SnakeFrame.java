package snake.ui;

import snake.audio.NokiaBeeper;
import snake.engine.SnakeEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Main Window Frame for Nokia 3310 Snake.
 */
public class SnakeFrame extends JFrame {

    private final NokiaScreenPanel panel;
    private final NokiaBeeper beeper;

    public SnakeFrame(SnakeEngine engine, NokiaBeeper beeper) {
        this.beeper = beeper;

        setTitle("Nokia 3310 Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        setIconImage(createSnakeAppIcon());

        this.panel = new NokiaScreenPanel(engine, beeper);
        this.panel.setPreferredSize(new Dimension(540, 420));
        this.setContentPane(panel);

        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                panel.requestFocusInWindow();
                panel.startLoop();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                panel.stopLoop();
                if (beeper != null) {
                    beeper.shutdown();
                }
            }
        });
    }

    private Image createSnakeAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(NokiaScreenPanel.LCD_BG);
        g2.fillRect(0, 0, 64, 64);
        g2.setColor(NokiaScreenPanel.LCD_PIXEL);
        g2.fillRect(10, 10, 44, 44);
        g2.setColor(NokiaScreenPanel.LCD_BG);
        g2.fillRect(14, 14, 36, 36);
        g2.setColor(NokiaScreenPanel.LCD_PIXEL);
        g2.fillRect(20, 20, 10, 10);
        g2.fillRect(30, 20, 10, 10);
        g2.fillRect(30, 30, 10, 10);
        g2.dispose();
        return img;
    }
}
