import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO; // 需補上這個 import
import java.io.File;         // 需補上這個 import
import java.io.IOException;  // 需補上這個 import


public class GamePanel extends JPanel {
    private GameModel model;
    public Image redImg, goldImg, poisonImg, stunImg, speedImg, trophyImg;
    public final int TILE_SIZE = 35;
    private final int GRID_COUNT;
    private final int HEADER_HEIGHT = 80;

    public GamePanel(GameModel model) {
        this.model = model;
        this.GRID_COUNT = model.GRID_SIZE;


        setPreferredSize(new Dimension(TILE_SIZE * GRID_COUNT, TILE_SIZE * GRID_COUNT + HEADER_HEIGHT));
        setBackground(Color.BLACK);

        // *** 補上這行：讀取圖片 ***
        loadImages();
    }

    private void loadImages() {
        try {
            // 讀取圖片，請確保路徑正確 (相對於專案根目錄)
            redImg = ImageIO.read(new File("resources/red_apple.png"));
            goldImg = ImageIO.read(new File("resources/gold_apple.png"));
            poisonImg = ImageIO.read(new File("resources/poison_apple.png"));
            stunImg = ImageIO.read(new File("resources/stun_apple.png"));
            speedImg = ImageIO.read(new File("resources/speed_apple.png"));
            trophyImg = ImageIO.read(new File("resources/trophy.png"));
        } catch (IOException e) {
            System.out.println("提示：部分圖片尚未放入 resources 資料夾，將使用預設顏色。");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int gameWidth = TILE_SIZE * GRID_COUNT;
        int gameHeight = TILE_SIZE * GRID_COUNT;

        // --- 先畫 Header，貼到頂部 ---
        g2d.setColor(new Color(74, 117, 44));
        g2d.fillRect(0, 0, panelWidth, HEADER_HEIGHT); // 整個面板寬度
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("SCORE: " + model.score, 20, 52);

        if (model.isSpeedUp) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("加速中", 220, 52);
        }
        if (model.isStunned) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("暈眩中", 330, 52);
        }
        if (model.bodyLength <= 0) {
            g2d.setColor(Color.RED);
            g2d.drawString("瀕死警告!", 440, 52);
        }

        // --- 右側最高分 ---
        if (trophyImg != null) {
            g2d.drawImage(trophyImg, panelWidth - 65, 18, 45, 45, null);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString("" + model.highScore, panelWidth - 5 - g2d.getFontMetrics().stringWidth("" + model.highScore), 52);

        // --- 計算棋盤置中偏移，只影響棋盤和蛇 ---
        int offsetX = (panelWidth - gameWidth) / 2;
        int offsetY = HEADER_HEIGHT + (panelHeight - HEADER_HEIGHT - gameHeight) / 2;
        g2d.translate(offsetX, offsetY);

        // 畫棋盤
        for (int row = 0; row < GRID_COUNT; row++) {
            for (int col = 0; col < GRID_COUNT; col++) {
                g2d.setColor((row + col) % 2 == 0 ? new Color(170, 215, 81) : new Color(162, 209, 73));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
// 3. 畫蛇
        for (int i = 0; i < model.snake.size(); i++) {
            SnakeNode node = model.snake.get(i);

            // 顏色處理
            if (node.type.equals("HEAD")) {
                g2d.setColor(new Color(0, 102, 204));
            } else {
                int gradient = Math.min(255, 150 + (i * 5));
                g2d.setColor(new Color(51, 153, gradient));
            }

            // --- 變細邏輯 ---
            // i=0(頭)不縮減，之後每一節縮小一些
            // Math.min(i, 24) 是為了防止長蛇縮到不見
            int shrink = Math.min(i * 2, 24);
            int currentSize = TILE_SIZE - 4 - shrink;
            int offset = 2 + (shrink / 2); // 居中偏移

            g2d.fillRoundRect(
                    node.x * TILE_SIZE + offset,
                    node.y * TILE_SIZE + offset,
                    currentSize,
                    currentSize,
                    15, 15
            );

            // 畫眼睛 (維持原樣)
            if (node.type.equals("HEAD")) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(node.x * TILE_SIZE + 6, node.y * TILE_SIZE + 6, 8, 8);
                g2d.fillOval(node.x * TILE_SIZE + 21, node.y * TILE_SIZE + 6, 8, 8);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(node.x * TILE_SIZE + 9, node.y * TILE_SIZE + 8, 3, 3);
                g2d.fillOval(node.x * TILE_SIZE + 24, node.y * TILE_SIZE + 8, 3, 3);
            }
        }
        // 4. 畫道具 (這裡會自動根據紅Img等是否為空來決定畫圖還是畫圓)
        for (Item item : model.items) {
            item.draw(g2d, TILE_SIZE, this);
        }

        g2d.translate(0, -HEADER_HEIGHT);

    }
}
