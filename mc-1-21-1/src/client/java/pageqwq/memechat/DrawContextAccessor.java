package pageqwq.memechat;

import pageqwq.guilib.GuiDrawContext;

/** Bridges the guilib draw context to the version-specific graphics object */
public final class DrawContextAccessor {

    private DrawContextAccessor() {}

    public static net.minecraft.client.gui.GuiGraphics graphics(GuiDrawContext ctx) {
        return ((pageqwq.guilib.impl.DrawContextImpl) ctx).graphics();
    }
}
