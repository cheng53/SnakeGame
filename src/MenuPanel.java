import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;

public class MenuPanel extends JPanel {
    private final MainFrame mainFrame;
    private final int width;
    private final int height;

    private Image bgImage; // 儲存封面背景圖片
    private RoundRectangle2D.Float startButton; // 自訂按鈕點擊區域
    private boolean isHovered = false;

    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        // 保持與遊戲畫面一致的尺寸 (525 x 605)
        this.width = 35 * 15;
        this.height = 35 * 15 + 80;
        setPreferredSize(new Dimension(width, height));

        // 1. 讀取封面圖片
        try {
            bgImage = ImageIO.read(new File("resources/menu_bg.png"));
        } catch (Exception e) {
            System.out.println("錯誤：找不到 resources/menu_bg.png 圖片，將使用備用漸層背景。");
        }

        // 2. 設定隱形的「開始遊玩」按鈕感應區 (精密對齊圖片中木質按鈕的位置)
        // 依據比例計算：按鈕寬度約占畫面的 54%，高度約 12%，位於畫面底部偏上
        // === 請用這段新數值替換原本建構子內的 startButton 設定 ===

// 1. 精密縮小按鈕的寬度與高度，讓它剛好貼合木質按鈕
        int btnW = (int) (width * 0.44);  // 從 0.54 縮小到 0.44
        int btnH = (int) (height * 0.08); // 從 0.12 縮小到 0.08
        int btnX = (width - btnW) / 2;

// 2. 修正 Y 軸高度（原圖按鈕極度偏下方，比例約在 85% 處）
        int btnY = (int) (height * 0.85); // 從 0.81 往下修正到 0.85

// 3. 調整圓角弧度，使其與圖片的圓潤木頭按鈕一致
        startButton = new RoundRectangle2D.Float(btnX, btnY, btnW, btnH, 20, 20);

        // 3. 滑鼠事件監聽
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 點擊到木質按鈕範圍內，切換到遊戲
                if (startButton.contains(e.getPoint())) {
                    mainFrame.switchToGame();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean prevStatus = isHovered;
                isHovered = startButton.contains(e.getPoint());

                if (prevStatus != isHovered) {
                    if (isHovered) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 移入變手指
                    } else {
                        setCursor(Cursor.getDefaultCursor()); // 移出變箭頭
                    }
                    repaint(); // 若想做滑鼠移入的微幅外框特效，可觸發重繪
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (bgImage != null) {
            // 繪製精美封面圖片，並自動縮放填滿整個面板
            g2d.drawImage(bgImage, 0, 0, width, height, null);
        } else {
            // 備用背景：若圖片加載失敗，則顯示科技漸層，防止畫面全白
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(15, 23, 42), 0, height, new Color(30, 27, 75));
            g2d.setPaint(bgGradient);
            g2d.fillRect(0, 0, width, height);

            // 備用文字
            g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 40));
            g2d.setColor(Color.WHITE);
            g2d.drawString("生存貪吃蛇 (圖片載入失敗)", 50, height / 2);
        }
    }
}