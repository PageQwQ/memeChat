package pageqwq.guilib;

/**
 * 屏幕抽象：跨版本包装 Screen（1.21.x render / 26.x extractRenderState）。
 * 由 Gui.openScreen 打开，生命周期：init → render/输入事件 → onClose。
 */
public interface GuiScreen {

    /** 屏幕初始化（可在此布局） */
    default void init() {}

    /** 每帧渲染 */
    void render(GuiDrawContext ctx, int mouseX, int mouseY, float deltaTicks);

    /** 每 tick 更新 */
    default void tick() {}

    default void onClose() {}

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    int width();

    int height();
}
