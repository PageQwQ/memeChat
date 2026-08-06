package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;

/**
 * 26.1.2 表情包 glyph：复用 BakedSheetGlyph 渲染（纹理/UV/着色全由父类处理）。
 */
public class MemechatGlyph extends BakedSheetGlyph {

    public static final float HEIGHT = 8f;

    public MemechatGlyph(
            GlyphInfo info,
            GlyphRenderTypes glyphRenderTypes,
            GpuTextureView view,
            float u0, float v0, float u1, float v1,
            float left, float top, float right, float bottom
    ) {
        super(info, glyphRenderTypes, view, u0, v0, u1, v1, left, top, right, bottom);
    }
}
