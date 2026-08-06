package pageqwq.memechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Icon button shown above-right of the chat box; opens the meme picker (1.21.9-1.21.11) */
public class MemeButton extends Button {

    private static final net.minecraft.resources.Identifier ICON =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("memechat", "textures/gui/meme_button.png");

    public MemeButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xcc000000);
        if (isHovered()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x44ffffff);
        }
        int iconSize = 14;
        int offset = (getWidth() - iconSize) / 2;
        graphics.blit(ICON, getX() + offset, getY() + offset, iconSize, iconSize,
                0f, 0f, 1f, 1f);
    }
}
