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

    // ✨ 新增：用來置中並自由調整圖片比例的輔助方法
    protected void drawCenteredImage(Graphics g, Image img, int gridSize, double widthRatio, double heightRatio) {
        // 1. 找出格子的正中心點
        int centerX = x * gridSize + (gridSize / 2);
        int centerY = y * gridSize + (gridSize / 2);

        // 2. 根據傳入的比例計算實際要畫的寬度與高度
        int drawW = (int)(gridSize * widthRatio);
        int drawH = (int)(gridSize * heightRatio);

        // 3. 計算左上角起點
        int drawX = centerX - (drawW / 2);
        int drawY = centerY - (drawH / 2);

        g.drawImage(img, drawX, drawY, drawW, drawH, null);
    }
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
            // 💡 調整這裡：0.9 是稍微縮減寬度，1.15 是把高度拉長 15%，解決上下壓縮的問題
            drawCenteredImage(g, panel.redImg, gridSize, 0.9, 1.15);
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
            drawCenteredImage(g, panel.goldImg, gridSize, 0.9, 1.15);
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
            drawCenteredImage(g, panel.poisonImg, gridSize, 0.9, 1.15);
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
            drawCenteredImage(g, panel.stunImg, gridSize, 0.9, 1.15);
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
            drawCenteredImage(g, panel.speedImg, gridSize, 0.9, 1.15);
        } else {
            g.setColor(Color.CYAN);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}