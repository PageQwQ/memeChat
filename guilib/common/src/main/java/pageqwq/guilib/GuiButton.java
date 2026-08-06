package pageqwq.guilib;

/**
 * 按钮控件：跨版本包装（1.21.x 原版 Button / 26.x 对应控件）。
 */
public interface GuiButton {

    int x();

    int y();

    int width();

    int height();

    GuiButton setPosition(int x, int y);

    GuiButton setSize(int width, int height);

    GuiButton setVisible(boolean visible);

    GuiButton setText(String text);

    GuiButton setOnClick(Runnable action);

    boolean isVisible();

    /** 鼠标点击命中（返回 true 表示已消费） */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    void render(GuiDrawContext ctx, int mouseX, int mouseY, float deltaTicks);
}
