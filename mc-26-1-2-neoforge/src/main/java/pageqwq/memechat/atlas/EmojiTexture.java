package pageqwq.memechat.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 26.1.2 表情包纹理：每帧一个 GpuTexture（方案 B）。
 * 渲染线程调用：createTexture + CommandEncoder.writeToTexture 上传像素 + createTextureView。
 */
public final class EmojiTexture implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmojiTexture.class);

    private static final int USAGE = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING;

    public final GpuTextureView view;
    public final int width;
    public final int height;

    private EmojiTexture(GpuTextureView view, int width, int height) {
        this.view = view;
        this.width = width;
        this.height = height;
    }

    /** 渲染线程：由 NativeImage 创建纹理（RGBA8，单 mip，单采样） */
    public static EmojiTexture create(String label, NativeImage image) {
        RenderSystem.assertOnRenderThread();
        var device = RenderSystem.getDevice();
        GpuTexture texture = device.createTexture(
                label, USAGE, TextureFormat.RGBA8, image.getWidth(), image.getHeight(), 1, 1);
        try {
            device.createCommandEncoder().writeToTexture(texture, image);
        } catch (Exception e) {
            texture.close();
            throw e;
        }
        GpuTextureView view = device.createTextureView(texture);
        return new EmojiTexture(view, image.getWidth(), image.getHeight());
    }

    @Override
    public void close() {
        view.texture().close();
        view.close();
    }
}
