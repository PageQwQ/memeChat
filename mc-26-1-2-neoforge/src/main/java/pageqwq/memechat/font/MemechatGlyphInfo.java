package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphInfo;

/**
 * 26.1.2 GlyphInfo：advance = 8px 字高 × 宽高比。
 */
public class MemechatGlyphInfo implements GlyphInfo {

    private final float aspectRatio;

    public MemechatGlyphInfo(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @Override
    public float getAdvance() {
        return MemechatGlyph.HEIGHT * aspectRatio;
    }

    @Override
    public float getAdvance(boolean bold) {
        return getAdvance();
    }

    @Override
    public float getBoldOffset() {
        return 0f;
    }

    @Override
    public float getShadowOffset() {
        return 0f;
    }
}
