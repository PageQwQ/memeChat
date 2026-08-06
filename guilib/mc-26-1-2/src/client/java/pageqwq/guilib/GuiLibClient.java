package pageqwq.guilib;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

import pageqwq.guilib.impl.ButtonImpl;
import pageqwq.guilib.impl.ScreenWrapper;

/** 26.1.2 implementation: registers the GUI provider */
public class GuiLibClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Gui.setProvider(new Gui.Provider() {
            @Override
            public void openScreen(GuiScreen screen) {
                Minecraft.getInstance().setScreen(new ScreenWrapper(screen));
            }

            @Override
            public void closeScreen() {
                Minecraft.getInstance().setScreen(null);
            }

            @Override
            public GuiScreen currentScreen() {
                if (Minecraft.getInstance().screen instanceof ScreenWrapper wrapper) {
                    return wrapper.delegate();
                }
                return null;
            }

            @Override
            public GuiButton createButton(int x, int y, int width, int height, String text, Runnable onClick) {
                return new ButtonImpl(x, y, width, height, text, onClick);
            }

            @Override
            public int screenWidth() {
                return Minecraft.getInstance().getWindow().getGuiScaledWidth();
            }

            @Override
            public int screenHeight() {
                return Minecraft.getInstance().getWindow().getGuiScaledHeight();
            }
        });
    }
}
