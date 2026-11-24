package florante.mingmong;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.imageio.ImageIO;

public class Paddle extends Rectangle {

    private static final int SPEED = 10;

    private final int id;
    private int yVelocity;
    private Image paddleImage;

    public Paddle(int x, int y, int width, int height, int id) {
        super(x, y, width, height);
        this.id = id;

        String path = id == 1
                ? "/images/ming_paddle.PNG"
                : "/images/mong_paddle.PNG";

        try {
            URL resource = getClass().getResource(path);

            if (resource != null) {
                paddleImage = ImageIO.read(resource);
            } else {
                System.err.println("Paddle image not found: " + path);
            }
        } catch (Exception e) {
            System.err.println("Unable to load paddle image: " + e.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (id == 1) {
            if (key == KeyEvent.VK_W) yVelocity = -SPEED;
            if (key == KeyEvent.VK_S) yVelocity = SPEED;
        } else {
            if (key == KeyEvent.VK_UP) yVelocity = -SPEED;
            if (key == KeyEvent.VK_DOWN) yVelocity = SPEED;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (id == 1) {
            if (key == KeyEvent.VK_W || key == KeyEvent.VK_S) {
                yVelocity = 0;
            }
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            yVelocity = 0;
        }
    }

    public void move() {
        y += yVelocity;
    }

    public void draw(Graphics g) {
        if (paddleImage != null) {
            g.drawImage(paddleImage, x, y, width, height, null);
        }
    }
}