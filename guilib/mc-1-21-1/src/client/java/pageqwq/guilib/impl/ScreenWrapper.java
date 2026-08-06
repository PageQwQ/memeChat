package pageqwq.guilib.impl;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import pageqwq.guilib.GuiScreen;

/** 1.21.1 屏幕包装：把 GuiScreen 转发到原版 Screen 生命周期 */
public class ScreenWrapper extends Screen {

    private final GuiScreen delegate;

    public ScreenWrapper(GuiScreen delegate) {
        super(Component.empty());
        this.delegate = delegate;
    }

    public GuiScreen delegate() {
        return delegate;
    }

    @Override
    protected void init() {
        delegate.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        super.render(graphics, mouseX, mouseY, deltaTicks);
        delegate.render(new DrawContextImpl(graphics), mouseX, mouseY, deltaTicks);
    }

    @Override
    public void tick() {
        delegate.tick();
    }

    @Override
    public void onClose() {
        delegate.onClose();
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (delegate.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (delegate.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (delegate.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (delegate.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (delegate.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
