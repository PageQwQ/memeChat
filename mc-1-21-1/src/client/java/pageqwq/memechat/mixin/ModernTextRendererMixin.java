package pageqwq.memechat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.MemechatConstants;

/**
 * ModernUI 兼容：ModernUI 的 ModernTextRenderer 接管了 Font.drawInBatch，
 * 其字体系统不支持 memeChat 的图片字形。拦截含表情的文本，改用 vanilla
 * renderText 渲染（@Pseudo：未安装 ModernUI 时自动跳过）。
 */
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.text.ModernTextRenderer")
public abstract class ModernTextRendererMixin {

    @Inject(method = "drawText(Lnet/minecraft/util/FormattedCharSequence;FFFFZILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"), cancellable = true)
    private void memechat$drawTextFcs(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                      Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                      int colorBackground, int packedLight, CallbackInfoReturnable<Float> cir) {
        if (containsMeme(text)) {
            cir.setReturnValue(renderVanilla(text, x, y, color, dropShadow, matrix, source,
                    displayMode, colorBackground, packedLight));
        }
    }

    @Inject(method = "drawText(Lnet/minecraft/network/chat/FormattedText;FFFFZILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"), cancellable = true)
    private void memechat$drawTextComponent(FormattedText text, float x, float y, int color, boolean dropShadow,
                                            Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                            int colorBackground, int packedLight, CallbackInfoReturnable<Float> cir) {
        FormattedCharSequence sequence = Language.getInstance().getVisualOrder(text);
        if (containsMeme(sequence)) {
            cir.setReturnValue(renderVanilla(sequence, x, y, color, dropShadow, matrix, source,
                    displayMode, colorBackground, packedLight));
        }
    }

    private static boolean containsMeme(FormattedCharSequence text) {
        final boolean[] found = {false};
        text.accept((index, style, codePoint) -> {
            ResourceLocation font = style.getFont();
            if (font != null && font.equals(MemechatConstants.EMOJI_FONT)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    private static float renderVanilla(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                       Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                       int colorBackground, int packedLight) {
        return ((FontRenderInvoker) (Object) Minecraft.getInstance().font)
                .memechat$invokeRenderText(text, x, y, color, dropShadow, matrix, source,
                        displayMode, colorBackground, packedLight);
    }
}
