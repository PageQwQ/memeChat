package pageqwq.memechat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;

import pageqwq.guilib.GuiDrawContext;
import pageqwq.guilib.GuiImage;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.font.MemechatGlyph;
import pageqwq.memechat.DrawContextAccessor;

/** 1.21.1 meme image provider: renders meme glyphs as GuiImage */
public class MemechatEmojiImage implements GuiImage {

    private final MemechatEmoji emoji;

    public MemechatEmojiImage(MemechatEmoji emoji) {
        this.emoji = emoji;
    }

    /** Provider entry point for the meme picker */
    public static GuiImage imageFor(Emoji emoji) {
        MemechatEmoji runtime = MemechatEmojis.getInstance().byId(emoji.id());
        return runtime == null ? null : new MemechatEmojiImage(runtime);
    }

    @Override
    public int width() {
        return 16;
    }

    @Override
    public int height() {
        return 16;
    }

    @Override
    public void draw(GuiDrawContext ctx, int x, int y, int targetWidth, int targetHeight) {
        MemechatGlyph glyph = emoji.getGlyph();
        var graphics = DrawContextAccessor.graphics(ctx);
        var renderType = glyph.renderType(Font.DisplayMode.NORMAL);
        VertexConsumer builder = graphics.bufferSource().getBuffer(renderType);
        float scale = targetHeight / Math.max(glyph.getAdvance(), MemechatGlyph.HEIGHT);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0f);
        graphics.pose().scale(scale, scale, 0f);
        glyph.render(false, 0f, 0f, graphics.pose().last().pose(), builder,
                1f, 1f, 1f, 1f, LightTexture.FULL_BRIGHT);
        graphics.pose().popPose();
    }
}
