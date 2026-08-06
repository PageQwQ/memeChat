package pageqwq.memechat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Function;

import pageqwq.memechat.common.EmojiRegistry;
import pageqwq.memechat.font.MemechatFontSet;
import pageqwq.memechat.mixin.MutableComponentAccessor;

/**
 * 组件解析（移植自 emogg EmojiParser）：把 :名字: 替换为带 memechat:emoji
 * 字体样式的私有码点字符子组件，并保存原文用于 getString() 恢复。
 */
public final class EmojiParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmojiParser.class);

    /** 解析结果组件 → 原文组件（弱引用，避免泄漏） */
    private static final WeakHashMap<MutableComponent, MutableComponent> PARSED_TO_ORIGINAL = new WeakHashMap<>();

    private static boolean parsing = false;

    private EmojiParser() {}

    public static void parse(MutableComponent component) {
        if (!isParsable(component)) return;
        if (PARSED_TO_ORIGINAL.containsKey(component)) return;
        if (parsing) return;
        try {
            parsing = true;
            parseInternal(component);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse component <{}>", component, e);
        } finally {
            parsing = false;
        }
    }

    private static void parseInternal(MutableComponent component) {
        if (!(component.getContents() instanceof PlainTextContents.LiteralContents literal)) {
            // 1.21.1 中 PlainTextContents 只有一种实现（LiteralContents），create() 也返回它
            return;
        }
        String originalText = literal.text();
        var sections = pageqwq.memechat.common.EmojiParser.sections(originalText);
        if (sections.isEmpty()) return;

        PARSED_TO_ORIGINAL.put(component, component.copy());

        List<Component> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        for (int i = 0; i <= sections.size(); i++) {
            if (i != sections.size()) {
                var section = sections.get(i);
                sb.append(originalText, lastEnd, section.start());
                if (!section.escaped()) {
                    var emoji = EmojiRegistry.getInstance().byName(section.name());
                    if (emoji != null) {
                        if (!sb.isEmpty()) {
                            parts.add(MutableComponent.create(PlainTextContents.create(sb.toString())));
                            sb.setLength(0);
                        }
                        lastEnd = section.end();
                        parts.add(MutableComponent.create(PlainTextContents.create(
                                        Character.toString(MemechatFontSet.idToCodePoint(emoji.id()))
                                ))
                                .setStyle(Style.EMPTY.withFont(MemechatConstants.EMOJI_FONT)));
                    }
                } else {
                    // 转义：去掉反斜杠
                    sb.append(originalText, section.start() + 1, section.end());
                    lastEnd = section.end();
                }
            } else {
                sb.append(originalText, lastEnd, originalText.length());
            }
        }
        parts.add(MutableComponent.create(PlainTextContents.create(sb.toString())));

        // 保留原有 siblings
        parts.addAll(component.getSiblings());

        MutableComponentAccessor accessor = (MutableComponentAccessor) component;
        accessor.memechat$setSiblings(parts);
        accessor.memechat$setContents(PlainTextContents.EMPTY);
    }

    public static boolean isParsable(Component component) {
        return component instanceof MutableComponent mc && mc.getContents() instanceof PlainTextContents.LiteralContents;
    }

    /** 是否为已解析的结果组件（contents 为空） */
    public static boolean mayBeParseResult(Component component) {
        return component instanceof MutableComponent mc && mc.getContents() == PlainTextContents.EMPTY;
    }

    public static boolean isOnLogicalClient() {
        return RenderSystem.isOnRenderThread();
    }

    @Nullable
    public static MutableComponent getOriginal(Component component) {
        if (!mayBeParseResult(component)) return null;
        return PARSED_TO_ORIGINAL.get(component);
    }

    /** mixin 辅助：若组件已解析，用原文执行操作并重定向返回值 */
    public static <T> void mixinApplyUsingOriginal(
            Component component,
            CallbackInfoReturnable<T> cir,
            Function<Component, T> operation
    ) {
        MutableComponent original = getOriginal(component);
        if (original != null) {
            cir.cancel();
            cir.setReturnValue(operation.apply(original));
        }
    }

    public static boolean isInGetString = false;
}
