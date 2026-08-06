package pageqwq.memechat.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pageqwq.memechat.gui.MemeButton;
import pageqwq.memechat.gui.MemePickerPanel;

/**
 * Adds a meme picker button above-right of the chat box. Clicking toggles the
 * meme panel rendered directly on the chat screen. The button is render-only
 * (not in children), so clicking it never steals focus from the input box.
 */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    private static final int BUTTON_SIZE = 20;

    @Shadow protected EditBox input;

    @Unique
    private Button memechat$memeButton;

    @Unique
    private MemePickerPanel memechat$picker;

    @Unique
    private boolean memechat$pickerOpen;

    @Inject(method = "init", at = @At("TAIL"))
    private void memechat$addMemeButton(CallbackInfo ci) {
        int x = this.input.getX() + this.input.getWidth() - BUTTON_SIZE;
        int y = this.input.getY() - BUTTON_SIZE - 4;
        this.memechat$memeButton = new MemeButton(x, y, BUTTON_SIZE, BUTTON_SIZE, btn -> toggleMemePicker());
        ((ScreenAccessor) (Object) this).memechat$renderables().add(this.memechat$memeButton);
    }

    @Unique
    private void toggleMemePicker() {
        if (this.memechat$pickerOpen) {
            this.memechat$pickerOpen = false;
            this.memechat$picker = null;
        } else {
            this.memechat$picker = new MemePickerPanel(this::insertMeme, () -> {
                this.memechat$pickerOpen = false;
                this.memechat$picker = null;
            });
            this.memechat$picker.init();
            this.memechat$pickerOpen = true;
        }
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
            this.memechat$picker.render(graphics, mouseX, mouseY, deltaTicks);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void memechat$handleClicks(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        double mouseX = event.x();
        double mouseY = event.y();
        // meme button click (button is not in children, handle manually to keep focus on the input)
        if (this.memechat$memeButton != null && this.memechat$memeButton.isMouseOver(mouseX, mouseY)) {
            this.toggleMemePicker();
            cir.setReturnValue(true);
            return;
        }
        // meme panel clicks
        if (this.memechat$pickerOpen && this.memechat$picker != null
                && this.memechat$picker.mouseClicked(mouseX, mouseY, event.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void memechat$scrollPicker(double mouseX, double mouseY, double horizontalAmount, double verticalAmount,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (this.memechat$pickerOpen && this.memechat$picker != null
                && this.memechat$picker.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void memechat$keyPicker(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (this.memechat$pickerOpen && this.memechat$picker != null
                && this.memechat$picker.keyPressed(event.key(), 0, event.modifiers())) {
            cir.setReturnValue(true);
        }
    }
}
