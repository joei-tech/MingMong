package florante.mingmong;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChooseSidePanel extends JPanel {

    private static final String ENTER_NAME = "Enter Name";
    private static final Color READY_COLOR = new Color(120, 200, 120);

    private static final Color[] PLAYER_COLORS = {
        new Color(135, 190, 235),
        new Color(255, 182, 193)
    };

    private final JTextField[] fields = new JTextField[2];
    private final JButton[] readyButtons = new JButton[2];
    private final JPanel[] playerPanels = new JPanel[2];
    private final boolean[] ready = new boolean[2];

    private final StartMatchListener listener;

    private final JPanel playersPanel =
        new JPanel(new GridLayout(1, 2, 160, 0));

    private BufferedImage backgroundImage;

    public interface StartMatchListener {
        void start(String player1, String player2);
    }

    public ChooseSidePanel(StartMatchListener listener) {
        this.listener = listener;

        setLayout(new BorderLayout());
        playersPanel.setOpaque(false);
        playersPanel.setBorder(
            BorderFactory.createEmptyBorder(0, 0, 35, 0)
        );

        loadBackground();
        createPlayerSections();

        add(playersPanel, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizePlayerComponents();
            }
        });
    }

    private void loadBackground() {
        try {
            URL resource = getClass().getResource("/images/choose_bg.PNG");
            if (resource != null) backgroundImage = ImageIO.read(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        double scale = Math.max(
            (double) w / backgroundImage.getWidth(),
            (double) h / backgroundImage.getHeight()
        );

        int sw = (int) (backgroundImage.getWidth() * scale);
        int sh = (int) (backgroundImage.getHeight() * scale);

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        g2.drawImage(
            backgroundImage,
            (w - sw) / 2, (h - sh) / 2,
            sw, sh, null
        );

        g2.dispose();
    }

    private void createPlayerSections() {
        for (int i = 0; i < 2; i++) {
            final int player = i;

            fields[i] = createNameField();
            readyButtons[i] = createReadyButton(i);
            readyButtons[i].addActionListener(e -> playerReady(player));

            playerPanels[i] = new JPanel(new GridBagLayout());
            playerPanels[i].setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 12, 0);

            gbc.gridy = 0;
            playerPanels[i].add(fields[i], gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            playerPanels[i].add(readyButtons[i], gbc);

            playersPanel.add(playerPanels[i]);
        }
    }

    private JTextField createNameField() {
        JTextField field = new JTextField(ENTER_NAME);

        field.setFont(new Font("Comic Sans MS", Font.PLAIN, 22));
        field.setForeground(GingerKitty_Theme.TEXT);
        field.setBackground(new Color(255, 228, 225));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createLineBorder(
            GingerKitty_Theme.BUTTON_BORDER, 3
        ));

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(ENTER_NAME)) field.setText("");
            }

            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) field.setText(ENTER_NAME);
            }
        });

        return field;
    }

    private JButton createReadyButton(int player) {
        JButton button = new JButton("Ready");

        button.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
        button.setForeground(GingerKitty_Theme.TEXT);
        button.setBackground(PLAYER_COLORS[player]);
        button.setBorder(BorderFactory.createLineBorder(
            GingerKitty_Theme.BUTTON_BORDER, 3
        ));
        button.setOpaque(true);
        button.setFocusPainted(false);

        return button;
    }

    private void resizePlayerComponents() {
        int width = Math.max(200, Math.min(360, getWidth() / 4));
        int height = Math.max(45, Math.min(60, getHeight() / 11));
        int fontSize = Math.max(18, Math.min(26, height / 2));

        Dimension size = new Dimension(width, height);

        for (int i = 0; i < 2; i++) {
            fields[i].setPreferredSize(size);
            readyButtons[i].setPreferredSize(size);

            fields[i].setFont(new Font("Comic Sans MS", Font.PLAIN, fontSize));
            readyButtons[i].setFont(new Font("Comic Sans MS", Font.BOLD, fontSize + 2));
        }

        playersPanel.revalidate();
        revalidate();
        repaint();
    }

    private void playerReady(int player) {
        if (ready[player]) return;

        String username = fields[player].getText().trim();

        if (username.isEmpty() || username.equals(ENTER_NAME)) {
            JOptionPane.showMessageDialog(this, "Please enter a username.");
            return;
        }

        String otherName = fields[1 - player].getText().trim();

        if (ready[1 - player] && username.equalsIgnoreCase(otherName)) {
            JOptionPane.showMessageDialog(
                this, "Both players cannot use the same account."
            );
            return;
        }

        try {
            JDBC.PlayerData data = JDBC.getPlayer(username);

            if (data == null) {
                if (!JDBC.addPlayer(username)) {
                    throw new SQLException("Unable to create player account.");
                }

                JOptionPane.showMessageDialog(
                    this, "Account created successfully!",
                    "Registration", JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                fields[player].setText(data.username);

                JOptionPane.showMessageDialog(
                    this, "Welcome back, " + data.username + "!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE
                );
            }

            ready[player] = true;
            fields[player].setEnabled(false);
            readyButtons[player].setBackground(READY_COLOR);

            if (ready[0] && ready[1] && listener != null) {
                listener.start(
                    fields[0].getText().trim(),
                    fields[1].getText().trim()
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Database error:\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void resetReadyStates() {
        for (int i = 0; i < 2; i++) {
            ready[i] = false;
            fields[i].setText(ENTER_NAME);
            fields[i].setEnabled(true);
            readyButtons[i].setBackground(PLAYER_COLORS[i]);
        }

        repaint();
    }
}
