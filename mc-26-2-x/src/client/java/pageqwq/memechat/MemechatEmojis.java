package pageqwq.memechat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 26.1.2 表情包运行时注册表：扫描资源包 + 管理运行时状态。
 */
public final class MemechatEmojis {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemechatEmojis.class);
    private static final MemechatEmojis INSTANCE = new MemechatEmojis();

    private static final Predicate<Identifier> IS_MEME = loc ->
            loc.getPath().startsWith(MemechatConstants.MEMES_PATH + "/")
                    && (loc.getPath().endsWith(".png") || loc.getPath().endsWith(".gif"));

    private final Map<Integer, MemechatEmoji> byId = new ConcurrentHashMap<>();
    private final Map<String, String> packNames = new LinkedHashMap<>();
    private final EmojiRegistry registry = EmojiRegistry.getInstance();

    private MemechatEmojis() {}

    public static MemechatEmojis getInstance() {
        return INSTANCE;
    }

    /** 资源重载：清空运行时状态、重新扫描并同步预加载所有纹理 */
    public void reload(ResourceManager resourceManager) {
        registry.beginReload();
        byId.clear();
        packNames.clear();

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

        // CommandEncoder 纹理上传不能在渲染帧中途执行，必须在重载时（帧外）完成
        byId.values().forEach(MemechatEmoji::loadSynchronously);

        LOGGER.info("[memechat] Discovered {} memes", registry.all().size());
    }

    private void register(Identifier location, String pack) {
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
