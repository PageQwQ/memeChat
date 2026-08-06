package pageqwq.memechat.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pageqwq.guilib.Gui;
import pageqwq.memechat.MemechatEmojiImage;
import pageqwq.memechat.common.gui.MemePickerScreen;
import pageqwq.memechat.gui.MemeButton;

/** Adds a meme picker button to the left of the chat input box */
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    private static final int BUTTON_SIZE = 20;

    @Shadow protected EditBox input;

    @Unique
    private Button memechat$memeButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void memechat$addMemeButton(CallbackInfo ci) {
        int x = this.input.getX() + this.input.getWidth() - BUTTON_SIZE - 2;
        int y = this.input.getY() - 2;
        this.input.setWidth(this.input.getWidth() - BUTTON_SIZE - 2);
        this.memechat$memeButton = new MemeButton(x, y, BUTTON_SIZE, BUTTON_SIZE, btn -> openMemePicker());
        ((ScreenAccessor) (Object) this).memechat$renderables().add(this.memechat$memeButton);
        ((ScreenAccessor) (Object) this).memechat$children().add(this.memechat$memeButton);
    }

    @Unique
    private void openMemePicker() {
        Gui.openScreen(new MemePickerScreen(
                MemechatEmojiImage::imageFor,
                this::insertMeme
        ));
    }

    @Unique
    private void insertMeme(String code) {
        String value = this.input.getValue();
        int pos = this.input.getCursorPosition();
        this.input.setValue(value.substring(0, pos) + code + value.substring(pos));
        this.input.setCursorPosition(pos + code.length());
    }
}
