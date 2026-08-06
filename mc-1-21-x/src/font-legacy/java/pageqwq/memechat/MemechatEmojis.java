package pageqwq.memechat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;

import java.util.Comparator;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 表情包运行时注册表：扫描资源包 + 管理每个表情包的运行时状态。
 */
public final class MemechatEmojis {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemechatEmojis.class);
    private static final MemechatEmojis INSTANCE = new MemechatEmojis();

    private static final Predicate<ResourceLocation> IS_MEME = loc ->
            loc.getPath().startsWith(MemechatConstants.MEMES_PATH + "/")
                    && (loc.getPath().endsWith(".png") || loc.getPath().endsWith(".gif"));

    private final Map<Integer, MemechatEmoji> byId = new ConcurrentHashMap<>();
    private final Map<String, String> packNames = new LinkedHashMap<>();
    private final EmojiRegistry registry = EmojiRegistry.getInstance();

    private MemechatEmojis() {}

    public static MemechatEmojis getInstance() {
        return INSTANCE;
    }

    /** 资源重载：清空图集与运行时状态，重新扫描 */
    public void reload(ResourceManager resourceManager) {
        registry.beginReload();
        byId.clear();
        packNames.clear();

        resourceManager.listPacks().forEach(pack -> {
            String id = pack.packId();
            String name = id;
            try {
                var meta = pack.getMetadataSection(PackMetadataSection.TYPE);
                if (meta != null && !meta.description().getString().isBlank()) {
                    name = meta.description().getString();
                }
            } catch (IOException ignored) {
            }
            packNames.put(id, name);
        });

        resourceManager.listResources(MemechatConstants.MEMES_PATH, IS_MEME)
                .forEach((loc, resource) -> {
                    if (loc.getNamespace().equals(MemechatConstants.NAMESPACE)) {
                        register(loc, resource.sourcePackId());
                    }
                });

        LOGGER.info("[memechat] Discovered {} memes", registry.all().size());
    }

    private void register(ResourceLocation location, String pack) {
        boolean isGif = location.getPath().endsWith(".gif");
        Emoji emoji = registry.register(location.getPath(), isGif, pack);
        if (emoji != null) {
            byId.put(emoji.id(), new MemechatEmoji(emoji));
        }
    }

    public String displayName(String packId) {
        return packNames.getOrDefault(packId, packId);
    }

    public MemechatEmoji byId(int id) {
        return byId.get(id);
    }

    public List<MemechatEmoji> all() {
        return byId.values().stream()
                .sorted(Comparator.comparingInt(e -> e.meta().id()))
                .toList();
    }

    public Optional<MemechatEmoji> getRandom() {
        var list = all();
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get((int) (list.size() * Math.random())));
    }
}
