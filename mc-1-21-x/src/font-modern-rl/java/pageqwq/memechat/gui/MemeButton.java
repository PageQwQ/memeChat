package pageqwq.memechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Icon button shown above-right of the chat box; opens the meme picker (1.21.9-1.21.10) */
public class MemeButton extends Button {

    private static final net.minecraft.resources.ResourceLocation ICON =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("memechat", "textures/gui/meme_button.png");

    public MemeButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xcc000000);
        if (isHovered()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x44ffffff);
        }
        // GuiGraphics.blit(RL, x, y, w, h, ...) 在 1.21.9+ 有参数错位 bug（矩形变成 (x, x+w)-(y, y+h)），
        // 直接构造 BlitRenderState 以正确坐标提交
        int iconSize = 14;
        int offset = (getWidth() - iconSize) / 2;
        int x = getX() + offset;
        int y = getY() + offset;
        var texture = Minecraft.getInstance().getTextureManager().getTexture(ICON);
        var blit = new net.minecraft.client.gui.render.state.BlitRenderState(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                net.minecraft.client.gui.render.TextureSetup.singleTexture(texture.getTextureView()),
                new org.joml.Matrix3x2f(graphics.pose()),
                x, y, x + iconSize, y + iconSize,
                0f, 1f, 0f, 1f,
                -1,
                ((pageqwq.memechat.mixin.GuiGraphicsAccessor) (Object) graphics).memechat$scissorStack().peek()
        );
        ((pageqwq.memechat.mixin.GuiGraphicsAccessor) (Object) graphics).memechat$guiRenderState().submitGuiElement(blit);
    }
}
