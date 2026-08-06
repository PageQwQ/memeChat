package pageqwq.memechat.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.function.Function;

import pageqwq.memechat.MemechatRenderTypes;
import pageqwq.memechat.atlas.EmojiAtlas.MemechatGlyphData;

/**
 * 表情包 glyph：扩展 BakedGlyph 自绘图片四边形（移植自 emogg EmojiGlyph，去掉自定义 shader）。
 * 高度固定 8px（字体高度），宽度按宽高比。
 */
public abstract class MemechatGlyph extends BakedGlyph implements GlyphInfo {

    public static final float HEIGHT = 8f;
    public static final float ITALIC_SHEAR = 0.25f;

    private static final Matrix4f TEMP_MAT = new Matrix4f();

    public MemechatGlyph(GlyphRenderTypes glyphRenderTypes) {
        super(glyphRenderTypes, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** 宽高比（宽/高），决定 advance */
    public abstract float getAspectRatio();

    private void setupMatrix(boolean italic, float x, float y, Matrix4f mat) {
        TEMP_MAT.set(
                HEIGHT * getAspectRatio(), 0f, 0f, 0f,
                italic ? -HEIGHT * ITALIC_SHEAR : 0f, HEIGHT, 0f, 0f,
                0f, 0f, 0f, 0f,
                x + (italic ? HEIGHT * ITALIC_SHEAR : 0f), y, 0f, 1f
        );
        TEMP_MAT.mulLocal(mat);
    }

    @Override
    public final void render(
            boolean italic,
            float x,
            float y,
            Matrix4f mat,
            VertexConsumer builder,
            float r,
            float g,
            float b,
            float a,
            int packedLightCoords
    ) {
        setupMatrix(italic, x, y, mat);
        // 表情包不参与文字染色，强制白色
        renderImpl(builder, TEMP_MAT, 1f, 1f, 1f, a, packedLightCoords);
    }

    /** 以归一化坐标系（0~1，x 右 y 下）绘制内容 */
    protected abstract void renderImpl(
            VertexConsumer builder,
            Matrix4f mat,
            float r,
            float g,
            float b,
            float a,
            int packedLightCoords
    );

    @Override
    public final float getAdvance() {
        return HEIGHT * getAspectRatio();
    }

    @Override
    public final float getAdvance(boolean bl) {
        return getAdvance();
    }

    @Override
    public final float getBoldOffset() {
        return 0f;
    }

    @Override
    public final float getShadowOffset() {
        return 0f;
    }

    @Override
    public final @NotNull BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
        return this;
    }

    /** 图集四边形 glyph */
    public static final class Atlas extends MemechatGlyph {
        private final MemechatGlyphData data;

        public Atlas(MemechatGlyphData data) {
            super(MemechatRenderTypes.emojiTextured(data.texture));
            this.data = data;
        }

        @Override
        public float getAspectRatio() {
            return (float) data.width / data.height;
        }

        @Override
        protected void renderImpl(
                VertexConsumer builder,
                Matrix4f mat,
                float r,
                float g,
                float b,
                float a,
                int packedLightCoords
        ) {
            float u0 = data.u0, v0 = data.v0, u1 = data.u1, v1 = data.v1;
            builder.addVertex(mat, 0f, 0f, 0f).setColor(r, g, b, a).setUv(u0, v0).setLight(packedLightCoords);
            builder.addVertex(mat, 0f, 1f, 0f).setColor(r, g, b, a).setUv(u0, v1).setLight(packedLightCoords);
            builder.addVertex(mat, 1f, 1f, 0f).setColor(r, g, b, a).setUv(u1, v1).setLight(packedLightCoords);
            builder.addVertex(mat, 1f, 0f, 0f).setColor(r, g, b, a).setUv(u1, v0).setLight(packedLightCoords);
        }
    }

    /** 缺失纹理占位 */
    public static final MemechatGlyph ERROR = new MissingTexture();

    private static final class MissingTexture extends MemechatGlyph {
        private MissingTexture() {
            super(MemechatRenderTypes.emojiTextured(MissingTextureAtlasSprite.getLocation()));
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(
                VertexConsumer builder,
                Matrix4f mat,
                float r,
                float g,
                float b,
                float a,
                int packedLightCoords
        ) {
            builder.addVertex(mat, 0f, 0f, 0f).setColor(r, g, b, a).setUv(0f, 0f).setLight(packedLightCoords);
            builder.addVertex(mat, 0f, 1f, 0f).setColor(r, g, b, a).setUv(0f, 1f).setLight(packedLightCoords);
            builder.addVertex(mat, 1f, 1f, 0f).setColor(r, g, b, a).setUv(1f, 1f).setLight(packedLightCoords);
            builder.addVertex(mat, 1f, 0f, 0f).setColor(r, g, b, a).setUv(1f, 0f).setLight(packedLightCoords);
        }
    }

    /** 空白 glyph（不可见，占位用） */
    public static final MemechatGlyph EMPTY = new EmptyGlyph();

    private static final class EmptyGlyph extends MemechatGlyph {
        private EmptyGlyph() {
            super(MemechatRenderTypes.emojiTextured(ResourceLocation.fromNamespaceAndPath("memechat", "empty")));
        }

        @Override
        public float getAspectRatio() {
            return 1f;
        }

        @Override
        protected void renderImpl(
                VertexConsumer builder,
                Matrix4f mat,
                float r,
                float g,
                float b,
                float a,
                int packedLightCoords
        ) {
        }
    }
}
