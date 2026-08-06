package pageqwq.memechat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 调用 vanilla 的 private renderText：ModernUI 环境下绕过其接管，用原版渲染路径绘制表情。
 */
@Mixin(Font.class)
public interface FontRenderInvoker {

    @Invoker("renderText")
    float memechat$invokeRenderText(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                                    Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                                    int colorBackground, int packedLight);
}
