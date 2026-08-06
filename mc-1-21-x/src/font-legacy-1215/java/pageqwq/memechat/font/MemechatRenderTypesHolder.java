package pageqwq.memechat.font;

import net.minecraft.Util;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * 过渡式 A（1.21.2~1.21.5）GlyphRenderTypes：按纹理 location 参数化。
 * 1.21.2~1.21.5 的 RenderType 纹理是静态绑定（TextureStateShard 按 location 查找），
 * 必须与实际注册的纹理同名，否则渲染为 missing 纹理（黑紫）。
 */
public final class MemechatRenderTypesHolder {

    private static final Function<ResourceLocation, GlyphRenderTypes> EMOJI_TEXTURED = Util.memoize(
            GlyphRenderTypes::createForColorTexture);

    private MemechatRenderTypesHolder() {}

    public static GlyphRenderTypes emojiTextured(ResourceLocation textureLocation) {
        return EMOJI_TEXTURED.apply(textureLocation);
    }
}
