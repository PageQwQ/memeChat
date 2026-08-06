package pageqwq.memechat.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 表情包纹理图集（移植自 emogg EmojiAtlas，去掉 debug HUD）。
 * 256x256 起步，倍增扩容，矩形剪枝打包；每张图/每帧缝合进图集并返回带 UV 的 glyph 数据。
 */
public final class EmojiAtlas {

    private static final List<AtlasTexture> TEXTURES = new ArrayList<>();

    /** 渲染线程调用：把一张图片缝合进图集 */
    public static MemechatGlyphData stitch(NativeImage image) {
        RenderSystem.assertOnRenderThreadOrInit();
        for (var texture : TEXTURES) {
            var glyph = texture.stitch(image);
            if (glyph != null) return glyph;
        }
        var texture = new AtlasTexture("memechat_emoji_atlas_" + TEXTURES.size());
        TEXTURES.add(texture);
        return Objects.requireNonNull(texture.stitch(image));
    }

    public static void clear() {
        TEXTURES.forEach(AtlasTexture::close);
        TEXTURES.clear();
    }

    private EmojiAtlas() {}

    /** 图集内一块图的数据：纹理位置 + UV + 宽高（UV 在图集扩容时更新） */
    public static final class MemechatGlyphData {
        public final ResourceLocation texture;
        public final int x, y, width, height;
        public float u0, v0, u1, v1;

        MemechatGlyphData(ResourceLocation texture, int x, int y, int width, int height, int atlasWidth, int atlasHeight) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            updateUV(atlasWidth, atlasHeight);
        }

        void updateUV(int atlasWidth, int atlasHeight) {
            this.u0 = (float) x / atlasWidth;
            this.v0 = (float) y / atlasHeight;
            this.u1 = (float) (x + width) / atlasWidth;
            this.v1 = (float) (y + height) / atlasHeight;
        }
    }

    private static final class AtlasTexture extends AbstractTexture {
        private final ResourceLocation location;
        private int totalWidth = 256, totalHeight = 256;
        private final LinkedList<int[]> freeSpace = new LinkedList<>(); // {x, y, w, h}
        private final List<MemechatGlyphData> stitched = new ArrayList<>();
        private static final int BG_FILL_COLOR = 0x00000000;

        AtlasTexture(String name) {
            RenderSystem.assertOnRenderThreadOrInit();
            this.location = ResourceLocation.fromNamespaceAndPath("memechat", name);
            Minecraft.getInstance().getTextureManager().register(location, this);
            TextureUtil.prepareImage(NativeImage.InternalGlFormat.RGBA, getId(), totalWidth, totalHeight);
            fillBackground();
            freeSpace.add(new int[]{0, 0, totalWidth, totalHeight});
        }

        @Override
        public void load(ResourceManager resourceManager) {}

        @Nullable
        MemechatGlyphData stitch(NativeImage image) {
            RenderSystem.assertOnRenderThreadOrInit();
            int[] pos;
            while ((pos = fit(image.getWidth(), image.getHeight(), 1)) == null) {
                if (!expand()) return null;
            }
            bind();
            image.upload(0, pos[0], pos[1], false);
            var data = new MemechatGlyphData(location, pos[0], pos[1], image.getWidth(), image.getHeight(), totalWidth, totalHeight);
            stitched.add(data);
            return data;
        }

        @Nullable
        private int[] fit(int width, int height, int padding) {
            width += padding * 2;
            height += padding * 2;
            var iter = freeSpace.listIterator();
            while (iter.hasNext()) {
                var rect = iter.next();
                if (rect[2] >= width && rect[3] >= height) {
                    int px = rect[0] + padding, py = rect[1] + padding;

                    // 切分剩余空间，保持矩形近似方形
                    double ratioA0 = (double) (rect[2] - width) / height;
                    double ratioA1 = (double) rect[2] / (rect[3] - height);
                    double ratioB0 = (double) width / (rect[3] - height);
                    double ratioB1 = (double) (rect[2] - width) / rect[3];
                    double ratioA = Math.max(ratioA0 > 1 ? ratioA0 : (1 / ratioA0), ratioA1 > 1 ? ratioA1 : (1 / ratioA1));
                    double ratioB = Math.max(ratioB0 > 1 ? ratioB0 : (1 / ratioB0), ratioB1 > 1 ? ratioB1 : (1 / ratioB1));

                    int[] toAdd;
                    if (ratioA < ratioB) {
                        // Solution A: 水平切
                        toAdd = new int[]{rect[0], rect[1], width, rect[3] - height};
                        rect[0] += width;
                        rect[2] -= width;
                    } else {
                        // Solution B: 垂直切
                        toAdd = new int[]{rect[0], rect[1] + height, rect[2] - width, height};
                        rect[1] += height;
                        rect[3] -= height;
                    }
                    iter.remove();
                    if (rect[2] > 0 && rect[3] > 0) freeSpace.addFirst(rect);
                    if (toAdd[2] > 0 && toAdd[3] > 0) freeSpace.addFirst(toAdd);
                    return new int[]{px, py};
                }
            }
            return null;
        }

        private boolean expand() {
            RenderSystem.assertOnRenderThreadOrInit();
            int limit = RenderSystem.maxSupportedTextureSize();
            if (totalWidth == limit && totalHeight == limit) return false;

            int oldWidth = totalWidth, oldHeight = totalHeight;
            if (totalWidth <= totalHeight) {
                totalWidth = Math.min(totalWidth * 2, limit);
                freeSpace.add(new int[]{oldWidth, 0, totalWidth - oldWidth, totalHeight});
            } else {
                totalHeight = Math.min(totalHeight * 2, limit);
                freeSpace.add(new int[]{0, oldHeight, totalWidth, totalHeight - oldHeight});
            }

            bind();
            var image = new NativeImage(NativeImage.Format.RGBA, oldWidth, oldHeight, false);
            image.downloadTexture(0, false);
            releaseId();
            TextureUtil.prepareImage(NativeImage.InternalGlFormat.RGBA, getId(), totalWidth, totalHeight);
            fillBackground();
            bind();
            image.upload(0, 0, 0, false);
            image.close();

            // 新尺寸下重算所有已缝合 glyph 的 UV
            stitched.forEach(g -> g.updateUV(totalWidth, totalHeight));
            return true;
        }

        private void fillBackground() {
            try (var image = new NativeImage(NativeImage.Format.RGBA, totalWidth, totalHeight, false)) {
                image.fillRect(0, 0, totalWidth, totalHeight, BG_FILL_COLOR);
                bind();
                image.upload(0, 0, 0, false);
            }
        }
    }
}
