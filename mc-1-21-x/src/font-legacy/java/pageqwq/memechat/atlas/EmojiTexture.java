package pageqwq.memechat.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * 过渡式 A（1.21.2~1.21.5）表情包纹理：每图一个独立 GL 纹理（旧 API）。
 * 渲染线程创建：register + prepareImage + NativeImage.upload。
 */
public final class EmojiTexture extends AbstractTexture {

    public final ResourceLocation location;
    public final int width;
    public final int height;

    private EmojiTexture(ResourceLocation location, NativeImage image) {
        this.location = location;
        this.width = image.getWidth();
        this.height = image.getHeight();
        Minecraft.getInstance().getTextureManager().register(location, this);
        TextureUtil.prepareImage(NativeImage.InternalGlFormat.RGBA, getId(), image.getWidth(), image.getHeight());
        bind();
        image.upload(0, 0, 0, false);
    }

    /** 渲染线程：由 NativeImage 创建纹理 */
    public static EmojiTexture create(String label, NativeImage image) {
        return new EmojiTexture(
                ResourceLocation.fromNamespaceAndPath("memechat", "emoji_tex_" + label),
                image);
    }

    public void load(ResourceManager resourceManager) {
    }
}
