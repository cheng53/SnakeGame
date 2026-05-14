import javax.swing.Timer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class GameController extends KeyAdapter {
    private GameModel model;
    private GamePanel panel;

    // 核心計時器
    private Timer gameTimer;      // 控制蛇移動
    private Timer survivalTimer;  // 每 10 秒縮短身體
    private Timer itemRefreshTimer; // 專門負責每 5 秒刷新道具
    // 狀態層級
    private int stunLayers = 0;
    private int speedLayers = 0;
    private final int BASE_SPEED = 200; // 基礎移動速度

    // 移動方向
    private int dx = 0, dy = -1;
    private int nextDx = 0, nextDy = -1;

    public GameController(GameModel model, GamePanel panel) {
        this.model = model;
        this.panel = panel;

        gameTimer = new Timer(BASE_SPEED, e -> moveSnake());

        // 身體縮短計時器：每 10 秒觸發一次
        survivalTimer = new Timer(10000, e -> {
            if (model.bodyLength > 0) {
                model.bodyLength--;
                panel.repaint();
            } else {
                gameOver();
            }
        });

        // 道具刷新計時器：每 5 秒觸發一次
        itemRefreshTimer = new Timer(5000, e -> {
            spawnItems();
            panel.repaint();
        });
    }

    public void startGame() {
        // 1. 停止舊的計時器
        gameTimer.stop();
        survivalTimer.stop();
        itemRefreshTimer.stop(); // 停止舊的
        model.gameSession++;// 關鍵：每一局新遊戲都有獨一無二的編號
        // 2. 重置數據與方向
        model.reset();
        model.isStunned = false;
        model.isSpeedUp = false;
        this.stunLayers = 0;
        this.speedLayers = 0;
        dx = 0; dy = -1; nextDx = 0; nextDy = -1;
        stunLayers = 0;
        speedLayers = 0;

        // 3. 重置速度延遲
        gameTimer.setDelay(BASE_SPEED);

        // 4. 重新啟動
        gameTimer.start();
        survivalTimer.start();
        itemRefreshTimer.start(); // 啟動新的
        spawnItems();
    }

    private void moveSnake() {
        dx = nextDx; dy = nextDy;
        int newX = model.snake.get(0).x + dx;
        int newY = model.snake.get(0).y + dy;

        // 死亡判定：撞牆、撞自己
        if (newX < 0 || newX >= 20 || newY < 0 || newY >= 20 || model.checkCollision(newX, newY)) {
            gameOver();
            return;
        }

        // 新增頭部
        model.snake.add(0, new SnakeNode(newX, newY, "HEAD"));

        // 檢查進食
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
        }

        // 維持長度邏輯
        while (model.snake.size() > model.bodyLength + 2) {
            model.snake.remove(model.snake.size() - 1);
        }

        // 更新節點類型
        for (int i = 1; i < model.snake.size() - 1; i++) model.snake.get(i).type = "BODY";
        if (model.snake.size() > 1) model.snake.get(model.snake.size() - 1).type = "TAIL";

        panel.repaint();
    }

    // --- 道具效果疊加處理 ---

    public void activateStun() {
        stunLayers++;
        model.isStunned = true; // 1. 當吃到時，把 Model 的暈眩開關打開
        int currentSession = model.gameSession; // 記錄這顆蘋果是在哪一局被吃掉的
        Timer t = new Timer(8000, e -> {
            // 只有「當前遊戲局數」等於「蘋果產生的局數」時，才執行還原
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                stunLayers--;
                // 2. 當層級回到 0，代表 8 秒結束且沒吃到新的，關閉開關
                if (stunLayers <= 0) model.isStunned = false;
            }
            ((Timer)e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    public void activateSpeed() {
        speedLayers++;
        model.isSpeedUp = true; // 1. 當吃到時，把 Model 的加速開關打開
        int currentSession = model.gameSession; // 記錄這顆蘋果是在哪一局被吃掉的
        updateSpeed();

        Timer t = new Timer(8000, e -> {
            if (model.gameSession == currentSession && gameTimer.isRunning()) {
                speedLayers--;
                // 2. 當層級回到 0，關閉開關
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
        gameTimer.setDelay(Math.max(50, newDelay)); // 限制最高速度以免過快
    }

    // --- 鍵盤控制 ---
    @Override
    public void keyPressed(KeyEvent e) {
        boolean isStunned = (stunLayers % 2 != 0);
        int key = e.getKeyCode();

        // 先計算出「玩家意圖」產生的方向
        int intentDx = 0;
        int intentDy = 0;

        if (key == KeyEvent.VK_UP)    { intentDx = 0;  intentDy = isStunned ? 1 : -1; }
        else if (key == KeyEvent.VK_DOWN)  { intentDx = 0;  intentDy = isStunned ? -1 : 1; }
        else if (key == KeyEvent.VK_LEFT)  { intentDx = isStunned ? 1 : -1;  intentDy = 0; }
        else if (key == KeyEvent.VK_RIGHT) { intentDx = isStunned ? -1 : 1;  intentDy = 0; }
        else { return; } // 按到其他鍵不處理

        // 關鍵修復：只有當「轉換後的方向」不是目前的「反方向」時，才允許轉彎
        // 這樣就算暈眩，蛇也不會瞬間 180 度回頭自殺
        if (intentDx != -dx && intentDy != -dy) {
            nextDx = intentDx;
            nextDy = intentDy;
        }
    }
    private void spawnItems() {
        model.items.clear();
        Random r = new Random();
        while (model.items.size() < 5) {
            int rx = r.nextInt(20);
            int ry = r.nextInt(20);
            // 檢查座標是否重疊 (蛇身或其他已生成的道具)
            boolean itemOverlap = false;
            for (Item existingItem : model.items) {
                if (existingItem.x == rx && existingItem.y == ry) {
                    itemOverlap = true;
                    break;
                }
            }
            if (!model.checkCollision(rx, ry) && !itemOverlap) {
                int count = model.items.size();
                // 前 3 顆固定為紅蘋果
                if (count < 3) {
                    model.items.add(new RedApple(rx, ry));
                } else {
                    // 後面 2 顆隨機生成道具
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
        survivalTimer.stop();
        itemRefreshTimer.stop(); // 停止
        model.updateHighScore();
        javax.swing.JOptionPane.showMessageDialog(panel, "遊戲結束！總分: " + model.score);
        startGame();
    }
}
