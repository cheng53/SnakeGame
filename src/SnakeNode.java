public class SnakeNode {
    public int x;
    public int y;
    public String type; // 用來區分 "HEAD" (頭), "BODY" (身), "TAIL" (尾)

    // 建構子：當我們寫 new SnakeNode(10, 10, "HEAD") 時會用到
    public SnakeNode(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
