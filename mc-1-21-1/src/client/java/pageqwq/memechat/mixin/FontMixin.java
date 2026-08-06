package pageqwq.memechat.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pageqwq.memechat.font.MemechatGlyph;
import pageqwq.memechat.MixinHelpers;

/** 表情包渲染修复：取消粗体双渲染、描边渲染跳过 */
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
}
