package pageqwq.memechat.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Meme registry (pure Java). Ids stay stable across resource reloads (nameToIdMap
 * is kept between reloads), so already-replaced code points in history keep working.
 * Memes are organized by resource pack (pack) and memegroup (group).
 */
public final class EmojiRegistry {

    /** Group name for memes placed directly in the memes folder */
    public static final String DEFAULT_GROUP = "default";

    private static final EmojiRegistry INSTANCE = new EmojiRegistry();

    private final Map<Integer, Emoji> byId = new ConcurrentHashMap<>();
    private final Map<String, Emoji> byName = new ConcurrentHashMap<>();

    /** Kept between reloads: name -> stable id */
    private final Map<String, Integer> nameToIdMap = new HashMap<>();

    /** pack -> (group -> memes), insertion-ordered */
    private final Map<String, Map<String, List<Emoji>>> byPackAndGroup = new LinkedHashMap<>();

    private EmojiRegistry() {}

    public static EmojiRegistry getInstance() {
        return INSTANCE;
    }

    /** Start of a resource reload: clear all memes (nameToIdMap is kept) */
    public synchronized void beginReload() {
        byId.clear();
        byName.clear();
        byPackAndGroup.clear();
    }

    /** Register a meme resource; returns null if the normalized name is empty (invalid file name) */
    public synchronized Emoji register(String path, boolean isGif, String pack) {
        String name = EmojiNames.fromPath(path);
        if (name.isEmpty()) return null;

        String group = groupFromPath(path);
        int id = nameToIdMap.computeIfAbsent(name, k -> nameToIdMap.size());
        Emoji emoji = new Emoji(id, name, path, isGif, group, pack);
        byId.put(id, emoji);
        byName.put(name, emoji);
        byPackAndGroup.computeIfAbsent(pack, k -> new LinkedHashMap<>())
                .computeIfAbsent(group, k -> new ArrayList<>())
                .add(emoji);
        return emoji;
    }

    /** Extract the memegroup from a path: "memes/<group>/file.png" -> group; "memes/file.png" -> DEFAULT_GROUP */
    private static String groupFromPath(String path) {
        String rest = path.startsWith("memes/") ? path.substring("memes/".length()) : path;
        int slash = rest.indexOf('/');
        if (slash <= 0) return DEFAULT_GROUP;
        return rest.substring(0, slash);
    }

    /** Lookup by id (may be null: removed or never registered) */
    public Emoji byId(int id) {
        return byId.get(id);
    }

    /** Lookup by name (may be null) */
    public Emoji byName(String name) {
        return byName.get(name);
    }

    /** All memes, ordered by id */
    public List<Emoji> all() {
        return byId.values().stream()
                .sorted(Comparator.comparingInt(Emoji::id))
                .toList();
    }

    public boolean isEmpty() {
        return byName.isEmpty();
    }

    /** Resource pack ids (insertion-ordered) */
    public List<String> packs() {
        return List.copyOf(byPackAndGroup.keySet());
    }

    /** Groups within a pack (insertion-ordered); empty map if the pack is unknown */
    public List<String> groupsInPack(String pack) {
        Map<String, List<Emoji>> groups = byPackAndGroup.get(pack);
        return groups == null ? List.of() : List.copyOf(groups.keySet());
    }

    /** Memes of a specific group within a pack */
    public List<Emoji> memesInGroup(String pack, String group) {
        Map<String, List<Emoji>> groups = byPackAndGroup.get(pack);
        if (groups == null) return List.of();
        List<Emoji> memes = groups.get(group);
        return memes == null ? List.of() : List.copyOf(memes);
    }

    /** Prefix suggestions (ordered by id), used for chat completion */
    public List<Emoji> suggest(String prefix, int limit) {
        List<Emoji> result = new ArrayList<>();
        for (Emoji emoji : all()) {
            if (emoji.name().startsWith(prefix)) {
                result.add(emoji);
                if (result.size() >= limit) break;
            }
        }
        return result;
    }
}
