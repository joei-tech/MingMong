package florante.mingmong;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class GameEngine {

    public interface Listener {
        void matchFinished(String winnerName, int player1Score, int player2Score);
        void databaseError(String message);
    }

    private static final int BALL_DIAMETER = 20;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 80;
    private static final int WINNING_SCORE = 10;

    private int gameWidth, gameHeight;
    private final Listener listener;
    private final Score score;

    private Paddle paddle1, paddle2;
    private Ball ball;

    private boolean paused = true;
    private boolean matchFinished;

    private int player1Id, player2Id, matchId, roundNumber;

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    private int player1Wins, player1Losses, player1HighestScore;
    private int player2Wins, player2Losses, player2HighestScore;

    public GameEngine(int width, int height, Listener listener) {
        gameWidth = width;
        gameHeight = height;
        this.listener = listener;

        score = new Score(width, height);
        resetRound();
    }

    // RESIZE GAME
    public void resizeGame(int width, int height) {
        if (width <= 0 || height <= 0) return;

        gameWidth = width;
        gameHeight = height;

        keepPaddlesInsideScreen();

        if (ball != null) {
            ball.x = Math.max(0, Math.min(ball.x, width - BALL_DIAMETER));
            ball.y = Math.max(0, Math.min(ball.y, height - BALL_DIAMETER));
        }
    }

    // SET MATCH DATA
    public void setMatchData(int matchId, JDBC.PlayerData player1,
                             JDBC.PlayerData player2) {

        this.matchId = matchId;

        player1Id = player1.playerId;
        player2Id = player2.playerId;

        player1Name = player1.username;
        player2Name = player2.username;

        player1Wins = player1.wins;
        player1Losses = player1.losses;
        player1HighestScore = player1.highestScore;

        player2Wins = player2.wins;
        player2Losses = player2.losses;
        player2HighestScore = player2.highestScore;

        score.setPlayerNames(player1Name, player2Name);
        resetMatch();
    }

    // UPDATE GAME
    public void update() {
        if (paused) return;

        paddle1.move();
        paddle2.move();
        ball.move();

        checkCollision();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void keyPressed(KeyEvent event) {
        if (!paused) {
            paddle1.keyPressed(event);
            paddle2.keyPressed(event);
        }
    }

    public void keyReleased(KeyEvent event) {
        if (!paused) {
            paddle1.keyReleased(event);
            paddle2.keyReleased(event);
        }
    }

    public void resetMatch() {
        score.reset();
        roundNumber = 0;
        matchFinished = false;
        resetRound();
    }

    public void restartMatch(int newMatchId) {
        matchId = newMatchId;
        resetMatch();
    }

    public void setPlayerNames(String player1Name, String player2Name) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;

        score.setPlayerNames(player1Name, player2Name);
    }

    private void resetRound() {
        paddle1 = new Paddle(
                0, (gameHeight - PADDLE_HEIGHT) / 2,
                PADDLE_WIDTH, PADDLE_HEIGHT, 1
        );

        paddle2 = new Paddle(
                gameWidth - PADDLE_WIDTH,
                (gameHeight - PADDLE_HEIGHT) / 2,
                PADDLE_WIDTH, PADDLE_HEIGHT, 2
        );

        ball = new Ball(
                (gameWidth - BALL_DIAMETER) / 2,
                (gameHeight - BALL_DIAMETER) / 2,
                BALL_DIAMETER, BALL_DIAMETER
        );
    }

    private void checkCollision() {

        if (ball.y <= 0) {
            ball.y = 0;
            ball.setYVelocity(Math.abs(ball.yVelocity));
        }

        if (ball.y + BALL_DIAMETER >= gameHeight) {
            ball.y = gameHeight - BALL_DIAMETER;
            ball.setYVelocity(-Math.abs(ball.yVelocity));
        }

        if (hitPaddle(paddle1) && ball.xVelocity < 0) {
            ball.x = paddle1.x + paddle1.width;
            ball.setXVelocity(Math.abs(ball.xVelocity) + 1);
            increaseVerticalSpeed();
        }

        if (hitPaddle(paddle2) && ball.xVelocity > 0) {
            ball.x = paddle2.x - BALL_DIAMETER;
            ball.setXVelocity(-(Math.abs(ball.xVelocity) + 1));
            increaseVerticalSpeed();
        }

        keepPaddlesInsideScreen();

        if (ball.x <= 0) {
            scorePoint(true);
        }
        else if (ball.x >= gameWidth - BALL_DIAMETER) {
            scorePoint(false);
        }
    }

    private boolean hitPaddle(Paddle paddle) {

        Rectangle hitbox = new Rectangle(
                paddle.x + 15,
                paddle.y + 10,
                paddle.width - 30,
                paddle.height - 20
        );

        return ball.intersects(hitbox);
    }

    private void scorePoint(boolean player2Scored) {

        if (player2Scored) {
            score.player2++;
            player2HighestScore = Math.max(player2HighestScore, score.player2);
        } else {
            score.player1++;
            player1HighestScore = Math.max(player1HighestScore, score.player1);
        }

        saveRound();
        resetRound();
        checkWinner();
    }

    private void increaseVerticalSpeed() {
        int speed = Math.abs(ball.yVelocity) + 1;
        ball.setYVelocity(ball.yVelocity > 0 ? speed : -speed);
    }

    // KEEP PADDLES INSIDE GAME
    private void keepPaddlesInsideScreen() {

        paddle1.y = Math.max(0,
                Math.min(paddle1.y, gameHeight - paddle1.height));

        paddle2.y = Math.max(0,
                Math.min(paddle2.y, gameHeight - paddle2.height));

        paddle1.x = 0;
        paddle2.x = gameWidth - paddle2.width;
    }

    private void saveRound() {
        roundNumber++;

        try {
            JDBC.saveRound(
                    matchId, roundNumber,
                    score.player1, score.player2
            );
        } catch (SQLException e) {
            paused = true;
            listener.databaseError("Unable to save round:\n" + e.getMessage());
        }
    }

    private void checkWinner() {
        if (matchFinished) return;

        if (score.player1 >= WINNING_SCORE) {
            finishMatch(player1Id, player2Id);
        } else if (score.player2 >= WINNING_SCORE) {
            finishMatch(player2Id, player1Id);
        }
    }

    private void finishMatch(int winnerId, int loserId) {
        matchFinished = true;
        paused = true;

        try {
            JDBC.finishMatch(
                    matchId, winnerId, loserId,
                    score.player1, score.player2
            );

            if (winnerId == player1Id) {
                player1Wins++;
                player2Losses++;
            } else {
                player2Wins++;
                player1Losses++;
            }

            String winnerName = winnerId == player1Id
                    ? player1Name : player2Name;

            listener.matchFinished(
                    winnerName, score.player1, score.player2
            );

        } catch (SQLException e) {
            listener.databaseError(
                    "Unable to save match results:\n" + e.getMessage()
            );
        }
    }

    public Paddle getPaddle1() { return paddle1; }
    public Paddle getPaddle2() { return paddle2; }
    public Ball getBall() { return ball; }
    public Score getScore() { return score; }

    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }

    public int getPlayer1Id() { return player1Id; }
    public int getPlayer2Id() { return player2Id; }

    public int getPlayer1Wins() { return player1Wins; }
    public int getPlayer1Losses() { return player1Losses; }
    public int getPlayer1HighestScore() { return player1HighestScore; }

    public int getPlayer2Wins() { return player2Wins; }
    public int getPlayer2Losses() { return player2Losses; }
    public int getPlayer2HighestScore() { return player2HighestScore; }
}
