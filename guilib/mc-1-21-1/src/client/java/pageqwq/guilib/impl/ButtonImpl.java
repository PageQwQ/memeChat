package pageqwq.guilib.impl;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import pageqwq.guilib.GuiButton;
import pageqwq.guilib.GuiDrawContext;

/** 1.21.1 按钮：包装原版 Button */
public class ButtonImpl implements GuiButton {

    private final Button button;
    private Runnable action = () -> {};

    public ButtonImpl(int x, int y, int width, int height, String text, Runnable onClick) {
        this.action = onClick;
        this.button = Button.builder(Component.literal(text), btn -> action.run())
                .bounds(x, y, width, height)
                .build();
    }

    @Override
    public int x() {
        return button.getX();
    }

    @Override
    public int y() {
        return button.getY();
    }

    @Override
    public int width() {
        return button.getWidth();
    }

    @Override
    public int height() {
        return button.getHeight();
    }

    @Override
    public GuiButton setPosition(int x, int y) {
        button.setPosition(x, y);
        return this;
    }

    @Override
    public GuiButton setSize(int width, int height) {
        button.setWidth(width);
        return this;
    }

    @Override
    public GuiButton setVisible(boolean visible) {
        button.visible = visible;
        return this;
    }

    @Override
    public GuiButton setText(String text) {
        button.setMessage(Component.literal(text));
        return this;
    }

    @Override
    public GuiButton setOnClick(Runnable action) {
        this.action = action;
        return this;
    }

    @Override
    public boolean isVisible() {
        return button.visible;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!button.visible || !button.active) return false;
        if (mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
                && mouseY >= button.getY() && mouseY < button.getY() + button.getHeight()) {
            button.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiDrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
        if (!button.visible) return;
        GuiGraphics graphics = ((DrawContextImpl) ctx).graphics();
        button.render(graphics, mouseX, mouseY, deltaTicks);
    }
}
