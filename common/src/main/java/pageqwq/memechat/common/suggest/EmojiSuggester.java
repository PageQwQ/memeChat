package pageqwq.memechat.common.suggest;

/**
 * 聊天补全：从光标位置向前扫描出未闭合的 :名字 前缀。
 * 已闭合的 :name: 块会被跳过，避免补全完成后光标停留在末尾再次触发补全。
 */
public final class EmojiSuggester {

    /** start: ':' 的位置（含），nameStart: 名字起始，name: 已输入部分；escaped: 被反斜杠转义 */
    public record Prefix(int start, int nameStart, String name, boolean escaped) {}

    private EmojiSuggester() {}

    /**
     * 在 text 中光标位置 cursor 处查找可补全的 :前缀。
     * 规则：从 cursor-1 向前扫描，遇到 ':' 或 '：' 停止；中间的字符必须全为 [_A-Za-z0-9]；
     * 若该冒号属于已闭合的 :name: 块（冒号前是名字段且名字段前还有一个冒号），跳过整个块继续向前；
     * 冒号前一字符为 '\' 时视为转义（不补全）。返回 null 表示无匹配。
     */
    public static Prefix findPrefix(String text, int cursor) {
        if (text == null || cursor <= 0) return null;

        int i = cursor - 1;
        while (i >= 0) {
            char c = text.charAt(i);
            if (c == ':') {
                // 跳过已闭合的 :name: 块：紧邻左边是名字段，且名字段前还有冒号
                if (i > 0 && isNameChar(text.charAt(i - 1))) {
                    int j = i - 1;
                    while (j >= 0 && isNameChar(text.charAt(j))) j--;
                    if (j >= 0 && text.charAt(j) == ':') {
                        i = j - 1;
                        continue;
                    }
                }
                // 真正的开头冒号
                int nameStart = i + 1;
                String name = text.substring(nameStart, cursor);
                if (!isName(name)) return null;

                boolean escaped = i > 0 && text.charAt(i - 1) == '\\';
                if (escaped) return null;

                return new Prefix(i, nameStart, name, false);
            }
            if (!isNameChar(c)) return null; // 中间出现非法字符，放弃
            i--;
        }
        return null;
    }

    private static boolean isName(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isNameChar(s.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isNameChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9') || c == '_';
    }
}
