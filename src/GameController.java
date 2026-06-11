import javax.swing.Timer;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GameController extends KeyAdapter {
    private GameModel model;
    private GamePanel panel;

    private Timer gameTimer;
    private Timer itemRefreshTimer;
    private int stunLayers = 0;
    private int speedLayers = 0;
    private final int BASE_SPEED = 200;

    private int dx = 0, dy = -1;
    private int nextDx = 0, nextDy = -1;

    public GameController(GameModel model, GamePanel panel) {
        this.model = model;
        this.panel = panel;

        gameTimer = new Timer(BASE_SPEED, e -> moveSnake());
        itemRefreshTimer = new Timer(5000, e -> {
            spawnItems();
            panel.repaint();
        });
    }

    public void startGame() {
        gameTimer.stop();
        itemRefreshTimer.stop();

        // 💡 關鍵修正：當彻底開新一局遊戲時，強制將關卡與分數初始化歸零
        model.gameSession++;
        model.reset();             // 這會把長度變回 2，關卡變回 1
        model.score = 0;           // ✨ 強制清除上一局殘留的分數，從 0 分重新開始！

        model.isStunned = false;
        model.isSpeedUp = false;
        model.hasShield = false;   // 確保護盾也被清除
        this.stunLayers = 0;
        this.speedLayers = 0;
        dx = 0; dy = -1; nextDx = 0; nextDy = -1;

        gameTimer.setDelay(BASE_SPEED);
        gameTimer.start();
        itemRefreshTimer.start();
        spawnObstacles();
        spawnItems();
    }
    private void moveSnake() {
        if (model.isPaused) return; // ✨ 安全防護：如果是暫停狀態，絕對不執行任何移動與碰撞邏輯
        dx = nextDx; dy = nextDy;
        int newX = model.snake.get(0).x + dx;
        int newY = model.snake.get(0).y + dy;

        // 1. 基本狀態判定
        boolean isInsideBoard = (newX >= 0 && newX < model.GRID_SIZE && newY >= 0 && newY < model.GRID_SIZE);
        boolean isInsideChannel = false;
        boolean isVictory = false;
        String exitDir = "";

        // 2. 檢查是否進入或通過隨機開啟的通道
        if (!model.exitCells.isEmpty()) {
            int max = model.GRID_SIZE - 1;
            int boardEnd = model.GRID_SIZE;

            if (model.exitCells.contains(new Point(4, 0))) {
                if ((newX == 4 || newX == 5) && (newY >= -3 && newY <= -1)) {
                    isInsideChannel = true;
                } else if ((newX == 4 || newX == 5) && newY == -4) {
                    isVictory = true; exitDir = "UP";
                }
            }
            else if (model.exitCells.contains(new Point(4, max))) {
                if ((newX == 4 || newX == 5) && (newY >= boardEnd && newY <= boardEnd + 2)) {
                    isInsideChannel = true;
                } else if ((newX == 4 || newX == 5) && newY == boardEnd + 3) {
                    isVictory = true; exitDir = "DOWN";
                }
            }
            else if (model.exitCells.contains(new Point(0, 4))) {
                if ((newY == 4 || newY == 5) && (newX >= -3 && newX <= -1)) {
                    isInsideChannel = true;
                } else if ((newY == 4 || newY == 5) && newX == -4) {
                    isVictory = true; exitDir = "LEFT";
                }
            }
            else if (model.exitCells.contains(new Point(max, 4))) {
                if ((newY == 4 || newY == 5) && (newX >= boardEnd && newX <= boardEnd + 2)) {
                    isInsideChannel = true;
                } else if ((newY == 4 || newY == 5) && newX == boardEnd + 3) {
                    isVictory = true; exitDir = "RIGHT";
                }
            }
        }

        // 3. 觸發通關
        if (isVictory) {
            nextLevel(exitDir);
            return;
        }

        // 4. 死亡判定：納入護盾抵擋機制
        if ((!isInsideBoard && !isInsideChannel) ||
                model.checkCollision(newX, newY) ||
                model.obstacles.contains(new Point(newX, newY)) ||
                model.bodyLength < 0) {

            if (model.hasShield) {
                model.hasShield = false; // 💡 關鍵：護盾只能抵擋一次，立刻破盾消失！
                respawnSnake();          // 重生回到起點
                return;
            } else {
                gameOver(); // 沒有護盾，直接結束游戲
                return;
            }
        }

        // 5. 前進移動
        model.snake.add(0, new SnakeNode(newX, newY, "HEAD"));

        // 6. 吃到道具判定
        Item ateItem = null;
        for (Item item : model.items) {
            if (item.x == newX && item.y == newY) {
                ateItem = item;
                break;
            }
        }

        if (ateItem != null) {
            ateItem.applyEffect(model, this);
            model.items.remove(ateItem);

            if (ateItem instanceof RedApple) {
                model.fruitsCollectedThisLevel++;
                if (model.fruitsCollectedThisLevel == model.fruitsRequiredForNextLevel) {
                    generateLevelExit();
                }
            }
        }

        // 7. 維持身體長度與型別更新
        while (model.snake.size() > model.bodyLength + 2) {
            model.snake.remove(model.snake.size() - 1);
        }
        for (int i = 1; i < model.snake.size() - 1; i++) model.snake.get(i).type = "BODY";
        if (model.snake.size() > 1) model.snake.get(model.snake.size() - 1).type = "TAIL";

        // 8. 防作弊機制：離開通道後關閉通道
        if (model.fruitsCollectedThisLevel < model.fruitsRequiredForNextLevel) {
            boolean allInsideBoard = true;
            for (SnakeNode node : model.snake) {
                if (node.x < 0 || node.x >= model.GRID_SIZE || node.y < 0 || node.y >= model.GRID_SIZE) {
                    allInsideBoard = false;
                    break;
                }
            }
            if (allInsideBoard && !model.exitCells.isEmpty()) {
                model.exitCells.clear();
            }
        }

        panel.repaint();
    }

    private void generateLevelExit() {
        model.exitCells.clear();
        int max = model.GRID_SIZE - 1;
        int randomDirection = (int) (Math.random() * 4);

        switch (randomDirection) {
            case 0:
                model.exitCells.add(new Point(4, 0));
                model.exitCells.add(new Point(5, 0));
                break;
            case 1:
                model.exitCells.add(new Point(4, max));
                model.exitCells.add(new Point(5, max));
                break;
            case 2:
                model.exitCells.add(new Point(0, 4));
                model.exitCells.add(new Point(0, 5));
                break;
            case 3:
                model.exitCells.add(new Point(max, 4));
                model.exitCells.add(new Point(max, 5));
                break;
        }
    }

    private void nextLevel(String lastExitDir) {
        // 💡 修正：先把舊的關卡、分數與長度存下來
        int nextLvl = model.currentLevel + 1;
        int currentScore = model.score + 1; // 通過一次橋加一分
        int currentLength = model.bodyLength;

        // 💡 執行重置（這時 model.currentLevel 會被 reset() 強制變回 1）
        model.reset();

        // 💡 重大修正：在 reset 之後，才把真正累加後的關卡與分數塞回去！
        model.currentLevel = nextLvl;
        model.score = currentScore;
        model.fruitsCollectedThisLevel = 0;

        String entranceDir = "";
        if (lastExitDir.equals("UP")) entranceDir = "DOWN";
        else if (lastExitDir.equals("DOWN")) entranceDir = "UP";
        else if (lastExitDir.equals("LEFT")) entranceDir = "RIGHT";
        else if (lastExitDir.equals("RIGHT")) entranceDir = "LEFT";

        model.currentEntranceDir = entranceDir; // 記錄出生方向
        model.snake.clear();
        int max = model.GRID_SIZE - 1;
        int boardEnd = model.GRID_SIZE;
        model.bodyLength = currentLength;

        // 動態長度生成，解決換關尾巴拉長 Bug
        if (entranceDir.equals("UP")) {
            dx = 0; dy = 1; nextDx = 0; nextDy = 1;
            model.snake.add(new SnakeNode(4, -1, "HEAD"));
            for (int i = 0; i < currentLength; i++) model.snake.add(new SnakeNode(4, -2 - i, "BODY"));
            model.snake.add(new SnakeNode(4, -2 - currentLength, "TAIL"));
            model.exitCells.add(new Point(4, 0));
            model.exitCells.add(new Point(5, 0));
        }
        else if (entranceDir.equals("DOWN")) {
            dx = 0; dy = -1; nextDx = 0; nextDy = -1;
            model.snake.add(new SnakeNode(4, boardEnd, "HEAD"));
            for (int i = 0; i < currentLength; i++) model.snake.add(new SnakeNode(4, boardEnd + 1 + i, "BODY"));
            model.snake.add(new SnakeNode(4, boardEnd + 1 + currentLength, "TAIL"));
            model.exitCells.add(new Point(4, max));
            model.exitCells.add(new Point(5, max));
        }
        else if (entranceDir.equals("LEFT")) {
            dx = 1; dy = 0; nextDx = 1; nextDy = 0;
            model.snake.add(new SnakeNode(-1, 4, "HEAD"));
            for (int i = 0; i < currentLength; i++) model.snake.add(new SnakeNode(-2 - i, 4, "BODY"));
            model.snake.add(new SnakeNode(-2 - currentLength, 4, "TAIL"));
            model.exitCells.add(new Point(0, 4));
            model.exitCells.add(new Point(0, 5));
        }
        else if (entranceDir.equals("RIGHT")) {
            dx = -1; dy = 0; nextDx = -1; nextDy = 0;
            model.snake.add(new SnakeNode(boardEnd, 4, "HEAD"));
            for (int i = 0; i < currentLength; i++) model.snake.add(new SnakeNode(boardEnd + 1 + i, 4, "BODY"));
            model.snake.add(new SnakeNode(boardEnd + 1 + currentLength, 4, "TAIL"));
            model.exitCells.add(new Point(max, 4));
            model.exitCells.add(new Point(max, 5));
        }

        spawnObstacles();
        spawnItems();
    }


    private void respawnSnake() {
        model.bodyLength = 2; // 重置回原始長度
        model.snake.clear();

        // 💡 關鍵修正：不論目前關卡從哪出生，破盾重生一律強制回到棋盤正中央 (5, 5)
        // 同時將移動方向預設改為朝上 (dx=0, dy=-1)，給玩家最直覺的反應時間
        dx = 0; dy = -1;
        nextDx = 0; nextDy = -1;

        model.snake.add(new SnakeNode(5, 5, "HEAD"));
        model.snake.add(new SnakeNode(5, 6, "BODY"));
        model.snake.add(new SnakeNode(5, 7, "BODY"));
        model.snake.add(new SnakeNode(5, 8, "TAIL"));

        panel.repaint();
    }


    // ==================== 🎮 道具狀態控制核心 ====================

    public void activateStun() {
        stunLayers++;
        model.isStunned = true;
        int currentSession = model.gameSession;

        Timer t = new Timer(8000, e -> {
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                stunLayers--;
                if (stunLayers <= 0) {
                    model.isStunned = false;
                }
            }
            ((Timer)e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    public void activateSpeed() {
        speedLayers++;
        model.isSpeedUp = true;
        int currentSession = model.gameSession;
        updateSpeed();

        Timer t = new Timer(8000, e -> {
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                speedLayers--;
                if (speedLayers <= 0) {
                    model.isSpeedUp = false;
                }
                updateSpeed();
            }
            ((Timer)e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    public void activateShield() {
        model.hasShield = true;
        model.shieldTimer = 5; // 5 秒計時
        int currentSession = model.gameSession;

        Timer t = new Timer(1000, e -> {
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                // 如果護盾在 5 秒內已經被撞破了，就提早停止計時器
                if (!model.hasShield) {
                    ((Timer)e.getSource()).stop();
                    return;
                }

                model.shieldTimer--;
                if (model.shieldTimer <= 0) {
                    model.hasShield = false; // 時間到，護盾消失
                    ((Timer)e.getSource()).stop();
                }
                panel.repaint();
            } else {
                ((Timer)e.getSource()).stop();
            }
        });
        t.start();
    }

    private void updateSpeed() {
        int newDelay = (int) (BASE_SPEED / Math.pow(2, speedLayers));
        gameTimer.setDelay(Math.max(50, newDelay));
    }

    // ==================== ⌨️ 鍵盤事件與地圖物件生成 ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ✨ 新增：按下 P 鍵切換暫停狀態
        if (key == KeyEvent.VK_P) {
            model.isPaused = !model.isPaused;

            if (model.isPaused) {
                gameTimer.stop();          // 暫停蛇的移動
                itemRefreshTimer.stop();   // 暫停道具刷新
            } else {
                gameTimer.start();         // 恢復蛇的移動
                itemRefreshTimer.start();  // 恢復道具刷新
            }
            panel.repaint(); // 立刻刷新畫面顯示「暫停中」字樣
            return;          // 暫停事件處理完畢，直接結束此方法
        }

        // 💡 如果目前是暫停狀態，不允許玩家透過方向鍵改變蛇的下一格前進方向
        if (model.isPaused) return;

        boolean isStunned = (stunLayers % 2 != 0);
        int intentDx = 0, intentDy = 0;

        if (key == KeyEvent.VK_UP) {
            intentDx = 0;  intentDy = isStunned ? 1 : -1;
        } else if (key == KeyEvent.VK_DOWN) {
            intentDx = 0;  intentDy = isStunned ? -1 : 1;
        } else if (key == KeyEvent.VK_LEFT) {
            intentDx = isStunned ? 1 : -1;  intentDy = 0;
        } else if (key == KeyEvent.VK_RIGHT) {
            intentDx = isStunned ? -1 : 1;  intentDy = 0;
        } else {
            return;
        }

        // 防自殺判定
        if (intentDx != -dx && intentDy != -dy) {
            nextDx = intentDx;
            nextDy = intentDy;
        }
    }


    private void spawnObstacles() {
        model.obstacles.clear();
        Random r = new Random();

        // 💡 難度梯度修正：
        // 第 1 ~ 2 關：3 顆石頭
        // 第 3 ~ 4 關：4 顆石頭
        // 第 5 ~ 6 關：5 顆石頭... 依此類推，最多不超過 12 顆以免沒路走
        int numObstacles = 3 + ((model.currentLevel - 1) / 2);
        numObstacles = Math.min(numObstacles, 12);

        while (model.obstacles.size() < numObstacles) {
            int rx = r.nextInt(model.GRID_SIZE);
            int ry = r.nextInt(model.GRID_SIZE);
            Point p = new Point(rx, ry);

            boolean overlapSnake = model.checkCollision(rx, ry);
            boolean overlapObstacle = model.obstacles.contains(p);
            boolean isCrossHighway = (rx == 4 || rx == 5 || ry == 4 || ry == 5);

            if (!overlapSnake && !overlapObstacle && !isCrossHighway) {
                model.obstacles.add(p);
            }
        }
    }


    private void spawnItems() {
        model.items.clear();
        Random r = new Random();
        int redAppleTarget = 3 + (model.currentLevel - 1);
        int totalAppleTarget = redAppleTarget + 2;

        while (model.items.size() < totalAppleTarget) {
            int rx = r.nextInt(model.GRID_SIZE);
            int ry = r.nextInt(model.GRID_SIZE);

            boolean itemOverlap = false;
            for (Item existingItem : model.items) {
                if (existingItem.x == rx && existingItem.y == ry) {
                    itemOverlap = true;
                    break;
                }
            }
            boolean obstacleOverlap = model.obstacles.contains(new Point(rx, ry));

            if (!model.checkCollision(rx, ry) && !itemOverlap && !obstacleOverlap) {
                int count = model.items.size();
                if (count < redAppleTarget) {
                    model.items.add(new RedApple(rx, ry));
                } else {
                    // 💡 調整機率：生成 0~99 的隨機數
                    int rand = r.nextInt(100);

                    if (rand < 10) {
                        model.items.add(new GoldApple(rx, ry)); // 🌟 0 ~ 9 共 10% 機率：金色無敵護盾
                    } else if (rand < 55) {
                        model.items.add(new PurpleApple(rx, ry)); // 10 ~ 54 共 45% 機率：紫色二選一
                    } else {
                        model.items.add(new BlueCrystal(rx, ry)); // 55 ~ 99 共 45% 機率：藍色水晶加速
                    }
                }
            }
        }
    }

    private void gameOver() {
        gameTimer.stop();
        itemRefreshTimer.stop();
        model.updateHighScore();

        // 💡 關鍵修正：建立一個 JLabel，並設定成超大、加粗的微軟正黑體
        javax.swing.JLabel messageLabel = new javax.swing.JLabel("遊戲結束！總分: " + model.score);
        messageLabel.setFont(new java.awt.Font("Microsoft JhengHei", java.awt.Font.BOLD, 20)); // 26 是字型大小，可隨意調整
        messageLabel.setForeground(java.awt.Color.DARK_GRAY); // 設定文字顏色

        // 將原本的字串，替換成剛剛做好的大字體 messageLabel
        javax.swing.JOptionPane.showMessageDialog(
                panel,
                messageLabel,
                "遊戲結束",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );

        startGame();
    }
}
