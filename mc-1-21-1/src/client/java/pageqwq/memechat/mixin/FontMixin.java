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

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.font.MemechatGlyph;
import pageqwq.memechat.MixinHelpers;
import pageqwq.memechat.modernui.MixedRenderer;
import pageqwq.memechat.modernui.ModernUICompat;

/** 表情包渲染修复：取消粗体双渲染、描边渲染跳过、ModernUI 回退 */
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

    // 用 RETURN 而非 INVOKE ordinal：其他渲染 mod（如 iris）可能改写方法内字节码导致注入目标消失
    @Inject(method = "drawInBatch8xOutline",
            at = @At("RETURN"))
    private void memechat$postGlowOutlineRender(FormattedCharSequence formattedCharSequence, float f, float g, int i, int j,
                                                Matrix4f matrix4f, MultiBufferSource multiBufferSource, int k, CallbackInfo ci) {
        MixinHelpers.shouldSkipEmojiGlyphRender = false;
    }

    // ---- ModernUI 兼容：默认走 ModernUI 渲染；文本含 memeChat 表情时
    //     直接改用 vanilla renderText 绘制（ModernUI 布局缓存会绕过异常方案，需主动检测） ----

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

    private static float renderMixed(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                     Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                     int colorBackground, int packedLight) {
        return MixedRenderer.render(text, x, y, color, dropShadow, matrix, source,
                displayMode, colorBackground, packedLight);
    }
}
