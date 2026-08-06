package pageqwq.memechat.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;

import pageqwq.memechat.atlas.EmojiTexture;

/**
 * 静态占位 glyph（ERROR / 空白 white）：懒创建 1x1 透明纹理。
 */
public final class MemechatGlyphs {

    private static volatile BakedGlyph error;
    private static volatile MemechatGlyph white;

    private MemechatGlyphs() {}

    public static BakedGlyph error() {
        BakedGlyph g = error;
        if (g == null) {
            synchronized (MemechatGlyphs.class) {
                if (error == null) error = createEmptyGlyph("memechat_error");
                g = error;
            }
        }
        return g;
    }

    public static MemechatGlyph white() {
        MemechatGlyph g = white;
        if (g == null) {
            synchronized (MemechatGlyphs.class) {
                if (white == null) white = createEmptyGlyph("memechat_white");
                g = white;
            }
        }
        return g;
    }

    private static MemechatGlyph createEmptyGlyph(String label) {
        try (NativeImage image = new NativeImage(1, 1, false)) {
            EmojiTexture texture = EmojiTexture.create(label, image);
            return new MemechatGlyph(
                    MemechatRenderTypesHolder.emojiTextured(),
                    texture.view,
                    1f
            );
        }
    }
}
