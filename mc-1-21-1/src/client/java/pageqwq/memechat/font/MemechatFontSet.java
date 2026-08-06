package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.MemechatEmoji;
import pageqwq.memechat.MemechatEmojis;

/**
 * 表情包字体集（移植自 emogg EmojiFontSet）。
 * 码点映射：codePoint = id + 33（仅在 memechat:emoji 字体内解释）。
 */
public class MemechatFontSet extends FontSet {

    public static final ResourceLocation NAME = MemechatConstants.EMOJI_FONT;

    private static MemechatFontSet instance;

    public MemechatFontSet(TextureManager textureManager) {
        super(textureManager, MemechatConstants.EMOJI_FONT);
        instance = this;
    }

    public static int codePointToId(int codePoint) {
        return codePoint - 33;
    }

    public static int idToCodePoint(int id) {
        return id + 33;
    }

    @Override
    public @NotNull GlyphInfo getGlyphInfo(int iChar, boolean bl) {
        return (GlyphInfo) getGlyph(iChar);
    }

    @Override
    public @NotNull BakedGlyph getGlyph(int iChar) {
        var emoji = MemechatEmojis.getInstance().byId(codePointToId(iChar));
        if (emoji == null) return MemechatGlyph.ERROR;
        return emoji.getGlyph();
    }

    @Override
    public @NotNull BakedGlyph whiteGlyph() {
        return MemechatGlyph.EMPTY;
    }

    @Override
    public @NotNull BakedGlyph getRandomGlyph(GlyphInfo glyphInfo) {
        return MemechatEmojis.getInstance()
                .getRandom()
                .map(MemechatEmoji::getGlyph)
                .orElse(MemechatGlyph.ERROR);
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
