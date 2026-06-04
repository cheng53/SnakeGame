import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform; // 用於旋轉蛇頭圖片
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private GameModel model;
    public Image redImg, goldImg, poisonImg, stunImg, speedImg, trophyImg;
    private Image gameFrameImage; // 遊戲的植物外框圖片
    private Image snakeHeadImg;    // 藍色精緻蛇頭圖片
    private Image snakeBodyImg;    // 藍色鱗片蛇身圖片

    // 💡 保持你目前使用的 TILE_SIZE = 36
    public final int TILE_SIZE = 36;
    private final int GRID_COUNT;
    private final int HEADER_HEIGHT = 80;

    public GamePanel(GameModel model) {
        this.model = model;
        this.GRID_COUNT = model.GRID_SIZE; // 已對齊你的大寫變數

        setPreferredSize(new Dimension(TILE_SIZE * GRID_COUNT, TILE_SIZE * GRID_COUNT + HEADER_HEIGHT));
        setBackground(new Color(116, 190, 63)); // 改用草綠色底色

        loadImages();
    }

    private void loadImages() {
        try {
            redImg = ImageIO.read(new File("resources/red_apple.png"));
            goldImg = ImageIO.read(new File("resources/gold_apple.png"));
            poisonImg = ImageIO.read(new File("resources/poison_apple.png"));
            stunImg = ImageIO.read(new File("resources/stun_apple.png"));
            speedImg = ImageIO.read(new File("resources/speed_apple.png"));
            trophyImg = ImageIO.read(new File("resources/trophy.png"));
            gameFrameImage = ImageIO.read(new File("resources/game_frame.png"));
            snakeHeadImg = ImageIO.read(new File("resources/snake_head.png"));
            snakeBodyImg = ImageIO.read(new File("resources/snake_body.png"));
        } catch (Exception e) {
            System.out.println("提示：部分圖片尚未放入 resources 資料夾，將使用預設顏色。");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int gameWidth = TILE_SIZE * GRID_COUNT;
        int gameHeight = TILE_SIZE * GRID_COUNT;

        // 計算棋盤在畫面上的實際偏移位置
        int offsetX = (panelWidth - gameWidth) / 2;
        int offsetY = HEADER_HEIGHT + (panelHeight - HEADER_HEIGHT - gameHeight) / 2;

        // ==========================================
        // 【圖層 1：最底層】先繪製草叢外框圖片
        // ==========================================
        if (gameFrameImage != null) {
            g2d.drawImage(gameFrameImage, 0, HEADER_HEIGHT, panelWidth, panelHeight - HEADER_HEIGHT, null);
        }

        // 切換坐標系到棋盤起點，準備繪製遊戲主體
        g2d.translate(offsetX, offsetY);

        // ==========================================
        // 【圖層 2：中層】繪製不透明的綠色棋盤
        // ==========================================
        g2d.setColor(new Color(162, 209, 73)); // 使用棋盤的深綠色
        g2d.fillRect(-4, -4, gameWidth + 8, gameHeight + 8);
        for (int row = 0; row < GRID_COUNT; row++) {
            for (int col = 0; col < GRID_COUNT; col++) {
                g2d.setColor((row + col) % 2 == 0 ? new Color(170, 215, 81) : new Color(162, 209, 73));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // ==========================================
        // 【圖層 3：遊戲物件層】繪製蛇與道具
        // ==========================================
        for (int i = 0; i < model.snake.size(); i++) {
            SnakeNode node = model.snake.get(i);

            if (node.type.equals("HEAD")) {
                // 🐍 蛇頭繪製邏輯：保持旋轉與放大
                if (snakeHeadImg != null) {
                    AffineTransform oldTransform = g2d.getTransform();

                    double centerX = node.x * TILE_SIZE + TILE_SIZE / 2.0;
                    double centerY = node.y * TILE_SIZE + TILE_SIZE / 2.0;
                    g2d.translate(centerX, centerY);

                    double angle = 0;

                    if (model.snake.size() > 1) {
                        SnakeNode nextNode = model.snake.get(1);
                        int dx = node.x - nextNode.x;
                        int dy = node.y - nextNode.y;

                        if (dx == 1)       angle = -Math.PI / 2.0;
                        else if (dx == -1) angle = Math.PI / 2.0;
                        else if (dy == -1) angle = Math.PI;
                        else if (dy == 1)  angle = 0;
                    }
                    g2d.rotate(angle);

                    int headSize = TILE_SIZE + 6;
                    g2d.drawImage(snakeHeadImg, -headSize / 2, -headSize / 2, headSize, headSize, null);

                    g2d.setTransform(oldTransform);
                } else {
                    g2d.setColor(new Color(0, 102, 204));
                    g2d.fillOval(node.x * TILE_SIZE + 2, node.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
            } else {
                // 🟢 【方案 A：圓角圖片版—幾何等比例漸細圓角正方形】
                if (snakeBodyImg != null) {
                    // 1. 完全沿用你原本最棒的幾何漸細與置中數學公式
                    int shrink = Math.min(i * 1, 24);
                    int currentSize = TILE_SIZE - 6 - shrink; // 💡 稍微再內縮一點點，讓身體更精緻
                    int offset = 3 + (shrink / 2);            // 💡 相應微調置中偏移量

                    int posX = node.x * TILE_SIZE + offset;
                    int posY = node.y * TILE_SIZE + offset;

                    // 2. 備份目前的剪裁區域
                    java.awt.Shape oldClip = g2d.getClip();

                    // 3. 創建一個與身體大小相同的圓角矩形遮罩 (圓角半徑設為 12)
                    g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(posX, posY, currentSize, currentSize, 12, 12));

                    // 4. 在圓角遮罩內繪製這張鱗片圖片
                    g2d.drawImage(snakeBodyImg, posX, posY, currentSize, currentSize, null);

                    // 5. 還原剪裁區域，避免影響後續繪製
                    g2d.setClip(oldClip);
                } else {
                    // 備用方案：圖片載入失敗時畫原本的漸層綠色方塊
                    int gradient = Math.min(255, 150 + (i * 5));
                    g2d.setColor(new Color(51, 153, gradient));

                    int shrink = Math.min(i * 2, 24);
                    int currentSize = TILE_SIZE - 4 - shrink;
                    int offset = 2 + (shrink / 2);

                    g2d.fillRoundRect(
                            node.x * TILE_SIZE + offset,
                            node.y * TILE_SIZE + offset,
                            currentSize,
                            currentSize,
                            15, 15
                    );
                }
            }
        } // 完美閉合畫蛇的 for 迴圈

        // 畫道具
        for (Item item : model.items) {
            item.draw(g2d, TILE_SIZE, this);
        }

        // 復原坐標系偏移
        g2d.translate(-offsetX, -offsetY);

        // ==========================================
        // 【圖層 4：最頂層】Header 計分區（💡 已修正中文字型，消滅方塊亂碼）
        // ==========================================
        g2d.setColor(new Color(74, 117, 44));
        g2d.fillRect(0, 0, panelWidth, HEADER_HEIGHT);
        g2d.setColor(Color.WHITE);

        // 將字型改為微軟正黑體，確保英文字 Score 和中文字狀態都能完美顯示
        g2d.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
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

        if (trophyImg != null) {
            g2d.drawImage(trophyImg, panelWidth - 65, 18, 45, 45, null);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString("" + model.highScore, panelWidth - 5 - g2d.getFontMetrics().stringWidth("" + model.highScore), 52);
    }
}
