package pageqwq.memechat.font;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.Identifier;

import pageqwq.memechat.MemechatConstants;

/**
 * 表情包共享 GlyphRenderTypes（单个图集纹理标识，懒创建）。
 */
public final class MemechatRenderTypesHolder {

    private static volatile GlyphRenderTypes instance;

    private MemechatRenderTypesHolder() {}

    public static GlyphRenderTypes emojiTextured() {
        GlyphRenderTypes rt = instance;
        if (rt == null) {
            synchronized (MemechatRenderTypesHolder.class) {
                if (instance == null) {
                    instance = GlyphRenderTypes.createForColorTexture(
                            Identifier.fromNamespaceAndPath(MemechatConstants.NAMESPACE, "emoji_atlas"));
                }
                rt = instance;
            }
        }
        return rt;
    }
}
