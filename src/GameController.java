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
        model.gameSession++;
        model.reset();

        model.isStunned = false;
        model.isSpeedUp = false;
        this.stunLayers = 0;
        this.speedLayers = 0;
        dx = 0; dy = -1; nextDx = 0; nextDy = -1;

        gameTimer.setDelay(BASE_SPEED);
        gameTimer.start();
        itemRefreshTimer.start();
        spawnItems();
    }

    private void moveSnake() {
        dx = nextDx; dy = nextDy;
        int newX = model.snake.get(0).x + dx;
        int newY = model.snake.get(0).y + dy;

        // 1. 基本狀態判定
        boolean isInsideBoard = (newX >= 0 && newX < model.GRID_SIZE && newY >= 0 && newY < model.GRID_SIZE);
        boolean isInsideChannel = false;
        boolean isVictory = false;
        String exitDir = "";

        // 2. 檢查是否進入或通過隨機開啟的通道 (通道長度設為 3 格)
        if (!model.exitCells.isEmpty()) {
            int max = model.GRID_SIZE - 1;
            int boardEnd = model.GRID_SIZE; // 通常是 10

            // 檢查上方通道
            if (model.exitCells.contains(new Point(4, 0))) {
                if ((newX == 4 || newX == 5) && (newY >= -3 && newY <= -1)) {
                    isInsideChannel = true;
                } else if ((newX == 4 || newX == 5) && newY == -4) {
                    isVictory = true; exitDir = "UP";
                }
            }
            // 檢查下方通道
            else if (model.exitCells.contains(new Point(4, max))) {
                if ((newX == 4 || newX == 5) && (newY >= boardEnd && newY <= boardEnd + 2)) {
                    isInsideChannel = true;
                } else if ((newX == 4 || newX == 5) && newY == boardEnd + 3) {
                    isVictory = true; exitDir = "DOWN";
                }
            }
            // 檢查左方通道
            else if (model.exitCells.contains(new Point(0, 4))) {
                if ((newY == 4 || newY == 5) && (newX >= -3 && newX <= -1)) {
                    isInsideChannel = true;
                } else if ((newY == 4 || newY == 5) && newX == -4) {
                    isVictory = true; exitDir = "LEFT";
                }
            }
            // 檢查右方通道
            else if (model.exitCells.contains(new Point(max, 4))) {
                if ((newY == 4 || newY == 5) && (newX >= boardEnd && newX <= boardEnd + 2)) {
                    isInsideChannel = true;
                } else if ((newY == 4 || newY == 5) && newX == boardEnd + 3) {
                    isVictory = true; exitDir = "RIGHT";
                }
            }
        }

        // 3. 觸發通關：將過關方向傳入下一關
        if (isVictory) {
            nextLevel(exitDir);
            return;
        }

        // 4. 死亡判定：既不在主棋盤內，也不在合法的通道內，或者撞到自己
        if ((!isInsideBoard && !isInsideChannel) || model.checkCollision(newX, newY)) {
            gameOver();
            return;
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

            // 判斷是否為紅蘋果，並推進關卡進度
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

        // 8. 防作弊機制：當開啟新關卡，蛇完全從出生通道走出來後，自動關閉通道
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

        // 隨機產生 0 到 3 的數字 (0:上, 1:下, 2:左, 3:右)
        int randomDirection = (int) (Math.random() * 4);

        switch (randomDirection) {
            case 0: // 上方通道入口
                model.exitCells.add(new Point(4, 0));
                model.exitCells.add(new Point(5, 0));
                break;
            case 1: // 下方通道入口
                model.exitCells.add(new Point(4, max));
                model.exitCells.add(new Point(5, max));
                break;
            case 2: // 左方通道入口
                model.exitCells.add(new Point(0, 4));
                model.exitCells.add(new Point(0, 5));
                break;
            case 3: // 右方通道入口
                model.exitCells.add(new Point(max, 4));
                model.exitCells.add(new Point(max, 5));
                break;
        }
    }

    private void nextLevel(String lastExitDir) {
        model.currentLevel++;
        model.fruitsCollectedThisLevel = 0;

        // 1. 先把「分數」和「長度」存起來，避免被 reset() 清除
        int currentScore = model.score;
        int currentLength = model.bodyLength;
        model.reset();
        model.score = currentScore;

        String entranceDir = "";
        if (lastExitDir.equals("UP")) entranceDir = "DOWN";
        else if (lastExitDir.equals("DOWN")) entranceDir = "UP";
        else if (lastExitDir.equals("LEFT")) entranceDir = "RIGHT";
        else if (lastExitDir.equals("RIGHT")) entranceDir = "LEFT";

        model.snake.clear();
        int max = model.GRID_SIZE - 1;
        int boardEnd = model.GRID_SIZE;
        model.bodyLength = currentLength;

        if (entranceDir.equals("UP")) {
            dx = 0; dy = 1; nextDx = 0; nextDy = 1;
            model.snake.add(new SnakeNode(4, -1, "HEAD"));
            model.snake.add(new SnakeNode(4, -2, "BODY"));
            model.snake.add(new SnakeNode(4, -3, "BODY"));
            model.snake.add(new SnakeNode(4, -4, "TAIL"));
            model.exitCells.add(new Point(4, 0));
            model.exitCells.add(new Point(5, 0));
        }
        else if (entranceDir.equals("DOWN")) {
            dx = 0; dy = -1; nextDx = 0; nextDy = -1;
            model.snake.add(new SnakeNode(4, boardEnd, "HEAD"));
            model.snake.add(new SnakeNode(4, boardEnd + 1, "BODY"));
            model.snake.add(new SnakeNode(4, boardEnd + 2, "BODY"));
            model.snake.add(new SnakeNode(4, boardEnd + 3, "TAIL"));
            model.exitCells.add(new Point(4, max));
            model.exitCells.add(new Point(5, max));
        }
        else if (entranceDir.equals("LEFT")) {
            dx = 1; dy = 0; nextDx = 1; nextDy = 0;
            model.snake.add(new SnakeNode(-1, 4, "HEAD"));
            model.snake.add(new SnakeNode(-2, 4, "BODY"));
            model.snake.add(new SnakeNode(-3, 4, "BODY"));
            model.snake.add(new SnakeNode(-4, 4, "TAIL"));
            model.exitCells.add(new Point(0, 4));
            model.exitCells.add(new Point(0, 5));
        }
        else if (entranceDir.equals("RIGHT")) {
            dx = -1; dy = 0; nextDx = -1; nextDy = 0;
            model.snake.add(new SnakeNode(boardEnd, 4, "HEAD"));
            model.snake.add(new SnakeNode(boardEnd + 1, 4, "BODY"));
            model.snake.add(new SnakeNode(boardEnd + 2, 4, "BODY"));
            model.snake.add(new SnakeNode(boardEnd + 3, 4, "TAIL"));
            model.exitCells.add(new Point(max, 4));
            model.exitCells.add(new Point(max, 5));
        }

        spawnItems();
    }

    public void activateStun() {
        stunLayers++;
        model.isStunned = true;
        int currentSession = model.gameSession;
        Timer t = new Timer(8000, e -> {
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                stunLayers--;
                if (stunLayers <= 0) model.isStunned = false;
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
                if (speedLayers <= 0) model.isSpeedUp = false;
                updateSpeed();
            }
            ((Timer)e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    private void updateSpeed() {
        int newDelay = (int) (BASE_SPEED / Math.pow(2, speedLayers));
        gameTimer.setDelay(Math.max(50, newDelay));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        boolean isStunned = (stunLayers % 2 != 0);
        int key = e.getKeyCode();
        int intentDx = 0, intentDy = 0;

        if (key == KeyEvent.VK_UP)    { intentDx = 0;  intentDy = isStunned ? 1 : -1; }
        else if (key == KeyEvent.VK_DOWN)  { intentDx = 0;  intentDy = isStunned ? -1 : 1; }
        else if (key == KeyEvent.VK_LEFT)  { intentDx = isStunned ? 1 : -1;  intentDy = 0; }
        else if (key == KeyEvent.VK_RIGHT) { intentDx = isStunned ? -1 : 1;  intentDy = 0; }
        else return;

        if (intentDx != -dx && intentDy != -dy) {
            nextDx = intentDx;
            nextDy = intentDy;
        }
    }

    private void spawnItems() {
        model.items.clear();
        Random r = new Random();
        while (model.items.size() < 4) {
            int rx = r.nextInt(model.GRID_SIZE);
            int ry = r.nextInt(model.GRID_SIZE);
            boolean itemOverlap = false;
            for (Item existingItem : model.items) {
                if (existingItem.x == rx && existingItem.y == ry) {
                    itemOverlap = true;
                    break;
                }
            }
            if (!model.checkCollision(rx, ry) && !itemOverlap) {
                int count = model.items.size();
                if (count < 3) {
                    model.items.add(new RedApple(rx, ry));
                } else {
                    int rand = r.nextInt(4);
                    if (rand == 0) model.items.add(new GoldApple(rx, ry));
                    else if (rand == 1) model.items.add(new PoisonApple(rx, ry));
                    else if (rand == 2) model.items.add(new StunApple(rx, ry));
                    else model.items.add(new SpeedApple(rx, ry));
                }
            }
        }
    }

    private void gameOver() {
        gameTimer.stop();
        itemRefreshTimer.stop();
        model.updateHighScore();
        javax.swing.JOptionPane.showMessageDialog(panel, "遊戲結束！總分: " + model.score);
        startGame();
    }
}