package pageqwq.memechat;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageqwq.memechat.atlas.EmojiTexture;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.gif.GifDecoder;
import pageqwq.memechat.common.gif.GifFrame;
import pageqwq.memechat.font.MemechatGlyph;
import pageqwq.memechat.font.MemechatGlyphInfo;
import pageqwq.memechat.font.MemechatGlyphs;
import pageqwq.memechat.font.MemechatRenderTypesHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 26.1.2 表情包运行时状态。
 * 纹理在资源重载时同步创建（CommandEncoder 上传不能在渲染帧中途执行，
 * 否则 GL 原生崩溃），解码在 IO 线程，纹理创建在渲染线程。
 */
public final class MemechatEmoji {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemechatEmoji.class);

    private final Emoji meta;
    private final Identifier resourceLocation;
    private MemechatGlyphProvider glyphProvider;
    private volatile boolean loadError = false;

    MemechatEmoji(Emoji meta) {
        this.meta = meta;
        this.resourceLocation = Identifier.fromNamespaceAndPath(MemechatConstants.NAMESPACE, meta.path());
    }

    public Emoji meta() {
        return meta;
    }

    public String getCode() {
        return ":" + meta.name() + ':';
    }

    public net.minecraft.client.gui.font.glyphs.BakedGlyph getGlyph() {
        if (glyphProvider != null) return glyphProvider.getGlyph();
        return loadError ? MemechatGlyphs.error() : MemechatGlyphs.white();
    }

    /** 资源重载时同步加载：IO 线程解码 → 渲染线程创建纹理 */
    void loadSynchronously() {
        try {
            var decodeFuture = CompletableFuture.supplyAsync(() -> {
                        try (InputStream in = Minecraft.getInstance().getResourceManager().open(resourceLocation)) {
                            if (meta.isGif()) {
                                return (Object) GifDecoder.decode(in);
                            } else {
                                NativeImage image = NativeImage.read(in);
                                if (image.getWidth() <= 0 || image.getHeight() <= 0) {
                                    image.close();
                                    throw new IOException("invalid PNG: " + resourceLocation);
                                }
                                return (Object) image;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, Util.ioPool());

            Object decoded = decodeFuture.join(); // 渲染线程等待 IO
            if (meta.isGif()) {
                glyphProvider = createGifProvider((List<GifFrame>) decoded);
            } else {
                // 纹理必须在重载时（帧外）立即创建，不能推迟到渲染帧
                MemechatGlyph glyph = toGlyph((NativeImage) decoded,
                        "memechat_" + resourceLocation.getPath().replace('/', '_'));
                glyphProvider = () -> glyph;
            }
        } catch (Exception e) {
            LOGGER.warn("meme \"{}\" failed to load: {}", getCode(), e.getCause() == null ? e : e.getCause());
            loadError = true;
        }
    }

    /** 渲染线程：GifFrame(BufferedImage) → NativeImage */
    private static NativeImage toNativeImage(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        NativeImage out = new NativeImage(w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF, r = (argb >>> 16) & 0xFF, g = (argb >>> 8) & 0xFF, b = argb & 0xFF;
                out.setPixelABGR(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    /** 渲染线程：NativeImage → EmojiTexture + glyph（每帧一个纹理） */
    private static MemechatGlyph toGlyph(NativeImage image, String label) {
        try (image) {
            var texture = EmojiTexture.create(label, image);
            float aspect = (float) image.getWidth() / image.getHeight();
            return new MemechatGlyph(
                    new MemechatGlyphInfo(aspect),
                    MemechatRenderTypesHolder.emojiTextured(),
                    texture.view,
                    // BakedSheetGlyph 参数顺序：u0,u1,v0,v1,left,right,up,down
                    0f, 1f, 0f, 1f,
                    0f, MemechatGlyph.HEIGHT * aspect, 0f, MemechatGlyph.HEIGHT
            );
        }
    }

    private static MemechatGlyphProvider createGifProvider(List<GifFrame> frames) {
        List<MultiFrameGlyph.Frame> glyphFrames = new ArrayList<>(frames.size());
        for (int i = 0; i < frames.size(); i++) {
            GifFrame frame = frames.get(i);
            NativeImage nativeImage = toNativeImage(frame.image());
            glyphFrames.add(new MultiFrameGlyph.Frame(
                    toGlyph(nativeImage, "memechat_gif_" + i),
                    frame.delayMillis()));
        }
        return new MultiFrameGlyph(glyphFrames);
    }

    /** 渲染时按当前时间选帧的 provider */
    public interface MemechatGlyphProvider {
        MemechatGlyph getGlyph();
    }

    /** 多帧（GIF）glyph provider */
    public static final class MultiFrameGlyph implements MemechatGlyphProvider {
        public record Frame(MemechatGlyph glyph, int duration) {}

        private final List<Frame> frames;
        private final int totalDuration;

        MultiFrameGlyph(List<Frame> frames) {
            this.frames = List.copyOf(frames);
            int total = 0;
            for (Frame f : frames) total += f.duration();
            this.totalDuration = total;
        }

        @Override
        public MemechatGlyph getGlyph() {
            int time = (int) (Util.getMillis() % totalDuration);
            MemechatGlyph last = null;
            for (Frame f : frames) {
                if (time < f.duration()) return f.glyph();
                time -= f.duration();
                last = f.glyph();
            }
            return last;
        }
    }
}
