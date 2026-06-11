import java.awt.Point;
import java.io.*; // ✨ 匯入 File I/O 函式庫
import java.util.ArrayList;
import java.util.List;

public class GameModel {
    // 遊戲參數
    public final int GRID_SIZE = 10;
    public int score = 0;
    public int highScore = 0;
    public int bodyLength = 2;
    public boolean isStunned = false;
    public boolean isSpeedUp = false;
    public boolean isPaused = false;
    public boolean isRespawning = false;
    public int respawnTimer = 0;

    // 無敵狀態控制變數
    public boolean hasShield = false;
    public int shieldTimer = 0;

    public String currentEntranceDir = "CENTER";
    public int gameSession = 0;

    // 儲存障礙物的清單
    public List<Point> obstacles = new ArrayList<>();
    // 蛇的資料
    public List<SnakeNode> snake = new ArrayList<>();
    // 道具清單
    public List<Item> items = new ArrayList<>();

    // 關卡與通道控制變數
    public int currentLevel = 1;
    public int fruitsCollectedThisLevel = 0;
    public int fruitsRequiredForNextLevel = 3;
    public List<Point> exitCells = new ArrayList<>();

    // ✨ 新增：儲存歷史前 5 名紀錄的清單與存檔路徑
    public List<Integer> leaderboard = new ArrayList<>();
    private final String SAVE_PATH = "resources/save.txt";

    public GameModel() {
        loadHighScore(); // ✨ 啟動時載入歷史排行榜
        reset();
    }

    // 初始化或重新開始遊戲
    public void reset() {
        bodyLength = 2;
        currentLevel = 1;
        fruitsCollectedThisLevel = 0;
        snake.clear();
        items.clear();
        exitCells.clear();
        obstacles.clear();

        isPaused = false;
        isRespawning = false;
        respawnTimer = 0;

        hasShield = false;
        shieldTimer = 0;

        currentEntranceDir = "CENTER";

        snake.add(new SnakeNode(5, 5, "HEAD"));
        snake.add(new SnakeNode(5, 6, "BODY"));
        snake.add(new SnakeNode(5, 7, "BODY"));
        snake.add(new SnakeNode(5, 8, "TAIL"));
    }

    public boolean checkCollision(int x, int y) {
        for (SnakeNode node : snake) {
            if (node.x == x && node.y == y) return true;
        }
        return false;
    }

    // 儲存障礙物的清單
    public List<Point> obstacles = new ArrayList<>();

    // ✨ 新增：特殊地形清單
    public List<Point> iceCells = new ArrayList<>();
    public List<Point> swampCells = new ArrayList<>();

    // ✨ 修改：更新最高分與排行榜機制
    public void updateHighScore() {
        // 1. 將這一局的分數放入排行榜
        leaderboard.add(score);

        // 2. 由大到小排序 (降序)
        leaderboard.sort((a, b) -> b - a);

        // 3. 嚴格限制只保留前 5 名
        while (leaderboard.size() > 5) {
            leaderboard.remove(leaderboard.size() - 1);
        }

        // 4. 同步更新即時看板上的最高紀錄 (第 0 個就是第一名)
        highScore = leaderboard.get(0);

        // 5. 寫入檔案永久保存
        saveHighScore();
    }

    // 📥 ✨ 新增：從檔案讀取多行分數
    private void loadHighScore() {
        File saveFile = new File(SAVE_PATH);
        leaderboard.clear();

        if (!saveFile.exists()) {
            highScore = 0;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    leaderboard.add(Integer.parseInt(line.trim()));
                }
            }
            // 排序並過濾，確保資料無誤
            leaderboard.sort((a, b) -> b - a);
            while (leaderboard.size() > 5) {
                leaderboard.remove(leaderboard.size() - 1);
            }

            highScore = leaderboard.isEmpty() ? 0 : leaderboard.get(0);
        } catch (IOException | NumberFormatException e) {
            System.out.println("提示：讀取排行榜失敗，將重置。原因: " + e.getMessage());
            highScore = 0;
        }
    }

    // 📤 ✨ 新增：將前 5 名依序寫入檔案
    private void saveHighScore() {
        File saveFile = new File(SAVE_PATH);
        File dir = saveFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            for (int s : leaderboard) {
                writer.write(s + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("警告：儲存排行榜失敗！原因: " + e.getMessage());
        }
    }
}