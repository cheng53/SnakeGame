import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout); // 用來裝「選單」與「遊戲」的容器
    private GameModel model;
    private GamePanel gamePanel;
    private GameController controller;

    public MainFrame() {
        setTitle("期末專案：貪吃蛇PRO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 初始化資料與控制器
        model = new GameModel();
        gamePanel = new GamePanel(model);
        controller = new GameController(model, gamePanel);

        // 建立選單面板
        MenuPanel menuPanel = new MenuPanel(this);

        // 將選單與遊戲面板放入 CardLayout 容器中
        container.add(menuPanel, "MENU");
        container.add(gamePanel, "GAME");

        add(container);
        addKeyListener(controller); // 讓視窗本身監聽鍵盤輸入
        setFocusable(true);

        pack(); // 根據內含面板的大小自動調整視窗大小
        setLocationRelativeTo(null); // 視窗居中
        setVisible(true);
    }

    // 提供一個方法讓 MenuPanel 呼叫，用來切換到遊戲
    public void switchToGame() {
        cardLayout.show(container, "GAME");
        this.requestFocus(); // 切換後強制視窗抓回鍵盤焦點，蛇才能轉彎
        controller.startGame();
    }

    public static void main(String[] args) {
        // 啟動程式
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
    // ✨ 新增：提供一個方法讓 MenuPanel 可以獲取資料模型以讀取排行榜
    public GameModel getGameModel() {
        return model;
    }
}
