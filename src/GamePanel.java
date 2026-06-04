import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel {
    private GameModel model;
    public Image redImg, goldImg, poisonImg, stunImg, speedImg, trophyImg;
    private Image gameFrameImage;
    private Image snakeHeadImg;
    private Image snakeBodyImg;

    public final int TILE_SIZE = 36;
    private final int GRID_COUNT;
    private final int HEADER_HEIGHT = 80;

    public GamePanel(GameModel model) {
        this.model = model;
        this.GRID_COUNT = model.GRID_SIZE;

        setPreferredSize(new Dimension(TILE_SIZE * GRID_COUNT, TILE_SIZE * GRID_COUNT + HEADER_HEIGHT));
        setBackground(new Color(116, 190, 63));

        loadImages();
    }

    private void loadImages() {
        try {
            redImg = ImageIO.read(new File("resources/red_apple.png"));
            goldImg = ImageIO.read(new File("resources/gold_apple.png"));
            poisonImg = ImageIO.read(new File("resources/blue_apple.png"));
            stunImg = poisonImg;
            speedImg = goldImg;
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

        int offsetX = (panelWidth - gameWidth) / 2;
        int offsetY = HEADER_HEIGHT + (panelHeight - HEADER_HEIGHT - gameHeight) / 2;

        // 【圖層 1：最底層】繪製草叢外框圖片
        if (gameFrameImage != null) {
            g2d.drawImage(gameFrameImage, 0, HEADER_HEIGHT, panelWidth, panelHeight - HEADER_HEIGHT, null);
        }

        g2d.translate(offsetX, offsetY);

        // 【圖層 2：中層】繪製不透明的綠色棋盤
        g2d.setColor(new Color(162, 209, 73));
        g2d.fillRect(-4, -4, gameWidth + 8, gameHeight + 8);
        for (int row = 0; row < GRID_COUNT; row++) {
            for (int col = 0; col < GRID_COUNT; col++) {
                g2d.setColor((row + col) % 2 == 0 ? new Color(170, 215, 81) : new Color(162, 209, 73));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // 【圖層 2.5：通道層】畫在網格之上、蛇的下方
        if (!model.exitCells.isEmpty()) {
            g2d.setColor(Color.YELLOW);
            int boardSize = GRID_COUNT * TILE_SIZE;
            int max = GRID_COUNT - 1;

            if (model.exitCells.contains(new Point(4, 0))) {
                g2d.fillRect(4 * TILE_SIZE, -3 * TILE_SIZE, 2 * TILE_SIZE, 3 * TILE_SIZE);
            }
            if (model.exitCells.contains(new Point(4, max))) {
                g2d.fillRect(4 * TILE_SIZE, boardSize, 2 * TILE_SIZE, 3 * TILE_SIZE);
            }
            if (model.exitCells.contains(new Point(0, 4))) {
                g2d.fillRect(-3 * TILE_SIZE, 4 * TILE_SIZE, 3 * TILE_SIZE, 2 * TILE_SIZE);
            }
            if (model.exitCells.contains(new Point(max, 4))) {
                g2d.fillRect(boardSize, 4 * TILE_SIZE, 3 * TILE_SIZE, 2 * TILE_SIZE);
            }
        }

        // 【圖層 3：遊戲物件層】繪製蛇與道具
        for (int i = 0; i < model.snake.size(); i++) {
            SnakeNode node = model.snake.get(i);

            if (node.type.equals("HEAD")) {
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
                if (snakeBodyImg != null) {
                    int shrink = Math.min(i * 1, 24);
                    int currentSize = TILE_SIZE - 6 - shrink;
                    int offset = 3 + (shrink / 2);

                    int posX = node.x * TILE_SIZE + offset;
                    int posY = node.y * TILE_SIZE + offset;

                    java.awt.Shape oldClip = g2d.getClip();
                    g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(posX, posY, currentSize, currentSize, 12, 12));
                    g2d.drawImage(snakeBodyImg, posX, posY, currentSize, currentSize, null);
                    g2d.setClip(oldClip);
                } else {
                    int gradient = Math.min(255, 150 + (i * 5));
                    g2d.setColor(new Color(51, 153, gradient));
                    int shrink = Math.min(i * 2, 24);
                    int currentSize = TILE_SIZE - 4 - shrink;
                    int offset = 2 + (shrink / 2);

                    g2d.fillRoundRect(node.x * TILE_SIZE + offset, node.y * TILE_SIZE + offset, currentSize, currentSize, 15, 15);
                }
            }
        }

        for (Item item : model.items) {
            item.draw(g2d, TILE_SIZE, this);
        }

        g2d.translate(-offsetX, -offsetY);

        // 【圖層 4：最頂層】Header 計分區
        g2d.setColor(new Color(74, 117, 44));
        g2d.fillRect(0, 0, panelWidth, HEADER_HEIGHT);
        g2d.setColor(Color.WHITE);

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