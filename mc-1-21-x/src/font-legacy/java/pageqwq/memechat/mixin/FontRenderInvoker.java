package pageqwq.memechat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 调用 vanilla 的 private renderText（1.21.2~1.21.4 签名含 bidi 参数）：ModernUI
 * 环境下含表情的文本用原版渲染路径绘制。
 */
@Mixin(Font.class)
public interface FontRenderInvoker {

    @Invoker("renderText")
    float memechat$invokeRenderText(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                    Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                    int colorBackground, int packedLight, boolean bidi);
}
