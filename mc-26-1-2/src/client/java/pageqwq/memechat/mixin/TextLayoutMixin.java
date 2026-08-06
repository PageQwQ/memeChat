package pageqwq.memechat.mixin;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.MemechatEmojis;
import pageqwq.memechat.modernui.ModernUICompat;

/**
 * 26.x ModernUI 兼容：ModernUI 布局输出无原始码点，但 TextLayout 保留
 * mTextBuf（原始字符）。GUI 文本经 prepareTextWithDensity 生成
 * ModernPreparedText（glyphs 引用 mBakedGlyphs），按 mTextBuf 的私有区
 * 码点（id + 0xE000）替换对应 glyph 为 memeChat 的表情 glyph。
 */
@Mixin(targets = "icyllis.modernui.mc.text.TextLayout", remap = false)
public abstract class TextLayoutMixin {

    @Shadow private char[] mTextBuf;

    @Shadow private BakedGlyph[] mBakedGlyphs;

    @Inject(method = "prepareTextWithDensity(FFIZIFIFF)Licyllis/modernui/mc/text/ModernPreparedText;",
            at = @At("HEAD"))
    private void memechat$replaceMemeGlyphs(float density, float shadowOffset, int color, boolean dropShadow,
                                            int bgColor, float xAdj, int mode, float yAdj, float uniformScale,
                                            CallbackInfoReturnable<Object> cir) {
        if (!ModernUICompat.isActive()) return;
        char[] textBuf = mTextBuf;
        BakedGlyph[] glyphs = mBakedGlyphs;
        if (textBuf == null || glyphs == null) return;
        for (int i = 0; i < textBuf.length && i < glyphs.length; i++) {
            char code = textBuf[i];
            if (code >= 0xE000) {
                var emoji = MemechatEmojis.getInstance().byId(code - 0xE000);
                if (emoji != null) {
                    BakedGlyph memeGlyph = emoji.getGlyph();
                    if (glyphs[i] != memeGlyph) {
                        glyphs[i] = memeGlyph;
                    }
                }
            }
        }
    }
}
