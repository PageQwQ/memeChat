package pageqwq.guilib.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import pageqwq.guilib.GuiDrawContext;
import pageqwq.guilib.GuiResource;

/** 1.21.1 绘制上下文：包装 GuiGraphics */
public class DrawContextImpl implements GuiDrawContext {

    private final GuiGraphics graphics;

    public DrawContextImpl(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    @Override
    public void fill(int x, int y, int width, int height, int colorArgb) {
        graphics.fill(x, y, x + width, y + height, colorArgb);
    }

    @Override
    public void outline(int x, int y, int width, int height, int colorArgb) {
        graphics.fill(x, y, x + width, y + 1, colorArgb);
        graphics.fill(x, y + height - 1, x + width, y + height, colorArgb);
        graphics.fill(x, y, x + 1, y + height, colorArgb);
        graphics.fill(x + width - 1, y, x + width, y + height, colorArgb);
    }

    @Override
    public void drawTexture(GuiResource texture, int x, int y, int width, int height) {
        graphics.blit(
                ResourceLocation.fromNamespaceAndPath(texture.namespace(), texture.path()),
                x, y, width, height, 0, 0
        );
    }

    @Override
    public void drawText(String text, int x, int y, int colorArgb) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, colorArgb, false);
    }

    @Override
    public void drawTextWithShadow(String text, int x, int y, int colorArgb) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, colorArgb, true);
    }

    @Override
    public int textWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    @Override
    public int fontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    @Override
    public int screenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Override
    public int screenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }
}
