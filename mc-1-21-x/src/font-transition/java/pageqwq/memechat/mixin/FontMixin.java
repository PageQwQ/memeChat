package pageqwq.memechat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.modernui.ModernUICompat;
import pageqwq.memechat.modernui.VanillaGlyphRenderer;

/**
 * ModernUI 兼容（1.21.6~1.21.8）：ModernUI @Overwrite 了 Font.drawInBatch，
 * 拦截后分段渲染——普通文字段走 ModernUI（反射），表情段走 vanilla prepareText
 * 管线。mixin priority 100 保证在 ModernUI 之后应用。
 */
@Mixin(Font.class)
public abstract class FontMixin {

    @Inject(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"), cancellable = true)
    private void memechat$modernuiDrawInBatch(Component text, float x, float y, int color, boolean dropShadow,
                                              Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                              int colorBackground, int packedLight, CallbackInfo ci) {
        if (!ModernUICompat.isActive()) return;
        renderMixed(text.getVisualOrderText(), x, y, color, dropShadow, matrix, source,
                displayMode, colorBackground, packedLight);
        ci.cancel();
    }

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V",
            at = @At("HEAD"), cancellable = true)
    private void memechat$modernuiDrawInBatchFcs(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                                 Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                                 int colorBackground, int packedLight, CallbackInfo ci) {
        if (!ModernUICompat.isActive()) return;
        renderMixed(text, x, y, color, dropShadow, matrix, source,
                displayMode, colorBackground, packedLight);
        ci.cancel();
    }

    /** 混合渲染：普通文字段走 ModernUI，表情段走 vanilla prepareText 管线 */
    private static void renderMixed(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
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
                cx = VanillaGlyphRenderer.render(seq, cx, y, color, dropShadow, matrix, source,
                        displayMode, colorBackground, packedLight);
            } else {
                cx = ModernUICompat.draw(seq, cx, y, color, dropShadow, matrix, source,
                        displayMode, colorBackground, packedLight);
            }
        }
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
