package florante.mingmong;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class SettingsPanel extends JPanel {

    public interface Listener {
        void usernamesChanged(String player1Name, String player2Name);
        void accountDeleted();
        void backToPauseMenu();
        void fullscreen();
    }

    private final Listener listener;

    private int player1Id, player2Id;

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    private int player1Wins, player1Losses, player1HighestScore;
    private int player2Wins, player2Losses, player2HighestScore;

    private final JLabel title;
    private final JLabel player1NameLabel, player2NameLabel;
    private final JLabel player1Stats, player2Stats;

    private final JButton changeButton, deleteButton, backButton;

    public SettingsPanel(Listener listener) {
        this.listener = listener;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(GingerKitty_Theme.WELCOME_BACKGROUND);

        title = new JLabel("Settings");
        title.setFont(new Font("Comic Sans MS", Font.PLAIN, 64));
        title.setForeground(GingerKitty_Theme.TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(title);
        addGap(22);

        JPanel names = new JPanel(new GridLayout(1, 2, 20, 0));
        names.setOpaque(false);
        names.setMaximumSize(new Dimension(760, 55));

        player1NameLabel = createInfoLabel(player1Name);
        player2NameLabel = createInfoLabel(player2Name);

        names.add(player1NameLabel);
        names.add(player2NameLabel);

        add(names);
        addGap(12);

        JPanel stats = new JPanel(new GridLayout(1, 2, 20, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(760, 150));

        player1Stats = createInfoLabel("");
        player2Stats = createInfoLabel("");

        stats.add(player1Stats);
        stats.add(player2Stats);

        add(stats);
        addGap(22);

        changeButton = createMenuButton("Change Username");
        deleteButton = createMenuButton("Delete Username");
        backButton = createMenuButton("Back");

        changeButton.addActionListener(e -> changeUsernames());
        deleteButton.addActionListener(e -> deleteUsernames());
        backButton.addActionListener(e -> listener.backToPauseMenu());

        add(changeButton);
        addGap(12);
        add(deleteButton);
        addGap(12);
        add(backButton);

        add(Box.createVerticalGlue());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adjustComponentSizes();
            }
        });
    }

    private void adjustComponentSizes() {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        int baseSize = Math.min(width / 30, height / 18);

        int titleSize = Math.max(28, Math.min(64, baseSize * 2));
        int infoSize = Math.max(14, Math.min(24, baseSize));
        int buttonSize = Math.max(16, Math.min(30, baseSize + 4));

        title.setFont(new Font("Comic Sans MS", Font.PLAIN, titleSize));

        JLabel[] labels = {
            player1NameLabel, player2NameLabel,
            player1Stats, player2Stats
        };

        for (JLabel label : labels) {
            label.setFont(new Font("Comic Sans MS", Font.PLAIN, infoSize));
        }

        player1NameLabel.getParent().setMaximumSize(
                new Dimension((int) (width * 0.9), Math.max(40, height / 10))
        );

        player1Stats.getParent().setMaximumSize(
                new Dimension((int) (width * 0.9), Math.max(70, height / 4))
        );

        Dimension buttonDimension = new Dimension(
                (int) (width * 0.7),
                Math.max(40, height / 12)
        );

        for (JButton button : new JButton[]{
            changeButton, deleteButton, backButton
        }) {
            button.setFont(new Font("Comic Sans MS", Font.PLAIN, buttonSize));
            button.setPreferredSize(buttonDimension);
            button.setMinimumSize(buttonDimension);
            button.setMaximumSize(buttonDimension);
        }

        revalidate();
        repaint();
    }

    public void setPlayerData(
            int player1Id, String player1Name,
            int player1Wins, int player1Losses, int player1HighestScore,
            int player2Id, String player2Name,
            int player2Wins, int player2Losses, int player2HighestScore) {

        this.player1Id = player1Id;
        this.player1Name = player1Name;
        this.player1Wins = player1Wins;
        this.player1Losses = player1Losses;
        this.player1HighestScore = player1HighestScore;

        this.player2Id = player2Id;
        this.player2Name = player2Name;
        this.player2Wins = player2Wins;
        this.player2Losses = player2Losses;
        this.player2HighestScore = player2HighestScore;

        refresh();
    }

    public void updateStatistics(
            int player1Wins, int player1Losses, int player1HighestScore,
            int player2Wins, int player2Losses, int player2HighestScore) {

        this.player1Wins = player1Wins;
        this.player1Losses = player1Losses;
        this.player1HighestScore = player1HighestScore;

        this.player2Wins = player2Wins;
        this.player2Losses = player2Losses;
        this.player2HighestScore = player2HighestScore;

        refresh();
    }

    private void refresh() {
        player1NameLabel.setText(player1Name);
        player2NameLabel.setText(player2Name);

        player1Stats.setText(statsText(
                player1Name, player1Wins, player1Losses, player1HighestScore
        ));

        player2Stats.setText(statsText(
                player2Name, player2Wins, player2Losses, player2HighestScore
        ));
    }

    private String statsText(String name, int wins, int losses, int highestScore) {
        return "<html><center>" + name
                + "<br>Wins: " + wins
                + "<br>Losses: " + losses
                + "<br>Highest Score: " + highestScore
                + "</center></html>";
    }

    private void changeUsernames() {
        String[] players = {
            "Player 1 - " + player1Name,
            "Player 2 - " + player2Name
        };

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Which player's username do you want to change?",
                "Change Username",
                JOptionPane.QUESTION_MESSAGE,
                null, players, players[0]
        );

        if (selected == null) return;

        boolean player1 = selected.startsWith("Player 1");
        String oldName = player1 ? player1Name : player2Name;

        String newName = JOptionPane.showInputDialog(
                this, "Enter the new username:", oldName
        );

        if (newName == null) return;

        newName = newName.trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        if (newName.equalsIgnoreCase(player1Name)
                || newName.equalsIgnoreCase(player2Name)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This username is already being used by a player.",
                    "Duplicate Username",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            if (JDBC.getPlayer(newName) != null) {
                JOptionPane.showMessageDialog(
                        this, "That username already exists!",
                        "Username Taken", JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JDBC.updatePlayer(player1 ? player1Id : player2Id, newName);

            if (player1) player1Name = newName;
            else player2Name = newName;

            refresh();
            listener.usernamesChanged(player1Name, player2Name);

            JOptionPane.showMessageDialog(
                    this, "Username successfully changed!"
            );

        } catch (SQLException e) {
            showDatabaseError("Unable to change username:\n" + e.getMessage());
        }
    }

    private void deleteUsernames() {
        String[] players = {
            "Player 1 - " + player1Name,
            "Player 2 - " + player2Name
        };

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Which account do you want to delete?",
                "Delete Username",
                JOptionPane.WARNING_MESSAGE,
                null, players, players[0]
        );

        if (selected == null) return;

        boolean player1 = selected.startsWith("Player 1");
        String username = player1 ? player1Name : player2Name;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete "
                        + username
                        + "?\n\nThis will also delete their match "
                        + "history and associated rounds.",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            JDBC.deletePlayer(player1 ? player1Id : player2Id);

            JOptionPane.showMessageDialog(
                    this, username + "'s account has been deleted."
            );

            listener.accountDeleted();

        } catch (SQLException e) {
            showDatabaseError("Unable to delete account:\n" + e.getMessage());
        }
    }

    private void showDatabaseError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Database Error", JOptionPane.ERROR_MESSAGE
        );
    }

    private void addGap(int height) {
        add(Box.createRigidArea(new Dimension(0, height)));
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);

        label.setFont(new Font("Comic Sans MS", Font.PLAIN, 24));
        label.setForeground(GingerKitty_Theme.TEXT);
        label.setOpaque(true);
        label.setBackground(GingerKitty_Theme.BUTTON_BACKGROUND);

        label.setBorder(BorderFactory.createLineBorder(
                GingerKitty_Theme.BUTTON_BORDER, 3
        ));

        return label;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);

        Dimension size = new Dimension(360, 65);

        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.setFont(new Font("Comic Sans MS", Font.PLAIN, 30));
        button.setForeground(GingerKitty_Theme.TEXT);
        button.setBackground(GingerKitty_Theme.BUTTON_BACKGROUND);

        button.setBorder(BorderFactory.createLineBorder(
                GingerKitty_Theme.BUTTON_BORDER, 3
        ));

        button.setFocusPainted(false);

        return button;
    }
}