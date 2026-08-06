package pageqwq.memechat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

import pageqwq.guilib.GuiDrawContext;
import pageqwq.guilib.GuiImage;
import pageqwq.memechat.common.Emoji;

/**
 * 26.1.2 meme image: renders the meme glyph through the font pipeline
 * (code point + memechat:emoji font style), scaled to the target size.
 */
public class MemechatEmojiImage implements GuiImage {

    private final MemechatEmoji emoji;

    private MemechatEmojiImage(MemechatEmoji emoji) {
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
        Font font = Minecraft.getInstance().font;
        Component component = Component.literal(Character.toString(emoji.meta().codePoint()))
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(MemechatConstants.EMOJI_FONT)));
        Matrix4f matrix = new Matrix4f()
                .translate(x, y, 0f)
                .scale(targetWidth / 8f, targetHeight / 8f, 1f);
        font.drawInBatch(component, 0f, 0f, 0xFFFFFFFF, false, matrix,
                Minecraft.getInstance().renderBuffers().bufferSource(),
                Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }
}
