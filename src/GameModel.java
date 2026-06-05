import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GameModel {
    // 遊戲參數
    public final int GRID_SIZE = 10; // 10*10 的格子
    public int score = 0;
    public int highScore = 0;
    public int bodyLength = 2; // 初始身體長度 (不含頭尾)
    public boolean isStunned = false;
    public boolean isSpeedUp = false;
    public int gameSession = 0; // 每次開始新遊戲就 +1

    // ✨ 新增：儲存障礙物的清單
    public List<Point> obstacles = new ArrayList<>();

    // 蛇的資料：List 的第 0 個是頭，最後一個是尾
    public List<SnakeNode> snake = new ArrayList<>();
    // 道具清單
    public List<Item> items = new ArrayList<>();

    // --- 新增：關卡與通道控制變數 ---
    public int currentLevel = 1;
    public int fruitsCollectedThisLevel = 0;
    public int fruitsRequiredForNextLevel = 3;
    public List<Point> exitCells = new ArrayList<>();
    // --------------------------------

    public GameModel() {
        reset();
    }

    // 初始化或重新開始遊戲
    public void reset() {
        score = 0;
        bodyLength = 2;
        currentLevel = 1;
        fruitsCollectedThisLevel = 0;
        snake.clear();
        items.clear();
        exitCells.clear(); // 每次重置遊戲狀態時，確保通道也會被完全清空
        obstacles.clear();

        // 初始位置：頭在 (5,5)，身體在下方
        snake.add(new SnakeNode(5, 5, "HEAD"));
        snake.add(new SnakeNode(5, 6, "BODY"));
        snake.add(new SnakeNode(5, 7, "BODY"));
        snake.add(new SnakeNode(5, 8, "TAIL"));
    }

    // 檢查座標是否撞到蛇全身
    public boolean checkCollision(int x, int y) {
        for (SnakeNode node : snake) {
            if (node.x == x && node.y == y) return true;
        }
        return false;
    }

    // 更新最高分
    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }
}