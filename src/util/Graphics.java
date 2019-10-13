package util;

import javax.swing.*;

public class Graphics extends JFrame {

	private static final long serialVersionUID = -4825279720757909719L;

	public static Graphics getOpenWindow() {
        Graphics jf = new Graphics();
        ImageIcon icon = new ImageIcon("images/back.jpg");
        jf.setUndecorated(true);
        jf.setSize(icon.getIconWidth(), icon.getIconHeight());
        jf.setVisible(true);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel(icon);
        label.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        jf.getLayeredPane().add(label);
        return jf;
    }

    public static void main() throws InterruptedException {
        Graphics g = getOpenWindow();
        g.setVisible(true);
        Thread.sleep(3000);
        g.setVisible(false);
        g.dispose();
    }
}