import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel(MainFrame frame) {
        setPreferredSize(new Dimension(600, 700));
        setBackground(new Color(40, 40, 40));
        setLayout(null); // 使用絕對定位

        // 先建立元件
        JLabel title = new JLabel("生存貪吃蛇", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 60));
        title.setForeground(Color.WHITE);
        add(title);

        JButton startBtn = new JButton("開始遊玩");
        startBtn.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        startBtn.addActionListener(e -> frame.switchToGame());
        add(startBtn);

        // 計算置中位置
        int panelWidth = getPreferredSize().width;
        int panelHeight = getPreferredSize().height;

        // 標題置中
        int titleWidth = 400;
        int titleHeight = 80;
        title.setBounds((panelWidth - titleWidth) / 2, panelHeight / 4, titleWidth, titleHeight);

        // 按鈕置中
        int btnWidth = 200;
        int btnHeight = 60;
        startBtn.setBounds((panelWidth - btnWidth) / 2, panelHeight / 2, btnWidth, btnHeight);
    }
}