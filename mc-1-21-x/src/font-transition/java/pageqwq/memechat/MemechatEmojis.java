package pageqwq.memechat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
 * 过渡式（1.21.6~1.21.9）表情包运行时注册表。
 */
public final class MemechatEmojis {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemechatEmojis.class);
    private static final MemechatEmojis INSTANCE = new MemechatEmojis();

    private static final Predicate<ResourceLocation> IS_MEME = loc ->
            loc.getPath().startsWith(MemechatConstants.MEMES_PATH + "/")
                    && (loc.getPath().endsWith(".png") || loc.getPath().endsWith(".gif"));

    private final Map<Integer, MemechatEmoji> byId = new ConcurrentHashMap<>();
    private final EmojiRegistry registry = EmojiRegistry.getInstance();

    private MemechatEmojis() {}

    public static MemechatEmojis getInstance() {
        return INSTANCE;
    }

    /** 资源重载：清空运行时状态、重新扫描并同步预加载所有纹理 */
    public void reload(ResourceManager resourceManager) {
        registry.beginReload();
        byId.clear();

        resourceManager.listResources(MemechatConstants.MEMES_PATH, IS_MEME)
                .keySet().stream()
                .filter(loc -> loc.getNamespace().equals(MemechatConstants.NAMESPACE))
                .forEach(this::register);

        // CommandEncoder 纹理上传不能在渲染帧中途执行，必须在重载时（帧外）完成
        byId.values().forEach(MemechatEmoji::loadSynchronously);

        LOGGER.info("[memechat] Discovered {} memes", registry.all().size());
    }

    private void register(ResourceLocation location) {
        boolean isGif = location.getPath().endsWith(".gif");
        Emoji emoji = registry.register(location.getPath(), isGif);
        if (emoji != null) {
            byId.put(emoji.id(), new MemechatEmoji(emoji));
        }
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
