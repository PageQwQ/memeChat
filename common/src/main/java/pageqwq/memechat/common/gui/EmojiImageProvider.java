package pageqwq.memechat.common.gui;

import pageqwq.guilib.GuiImage;
import pageqwq.memechat.common.Emoji;

/**
 * Provides a drawable image for a meme. Implemented by the version-specific
 * adapter layer (each version renders its own glyph type).
 */
public interface EmojiImageProvider {
    GuiImage imageFor(Emoji emoji);
}
