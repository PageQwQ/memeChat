package pageqwq.memechat.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SequencedMap;

/** 1.21.1 中 fixedBuffers 为 protected 字段，用 accessor 注入自定义 RenderType 的固定 buffer */
@Mixin(MultiBufferSource.BufferSource.class)
public interface BufferSourceAccessor {
    @Accessor("fixedBuffers")
    SequencedMap<RenderType, ByteBufferBuilder> memechat$fixedBuffers();
}
