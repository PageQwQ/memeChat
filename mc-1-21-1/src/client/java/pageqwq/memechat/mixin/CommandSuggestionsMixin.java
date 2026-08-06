package pageqwq.memechat.mixin;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import pageqwq.memechat.MemechatEmojis;
import pageqwq.memechat.common.Emoji;
import pageqwq.memechat.common.EmojiRegistry;
import pageqwq.memechat.common.suggest.EmojiSuggester;
import pageqwq.memechat.common.suggest.EmojiSuggester.Prefix;

/**
 * 聊天补全：输入 :名字 前缀时提供表情包候选。
 * 复用原版 showSuggestions 的渲染/定位流程（注入 updateCommandInfo 并设置 pendingSuggestions）。
 */
@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow @Final private EditBox input;

    @Shadow private boolean allowSuggestions;

    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow public abstract void showSuggestions(boolean bl);

    private static final int MAX_CANDIDATES = 16;

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
    private void memechat$emojiSuggestions(CallbackInfo ci) {
        if (!this.allowSuggestions || EmojiRegistry.getInstance().isEmpty()) return;

        String text = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        Prefix prefix = EmojiSuggester.findPrefix(text, cursor);
        if (prefix == null) return;

        List<Emoji> candidates = EmojiRegistry.getInstance().suggest(prefix.name(), MAX_CANDIDATES);
        if (candidates.isEmpty()) return;

        List<Suggestion> list = candidates.stream()
                .map(e -> {
                    // 预热加载，保证候选弹出时预览图已就绪
                    var runtime = MemechatEmojis.getInstance().byId(e.id());
                    if (runtime != null) runtime.getGlyph();
                    return new Suggestion(
                            StringRange.between(prefix.nameStart(), cursor),
                            e.name() + ":");
                })
                .toList();
        this.pendingSuggestions = CompletableFuture.completedFuture(
                new Suggestions(StringRange.between(prefix.start(), cursor), list));
        this.showSuggestions(false);
        ci.cancel();
    }
}
