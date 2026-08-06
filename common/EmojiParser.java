package pageqwq.memechat.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * :名字: 语法解析（纯 Java，迁移自 emogg 的 EmojiParser 正则部分）。
 * 支持半角/全角冒号，支持 \:名字: 转义。
 */
public final class EmojiParser {

    public static final Pattern PATTERN = Pattern.compile("(\\\\?):([_A-Za-z0-9]+):");

    public record Section(int start, int end, String name, boolean escaped) {}

    private EmojiParser() {}

    /** 找出文本中所有表情包片段（含转义标记） */
    public static List<Section> sections(String text) {
        Matcher matcher = PATTERN.matcher(text);
        List<Section> sections = new ArrayList<>();
        while (matcher.find()) {
            sections.add(new Section(matcher.start(), matcher.end(), matcher.group(2), !matcher.group(1).isEmpty()));
        }
        return sections;
    }

    /** 快速预检：文本中是否存在可解析的表情包语法 */
    public static boolean containsEmoji(String text) {
        return PATTERN.matcher(text).find();
    }
}
