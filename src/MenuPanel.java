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

    // 按鈕定義
    private RoundRectangle2D.Float startBtn, introBtn, exitBtn;
    private String hoveredBtn = ""; // 紀錄目前滑鼠在哪個按鈕上

    public MenuPanel(MainFrame frame) {
        this.mainFrame = frame;

        // 配合封面比例與遊戲視窗大小
        int width = 35 * 15;
        int height = 35 * 15 + 80;
        setPreferredSize(new Dimension(width, height));

        // 1. 載入圖片
        try {
            bgImage = ImageIO.read(new File("resources/menu_bg.png"));
        } catch (Exception e) {
            System.out.println("找不到背景圖片 resources/menu_bg.png");
        }

        // 2. 初始化按鈕區域 (位於畫面下方)
        int btnW = 200;
        int btnH = 50;
        int btnX = (width - btnW) / 2;
        int startY = height - 220; // 第一個按鈕的高度起點

        startBtn = new RoundRectangle2D.Float(btnX, startY, btnW, btnH, 15, 15);
        introBtn = new RoundRectangle2D.Float(btnX, startY + 65, btnW, btnH, 15, 15);
        exitBtn  = new RoundRectangle2D.Float(btnX, startY + 130, btnW, btnH, 15, 15);

        // 3. 滑鼠監聽邏輯
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (startBtn.contains(p)) {
                    mainFrame.switchToGame();
                } else if (introBtn.contains(p)) {
                    showIntroduction();
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
                else if (exitBtn.contains(p)) hoveredBtn = "EXIT";
                else hoveredBtn = "";

                if (!lastHover.equals(hoveredBtn)) {
                    // 若懸停狀態改變，改變游標圖示並重繪
                    setCursor(hoveredBtn.isEmpty() ?
                            Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        });
    }

    // 遊戲介紹彈窗
    private void showIntroduction() {
        String msg = "【Snake Adventure 遊戲介紹】\n\n" +
                "1. 控制：使用方向鍵控制小藍蛇移動。\n" +
                "2. 過關：每一關收集足夠數量的紅蘋果，開啟神秘木橋。\n" +
                "3. 進階：穿過木橋即可進入下一關，難度會隨之提升！\n" +
                "4. 道具：\n" +
                "   - 金蘋果：獲得大量分數。\n" +
                "   - 藍蘋果：可能獲得加速或陷入暈眩反向控制。\n" +
                "5. 挑戰：避開岩石與自己的身體，挑戰最高分吧！";
        JOptionPane.showMessageDialog(this, msg, "遊戲介紹", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 畫背景圖
        if (bgImage != null) {
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // 畫三個按鈕
        drawStyledButton(g2d, startBtn, "開始遊戲", hoveredBtn.equals("START"));
        drawStyledButton(g2d, introBtn, "遊戲介紹", hoveredBtn.equals("INTRO"));
        drawStyledButton(g2d, exitBtn,  "退出遊戲", hoveredBtn.equals("EXIT"));
    }

    // 繪製美化按鈕的方法
    private void drawStyledButton(Graphics2D g2d, RoundRectangle2D.Float btn, String text, boolean isHover) {
        // 按鈕主體 (半透明深色)
        g2d.setColor(isHover ? new Color(255, 204, 0, 200) : new Color(0, 0, 0, 150));
        g2d.fill(btn);

        // 按鈕邊框
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(btn);

        // 按鈕文字
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (int) (btn.x + (btn.width - fm.stringWidth(text)) / 2);
        int ty = (int) (btn.y + (btn.height + fm.getAscent()) / 2 - 5);

        g2d.setColor(isHover ? Color.BLACK : Color.WHITE);
        g2d.drawString(text, tx, ty);
    }
}