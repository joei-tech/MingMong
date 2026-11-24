package florante.mingmong;

import java.awt.Rectangle;
import java.awt.event.*;

public class GameInput implements KeyListener, MouseListener, MouseMotionListener {

    public interface Listener {
        void pauseGame();
        void resumeGame();
        void pauseButtonHoverChanged(boolean hovered);
    }

    private final GameEngine engine;
    private final Rectangle pauseButton;
    private final Listener listener;

    private boolean hovered;

    public GameInput(GameEngine engine, Rectangle pauseButton, Listener listener) {
        this.engine = engine;
        this.pauseButton = pauseButton;
        this.listener = listener;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (engine.isPaused()) listener.resumeGame();
            else listener.pauseGame();
            return;
        }

        engine.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        engine.keyReleased(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!engine.isPaused() && pauseButton.contains(e.getPoint())) {
            listener.pauseGame();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        boolean newHovered = !engine.isPaused()
                && pauseButton.contains(e.getPoint());

        if (hovered != newHovered) {
            hovered = newHovered;
            listener.pauseButtonHoverChanged(hovered);
        }
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
