package pageqwq.memechat.modernui;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.lang.reflect.Method;

/**
 * ModernUI 反射桥：不依赖 ModernUI 编译，运行时反射调用其文本渲染。
 * 渲染抛出的 MemechatFontException 直接传播给调用方（FontMixin 捕获后回退 vanilla）。
 */
public final class ModernUICompat {

    private static final String ENGINE = "icyllis.modernui.mc.text.TextLayoutEngine";
    private static final String RENDERER = "icyllis.modernui.mc.text.ModernTextRenderer";

    private static boolean checked;
    private static boolean loaded;
    private static Object engineInstance;
    private static Method getRendererMethod;
    private static Method drawTextMethod;

    /** 仅在 ModernUI 渲染调用链中允许布局抛 MemechatFontException（测量/宽度计算路径必须放行，否则崩溃） */
    private static final ThreadLocal<Boolean> RENDERING = ThreadLocal.withInitial(() -> false);

    public static boolean isRendering() {
        return RENDERING.get();
    }

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

    /** 反射调用 ModernUI 渲染；文本含 memechat:emoji 时会抛出 MemechatFontException */
    public static float draw(FormattedCharSequence text, float x, float y, int color, boolean dropShadow,
                             Matrix4f matrix, MultiBufferSource source, Font.DisplayMode displayMode,
                             int colorBackground, int packedLight) {
        RENDERING.set(true);
        try {
            Object renderer = getRendererMethod.invoke(engineInstance);
            float result = (float) drawTextMethod.invoke(renderer, text, x, y, color, dropShadow, matrix, source,
                    displayMode, colorBackground, packedLight);
            System.out.println("[memechat] ModernUI draw succeeded");
            return result;
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MemechatFontException) {
                throw (MemechatFontException) cause;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } finally {
            RENDERING.set(false);
        }
    }
}
