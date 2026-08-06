package pageqwq.guilib;

/**
 * GUI 静态入口：打开/关闭屏幕、创建控件、查询屏幕尺寸。
 * 实现由各版本模组提供（在 ModInitializer 中 setProvider）。
 */
public final class Gui {

    public interface Provider {
        void openScreen(GuiScreen screen);

        void closeScreen();

        GuiScreen currentScreen();

        GuiButton createButton(int x, int y, int width, int height, String text, Runnable onClick);

        int screenWidth();

        int screenHeight();
    }

    private static Provider provider;

    private Gui() {}

    /** 由版本实现模组在初始化时调用 */
    public static void setProvider(Provider p) {
        provider = p;
    }

    public static void openScreen(GuiScreen screen) {
        provider.openScreen(screen);
    }

    public static void closeScreen() {
        provider.closeScreen();
    }

    public static GuiScreen currentScreen() {
        return provider.currentScreen();
    }

    public static GuiButton createButton(int x, int y, int width, int height, String text, Runnable onClick) {
        return provider.createButton(x, y, width, height, text, onClick);
    }

    public static int screenWidth() {
        return provider.screenWidth();
    }

    public static int screenHeight() {
        return provider.screenHeight();
    }
}
