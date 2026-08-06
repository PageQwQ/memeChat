package pageqwq.memechat.font;

import net.minecraft.Util;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;

import pageqwq.memechat.MemechatConstants;

/**
 * 过渡式共享 GlyphRenderTypes：复用原版颜色纹理字体工厂。
 */
public final class MemechatRenderTypesHolder {

    private static final GlyphRenderTypes INSTANCE = Util.memoize(
            GlyphRenderTypes::createForColorTexture)
            .apply(ResourceLocation.fromNamespaceAndPath(MemechatConstants.NAMESPACE, "emoji_atlas"));

    private MemechatRenderTypesHolder() {}

    public static GlyphRenderTypes emojiTextured() {
        return INSTANCE;
    }
}
