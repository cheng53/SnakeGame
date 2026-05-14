import java.awt.*;

// 1. 父類別：定義所有道具的共同特徵
public abstract class Item {
    public int x, y;

    public Item(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 定義效果：由子類別實作
    public abstract void applyEffect(GameModel model, GameController controller);

    // 定義繪製：支援圖片載入，若無圖片則回退至顏色圓圈
    public abstract void draw(Graphics g, int gridSize, GamePanel panel);
}

// 2. 紅蘋果：長度+1, 分數+1
class RedApple extends Item {
    public RedApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        model.bodyLength++;
        model.score += 1;
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.redImg != null) {
            g.drawImage(panel.redImg, x * gridSize, y * gridSize, gridSize, gridSize, null);
        } else {
            g.setColor(Color.RED);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 3. 金蘋果：分數+10
class GoldApple extends Item {
    public GoldApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        model.score += 10;
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.goldImg != null) {
            g.drawImage(panel.goldImg, x * gridSize, y * gridSize, gridSize, gridSize, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 4. 毒蘋果：身體長度-1
class PoisonApple extends Item {
    public PoisonApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        model.bodyLength--;
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.poisonImg != null) {
            g.drawImage(panel.poisonImg, x * gridSize, y * gridSize, gridSize, gridSize, null);
        } else {
            g.setColor(new Color(128, 0, 128)); // 紫色
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 5. 暈眩蘋果：控制反向 8 秒
class StunApple extends Item {
    public StunApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        controller.activateStun();
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.stunImg != null) {
            g.drawImage(panel.stunImg, x * gridSize, y * gridSize, gridSize, gridSize, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 6. 加速蘋果：移動速度*2, 持續 8 秒
class SpeedApple extends Item {
    public SpeedApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        controller.activateSpeed();
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.speedImg != null) {
            g.drawImage(panel.speedImg, x * gridSize, y * gridSize, gridSize, gridSize, null);
        } else {
            g.setColor(Color.CYAN); // 淺藍色
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}
