package pageqwq.memechat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.modernui.MixedRenderer;
import pageqwq.memechat.modernui.ModernUICompat;

/**
 * ModernUI 兼容（补充层）：拦截 ModernTextRenderer.drawText 本身——覆盖直接调用
 * ModernUI 渲染的路径（如 immersiveui 的聊天界面），这些路径不经过
 * Font.drawInBatch，FontMixin 拦不到。@Pseudo：未安装 ModernUI 时自动跳过。
 */
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.text.ModernTextRenderer", remap = false)
public abstract class ModernTextRendererMixin {

    @Inject(method = "drawText(Lnet/minecraft/util/FormattedCharSequence;FFFFZILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"), cancellable = true)
    private void memechat$drawText(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                   Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                   int colorBackground, int packedLight, CallbackInfoReturnable<Float> cir) {
        if (!ModernUICompat.isActive()) return;
        if (ModernUICompat.containsMeme(text)) {
            cir.setReturnValue(MixedRenderer.render(text, x, y, color, dropShadow, matrix, source,
                    displayMode, colorBackground, packedLight));
        }
    }
}
