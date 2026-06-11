import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;

public class MenuPanel extends JPanel {
    private final MainFrame mainFrame;
    private Image bgImage;

    // ✨ 修改：新增 rankBtn 變數
    private RoundRectangle2D.Float startBtn, introBtn, rankBtn, exitBtn;
    private String hoveredBtn = "";

    public MenuPanel(MainFrame frame) {
        this.mainFrame = frame;

        int width = 35 * 15;
        int height = 35 * 15 + 80;
        setPreferredSize(new Dimension(width, height));

        try {
            bgImage = ImageIO.read(new File("resources/menu_bg.png"));
        } catch (Exception e) {
            System.out.println("找不到背景圖片 resources/menu_bg.png");
        }

        // 💡 佈局微調：縮小按鈕高度與間距，完美容納 4 個按鈕
        int btnW = 200;
        int btnH = 45;
        int btnX = (width - btnW) / 2;
        int startY = height - 240; // 整體佈局稍微向上收攏

        startBtn = new RoundRectangle2D.Float(btnX, startY, btnW, btnH, 15, 15);
        introBtn = new RoundRectangle2D.Float(btnX, startY + 55, btnW, btnH, 15, 15);
        rankBtn  = new RoundRectangle2D.Float(btnX, startY + 110, btnW, btnH, 15, 15); // ✨ 排行榜
        exitBtn  = new RoundRectangle2D.Float(btnX, startY + 165, btnW, btnH, 15, 15);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (startBtn.contains(p)) {
                    mainFrame.switchToGame();
                } else if (introBtn.contains(p)) {
                    showIntroduction();
                } else if (rankBtn.contains(p)) {
                    showLeaderboard(); // ✨ 點擊彈出排行榜
                } else if (exitBtn.contains(p)) {
                    System.exit(0);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String lastHover = hoveredBtn;
                Point p = e.getPoint();

                if (startBtn.contains(p)) hoveredBtn = "START";
                else if (introBtn.contains(p)) hoveredBtn = "INTRO";
                else if (rankBtn.contains(p)) hoveredBtn = "RANK"; // ✨ 懸停偵測
                else if (exitBtn.contains(p)) hoveredBtn = "EXIT";
                else hoveredBtn = "";

                if (!lastHover.equals(hoveredBtn)) {
                    setCursor(hoveredBtn.isEmpty() ?
                            Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        });
    }

    private void showIntroduction() {
        String msg =  "【Snake Adventure 遊戲介紹】\n\n" +
                "1. 控制：使用方向鍵控制小藍蛇移動，按 P 鍵可暫停遊戲。\n" +
                "2. 過關：每關收集 3 顆紅蘋果，開啟神秘木橋。\n" +
                "3. 計分：每穿過一次木橋進入下一關加 1 分！吃道具不會加分。\n" +
                "4. 道具：\n" +
                "   - 金色蘋果：獲得 5 秒護盾，可抵擋一次撞擊並回到中央重生。\n" +
                "   - 藍色水晶：使小藍蛇移動速度翻倍，持續 8 秒。\n" +
                "   - 紫色蘋果：50% 機率使身體縮短，50% 機率陷入暈眩反向控制。\n" +
                "5. 挑戰：每過 2 關會多出 1 顆岩石，考驗你的走位極限！";
        JOptionPane.showMessageDialog(this, msg, "遊戲介紹", JOptionPane.INFORMATION_MESSAGE);
    }

    // 🏆 ✨ 新增方法：彈出歷史前五名視窗
    private void showLeaderboard() {
        java.util.List<Integer> scores = mainFrame.getGameModel().leaderboard;
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 ★ Snake Adventure 歷史排行榜 ★ 🏆<br><br>");

        if (scores == null || scores.isEmpty()) {
            sb.append("&nbsp;&nbsp;目前尚無冒險紀錄，快去拿下第一名吧！<br>");
        } else {
            for (int i = 0; i < scores.size(); i++) {
                String medal = "";
                if (i == 0) medal = "🥇 ";
                else if (i == 1) medal = "🥈 ";
                else if (i == 2) medal = "🥉 ";
                else medal = "✨ ";
                sb.append(String.format("&nbsp;&nbsp;%s第 %d 名 ： 通過 %d 層<br>", medal, i + 1, scores.get(i)));
            }
        }
        sb.append("<br>挑戰最高分，將你的名字留在這座森林吧！");

        JLabel label = new JLabel("<html>" + sb.toString() + "</html>");
        label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
        JOptionPane.showMessageDialog(this, label, "歷史排行榜", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // 畫四個按鈕
        drawStyledButton(g2d, startBtn, "開始遊戲", hoveredBtn.equals("START"));
        drawStyledButton(g2d, introBtn, "遊戲介紹", hoveredBtn.equals("INTRO"));
        drawStyledButton(g2d, rankBtn,  "排行榜",   hoveredBtn.equals("RANK")); // ✨ 繪製排行榜
        drawStyledButton(g2d, exitBtn,  "退出遊戲", hoveredBtn.equals("EXIT"));
    }

    private void drawStyledButton(Graphics2D g2d, RoundRectangle2D.Float btn, String text, boolean isHover) {
        g2d.setColor(isHover ? new Color(255, 204, 0, 200) : new Color(0, 0, 0, 150));
        g2d.fill(btn);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(btn);

        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (int) (btn.x + (btn.width - fm.stringWidth(text)) / 2);
        int ty = (int) (btn.y + (btn.height + fm.getAscent()) / 2 - 3);

        g2d.setColor(isHover ? Color.BLACK : Color.WHITE);
        g2d.drawString(text, tx, ty);
    }
}