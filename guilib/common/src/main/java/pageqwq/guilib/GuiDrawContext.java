package pageqwq.guilib;

/**
 * 绘制上下文：跨版本包装 GUI 渲染（矩形 / 纹理 / 文字）。
 * 各版本实现内部映射到 GuiGraphics（1.21.x）或 GuiGraphicsExtractor（26.x）。
 */
public interface GuiDrawContext {

    /** 实心矩形（ARGB 颜色） */
    void fill(int x, int y, int width, int height, int colorArgb);

    /** 实心矩形描边 */
    void outline(int x, int y, int width, int height, int colorArgb);

    /** 绘制纹理（拉伸到目标尺寸） */
    void drawTexture(GuiResource texture, int x, int y, int width, int height);

    /** 绘制文字（无阴影） */
    void drawText(String text, int x, int y, int colorArgb);

    /** 绘制文字（带阴影） */
    void drawTextWithShadow(String text, int x, int y, int colorArgb);

    int textWidth(String text);

    int fontHeight();

    /** 屏幕尺寸 */
    int screenWidth();

    int screenHeight();
}
