package pageqwq.memechat.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.font.MemechatGlyph;
import pageqwq.memechat.MixinHelpers;
import pageqwq.memechat.modernui.ModernUICompat;

/** 表情包渲染修复：取消粗体双渲染、描边渲染跳过、ModernUI 混合渲染回退 */
@Mixin(Font.class)
public abstract class FontMixin {
    @Inject(method = "renderChar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;render(ZFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V",
                    ordinal = 1
            ),
            cancellable = true)
    private void memechat$noBoldDoubleRender(BakedGlyph bakedGlyph, boolean bl, boolean bl2, float f, float g, float h,
                                             Matrix4f matrix4f, VertexConsumer vertexConsumer, float i, float j,
                                             float k, float l, int m, CallbackInfo ci) {
        if (bakedGlyph instanceof MemechatGlyph) ci.cancel();
    }

    @Inject(method = "drawInBatch8xOutline",
            at = @At("HEAD"))
    private void memechat$preGlowOutlineRender(FormattedCharSequence formattedCharSequence, float f, float g, int i, int j,
                                               Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k, CallbackInfo ci) {
        MixinHelpers.shouldSkipEmojiGlyphRender = true;
    }

    @Inject(method = "drawInBatch8xOutline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z",
                    ordinal = 1
            ))
    private void memechat$postGlowOutlineRender(FormattedCharSequence formattedCharSequence, float f, float g, int i, int j,
                                                Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k, CallbackInfo ci) {
        MixinHelpers.shouldSkipEmojiGlyphRender = false;
    }

    // ---- ModernUI 兼容：普通文字段走 ModernUI（亮度/字体一致），表情段走 vanilla renderText ----

    @Inject(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"), cancellable = true)
    private void memechat$modernuiDrawInBatch(Component text, float x, float y, int color, boolean dropShadow,
                                              Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                              int colorBackground, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (!ModernUICompat.isActive()) return;
        FormattedCharSequence sequence = text.getVisualOrderText();
        cir.setReturnValue((int) renderMixed(sequence, x, y, color, dropShadow, matrix, source,
                displayMode, colorBackground, packedLight) + (dropShadow ? 1 : 0));
    }

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"), cancellable = true)
    private void memechat$modernuiDrawInBatchFcs(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                                 Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                                 int colorBackground, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (!ModernUICompat.isActive()) return;
        cir.setReturnValue((int) renderMixed(text, x, y, color, dropShadow, matrix, source,
                displayMode, colorBackground, packedLight) + (dropShadow ? 1 : 0));
    }

    /** 混合渲染：普通文字段走 ModernUI，表情段走 vanilla renderText */
    private static float renderMixed(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
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
                                displayMode, colorBackground, packedLight, true);
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
