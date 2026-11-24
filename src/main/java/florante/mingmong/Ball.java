package florante.mingmong;

import java.awt.Rectangle;
import java.util.Random;

public class Ball extends Rectangle {

    private static final int INITIAL_SPEED = 5;
    private static final Random random = new Random();

    int xVelocity;
    int yVelocity;

    public Ball(int x, int y, int width, int height) {
        super(x, y, width, height);

        xVelocity = random.nextBoolean() ? INITIAL_SPEED : -INITIAL_SPEED;
        yVelocity = random.nextBoolean() ? INITIAL_SPEED : -INITIAL_SPEED;
    }

    public void setXVelocity(int velocity) {
        xVelocity = velocity;
    }

    public void setYVelocity(int velocity) {
        yVelocity = velocity;
    }

    public void move() {
        x += xVelocity;
        y += yVelocity;
    }
}
