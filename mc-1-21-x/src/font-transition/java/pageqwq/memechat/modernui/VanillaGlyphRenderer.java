package pageqwq.memechat.modernui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

/**
 * 1.21.6~1.21.8 的表情段 vanilla 渲染：Font.prepareText + GlyphVisitor（原版渲染管线）。
 */
public final class VanillaGlyphRenderer {

    private VanillaGlyphRenderer() {}

    public static float render(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                               Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                               int colorBackground, int packedLight) {
        Font font = Minecraft.getInstance().font;
        Font.PreparedText prepared = font.prepareText(text, x, y, color, dropShadow, packedLight);
        prepared.visit(Font.GlyphVisitor.forMultiBufferSource(source, matrix, displayMode, packedLight));
        return x + font.width(text);
    }
}
