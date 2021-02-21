package de.saschat.sdcs3.mixin;

import com.mojang.authlib.GameProfile;
import de.saschat.sdcs3.SDCS3;
import de.saschat.sdcs3.common.message.mc.MMessage;
import de.saschat.sdcs3.common.message.mc.impl.ConnectionMessage;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow
    private GameProfile profile;
    @Inject(at=@At("HEAD"), method="acceptPlayer")
    public void acceptPlayer(CallbackInfo ci) {
        ConnectionMessage msg = new ConnectionMessage(); // This may not need to be here, but I put it here because of a stupid mistake, so it's staying here ^.^
        MMessage.Person person = new MMessage.Person();
        person.Username = profile.getName();
        person.UUID = profile.getId().toString();
        msg.Action = ConnectionMessage.ConnectionAction.JOIN;
        msg.User = person;
        SDCS3.INSTANCE.common.sendMessage(msg);
    }
}
