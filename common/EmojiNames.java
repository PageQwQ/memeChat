package pageqwq.memechat.common;

/**
 * 表情包文件名规范化（迁移自 emogg EmojiUtil.normalizeEmojiObjectKey 的纯 Java 版）。
 * 规则：小写 → 空格/点/连字符转下划线 → 去非法字符 → 去首尾下划线。
 */
public final class EmojiNames {

    private EmojiNames() {}

    /** 从资源路径（如 "memes/My-Meme.PNG"）提取并规范化为表情名 */
    public static String fromPath(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0) fileName = fileName.substring(0, dot);
        return normalize(fileName);
    }

    public static String normalize(String raw) {
        return stripUnderscores(
                raw.toLowerCase()
                        .replaceAll("-+| +|\\.+", "_")
                        .replaceAll("[^a-z0-9_]", "")
        );
    }

    private static String stripUnderscores(String s) {
        int start = 0, end = s.length();
        while (start < end && s.charAt(start) == '_') start++;
        while (end > start && s.charAt(end - 1) == '_') end--;
        return s.substring(start, end);
    }
}
