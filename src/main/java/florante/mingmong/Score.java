package florante.mingmong;

import java.awt.Font;
import java.awt.Graphics;

public class Score {

    int player1;
    int player2;

    public Score(int gameWidth, int gameHeight) {
    }

    public void reset() {
        player1 = 0;
        player2 = 0;
    }

    public void setPlayerNames(String player1Name, String player2Name) {
    }

    public void draw(Graphics g, int width, int height) {
        g.setColor(GingerKitty_Theme.TEXT);
        g.setFont(new Font("Consolas", Font.BOLD, 60));

        int leftCenter = width / 4;
        int rightCenter = width * 3 / 4;

        String[] scores = {
            String.valueOf(player1),
            String.valueOf(player2)
        };

        int[] centers = {leftCenter, rightCenter};

        for (int i = 0; i < scores.length; i++) {
            int scoreWidth = g.getFontMetrics().stringWidth(scores[i]);
            g.drawString(scores[i], centers[i] - scoreWidth / 2, 115);
        }
    }
}