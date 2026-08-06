package pageqwq.memechat.mixin;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.font.MemechatFontSet;
import pageqwq.memechat.MixinHelpers;

/** 阴影/描边渲染时表情包不绘制，只推进 x 坐标 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class StringRenderOutputMixin {
    @Shadow float x;

    @Shadow @Final private boolean dropShadow;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void memechat$noShadowAndGlowOutlineForEmojis(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        if (this.dropShadow || MixinHelpers.shouldSkipEmojiGlyphRender) {
            if (style.getFont().equals(MemechatFontSet.NAME)) {
                this.x += MemechatFontSet.getInstance()
                        .getGlyphInfo(i, false)
                        .getAdvance(style.isBold());
                cir.setReturnValue(true);
            }
        }
    }
}
