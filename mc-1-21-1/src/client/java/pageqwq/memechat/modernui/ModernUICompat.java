package pageqwq.memechat.modernui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.lang.reflect.Method;

import pageqwq.memechat.MemechatConstants;

/**
 * ModernUI 反射桥：不依赖 ModernUI 编译，运行时反射调用其文本渲染。
 * 含 memeChat 表情样式的文本由调用方（FontMixin）改用 vanilla 渲染。
 */
public final class ModernUICompat {

    private static final String ENGINE = "icyllis.modernui.mc.text.TextLayoutEngine";
    private static final String RENDERER = "icyllis.modernui.mc.text.ModernTextRenderer";

    private static boolean checked;
    private static boolean loaded;
    private static Object engineInstance;
    private static Method getRendererMethod;
    private static Method drawTextMethod;

    private ModernUICompat() {}

    public static boolean isActive() {
        if (!checked) {
            checked = true;
            if (FabricLoader.getInstance().isModLoaded("modernui")) {
                try {
                    Class<?> engine = Class.forName(ENGINE);
                    engineInstance = engine.getMethod("getInstance").invoke(null);
                    getRendererMethod = engine.getMethod("getTextRenderer");
                    drawTextMethod = Class.forName(RENDERER).getMethod("drawText",
                            FormattedCharSequence.class, float.class, float.class, int.class, boolean.class,
                            Matrix4f.class, MultiBufferSource.class, Font.DisplayMode.class, int.class, int.class);
                    loaded = true;
                    System.out.println("[memechat] ModernUI compat active");
                } catch (Exception e) {
                    loaded = false;
                    System.out.println("[memechat] ModernUI compat init failed: " + e);
                }
            } else {
                System.out.println("[memechat] ModernUI not loaded, compat disabled");
            }
        }
        return loaded;
    }

    /** 文本是否含 memeChat 表情样式（emojChat:emoji 字体） */
    public static boolean containsMeme(FormattedCharSequence text) {
        final boolean[] found = {false};
        text.accept((index, style, codePoint) -> {
            ResourceLocation font = style.getFont();
            if (font != null && font.equals(MemechatConstants.EMOJI_FONT)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    /** 反射调用 ModernUI 渲染 */
    public static float draw(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                             Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                             int colorBackground, int packedLight) {
        try {
            Object renderer = getRendererMethod.invoke(engineInstance);
            return (float) drawTextMethod.invoke(renderer, text, x, y, color, dropShadow, matrix, source,
                    displayMode, colorBackground, packedLight);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
