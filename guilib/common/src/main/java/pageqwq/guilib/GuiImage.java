package pageqwq.guilib;

/**
 * 可绘制图像：由使用者（如 memeChat 的表情 glyph）实现，
 * 让 GUI 控件能绘制任意来源的图片（保持宽高比缩放）。
 */
public interface GuiImage {

    int width();

    int height();

    /** 绘制到指定目标矩形（保持宽高比，居中） */
    void draw(GuiDrawContext ctx, int x, int y, int targetWidth, int targetHeight);
}
