package pageqwq.memechat.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** 26.1.2 中 contents/siblings 为 private final 字段，需 accessor 修改 */
@Mixin(MutableComponent.class)
public interface MutableComponentAccessor {
    @Accessor("contents") @Mutable
    void memechat$setContents(ComponentContents contents);

    @Accessor("siblings") @Mutable
    void memechat$setSiblings(List<Component> siblings);
}
