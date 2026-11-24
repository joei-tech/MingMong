package florante.mingmong;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    static final int GAME_WIDTH = 1000;
    static final int GAME_HEIGHT = 555;

    private final Runnable backToWelcome;
    private final GameRenderer renderer = new GameRenderer();
    private final Rectangle pauseButton = new Rectangle();

    private final GameEngine engine;
    private final GameInput input;
    private final Thread gameThread;

    private PausePanel pausePanel;
    private SettingsPanel settingsPanel;

    public GamePanel(Runnable backToWelcome) {
        this.backToWelcome = backToWelcome;

        setLayout(null);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);

        engine = new GameEngine(
                GAME_WIDTH, GAME_HEIGHT, new EngineEvents()
        );

        input = new GameInput(
                engine, pauseButton, new InputEvents()
        );

        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);

        createMenus();

        gameThread = new Thread(this);
        gameThread.start();
    }

    public void startMatch(String name1, String name2) {
        try {
            JDBC.PlayerData player1 = JDBC.getPlayer(name1);
            JDBC.PlayerData player2 = JDBC.getPlayer(name2);

            if (player1 == null || player2 == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "One or both player accounts could not be found."
                );
                backToWelcome.run();
                return;
            }

            int matchId = JDBC.createMatch(
                    player1.playerId, player2.playerId
            );

            engine.setMatchData(matchId, player1, player2);
            updateSettingsPanel();

            engine.setPaused(false);
            setMenusVisible(false);

            requestFocusInWindow();
            repaint();

        } catch (SQLException e) {
            showDatabaseError("Unable to start match:\n" + e.getMessage());
            backToWelcome.run();
        }
    }

    private void createMenus() {

        pausePanel = new PausePanel(new PausePanel.Listener() {

            @Override
            public void continueGame() {
                resumeGame();
            }

            @Override
            public void restartGame() {
                restartMatch();
            }

            @Override
            public void showSettings() {
                showSettingsPanel();
            }

            @Override
            public void quitGame() {
                quitToWelcome();
            }
        });

        settingsPanel = new SettingsPanel(new SettingsPanel.Listener() {

            @Override
            public void usernamesChanged(String name1, String name2) {
                engine.setPlayerNames(name1, name2);
                repaint();
            }

            @Override
            public void accountDeleted() {
                engine.setPaused(true);
                setMenusVisible(false);
                backToWelcome.run();
            }

            @Override
            public void backToPauseMenu() {
                hideSettingsPanel();
            }

            @Override
            public void fullscreen() {
                Window window = SwingUtilities.getWindowAncestor(GamePanel.this);

                if (window instanceof GameFrame) {
                    ((GameFrame) window).maximizeWindow();
                }
            }
        });

        pausePanel.setVisible(false);
        settingsPanel.setVisible(false);

        add(pausePanel);
        add(settingsPanel);
    }

    private void setMenusVisible(boolean visible) {
        pausePanel.setVisible(visible);
        settingsPanel.setVisible(false);
    }

    private void updateSettingsPanel() {
        settingsPanel.setPlayerData(
                engine.getPlayer1Id(),
                engine.getPlayer1Name(),
                engine.getPlayer1Wins(),
                engine.getPlayer1Losses(),
                engine.getPlayer1HighestScore(),
                engine.getPlayer2Id(),
                engine.getPlayer2Name(),
                engine.getPlayer2Wins(),
                engine.getPlayer2Losses(),
                engine.getPlayer2HighestScore()
        );
    }

    private void updateSettingsStatistics() {
        settingsPanel.updateStatistics(
                engine.getPlayer1Wins(),
                engine.getPlayer1Losses(),
                engine.getPlayer1HighestScore(),
                engine.getPlayer2Wins(),
                engine.getPlayer2Losses(),
                engine.getPlayer2HighestScore()
        );
    }

    private void showSettingsPanel() {
        engine.setPaused(true);

        pausePanel.setVisible(false);
        settingsPanel.setVisible(true);

        repaint();
    }

    private void hideSettingsPanel() {
        settingsPanel.setVisible(false);
        pausePanel.setVisible(true);

        repaint();
    }

    private void pauseGame() {
        engine.setPaused(true);
        setMenusVisible(true);
        repaint();
    }

    private void resumeGame() {
        engine.setPaused(false);
        setMenusVisible(false);

        requestFocusInWindow();
        repaint();
    }

    private void restartMatch() {
        try {
            int newMatchId = JDBC.createMatch(
                    engine.getPlayer1Id(),
                    engine.getPlayer2Id()
            );

            engine.restartMatch(newMatchId);
            resumeGame();

        } catch (SQLException e) {
            showDatabaseError("Unable to restart match:\n" + e.getMessage());
        }
    }

    private void quitToWelcome() {
        engine.setPaused(true);
        setMenusVisible(false);
        backToWelcome.run();
    }

    private void showDatabaseError(String message) {
        JOptionPane.showMessageDialog(
                this, message,
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        engine.resizeGame(width, height);

        pauseButton.setBounds(width - 60, 20, 30, 30);

        pausePanel.setBounds(0, 0, width, height);
        settingsPanel.setBounds(0, 0, width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        renderer.draw(
                g,
                getWidth(),
                getHeight(),
                engine.getBall(),
                engine.getPaddle1(),
                engine.getPaddle2(),
                engine.getScore(),
                engine.getPlayer1Name(),
                engine.getPlayer2Name(),
                pauseButton,
                engine.isPaused(),
                input.isHovered()
        );
    }

    @Override
    public void run() {

        final double frameTime = 1_000_000_000.0 / 60;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (!Thread.currentThread().isInterrupted()) {

            long currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / frameTime;
            lastTime = currentTime;

            while (delta >= 1) {
                engine.update();
                delta--;
            }

            repaint();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class InputEvents implements GameInput.Listener {

        @Override
        public void pauseGame() {
            GamePanel.this.pauseGame();
        }

        @Override
        public void resumeGame() {
            GamePanel.this.resumeGame();
        }

        @Override
        public void pauseButtonHoverChanged(boolean hovered) {
            repaint();
        }
    }

    private class EngineEvents implements GameEngine.Listener {

        
        @Override
        public void matchFinished(
                String winnerName,
                int player1Score,
                int player2Score) {

            updateSettingsStatistics();

            JOptionPane.showMessageDialog(
                    GamePanel.this,
                    winnerName + " wins!\n\n"
                    + engine.getPlayer1Name() + ": " + player1Score
                    + "\n" + engine.getPlayer2Name() + ": " + player2Score,
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE
            );

            engine.setPaused(true);
            setMenusVisible(false);
            backToWelcome.run();
        }

        @Override
        public void databaseError(String message) {
            showDatabaseError(message);
            pausePanel.setVisible(true);
        }
    }
}