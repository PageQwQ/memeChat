package pageqwq.memechat.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.guilib.impl.DrawContextImpl;
import pageqwq.memechat.MemechatEmojiImage;
import pageqwq.memechat.common.gui.MemePickerScreen;
import pageqwq.memechat.gui.MemeButton;

/**
 * Adds a meme picker button above-right of the chat box. Clicking toggles the
 * meme panel rendered directly on the chat screen (no separate screen).
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("memechat");

    private static final int BUTTON_SIZE = 20;

    @Shadow protected EditBox input;

    @Unique
    private Button memechat$memeButton;

    @Unique
    private MemePickerScreen memechat$picker;

    @Unique
    private boolean memechat$pickerOpen;

    @Inject(method = "init", at = @At("TAIL"))
    private void memechat$addMemeButton(CallbackInfo ci) {
        int x = this.input.getX() + this.input.getWidth() - BUTTON_SIZE;
        int y = this.input.getY() - BUTTON_SIZE - 4;
        this.memechat$memeButton = new MemeButton(x, y, BUTTON_SIZE, BUTTON_SIZE, btn -> toggleMemePicker());
        ((ScreenAccessor) (Object) this).memechat$renderables().add(this.memechat$memeButton);
        ((ScreenAccessor) (Object) this).memechat$children().add(this.memechat$memeButton);
    }

    @Unique
    private void toggleMemePicker() {
        if (this.memechat$pickerOpen) {
            this.memechat$pickerOpen = false;
            this.memechat$picker = null;
            return;
        }
        this.memechat$picker = new MemePickerScreen(
                MemechatEmojiImage::imageFor,
                this::insertMeme,
                () -> {
                    this.memechat$pickerOpen = false;
                    this.memechat$picker = null;
                }).setDimBackground(false);
        this.memechat$pickerOpen = true;
        LOGGER.info("[memechat] picker opened, packs={}", pageqwq.memechat.common.EmojiRegistry.getInstance().packs());
    }

    @Unique
    private void insertMeme(String code) {
        String value = this.input.getValue();
        int pos = this.input.getCursorPosition();
        this.input.setValue(value.substring(0, pos) + code + value.substring(pos));
        this.input.setCursorPosition(pos + code.length());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void memechat$renderPicker(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (this.memechat$pickerOpen && this.memechat$picker != null) {
            this.memechat$picker.render(new DrawContextImpl(graphics), mouseX, mouseY, deltaTicks);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void memechat$clickPicker(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.memechat$pickerOpen && this.memechat$picker != null) {
            if (this.memechat$picker.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void memechat$keyPicker(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.memechat$pickerOpen && this.memechat$picker != null
                && this.memechat$picker.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }
}
