package pageqwq.memechat;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageqwq.memechat.atlas.EmojiAtlas;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.gif.GifDecoder;
import pageqwq.memechat.common.gif.GifFrame;
import pageqwq.memechat.font.MemechatGlyph;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 表情包运行时状态：懒加载（INACTIVE → LOADING → ACTIVE / ERROR）。
 * 图片在 IO 线程解码，图集缝合在渲染线程完成。
 */
public final class MemechatEmoji {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemechatEmoji.class);

    public enum State { INACTIVE, LOADING, ACTIVE, ERROR }

    private final Emoji meta;
    private final ResourceLocation resourceLocation;
    private Future<MemechatGlyphProvider> loadingFuture;
    private MemechatGlyphProvider glyphProvider;
    private State state = State.INACTIVE;

    MemechatEmoji(Emoji meta) {
        this.meta = meta;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath(MemechatConstants.NAMESPACE, meta.path());
    }

    public Emoji meta() {
        return meta;
    }

    public State getState() {
        return state;
    }

    public String getCode() {
        return ":" + meta.name() + ':';
    }

    public MemechatGlyph getGlyph() {
        updateLoadingState();
        return switch (state) {
            case LOADING, INACTIVE -> MemechatGlyph.EMPTY;
            case ERROR -> MemechatGlyph.ERROR;
            case ACTIVE -> glyphProvider.getGlyph();
        };
    }

    private void updateLoadingState() {
        if (state == State.INACTIVE) {
            loadingFuture = startLoading();
            state = State.LOADING;
        }
        if (state != State.LOADING || !loadingFuture.isDone()) return;

        try {
            glyphProvider = loadingFuture.get();
            state = State.ACTIVE;
        } catch (ExecutionException e) {
            LOGGER.warn("meme \"{}\" failed to load: {}", getCode(), e.getCause() == null ? e : e.getCause());
            state = State.ERROR;
        } catch (InterruptedException e) {
            LOGGER.warn("meme \"{}\" loading interrupted", getCode());
            Thread.currentThread().interrupt();
            state = State.ERROR;
        }
    }

    private Future<MemechatGlyphProvider> startLoading() {
        return meta.isGif()
                ? GifLoader.load(resourceLocation)
                : PngLoader.load(resourceLocation);
    }

    /** 静态图加载器 */
    private static final class PngLoader {
        static Future<MemechatGlyphProvider> load(ResourceLocation location) {
            return CompletableFuture.supplyAsync(() -> {
                        try (InputStream in = Minecraft.getInstance().getResourceManager().open(location)) {
                            NativeImage image = NativeImage.read(in);
                            if (image.getWidth() <= 0 || image.getHeight() <= 0) {
                                image.close();
                                throw new IOException("invalid PNG: " + location);
                            }
                            return image;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, Util.ioPool())
                    .thenApplyAsync(image -> {
                        var data = EmojiAtlas.stitch(image);
                        image.close();
                        return (MemechatGlyphProvider) () -> new MemechatGlyph.Atlas(data);
                    }, Minecraft.getInstance());
        }
    }

    /** GIF 动图加载器：解码每帧 → 逐帧缝合进图集 → 多帧 provider */
    private static final class GifLoader {
        static Future<MemechatGlyphProvider> load(ResourceLocation location) {
            return CompletableFuture.supplyAsync(() -> {
                        try (InputStream in = Minecraft.getInstance().getResourceManager().open(location)) {
                            return GifDecoder.decode(in);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, Util.ioPool())
                    .thenApplyAsync(frames -> {
                        List<MultiFrameGlyph.Frame> glyphFrames = new ArrayList<>(frames.size());
                        for (GifFrame frame : frames) {
                            NativeImage nativeImage = fromBufferedImage(frame.image());
                            var data = EmojiAtlas.stitch(nativeImage);
                            nativeImage.close();
                            glyphFrames.add(new MultiFrameGlyph.Frame(new MemechatGlyph.Atlas(data), frame.delayMillis()));
                        }
                        return new MultiFrameGlyph(glyphFrames);
                    }, Minecraft.getInstance());
        }

        /** BufferedImage(ARGB) → NativeImage(RGBA) */
        private static NativeImage fromBufferedImage(java.awt.image.BufferedImage img) {
            int w = img.getWidth(), h = img.getHeight();
            NativeImage out = new NativeImage(w, h, false);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = img.getRGB(x, y);
                    int a = (argb >>> 24) & 0xFF, r = (argb >>> 16) & 0xFF, g = (argb >>> 8) & 0xFF, b = argb & 0xFF;
                    out.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            return out;
        }
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
