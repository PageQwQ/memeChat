package pageqwq.memechat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 初始化：注册资源包重载监听，扫描 assets/memechat/memes/ 下的表情包 */
public class MemechatClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("memechat");

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(MemechatConstants.NAMESPACE, "memes");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        MemechatEmojis.getInstance().reload(resourceManager);
                    }
                });
    }
}
