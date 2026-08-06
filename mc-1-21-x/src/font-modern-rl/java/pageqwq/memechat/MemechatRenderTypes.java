package pageqwq.memechat;

import net.minecraft.Util;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * 表情包渲染类型（26.1.2）：复用原版颜色纹理字体的 RenderType 工厂。
 * 纹理由 BakedSheetGlyph 的 GpuTextureView 提供，不依赖 RenderType 绑定。
 */
public final class MemechatRenderTypes {

    private static final Function<ResourceLocation, GlyphRenderTypes> EMOJI_TEXTURED = Util.memoize(
            texture -> GlyphRenderTypes.createForColorTexture(texture)
    );

    public static GlyphRenderTypes emojiTextured(ResourceLocation atlasTexture) {
        return EMOJI_TEXTURED.apply(atlasTexture);
    }

    private MemechatRenderTypes() {}
}
