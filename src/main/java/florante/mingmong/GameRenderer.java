package florante.mingmong;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class GameRenderer {

    private final BufferedImage background;
    private final BufferedImage rat;
    private final BufferedImage mingPaddle;
    private final BufferedImage mongPaddle;
    private final BufferedImage pauseImage;

    private static final int RAT_SIZE = 45;
    private static final int PAW_WIDTH = 100;
    private static final int PAW_HEIGHT = 150;

    public GameRenderer() {
        background = loadImage("/images/game_bg.PNG");
        rat = loadImage("/images/ratatouille.PNG");
        mingPaddle = loadImage("/images/ming_paddle.PNG");
        mongPaddle = loadImage("/images/mong_paddle.PNG");
        pauseImage = loadImage("/images/pause_button.PNG");
    }

    private BufferedImage loadImage(String path) {
        try {
            URL resource = getClass().getResource(path);

            if (resource == null) {
                throw new IllegalArgumentException("Image not found: " + path);
            }

            BufferedImage image = ImageIO.read(resource);

            if (image == null) {
                throw new IllegalArgumentException("Invalid image: " + path);
            }

            return image;

        } catch (IOException e) {
            throw new RuntimeException("Unable to load image: " + path, e);
        }
    }

    public void draw(
            Graphics g,
            int width,
            int height,
            Ball ball,
            Paddle paddle1,
            Paddle paddle2,
            Score score,
            String player1Name,
            String player2Name,
            Rectangle pauseButtonBounds,
            boolean paused,
            boolean pauseButtonHovered) {

        Graphics2D graphics = (Graphics2D) g.create();

        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            graphics.drawImage(background, 0, 0, width, height, null);

            drawBall(graphics, ball);
            drawNames(graphics, width, player1Name, player2Name);

            score.draw(graphics, width, height);

            if (!paused) {
                drawPauseButton(graphics, pauseButtonBounds, pauseButtonHovered);
            }

            drawPaddle(graphics, mingPaddle, paddle1);
            drawPaddle(graphics, mongPaddle, paddle2);

        } finally {
            graphics.dispose();
        }
    }

    private void drawBall(Graphics2D g, Ball ball) {
        int x = ball.x - (RAT_SIZE - ball.width) / 2;
        int y = ball.y - (RAT_SIZE - ball.height) / 2;

        g.drawImage(rat, x, y, RAT_SIZE, RAT_SIZE, null);
    }

    private void drawPaddle(Graphics2D g, BufferedImage image, Paddle paddle) {
        int x = paddle.x - (PAW_WIDTH - paddle.width) / 2;

        g.drawImage(
                image,
                x,
                paddle.y,
                PAW_WIDTH,
                paddle.height,
                null
        );
        
        g.drawImage(image, x, paddle.y, PAW_WIDTH, paddle.height, null);
    }

    private void drawNames(Graphics2D g, int width, String name1, String name2) {
        g.setFont(new Font("Comic Sans MS", Font.PLAIN, 28));
        g.setColor(GingerKitty_Theme.TEXT);

        FontMetrics metrics = g.getFontMetrics();

        String[] names = {name1, name2};
        int[] centers = {width / 4, width * 3 / 4};

        for (int i = 0; i < 2; i++) {
            int x = centers[i] - metrics.stringWidth(names[i]) / 2;
            g.drawString(names[i], x, 50);
        }
    }

    private void drawPauseButton(Graphics2D g, Rectangle bounds, boolean hovered) {
        g.drawImage(
                pauseImage,
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                null
        );

        if (hovered) {
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRoundRect(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    10,
                    10
            );
        }
    }
}