package pageqwq.memechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.gui.GuiGraphics;

import pageqwq.memechat.MemechatConstants;
import pageqwq.memechat.MemechatEmojis;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Meme picker panel rendered on top of the chat screen (1.21.2-1.21.8).
 * Left column lists resource packs, the right area shows the selected
 * pack's memegroups with a meme grid. Clicking a meme inserts ":name:"
 * into the chat box via the callback and closes the panel.
 */
public class MemePickerPanel {

    private static final int PACK_COLUMN_WIDTH = 96;
    private static final int PANEL_MARGIN = 4;
    private static final int GROUP_HEADER_HEIGHT = 12;
    private static final int EMOJI_SIZE = 20;
    private static final int EMOJI_GAP = 2;
    private static final int COLUMNS = 8;
    private static final int ROW_HEIGHT = EMOJI_SIZE + EMOJI_GAP;

    private final Consumer<String> onMemeSelected;
    private final Runnable onClosed;

    private List<String> packs = List.of();
    private String selectedPack;
    private String selectedGroup;
    private List<String> groups = List.of();
    private List<Emoji> memes = List.of();

    private int panelX, panelY, panelWidth, panelHeight;
    private int gridX, gridY, gridWidth;
    private int scrollOffset;
    private int tabPage;
    private int tabsPerPage = 4;

    public MemePickerPanel(Consumer<String> onMemeSelected, Runnable onClosed) {
        this.onMemeSelected = onMemeSelected;
        this.onClosed = onClosed;
    }

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
        tabPage = 0;
    }

    /** 按可用宽度计算当前页能放下的分组数（留 24px 给翻页按钮） */
    private void computeTabsPerPage(Font font) {
        int avail = gridWidth - 4 - 22;
        int count = 0;
        int w = 0;
        for (String group : groups) {
            int gw = font.width(group) + 10;
            if (count > 0 && w + gw > avail) break;
            w += gw;
            count++;
        }
        tabsPerPage = Math.max(1, count);
    }

    private int tabPageCount() {
        return Math.max(1, (groups.size() + tabsPerPage - 1) / tabsPerPage);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        panelWidth = PACK_COLUMN_WIDTH + COLUMNS * ROW_HEIGHT + 8;
        panelHeight = Math.min(screenH - 48, 6 * ROW_HEIGHT + GROUP_HEADER_HEIGHT + 8);
        panelX = screenW - panelWidth - PANEL_MARGIN;
        panelY = screenH - 48 - panelHeight;
        gridX = panelX + 4;
        gridY = panelY + GROUP_HEADER_HEIGHT;
        gridWidth = panelWidth - PACK_COLUMN_WIDTH - 8;
        int packColumnX = panelX + panelWidth - PACK_COLUMN_WIDTH;

        // panel background
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xee1e1e1e);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xff000000);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xff000000);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, 0xff000000);
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0xff000000);

        // pack column (right side)
        graphics.fill(packColumnX, panelY, panelX + panelWidth, panelY + panelHeight, 0xaa2a2a2a);
        int y = panelY + 2;
        Font font = Minecraft.getInstance().font;
        for (String pack : packs) {
            boolean hovered = mouseX >= packColumnX && mouseX < packColumnX + PACK_COLUMN_WIDTH
                    && mouseY >= y && mouseY < y + GROUP_HEADER_HEIGHT;
            if (pack.equals(selectedPack)) {
                graphics.fill(packColumnX + 1, y, packColumnX + PACK_COLUMN_WIDTH - 1, y + GROUP_HEADER_HEIGHT, 0x6633aaff);
            } else if (hovered) {
                graphics.fill(packColumnX + 1, y, packColumnX + PACK_COLUMN_WIDTH - 1, y + GROUP_HEADER_HEIGHT, 0x33ffffff);
            }
            String displayName = pageqwq.memechat.MemechatEmojis.getInstance().displayName(pack);
            graphics.drawString(font, shorten(displayName, font, PACK_COLUMN_WIDTH - 6),
                    packColumnX + 3, y + 2, 0xffffffff, true);
            y += GROUP_HEADER_HEIGHT;
        }

        // memegroup tabs (click to switch, paged when too many groups)
        computeTabsPerPage(font);
        int pageCount = tabPageCount();
        if (tabPage >= pageCount) tabPage = pageCount - 1;
        int from = tabPage * tabsPerPage;
        int to = Math.min(groups.size(), from + tabsPerPage);
        int tabX = gridX + 2;
        for (int i = from; i < to; i++) {
            String group = groups.get(i);
            boolean selected = group.equals(selectedGroup);
            boolean hovered = mouseX >= tabX && mouseX < tabX + font.width(group) + 8
                    && mouseY >= panelY + 1 && mouseY < panelY + GROUP_HEADER_HEIGHT - 1;
            if (selected) {
                graphics.fill(tabX, panelY + 1, tabX + font.width(group) + 8, panelY + GROUP_HEADER_HEIGHT - 1, 0x6633aaff);
            } else if (hovered) {
                graphics.fill(tabX, panelY + 1, tabX + font.width(group) + 8, panelY + GROUP_HEADER_HEIGHT - 1, 0x33ffffff);
            }
            graphics.drawString(font, group, tabX + 4, panelY + 3, selected ? 0xffffcc00 : 0xffffffff, true);
            tabX += font.width(group) + 10;
        }
        // page arrows
        if (pageCount > 1) {
            int btnX = gridX + gridWidth - 24;
            graphics.drawString(font, "<", btnX, panelY + 3, tabPage > 0 ? 0xffffffff : 0x66ffffff, true);
            graphics.drawString(font, ">", btnX + 12, panelY + 3, tabPage < pageCount - 1 ? 0xffffffff : 0x66ffffff, true);
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
                    graphics.fill(ex, ey, ex + EMOJI_SIZE, ey + EMOJI_SIZE, 0x77ffffff);
                }
                renderEmoji(emoji, graphics, ex + 1, ey + 1, EMOJI_SIZE - 2);
            }
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    /** Renders the meme via the font pipeline (code point + emoji font style), scaled to the cell */
    private void renderEmoji(Emoji emoji, GuiGraphics graphics, int x, int y, int size) {
        if (MemechatEmojis.getInstance().byId(emoji.id()) == null) return;
        Component component = Component.literal(Character.toString(emoji.codePoint()))
                .setStyle(Style.EMPTY.withFont(MemechatConstants.EMOJI_FONT));
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(size / 8f, size / 8f);
        graphics.drawString(Minecraft.getInstance().font, component, 0, 0, 0xFFFFFFFF, false);
        pose.popMatrix();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // pack column click (right side)
        int packColumnX = panelX + panelWidth - PACK_COLUMN_WIDTH;
        if (mouseX >= packColumnX && mouseX < packColumnX + PACK_COLUMN_WIDTH
                && mouseY >= panelY && mouseY < panelY + panelHeight) {
            int index = (int) ((mouseY - panelY) / GROUP_HEADER_HEIGHT);
            if (index >= 0 && index < packs.size()) {
                selectedPack = packs.get(index);
                refreshGroups();
                return true;
            }
        }
        // page arrows
        if (mouseY >= panelY + 1 && mouseY < panelY + GROUP_HEADER_HEIGHT - 1
                && tabPageCount() > 1) {
            int btnX = gridX + gridWidth - 24;
            if (mouseX >= btnX && mouseX < btnX + 12 && tabPage > 0) {
                tabPage--;
                return true;
            }
            if (mouseX >= btnX + 12 && mouseX < btnX + 24 && tabPage < tabPageCount() - 1) {
                tabPage++;
                return true;
            }
        }
        // memegroup tab click
        if (mouseY >= panelY + 1 && mouseY < panelY + GROUP_HEADER_HEIGHT - 1) {
            int from = tabPage * tabsPerPage;
            int to = Math.min(groups.size(), from + tabsPerPage);
            int tabX = gridX + 2;
            Font font = net.minecraft.client.Minecraft.getInstance().font;
            for (int i = from; i < to; i++) {
                String group = groups.get(i);
                if (mouseX >= tabX && mouseX < tabX + font.width(group) + 8) {
                    if (!group.equals(selectedGroup)) {
                        selectedGroup = group;
                        memes = EmojiRegistry.getInstance().memesInGroup(selectedPack, selectedGroup);
                        scrollOffset = 0;
                    }
                    return true;
                }
                tabX += font.width(group) + 10;
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            onClosed.run();
            return true;
        }
        return false;
    }

    private static String shorten(String text, Font font, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        String shortened = text;
        while (!shortened.isEmpty() && font.width(shortened + "…") > maxWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "…";
    }
}
