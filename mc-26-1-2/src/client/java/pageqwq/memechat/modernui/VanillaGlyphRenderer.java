package pageqwq.memechat.modernui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;

/**
 * 26.x 的表情段 vanilla 渲染：Font.prepareText + GlyphVisitor（原版渲染管线，
 * 26.x 没有 1.21.1 那种 private renderText 可 @Invoker）。
 */
public final class VanillaGlyphRenderer {

    private VanillaGlyphRenderer() {}

    public static float render(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                               Matrix4fc matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                               int colorBackground, int packedLight) {
        Font font = Minecraft.getInstance().font;
        Font.PreparedText prepared = font.prepareText(text, x, y, color, dropShadow, false, packedLight);
        prepared.visit(Font.GlyphVisitor.forMultiBufferSource(source, matrix, displayMode, packedLight));
        return x + font.width(text);
    }
}
