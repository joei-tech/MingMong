package florante.mingmong;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class WelcomePanel extends JPanel {

    private BufferedImage backgroundImage;
    private JPanel leftPanel;
    private JButton startButton, quitButton;

    public WelcomePanel(Runnable startAction) {

        try {
            backgroundImage = ImageIO.read(
                getClass().getResource("/images/welcome_bg.PNG")
            );
        } catch (Exception e) {
            System.err.println("Could not load welcome_bg.PNG!");
            e.printStackTrace();
        }

        setLayout(null);
        setOpaque(false);

        leftPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        leftPanel.setOpaque(false);

        startButton = createButton("Start", startAction);
        quitButton = createButton("Quit", () -> System.exit(0));

        leftPanel.add(startButton);
        leftPanel.add(quitButton);
        add(leftPanel);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateButtonSize();
            }
        });
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);

        button.setForeground(GingerKitty_Theme.TEXT);
        button.setBackground(GingerKitty_Theme.BUTTON_BACKGROUND);
        button.setBorder(BorderFactory.createLineBorder(
            GingerKitty_Theme.BUTTON_BORDER, 4
        ));

        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());

        return button;
    }

    private void updateButtonSize() {
        int w = getWidth();
        int h = getHeight();

        int buttonWidth = Math.max(220, Math.min((int)(w * 0.25), 480));
        int buttonHeight = Math.max(65, Math.min((int)(h * 0.11), 120));
        int fontSize = Math.max(24, Math.min(buttonHeight / 2, 48));

        Font font = new Font("Comic Sans MS", Font.PLAIN, fontSize);
        startButton.setFont(font);
        quitButton.setFont(font);

        int totalHeight = (buttonHeight * 2) + 15;
        int x = (int)(w * 0.066);
        int y = (h - totalHeight) / 2 + 20;

        leftPanel.setBounds(x, y, buttonWidth, totalHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2.drawImage(
                backgroundImage, 0, 0,
                getWidth(), getHeight(), this
            );

            g2.dispose();
        }
    }
}