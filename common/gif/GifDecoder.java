package pageqwq.memechat.common.gif;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

/**
 * GIF 解码器：基于 JDK ImageIO（零第三方依赖）。
 * ImageIO 的 GIF reader 在 read(i) 时返回已合成的画布帧（自动处理局部帧与 disposal），
 * 帧延迟从 "javax_imageio_gif_image_1.0" 元数据读取（1/100 秒，0 修正为 100ms）。
 */
public final class GifDecoder {

    private GifDecoder() {}

    public static List<GifFrame> decode(InputStream in) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) {
            throw new IOException("No GIF ImageReader available");
        }
        ImageReader reader = readers.next();
        try (ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            reader.setInput(iis, false, true);
            int count = reader.getNumImages(true);
            if (count <= 0) {
                throw new IOException("GIF contains no frames");
            }

            List<GifFrame> frames = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                var image = reader.read(i);
                if (image.getWidth() <= 0 || image.getHeight() <= 0) {
                    throw new IOException("Invalid GIF frame " + i);
                }
                frames.add(new GifFrame(image, readDelayMillis(reader, i)));
            }
            return frames;
        } finally {
            reader.dispose();
        }
    }

    /** 读取第 i 帧的显示时长；缺失或为 0 时按浏览器惯例用 100ms */
    private static int readDelayMillis(ImageReader reader, int frameIndex) throws IOException {
        IIOMetadata metadata = reader.getImageMetadata(frameIndex);
        if (metadata != null) {
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = child(root, "GraphicControlExtension");
            if (gce != null) {
                String delay = gce.getAttribute("delayTime");
                if (delay != null && !delay.isEmpty()) {
                    int centis = Integer.parseInt(delay);
                    return centis <= 0 ? 100 : centis * 10;
                }
            }
        }
        return 100;
    }

    private static IIOMetadataNode child(IIOMetadataNode node, String name) {
        for (int i = 0; i < node.getLength(); i++) {
            if (node.item(i) instanceof IIOMetadataNode n && n.getNodeName().equals(name)) {
                return n;
            }
        }
        return null;
    }
}
