import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(MainFrame frame) {
        setPreferredSize(new Dimension(600, 700)); // 配合加大後的視窗
        setBackground(new Color(40, 40, 40));
        setLayout(null); // 使用絕對定位來精確還原你的 GUI 設計

        JLabel title = new JLabel("生存貪吃蛇", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 60));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 80, 350, 60);
        add(title);

        JButton startBtn = new JButton("開始遊玩");
        startBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        startBtn.setBounds(75, 280, 200, 60);
        startBtn.addActionListener(e -> frame.switchToGame());
        add(startBtn);
    }
}