package florante.mingmong;

import javax.swing.SwingUtilities;

public class MingMong {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}