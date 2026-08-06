package pageqwq.memechat;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

import pageqwq.memechat.mixin.BufferSourceAccessor;

import static net.minecraft.client.renderer.RenderStateShard.COLOR_WRITE;
import static net.minecraft.client.renderer.RenderStateShard.LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.NO_DEPTH_TEST;
import static net.minecraft.client.renderer.RenderStateShard.POLYGON_OFFSET_LAYERING;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_TEXT_SEE_THROUGH_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_TEXT_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;
import static net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import static net.minecraft.client.renderer.RenderType.CompositeState;

/**
 * 表情包渲染类型：复用原版文本 shader，无自定义 shader。
 * 每个图集纹理对应一组 GlyphRenderTypes（normal / seeThrough / polygonOffset）。
 */
public final class MemechatRenderTypes {

    private static RenderType setup(RenderType renderType) {
        ((BufferSourceAccessor) Minecraft.getInstance()
                .renderBuffers()
                .bufferSource())
                .memechat$fixedBuffers()
                .put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
        return renderType;
    }

    private static RenderType create(
            String name,
            VertexFormat.Mode mode,
            TextureStateShard texture
    ) {
        return setup(RenderType.create(
                name,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                mode,
                256,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_TEXT_SHADER)
                        .setTextureState(texture)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false)
        ));
    }

    private static RenderType createSeeThrough(String name, TextureStateShard texture) {
        return setup(RenderType.create(
                name + "_see_through",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
                        .setTextureState(texture)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false)
        ));
    }

    private static RenderType createPolygonOffset(String name, TextureStateShard texture) {
        return setup(RenderType.create(
                name + "_polygon_offset",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_TEXT_SHADER)
                        .setTextureState(texture)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .setLayeringState(POLYGON_OFFSET_LAYERING)
                        .createCompositeState(false)
        ));
    }

    private static final Function<ResourceLocation, GlyphRenderTypes> EMOJI_TEXTURED = Util.memoize(
            texture -> {
                var state = new TextureStateShard(texture, true, false);
                return new GlyphRenderTypes(
                        create("memechat_emoji", VertexFormat.Mode.QUADS, state),
                        createSeeThrough("memechat_emoji", state),
                        createPolygonOffset("memechat_emoji", state)
                );
            }
    );

    public static GlyphRenderTypes emojiTextured(ResourceLocation atlasTexture) {
        return EMOJI_TEXTURED.apply(atlasTexture);
    }

    private MemechatRenderTypes() {}
}
