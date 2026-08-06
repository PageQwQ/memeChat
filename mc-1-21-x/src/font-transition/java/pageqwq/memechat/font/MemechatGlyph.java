package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * 过渡式（1.21.6~1.21.9）表情包 glyph：扩展 BakedGlyph 类（渲染由父类完成），
 * 自身实现 GlyphInfo 提供排版宽度。BakedGlyph 构造参数顺序：u0,u1,v0,v1,left,right,up,down。
 */
public class MemechatGlyph extends BakedGlyph implements GlyphInfo {

    public static final float HEIGHT = 8f;

    private final float aspectRatio;

    public MemechatGlyph(
            GlyphRenderTypes glyphRenderTypes,
            GpuTextureView view,
            float aspectRatio
    ) {
        super(glyphRenderTypes, view,
                0f, 1f, 0f, 1f,                    // u0,u1,v0,v1 全纹理
                0f, HEIGHT * aspectRatio, 0f, HEIGHT // left,right,up,down（基线向下 8px）
        );
        this.aspectRatio = aspectRatio;
    }

    @Override
    public float getAdvance() {
        return HEIGHT * aspectRatio;
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

    @Override
    public @NotNull BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
        return this;
    }
}
