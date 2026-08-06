package pageqwq.memechat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import pageqwq.memechat.MemechatEmoji;
import pageqwq.memechat.MemechatEmojis;
import net.minecraft.network.chat.FontDescription;
import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.common.EmojiRegistry;

/**
 * Completion preview: renders the meme image left of the candidate text (1.21.9-1.21.11).
 */
@Mixin(targets = "net.minecraft.client.gui.components.CommandSuggestions$SuggestionsList")
public abstract class SuggestionsListMixin {

    private static final int PREVIEW_WIDTH = 10;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V")
    )
    private void memechat$drawWithPreview(GuiGraphics gui, Font font, String text, int x, int y, int color) {
        // candidate text looks like "beluga:", look up the meme and draw the preview
        String name = text.endsWith(":") ? text.substring(0, text.length() - 1) : text;
        var meta = EmojiRegistry.getInstance().byName(name);
        MemechatEmoji emoji = meta == null ? null : MemechatEmojis.getInstance().byId(meta.id());
        if (emoji != null) {
            renderEmoji(emoji, gui, x, y);
            x += PREVIEW_WIDTH;
        }
        gui.drawString(font, text, x, y, color);
    }

    private static void renderEmoji(MemechatEmoji emoji, GuiGraphics gui, int x, int y) {
        Font font = Minecraft.getInstance().font;
        Component component = Component.literal(Character.toString(emoji.meta().codePoint()))
                .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(MemechatConstants.EMOJI_FONT)));
        var pose = gui.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(9f / 8f, 9f / 8f);
        gui.drawString(font, component, 0, 0, 0xFFFFFFFF, false);
        pose.popMatrix();
    }
}
