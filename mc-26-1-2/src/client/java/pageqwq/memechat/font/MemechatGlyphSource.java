package pageqwq.memechat.font;

import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.RandomSource;

import pageqwq.memechat.MemechatEmoji;
import pageqwq.memechat.MemechatEmojis;

/**
 * 26.1.2 GlyphSource：按码点反查表情包 glyph（码点 = id + 33）。
 */
public class MemechatGlyphSource implements GlyphSource {

    @Override
    public BakedGlyph getGlyph(int codePoint) {
        var emoji = MemechatEmojis.getInstance().byId(codePoint - 33);
        if (emoji == null) return MemechatGlyphs.error();
        return emoji.getGlyph();
    }

    @Override
    public BakedGlyph getRandomGlyph(RandomSource randomSource, int i) {
        return MemechatEmojis.getInstance()
                .getRandom()
                .map(MemechatEmoji::getGlyph)
                .orElse(MemechatGlyphs.error());
    }
}
