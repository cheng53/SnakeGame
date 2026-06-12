import java.awt.*;
import java.util.Random;

public abstract class Item {
    public int x, y;

    public Item(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void applyEffect(GameModel model, GameController controller);
    public abstract void draw(Graphics g, int gridSize, GamePanel panel);

    protected void drawCenteredImage(Graphics g, Image img, int gridSize, double widthRatio, double heightRatio) {
        int centerX = x * gridSize + (gridSize / 2);
        int centerY = y * gridSize + (gridSize / 2);
        int drawW = (int)(gridSize * widthRatio);
        int drawH = (int)(gridSize * heightRatio);
        int drawX = centerX - (drawW / 2);
        int drawY = centerY - (drawH / 2);
        g.drawImage(img, drawX, drawY, drawW, drawH, null);
    }
}

// 1. 紅蘋果：只用來推進關卡進度，不加分數
class RedApple extends Item {
    public RedApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        model.bodyLength++; // 吃紅蘋果依然會變長
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.redImg != null) {
            drawCenteredImage(g, panel.redImg, gridSize, 0.9, 1.0);
        } else {
            g.setColor(Color.RED);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 2. 金色蘋果：無敵 5 秒（防死保護）
class GoldApple extends Item {
    public GoldApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        controller.activateShield(); // 💡 改為啟用護盾
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.goldImg != null) {
            drawCenteredImage(g, panel.goldImg, gridSize, 0.9, 1);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(x * gridSize + 2, y * gridSize + 2, gridSize - 4, gridSize - 4);
        }
    }
}

// 3. 藍水晶：提供加速效果
class BlueCrystal extends Item {
    public BlueCrystal(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        controller.activateSpeed();
    }
    @Override
    public void draw(Graphics g, int gridSize, GamePanel panel) {
        if (panel.speedImg != null) {
            drawCenteredImage(g, panel.speedImg, gridSize, 0.9, 1.15);
        } else {
            g.setColor(Color.CYAN); // 預設藍色
            g.fillRect(x * gridSize + 4, y * gridSize + 4, gridSize - 8, gridSize - 8);
        }
    }
}

// 4. 紫蘋果：吃下去後隨機 2 選 1（縮短長度 或 暈眩 8 秒）
class PurpleApple extends Item {
    public PurpleApple(int x, int y) { super(x, y); }
    @Override
    public void applyEffect(GameModel model, GameController controller) {
        Random rand = new Random();
        if (rand.nextBoolean()) {
            // 選項 A：縮短長度 (防禦機制：最少保留 0，不扣到負數)
            if (model.bodyLength > 0) {
                model.bodyLength--;
            } else {
                model.bodyLength = -1; // 沒有無敵的話會在移動時暴斃
            }
        } else {
            // 選項 B：控制暈眩反向
            controller.activateStun();
        }
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
