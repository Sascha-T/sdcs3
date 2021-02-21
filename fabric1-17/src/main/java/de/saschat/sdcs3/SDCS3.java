package de.saschat.sdcs3;

import de.saschat.sdcs3.common.SDCS3Common;
import de.saschat.sdcs3.common.config.ConfigAdaptorArguments;
import de.saschat.sdcs3.common.message.mc.MMessage;
import de.saschat.sdcs3.common.message.mc.impl.*;
import de.saschat.sdcs3.common.message.sdcs.IMessage;

import de.saschat.sdcs3.common.message.sdcs.impl.TextMessage;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.SERVER)
public class SDCS3 implements DedicatedServerModInitializer {
    public UUID sdcs3 = UUID.fromString("b1840469-8d2f-4b9e-84cd-809709706b8c");
    public SDCS3Common common = new SDCS3Common();
    public boolean ready = false;
    public MinecraftServer server;
    public ConsoleAppender appender;
    public static SDCS3 INSTANCE;

    @Override
    public void onInitializeServer() {
        File root = FabricLoader.getInstance().getGameDir().toFile();
        ConfigAdaptorArguments arg = new ConfigAdaptorArguments();

        arg.minecraft_root = root;
        common.initialize(arg);
        common.addReceiver(this::onMessage);
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::serverStopped);
        Runtime.getRuntime().addShutdownHook(new Thread(common::shutdown));
        INSTANCE = this;
    }


    private void serverStopped(MinecraftServer minecraftServer) {
        ready = false;
        ServerMessage msg = new ServerMessage();
        msg.Action = ServerMessage.ServerAction.STOP;
        common.sendMessage(msg);
        common.shutdown();
    }

    private void serverStarted(MinecraftServer minecraftServer) {
        ready = true;
        server = minecraftServer;
        ServerMessage msg = new ServerMessage();
        msg.Action = ServerMessage.ServerAction.START;
        common.sendMessage(msg);

        appender = ConsoleAppender.createAppender("name", null);
    }

    private MutableText withColor(String text, Formatting color) {
        Style r = Style.EMPTY;
        r = r.withColor(color);
        LiteralText txt = new LiteralText(text);
        return txt.setStyle(r);
    }

    private void onMessage(IMessage iMessage) {
        if (!ready)
            return;
        System.out.print("Message received: ");
        if (iMessage instanceof de.saschat.sdcs3.common.message.sdcs.impl.CommandMessage) {
            de.saschat.sdcs3.common.message.sdcs.impl.CommandMessage imsg = (de.saschat.sdcs3.common.message.sdcs.impl.CommandMessage) iMessage;
            server.getCommandManager().execute(server.getCommandSource(), imsg.Command);
        }
        if (iMessage instanceof TextMessage) {
            TextMessage imsg = (TextMessage) iMessage;
            MutableText text = withColor("[", Formatting.GRAY);
            MutableText service = withColor(iMessage.Service, Formatting.BLUE);
            MutableText close = withColor("] ", Formatting.GRAY);

            text.append(service);
            text.append(close);

            LiteralText user = new LiteralText(iMessage.Nickname);
            Style hover = Style.EMPTY;
            hover = hover.withColor(Formatting.WHITE);
            hover = hover.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(iMessage.Name)));
            user.setStyle(hover);

            text.append(user);

            // [Discord] Sascha_T: ---
            MutableText before = withColor(":", Formatting.WHITE);
            MutableText msg = withColor(" " + imsg.Content, Formatting.WHITE);

            text.append(before);
            text.append(msg);

            System.out.println(imsg.Attachments.size());
            for (TextMessage.Attachment at : imsg.Attachments) {
                {
                    MutableText space = withColor(" ", Formatting.WHITE);
                    text.append(space);
                }
                MutableText open_2 = withColor("[", Formatting.DARK_GRAY);
                MutableText attachment = withColor(at.Filename, Formatting.DARK_BLUE);

                Style hover_2 = Style.EMPTY;
                hover_2 = hover_2.withColor(Formatting.DARK_BLUE);
                hover_2 = hover_2.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(at.Url)));
                hover_2 = hover_2.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, at.Url));
                attachment.setStyle(hover_2);

                MutableText close_2 = withColor("]", Formatting.DARK_GRAY);

                open_2.append(attachment);
                open_2.append(close_2);

                text.append(open_2);
            }

            System.out.println(text);
            server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, sdcs3);
        }
    }

    public void chatMessage(ChatMessageC2SPacket packet, ServerPlayerEntity player) {
        String msg = packet.getChatMessage();
        if (msg.startsWith("/")) {
            CommandMessage cmsg = new CommandMessage();
            cmsg.Content = msg;
            MMessage.Person p = new MMessage.Person();
            p.UUID = player.getUuidAsString();
            p.Username = player.getGameProfile().getName();
            cmsg.User = p;
            common.sendMessage(cmsg);
        } else {
            ChatMessage cmsg = new ChatMessage();
            cmsg.Content = msg;
            MMessage.Person p = new MMessage.Person();
            p.UUID = player.getUuidAsString();
            p.Username = player.getGameProfile().getName();
            cmsg.User = p;
            common.sendMessage(cmsg);
        }

    }

    public String parse(LiteralText text, Language lang) {
        String ret = text.asString();
        List<Text> texts = text.getSiblings();
        for (Text a : texts) {
            if (a instanceof LiteralText) {
                ret += parse((LiteralText) a, lang);
            } else if (a instanceof TranslatableText) {
                ret += parse((TranslatableText) a, lang);
            }
        }
        return ret;
    }

    public String parse(TranslatableText text, Language lang) {
        if (text.getArgs().length == 0) {
            return lang.get(text.getKey());
        } else {
            String ret = lang.get(text.getKey());
            if (text.getArgs().length == 1) {
                Object arg = text.getArgs()[0];
                if (arg instanceof LiteralText) {
                    ret = ret.replace("%s", parse((LiteralText) arg, lang));
                    ret = ret.replace("%1$s", parse((LiteralText) arg, lang));
                } else if (arg instanceof TranslatableText) {
                    ret = ret.replace("%s", parse((TranslatableText) arg, lang));
                    ret = ret.replace("%1$s", parse((TranslatableText) arg, lang));
                } else {
                    System.out.println(arg.getClass());
                }
            } else {
                int i = 1;
                for (Object arg : text.getArgs()) {
                    String tkey = "%" + i + "$s";
                    if (arg instanceof LiteralText) {
                        ret = ret.replace(tkey, parse((LiteralText) arg, lang));
                    } else if (arg instanceof TranslatableText) {
                        ret = ret.replace(tkey, parse((TranslatableText) arg, lang));
                    }
                    i++;
                }
            }
            return ret;
        }
    }

    public MMessage.Person getPlayer(String name) {
        MMessage.Person person = new MMessage.Person();
        ServerPlayerEntity e = server.getPlayerManager().getPlayer(name);
        person.UUID = e.getGameProfile().getId().toString();
        person.Username = name;
        return person;
    }

    public void gameMessage(Text _message, MessageType type, UUID senderUuid, CallbackInfo info) {
        if (!(_message instanceof TranslatableText))
            return;
        TranslatableText message = (TranslatableText) _message;
        String key = message.getKey();
        Language lang = Language.getInstance();
        String translated = lang.get(message.getKey());
        String text = "";
        if (key.startsWith("death")) {
            String player = parse((LiteralText) message.getArgs()[0], lang);
            text = parse(message, lang);
            DeathMessage dmsg = new DeathMessage();
            dmsg.Text = text;

            dmsg.User = getPlayer(player);

            common.sendMessage(dmsg);
        } else {
            switch (key) {
                case "chat.type.advancement.task":
                case "chat.type.advancement.challenge": { // Challenge
                    AdvancementMessage msg = new AdvancementMessage();
                    String name = parse((TranslatableText) message.getArgs()[1], lang);
                    msg.AdvancementName = name;
                    if (key.equals("chat.type.advancement.task")) {
                        msg.Type = AdvancementMessage.AdvancementType.TASK;
                    } else {
                        msg.Type = AdvancementMessage.AdvancementType.CHALLENGE;
                    }
                    String player = parse((LiteralText) message.getArgs()[0], lang);
                    msg.User = getPlayer(player);
                    common.sendMessage(msg);
                    break;
                }
                case "multiplayer.player.left": {
                    ConnectionMessage msg = new ConnectionMessage();
                    String player = parse((LiteralText) message.getArgs()[0], lang);
                    msg.User = getPlayer(player);
                    msg.Action = ConnectionMessage.ConnectionAction.LEAVE;
                    common.sendMessage(msg);
                    break;
                }
                case "chat.type.announcement": {
                    AnnouncementMessage msg = new AnnouncementMessage();
                    String player = parse((LiteralText) message.getArgs()[0], lang);
                    msg.Content = "[" + player + "] " + parse((LiteralText) message.getArgs()[1], lang);
                    // msg.User = getPlayer(player);
                    common.sendMessage(msg);
                }
                // @TODO: case "chat.type.announcement"
            }
        }

    }

}
