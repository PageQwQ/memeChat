package pageqwq.memechat.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析材质包 grouplist.txt：每行一条 `<目录>/ == "<显示名>",`，
 * 把 memegroup 目录名映射为面板上显示的自定义名字。
 */
public final class GroupListParser {

    private GroupListParser() {}

    public static Map<String, String> parse(String content) {
        Map<String, String> map = new LinkedHashMap<>();
        if (content == null) return map;
        for (String line : content.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            int idx = t.indexOf("==");
            if (idx <= 0) continue;
            String dir = t.substring(0, idx).trim();
            if (dir.endsWith("/")) dir = dir.substring(0, dir.length() - 1);
            String rest = t.substring(idx + 2).trim();
            if (rest.endsWith(",")) rest = rest.substring(0, rest.length() - 1).trim();
            if (rest.length() >= 2 && rest.startsWith("\"") && rest.endsWith("\"")) {
                rest = rest.substring(1, rest.length() - 1);
            }
            if (!dir.isEmpty() && !rest.isEmpty()) map.put(dir, rest);
        }
        return map;
    }
}
