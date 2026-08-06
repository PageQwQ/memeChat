package pageqwq.memechat.common.gui;

import pageqwq.guilib.Gui;
import pageqwq.guilib.GuiDrawContext;
import pageqwq.guilib.GuiImage;
import pageqwq.guilib.GuiScreen;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Meme picker screen: left column lists resource packs, the right area shows
 * the selected pack's memegroups with a meme grid. Clicking a meme inserts
 * ":name:" into the chat box (via the callback) and closes the screen.
 */
public class MemePickerScreen implements GuiScreen {

    private static final int PACK_COLUMN_WIDTH = 96;
    private static final int PANEL_MARGIN = 4;
    private static final int GROUP_HEADER_HEIGHT = 12;
    private static final int EMOJI_SIZE = 20;
    private static final int EMOJI_GAP = 2;
    private static final int COLUMNS = 8;
    private static final int ROW_HEIGHT = EMOJI_SIZE + EMOJI_GAP;

    private final EmojiImageProvider imageProvider;
    private final Consumer<String> onMemeSelected;
    private final Runnable onClosed;
    private boolean dimBackground = true;

    private List<String> packs = List.of();
    private String selectedPack;
    private String selectedGroup;
    private List<String> groups = List.of();
    private List<Emoji> memes = List.of();

    private int panelX, panelY, panelWidth, panelHeight;
    private int gridX, gridY, gridWidth;
    private int scrollOffset;

    public MemePickerScreen(EmojiImageProvider imageProvider, Consumer<String> onMemeSelected, Runnable onClosed) {
        this.imageProvider = imageProvider;
        this.onMemeSelected = onMemeSelected;
        this.onClosed = onClosed;
    }

    /** Panel mode: skip the full-screen dim, rendered on top of the host screen */
    public MemePickerScreen setDimBackground(boolean dim) {
        this.dimBackground = dim;
        return this;
    }

    @Override
    public void init() {
        EmojiRegistry registry = EmojiRegistry.getInstance();
        packs = registry.packs();
        if (packs.isEmpty()) {
            selectedPack = null;
            selectedGroup = null;
            return;
        }
        if (selectedPack == null || !packs.contains(selectedPack)) {
            selectedPack = packs.get(0);
        }
        refreshGroups();
        scrollOffset = 0;
    }

    private void refreshGroups() {
        EmojiRegistry registry = EmojiRegistry.getInstance();
        groups = registry.groupsInPack(selectedPack);
        if (groups.isEmpty()) {
            groups = List.of(EmojiRegistry.DEFAULT_GROUP);
        }
        if (selectedGroup == null || !groups.contains(selectedGroup)) {
            selectedGroup = groups.get(0);
        }
        memes = registry.memesInGroup(selectedPack, selectedGroup);
        scrollOffset = 0;
    }

    @Override
    public void render(GuiDrawContext ctx, int mouseX, int mouseY, float deltaTicks) {
        int screenW = ctx.screenWidth();
        int screenH = ctx.screenHeight();

        panelWidth = PACK_COLUMN_WIDTH + COLUMNS * ROW_HEIGHT + 8;
        panelHeight = Math.min(screenH - 48, 6 * ROW_HEIGHT + GROUP_HEADER_HEIGHT + 8);
        panelX = screenW - panelWidth - PANEL_MARGIN; // right-aligned, above the chat box
        panelY = screenH - 48 - panelHeight;
        gridX = panelX + PACK_COLUMN_WIDTH;
        gridY = panelY + GROUP_HEADER_HEIGHT;
        gridWidth = panelWidth - PACK_COLUMN_WIDTH;

        // dim background (skipped in panel mode)
        if (dimBackground) {
            ctx.fill(0, 0, screenW, screenH, 0x80000000);
        }

        // panel background
        ctx.fill(panelX, panelY, panelWidth, panelHeight, 0xffcc2222);
        System.out.println("[memechat] panel at (" + panelX + "," + panelY + ") " + panelWidth + "x" + panelHeight
                + " screen " + screenW + "x" + screenH);
        ctx.outline(panelX, panelY, panelWidth, panelHeight, 0xff000000);

        // pack column
        ctx.fill(panelX, panelY, PACK_COLUMN_WIDTH, panelHeight, 0xaa2a2a2a);
        int y = panelY + 2;
        for (String pack : packs) {
            boolean hovered = mouseX >= panelX && mouseX < panelX + PACK_COLUMN_WIDTH
                    && mouseY >= y && mouseY < y + GROUP_HEADER_HEIGHT;
            if (pack.equals(selectedPack)) {
                ctx.fill(panelX + 1, y, PACK_COLUMN_WIDTH - 2, GROUP_HEADER_HEIGHT, 0x6633aaff);
            } else if (hovered) {
                ctx.fill(panelX + 1, y, PACK_COLUMN_WIDTH - 2, GROUP_HEADER_HEIGHT, 0x33ffffff);
            }
            ctx.drawText(shorten(pack, ctx, PACK_COLUMN_WIDTH - 6),
                    panelX + 3, y + 2, 0xffffffff);
            y += GROUP_HEADER_HEIGHT;
        }

        // memegroup header
        if (selectedGroup != null) {
            ctx.drawTextWithShadow(selectedGroup, gridX + 2, panelY + 2, 0xffffcc00);
        }

        // meme grid
        int col = 0;
        int row = 0;
        for (Emoji emoji : memes) {
            int ex = gridX + 2 + col * ROW_HEIGHT;
            int ey = gridY + row * ROW_HEIGHT - scrollOffset;
            if (ey + EMOJI_SIZE >= panelY + GROUP_HEADER_HEIGHT && ey < panelY + panelHeight) {
                boolean hovered = mouseX >= ex && mouseX < ex + EMOJI_SIZE
                        && mouseY >= ey && mouseY < ey + EMOJI_SIZE;
                if (hovered) {
                    ctx.fill(ex, ey, EMOJI_SIZE, EMOJI_SIZE, 0x77ffffff);
                }
                GuiImage image = imageProvider.imageFor(emoji);
                if (image != null) {
                    image.draw(ctx, ex + 1, ey + 1, EMOJI_SIZE - 2, EMOJI_SIZE - 2);
                }
            }
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // pack column click
        if (mouseX >= panelX && mouseX < panelX + PACK_COLUMN_WIDTH
                && mouseY >= panelY && mouseY < panelY + panelHeight) {
            int index = (int) ((mouseY - panelY) / GROUP_HEADER_HEIGHT);
            if (index >= 0 && index < packs.size()) {
                selectedPack = packs.get(index);
                refreshGroups();
                return true;
            }
        }
        // meme grid click
        if (selectedGroup != null && mouseX >= gridX && mouseX < gridX + gridWidth
                && mouseY >= gridY && mouseY < panelY + panelHeight) {
            int col = (int) ((mouseX - gridX - 2) / ROW_HEIGHT);
            int row = (int) ((mouseY - gridY + scrollOffset) / ROW_HEIGHT);
            int index = row * COLUMNS + col;
            if (index >= 0 && index < memes.size()) {
                Emoji emoji = memes.get(index);
                onMemeSelected.accept(":" + emoji.name() + ":");
                onClosed.run();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= gridX && mouseX < gridX + gridWidth
                && mouseY >= panelY && mouseY < panelY + panelHeight) {
            int maxScroll = Math.max(0, ((memes.size() + COLUMNS - 1) / COLUMNS) * ROW_HEIGHT
                    - (panelHeight - GROUP_HEADER_HEIGHT));
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (delta * ROW_HEIGHT)));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC closes the picker
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            onClosed.run();
            return true;
        }
        return false;
    }

    @Override
    public int width() {
        return panelWidth;
    }

    @Override
    public int height() {
        return panelHeight;
    }

    private static String shorten(String text, GuiDrawContext ctx, int maxWidth) {
        if (ctx.textWidth(text) <= maxWidth) return text;
        String shortened = text;
        while (!shortened.isEmpty() && ctx.textWidth(shortened + "…") > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "…";
    }
}
