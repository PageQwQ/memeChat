package pageqwq.guilib.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import pageqwq.guilib.GuiDrawContext;
import pageqwq.guilib.GuiResource;

/**
 * 26.1.2 draw context: wraps GuiGraphicsExtractor.
 * Note: text colors are ARGB since 1.21.6.
 */
public class DrawContextImpl implements GuiDrawContext {

    private final GuiGraphicsExtractor graphics;

    public DrawContextImpl(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }

    @Override
    public void fill(int x, int y, int width, int height, int colorArgb) {
        graphics.fill(x, y, x + width, y + height, colorArgb);
    }

    @Override
    public void outline(int x, int y, int width, int height, int colorArgb) {
        graphics.outline(x, y, x + width, y + height, colorArgb);
    }

    @Override
    public void drawTexture(GuiResource texture, int x, int y, int width, int height) {
        graphics.blit(
                Identifier.fromNamespaceAndPath(texture.namespace(), texture.path()),
                x, y, width, height,
                0f, 0f, 1f, 1f
        );
    }

    @Override
    public void drawText(String text, int x, int y, int colorArgb) {
        graphics.text(font(), text, x, y, colorArgb, false);
    }

    @Override
    public void drawTextWithShadow(String text, int x, int y, int colorArgb) {
        graphics.text(font(), text, x, y, colorArgb, true);
    }

    @Override
    public int textWidth(String text) {
        return font().width(text);
    }

    @Override
    public int fontHeight() {
        return font().lineHeight;
    }

    @Override
    public int screenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Override
    public int screenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }
}
