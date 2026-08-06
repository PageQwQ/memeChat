package pageqwq.memechat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;
import pageqwq.memechat.common.GroupListParser;

import java.util.Comparator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    private final Map<String, Map<String, String>> groupDisplayNames = new HashMap<>();
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
        groupDisplayNames.clear();

        // 包显示名直接取材质包文件夹名（packId），不读 pack.mcmeta description
        resourceManager.listPacks().forEach(pack -> {
            String id = pack.packId();
            packNames.put(id, id.startsWith("file/") ? id.substring("file/".length()) : id);
        });

        // 按包枚举：listResources 会因资源覆盖只返回高优先级包，被覆盖的包会从列表消失
        resourceManager.listPacks().forEach(pack ->
                pack.listResources(PackType.CLIENT_RESOURCES, MemechatConstants.NAMESPACE,
                        MemechatConstants.MEMES_PATH,
                        (loc, supplier) -> {
                            if (IS_MEME.test(loc)) {
                                register(loc, pack.packId());
                            }
                        }));

        // 读取每个包的 grouplist.txt：分组目录 → 显示名映射
        resourceManager.listPacks().forEach(pack -> {
            var supplier = pack.getResource(PackType.CLIENT_RESOURCES,
                    ResourceLocation.fromNamespaceAndPath(MemechatConstants.NAMESPACE, "memes/grouplist.txt"));
            if (supplier != null) {
                try (InputStream in = supplier.get()) {
                    groupDisplayNames.put(pack.packId(),
                            GroupListParser.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    LOGGER.warn("[memechat] failed to read grouplist.txt from {}", pack.packId(), e);
                }
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

    /** grouplist.txt 映射的分组显示名；未映射时返回原目录名 */
    public String groupDisplayName(String packId, String group) {
        Map<String, String> map = groupDisplayNames.get(packId);
        if (map != null) {
            String name = map.get(group);
            if (name != null && !name.isBlank()) return name;
        }
        return group;
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
