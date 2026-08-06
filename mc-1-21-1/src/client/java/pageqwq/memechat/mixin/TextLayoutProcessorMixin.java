package pageqwq.memechat.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.MemechatConstants;

/**
 * ModernUI 兼容：ModernUI 的字体系统不支持 memeChat 的图片字形。
 * 在布局首个检测点发现 memechat:emoji 样式时抛异常，让 Font.drawInBatch
 * 的拦截逻辑回退到 vanilla 渲染（@Pseudo：未安装 ModernUI 时自动跳过）。
 */
@Pseudo
@Mixin(targets = "icyllis.modernui.mc.text.TextLayoutProcessor", remap = false)
public abstract class TextLayoutProcessorMixin {

    @Inject(method = "lambda$new$1", at = @At("HEAD"))
    private void memechat$checkMeme(int index, Style style, int codePoint,
                                    CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation font = style.getFont();
        if (font != null && font.equals(MemechatConstants.EMOJI_FONT)) {
            throw new MemechatFontException();
        }
    }
}
