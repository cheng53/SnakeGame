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
    private Image gateImg;
    private Image rockImg;
    private Image iceImg;
    private Image swampImg;

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

            // 💡 修正：對齊你實際的圖片檔名 purple.png 與 blue_apple.png
            poisonImg = ImageIO.read(new File("resources/purple.png"));
            speedImg = ImageIO.read(new File("resources/blue_apple.png"));

            // 狀態變數與上方加載的圖片同步
            stunImg = poisonImg;
            trophyImg = ImageIO.read(new File("resources/trophy.png"));
            gameFrameImage = ImageIO.read(new File("resources/game_frame.png"));
            snakeHeadImg = ImageIO.read(new File("resources/snake_head.png"));
            snakeBodyImg = ImageIO.read(new File("resources/snake_body.png"));
            rockImg = ImageIO.read(new File("resources/rock.png"));
            gateImg = ImageIO.read(new File("resources/gate.png"));
            // ✨ 新增這兩行：從資源夾讀取地形圖片
            iceImg = ImageIO.read(new File("resources/ice.png"));
            swampImg = ImageIO.read(new File("resources/swamp.png"));
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

        // ==========================================
        // 【圖層 2：中層】繪製半透明的棋盤
        // ==========================================
        // 1. 底框改為半透明 (第四個參數 120 代表大約 50% 的透明度)
        //g2d.setColor(new Color(96, 144, 44));
        g2d.setColor(new Color(96, 144, 44, 150));
        g2d.fillRect(-4, -4, gameWidth + 8, gameHeight + 8);

        for (int row = 0; row < GRID_COUNT; row++) {
            for (int col = 0; col < GRID_COUNT; col++) {
                // 2. 棋盤格子也加上透明度 (這裡設為 80，讓它比底框更透一點)
                //g2d.setColor((row + col) % 2 == 0 ? new Color(132, 178, 65) : new Color(121, 166, 56));
                g2d.setColor((row + col) % 2 == 0 ? new Color(132, 178, 65, 100) : new Color(121, 166, 56, 100));
                g2d.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // ==========================================
        // 【圖層 2.1：特殊地形層】冰塊與泥沼
        // ==========================================
        // 畫泥沼
        for (Point p : model.swampCells) {
            if (swampImg != null) {
                // ✨ 有圖片時：畫出泥沼圖片 (大小設定為剛好填滿一格 TILE_SIZE)
                g2d.drawImage(swampImg, p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            } else {
                // 找不到圖片時的防呆設計 (原本的色塊)
                g2d.setColor(new Color(101, 67, 33, 160));
                g2d.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g2d.setColor(new Color(60, 40, 20, 160));
                g2d.fillOval(p.x * TILE_SIZE + 5, p.y * TILE_SIZE + 10, 15, 8);
                g2d.fillOval(p.x * TILE_SIZE + 20, p.y * TILE_SIZE + 20, 10, 5);
            }
        }

        // 畫冰塊
        for (Point p : model.iceCells) {
            if (iceImg != null) {
                // ✨ 有圖片時：畫出冰塊圖片
                g2d.drawImage(iceImg, p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            } else {
                // 找不到圖片時的防呆設計 (原本的色塊)
                g2d.setColor(new Color(173, 216, 230, 180));
                g2d.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(p.x * TILE_SIZE + 22, p.y * TILE_SIZE + 6, p.x * TILE_SIZE + 30, p.y * TILE_SIZE + 14);
            }
        }


        // 【圖層 2.2：障礙物層】畫在綠色草地上
        for (Point p : model.obstacles) {
            if (rockImg != null) {
                int centerX = p.x * TILE_SIZE + (TILE_SIZE / 2);
                int centerY = p.y * TILE_SIZE + (TILE_SIZE / 2);
                int drawSize = (int)(TILE_SIZE * 1.5);
                int drawX = centerX - (drawSize / 2);
                int drawY = centerY - (drawSize / 2);
                g2d.drawImage(rockImg, drawX, drawY, drawSize, drawSize, null);
            } else {
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRoundRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, 8, 8);
            }
        }

        // 【圖層 2.5：通道層】畫在網格之上、蛇的下方
        if (!model.exitCells.isEmpty()) {
            int boardSize = GRID_COUNT * TILE_SIZE;
            int max = GRID_COUNT - 1;
            int bridgeLength = 5 * TILE_SIZE;
            int overlap = 40;

            if (model.exitCells.contains(new Point(4, 0))) {
                drawGate(g2d, 4 * TILE_SIZE, -bridgeLength, 2 * TILE_SIZE, bridgeLength + overlap, true);
            }
            if (model.exitCells.contains(new Point(4, max))) {
                drawGate(g2d, 4 * TILE_SIZE, boardSize - overlap, 2 * TILE_SIZE, bridgeLength + overlap, true);
            }
            if (model.exitCells.contains(new Point(0, 4))) {
                drawGate(g2d, -bridgeLength, 4 * TILE_SIZE, bridgeLength + overlap, 2 * TILE_SIZE, false);
            }
            if (model.exitCells.contains(new Point(max, 4))) {
                drawGate(g2d, boardSize - overlap, 4 * TILE_SIZE, bridgeLength + overlap, 2 * TILE_SIZE, false);
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

                        // ✨ 修正版：使用正負號判斷，相容邊界傳送與新關卡生成
                        if (dx > 0)        angle = -Math.PI / 2.0;
                        else if (dx < 0)   angle = Math.PI / 2.0;
                        else if (dy < 0)   angle = Math.PI;
                        else if (dy > 0)   angle = 0;
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
        g2d.drawString("第" + model.score + "層", 20, 52);

        // ✨ 優先顯示重生緩衝狀態
        if (model.isRespawning) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("重生中... " + model.respawnTimer, 200, 52);
        }
        else if (model.isPaused) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("遊戲暫停中", 200, 52);
        }
        else if (model.hasShield) {
            g2d.setColor(Color.YELLOW);
            g2d.drawString("護盾中 (" + model.shieldTimer + "s)", 200, 52);
        }
        else {
            // 沒有重生、沒有護盾、也沒有暫停時，才顯示常規的加速與暈眩狀態
            if (model.isSpeedUp) {
                g2d.setColor(Color.CYAN);
                g2d.drawString("加速中", 200, 52);
            }
            if (model.isStunned) {
                g2d.setColor(Color.GREEN);
                g2d.drawString("暈眩中", 310, 52);
            }
        }
        if (trophyImg != null) {
            g2d.drawImage(trophyImg, panelWidth - 65, 18, 45, 45, null);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString("" + model.highScore, panelWidth - 5 - g2d.getFontMetrics().stringWidth("" + model.highScore), 52);
    }

    private void drawGate(Graphics2D g2d, int x, int y, int w, int h, boolean isVertical) {
        if (gateImg != null) {
            int overflow = (int)(TILE_SIZE * 2);

            if (isVertical) {
                int drawW = w + overflow * 2;
                int drawX = x - overflow;

                AffineTransform oldTransform = g2d.getTransform();
                g2d.translate(drawX + drawW / 2.0, y + h / 2.0);
                g2d.rotate(Math.PI / 2);
                g2d.drawImage(gateImg, -h / 2, -drawW / 2, h, drawW, null);
                g2d.setTransform(oldTransform);
            } else {
                int drawH = h + overflow * 2;
                int drawY = y - overflow;
                g2d.drawImage(gateImg, x, drawY, w, drawH, null);
            }
        } else {
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(x, y, w, h);
        }
    }
}
