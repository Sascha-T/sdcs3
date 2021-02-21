package de.saschat.sdcs3.mixin;

import de.saschat.sdcs3.SDCS3;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "broadcastChatMessage", at = @At("HEAD"))
    public void broadcastChatMessage(Text message, MessageType type, UUID senderUuid, CallbackInfo info) {
        SDCS3.INSTANCE.gameMessage(message, type, senderUuid, info);
    }
}
