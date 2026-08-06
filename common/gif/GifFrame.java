package pageqwq.memechat.common.gif;

import java.awt.image.BufferedImage;

/** GIF 的一帧：合成后的画布图像 + 显示时长（毫秒） */
public record GifFrame(BufferedImage image, int delayMillis) {}
