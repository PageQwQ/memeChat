package pageqwq.memechat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import pageqwq.memechat.font.MemechatFontSet;

/**
 * 注册 memechat:emoji 字体。apply 会在每次资源重载时清空 fontSets，
 * 因此在字体查找入口（getFontSetRaw）懒注册，保证任何时机渲染都能找到。
 */
@Mixin(FontManager.class)
public abstract class FontManagerMixin {
    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private Map<ResourceLocation, FontSet> fontSets;

    @Inject(method = "createFont", at = @At("TAIL"))
    private void memechat$registerFontSet(CallbackInfoReturnable<Font> cir) {
        this.fontSets.put(MemechatFontSet.NAME, new MemechatFontSet(this.textureManager));
    }

    @Inject(method = "getFontSetRaw", at = @At("HEAD"))
    private void memechat$lazyRegister(ResourceLocation id, CallbackInfoReturnable<FontSet> cir) {
        if (id.equals(MemechatFontSet.NAME) && !this.fontSets.containsKey(id)) {
            this.fontSets.put(MemechatFontSet.NAME, new MemechatFontSet(this.textureManager));
        }
    }
}
