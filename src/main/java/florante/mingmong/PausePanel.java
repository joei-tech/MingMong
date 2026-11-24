package florante.mingmong;

import java.awt.*;
import javax.swing.*;

public class PausePanel extends JPanel {

    public interface Listener {
        void continueGame();
        void restartGame();
        void showSettings();
        void quitGame();
    }

    public PausePanel(Listener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(GingerKitty_Theme.WELCOME_BACKGROUND);

        add(Box.createVerticalGlue());
        addTitle("Ming Mong");
        addGap(25);

        addButton("Continue", listener::continueGame);
        addGap(12);
        addButton("Restart", listener::restartGame);
        addGap(12);
        addButton("Settings", listener::showSettings);
        addGap(12);
        addButton("Back to Home", listener::quitGame);

        add(Box.createVerticalGlue());
    }

    private void addTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Comic Sans MS", Font.PLAIN, 64));
        title.setForeground(GingerKitty_Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);
    }

    private void addButton(String text, Runnable action) {
        JButton button = new JButton(text);
        Dimension size = new Dimension(360, 65);

        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Comic Sans MS", Font.PLAIN, 30));
        button.setForeground(GingerKitty_Theme.TEXT);
        button.setBackground(GingerKitty_Theme.BUTTON_BACKGROUND);
        button.setBorder(BorderFactory.createLineBorder(GingerKitty_Theme.BUTTON_BORDER, 4));
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());

        add(button);
    }

    private void addGap(int height) {
        add(Box.createRigidArea(new Dimension(0, height)));
    }
}