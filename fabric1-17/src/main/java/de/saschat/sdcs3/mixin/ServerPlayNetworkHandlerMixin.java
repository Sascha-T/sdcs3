package de.saschat.sdcs3.mixin;

import de.saschat.sdcs3.SDCS3;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;
    @Inject(at=@At("HEAD"), method="onGameMessage")
    public void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo info) {
        SDCS3.INSTANCE.chatMessage(packet, player);
    }
}
