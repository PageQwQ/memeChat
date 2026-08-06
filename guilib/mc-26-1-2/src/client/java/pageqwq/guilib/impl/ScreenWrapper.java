package pageqwq.guilib.impl;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import pageqwq.guilib.GuiScreen;

/** 26.1.2 screen wrapper: forwards GuiScreen onto the vanilla Screen lifecycle */
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
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
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (delegate.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (delegate.mouseScrolled(mouseX, mouseY, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (delegate.keyPressed(event.key(), 0, event.modifiers())) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (delegate.charTyped(event.codepointAsString().charAt(0), 0)) return true;
        return super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
