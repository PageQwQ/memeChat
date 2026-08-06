package pageqwq.memechat.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.21.5 专用：AbstractTexture 已改为 GpuTexture 字段，纹理上传走 CommandEncoder。
 * 必须资源重载时（帧外）创建。
 */
public final class EmojiTexture extends AbstractTexture {

    public final ResourceLocation location;
    public final int width;
    public final int height;

    private EmojiTexture(ResourceLocation location, NativeImage image) {
        this.location = location;
        this.width = image.getWidth();
        this.height = image.getHeight();
        var device = RenderSystem.getDevice();
        GpuTexture gpu = device.createTexture(
                () -> "memechat_" + location.getPath().replace('/', '_'),
                TextureFormat.RGBA8, width, height, 1);
        device.createCommandEncoder().writeToTexture(gpu, image);
        this.texture = gpu;
        Minecraft.getInstance().getTextureManager().register(location, this);
    }

    /** 渲染线程：由 NativeImage 创建纹理 */
    public static EmojiTexture create(String label, NativeImage image) {
        return new EmojiTexture(
                ResourceLocation.fromNamespaceAndPath("memechat", "emoji_tex_" + label),
                image);
    }
}
