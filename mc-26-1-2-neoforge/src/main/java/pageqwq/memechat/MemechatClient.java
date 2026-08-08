package pageqwq.memechat;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端入口（仅物理客户端加载）：注册资源包重载监听，扫描 assets/memechat/memes/ 下的表情包。
 * 等价于 Fabric 版 {@code ClientModInitializer} + {@code SimpleSynchronousResourceReloadListener}：
 * 这里改为挂客户端 mod 总线的 {@link AddClientReloadListenersEvent}。
 */
@Mod(value = "memechat", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "memechat", value = Dist.CLIENT)
public class MemechatClient {

    public static final Logger LOGGER = LoggerFactory.getLogger("memechat");

    public MemechatClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(MemechatConstants.NAMESPACE, "memes"),
                new ResourceManagerReloadListener() {
                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        MemechatEmojis.getInstance().reload(resourceManager);
                    }
                });
    }
}