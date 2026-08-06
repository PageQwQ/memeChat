package pageqwq.memechat.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Icon button shown above-right of the chat box; opens the meme picker (1.21.2-1.21.8) */
public class MemeButton extends Button {

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath("memechat", "textures/gui/meme_button.png");

    public MemeButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xcc000000);
        if (isHovered()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x44ffffff);
        }
        int iconSize = 14;
        int offset = (getWidth() - iconSize) / 2;
        graphics.blit(RenderType::guiTextured, ICON, getX() + offset, getY() + offset,
                0f, 0f, iconSize, iconSize, iconSize, iconSize);
    }
}
