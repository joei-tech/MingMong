package florante.mingmong;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameFrame extends JFrame {

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 550;

    private static final String WELCOME = "WELCOME";
    private static final String CHOOSE_SIDE = "CHOOSE_SIDE";
    private static final String GAME = "GAME";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final JPanel topBar = new JPanel(new BorderLayout());

    private final GamePanel gamePanel;
    private final ChooseSidePanel chooseSidePanel;
    private final WelcomePanel welcomePanel;

    private boolean isFullscreen;
    private boolean isTransitioning;

    public GameFrame() {
        gamePanel = new GamePanel(() -> showScreen(WELCOME));

        chooseSidePanel = new ChooseSidePanel((p1, p2) -> {
            gamePanel.startMatch(p1, p2);
            showScreen(GAME);
        });

        welcomePanel = new WelcomePanel(() -> {
            chooseSidePanel.resetReadyStates();
            showScreen(CHOOSE_SIDE);
        });

        mainPanel.add(welcomePanel, WELCOME);
        mainPanel.add(chooseSidePanel, CHOOSE_SIDE);
        mainPanel.add(gamePanel, GAME);

        setTitle("Ming Mong");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(mainPanel);

        setupWindowsStyleHoverBar();

        addWindowStateListener(e -> {
            if (!isTransitioning
                    && !isFullscreen
                    && (e.getNewState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
                SwingUtilities.invokeLater(this::maximizeWindow);
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED
                            && e.getKeyCode() == KeyEvent.VK_ESCAPE
                            && isFullscreen) {
                        setWindowed();
                        return true;
                    }
                    return false;
                });

        setWindowed();
        showScreen(WELCOME);
    }

    private void setupWindowsStyleHoverBar() {
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, new Color(220, 220, 220)));
        topBar.setVisible(false);

        JLabel title = new JLabel("  Ming Mong");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topBar.add(title, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setOpaque(false);

        buttons.add(makeWinBtn("—", new Color(230, 230, 230),
                () -> {
                    topBar.setVisible(false);
                    setState(ICONIFIED);
                }));

        buttons.add(makeWinBtn("❐", new Color(230, 230, 230),
                this::setWindowed));

        buttons.add(makeWinBtn("✕", new Color(232, 17, 35),
                () -> System.exit(0)));

        topBar.add(buttons, BorderLayout.EAST);
        getLayeredPane().add(topBar, JLayeredPane.POPUP_LAYER);

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!isFullscreen || !(event instanceof MouseEvent me)) return;

            if (me.getID() != MouseEvent.MOUSE_MOVED
                    && me.getID() != MouseEvent.MOUSE_DRAGGED) return;

            Point p = SwingUtilities.convertPoint(
                    me.getComponent(), me.getPoint(), getLayeredPane());

            if (p.y <= 5 && !topBar.isVisible()) {
                topBar.setBounds(0, 0, getWidth(), 32);
                topBar.setVisible(true);
            } else if (p.y > 36) {
                topBar.setVisible(false);
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    private JButton makeWinBtn(String text, Color hover, Runnable action) {
        JButton button = new JButton(text);

        button.setPreferredSize(new Dimension(46, 32));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(e -> action.run());

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
                if (text.equals("✕")) button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }

    public void maximizeWindow() {
        if (isFullscreen || isTransitioning) return;

        isTransitioning = true;

        if (isDisplayable()) dispose();

        setPanelsSize(null);
        mainPanel.setPreferredSize(null);

        setUndecorated(true);
        setResizable(false);
        setAlwaysOnTop(true);
        setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);

        isFullscreen = true;
        isTransitioning = false;

        requestGameFocus();
    }

    public void setWindowed() {
        if (isTransitioning) return;

        isTransitioning = true;
        topBar.setVisible(false);

        if (isDisplayable()) dispose();

        setAlwaysOnTop(false);
        setUndecorated(false);
        setResizable(true);
        setExtendedState(NORMAL);

        Dimension size = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);

        setPanelsSize(size);
        mainPanel.setPreferredSize(size);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        isFullscreen = false;
        isTransitioning = false;

        requestGameFocus();
    }

    private void setPanelsSize(Dimension size) {
        welcomePanel.setPreferredSize(size);
        chooseSidePanel.setPreferredSize(size);
        gamePanel.setPreferredSize(size);
    }

    private void showScreen(String screen) {
        cardLayout.show(mainPanel, screen);

        if (screen.equals(GAME)) {
            requestGameFocus();
        }
    }

    private void requestGameFocus() {
        if (gamePanel.isVisible()) {
            SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
        }
    }
}
