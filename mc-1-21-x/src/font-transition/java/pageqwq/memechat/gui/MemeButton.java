package pageqwq.memechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Icon button shown above-right of the chat box; opens the meme picker (1.21.2-1.21.8) */
public class MemeButton extends Button {

    public MemeButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xcc000000);
        if (isHovered()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x44ffffff);
        }
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(getX() + 4, getY() + 4);
        pose.scale(1.5f, 1.5f);
        graphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "☺",
                0, 0, 0xffffffff, true);
        pose.popMatrix();
    }
}
