package pageqwq.memechat.mixin;

/**
 * 检测到 memeChat 表情样式时抛出，中断 ModernUI 布局并回退到 vanilla 渲染。
 */
public final class MemechatFontException extends RuntimeException {
}
