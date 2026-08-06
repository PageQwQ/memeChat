package pageqwq.memechat.modernui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.mixin.FontRenderInvoker;

/**
 * 混合渲染：普通文字段走 ModernUI（字体/亮度一致），表情段走 vanilla renderText。
 * 供 FontMixin（Font.drawInBatch 层）与 ModernTextRendererMixin（直接调 ModernUI
 * 的路径，如 immersiveui 的聊天渲染）共用。
 */
public final class MixedRenderer {

    private MixedRenderer() {}

    public static float render(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                               Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                               int colorBackground, int packedLight) {
        List<Segment> segments = new ArrayList<>();
        text.accept((index, style, codePoint) -> {
            boolean meme = style.getFont() != null && style.getFont().equals(MemechatConstants.EMOJI_FONT);
            Segment last = segments.isEmpty() ? null : segments.get(segments.size() - 1);
            if (last == null || last.meme != meme || !last.style.equals(style)) {
                segments.add(new Segment(style, meme));
                last = segments.get(segments.size() - 1);
            }
            last.text.appendCodePoint(codePoint);
            return true;
        });
        float cx = x;
        for (Segment s : segments) {
            FormattedCharSequence seq = Component.literal(s.text.toString())
                    .withStyle(s.style).getVisualOrderText();
            if (s.meme) {
                cx = ((FontRenderInvoker) (Object) Minecraft.getInstance().font)
                        .memechat$invokeRenderText(seq, cx, y, color, dropShadow, matrix, source,
                                displayMode, colorBackground, packedLight);
            } else {
                cx = ModernUICompat.draw(seq, cx, y, color, dropShadow, matrix, source,
                        displayMode, colorBackground, packedLight);
            }
        }
        return cx;
    }

    private static final class Segment {
        final net.minecraft.network.chat.Style style;
        final boolean meme;
        final StringBuilder text = new StringBuilder();

        Segment(net.minecraft.network.chat.Style style, boolean meme) {
            this.style = style;
            this.meme = meme;
        }
    }
}
