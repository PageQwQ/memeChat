package pageqwq.memechat;

/** 渲染线程单线程安全的状态标志 */
public final class MixinHelpers {
    /** 描边/发光渲染期间跳过表情包 glyph */
    public static boolean shouldSkipEmojiGlyphRender = false;

    private MixinHelpers() {}
}
