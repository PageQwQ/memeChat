package pageqwq.memechat.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import pageqwq.memechat.MemechatEmoji;
import pageqwq.memechat.MemechatEmojis;
import pageqwq.memechat.font.MemechatGlyph;

/**
 * 补全候选预览：在候选文本前渲染表情包图片。
 */
@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public abstract class SuggestionsListMixin {

    private static final int PREVIEW_WIDTH = 10;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I")
    )
    private int memechat$drawWithPreview(GuiGraphics gui, Font font, String text, int x, int y, int color) {
        // 候选文本形如 "beluga:"，按名字查找表情包并在文本左侧绘制预览
        String name = text.endsWith(":") ? text.substring(0, text.length() - 1) : text;
        var meta = pageqwq.memechat.common.EmojiRegistry.getInstance().byName(name);
        MemechatEmoji emoji = meta == null ? null : MemechatEmojis.getInstance().byId(meta.id());
        if (emoji != null) {
            renderEmoji(emoji, gui, x, y);
            x += PREVIEW_WIDTH;
        }
        return gui.drawString(font, text, x, y, color);
    }

    private static void renderEmoji(MemechatEmoji emoji, GuiGraphics gui, int x, int y) {
        MemechatGlyph glyph = emoji.getGlyph();
        var renderType = glyph.renderType(Font.DisplayMode.NORMAL);
        VertexConsumer builder = gui.bufferSource().getBuffer(renderType);
        float scale = 9f / Math.max(glyph.getAdvance(), MemechatGlyph.HEIGHT);
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0f);
        gui.pose().scale(scale, scale, 0f);
        glyph.render(false, 0f, 0f, gui.pose().last().pose(), builder,
                1f, 1f, 1f, 1f, LightTexture.FULL_BRIGHT);
        gui.pose().popPose();
    }
}
