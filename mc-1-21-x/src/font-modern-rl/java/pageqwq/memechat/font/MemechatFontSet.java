package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Set;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.MemechatEmoji;
import pageqwq.memechat.MemechatEmojis;

/**
 * 26.1.2 表情包字体集：覆盖 source() 返回自定义 GlyphSource。
 */
public class MemechatFontSet extends FontSet {

    public static final ResourceLocation NAME = MemechatConstants.EMOJI_FONT;

    private static MemechatFontSet instance;

    private final MemechatGlyphSource glyphSource = new MemechatGlyphSource();

    public MemechatFontSet(GlyphStitcher stitcher) {
        super(stitcher);
        instance = this;
    }

    @Override
    public GlyphSource source(boolean fishy) {
        return glyphSource;
    }

    @Override
    public EffectGlyph whiteGlyph() {
        return MemechatGlyphs.white();
    }

    @Override
    public BakedGlyph getRandomGlyph(RandomSource randomSource, int i) {
        return MemechatEmojis.getInstance()
                .getRandom()
                .map(MemechatEmoji::getGlyph)
                .orElse(MemechatGlyphs.error());
    }

    @Override
    public void reload(List<GlyphProvider.Conditional> list, Set<FontOption> set) {
    }

    @Override
    public void close() {
    }

    public static MemechatFontSet getInstance() {
        return instance;
    }
}
