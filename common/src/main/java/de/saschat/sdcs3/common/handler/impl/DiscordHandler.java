package de.saschat.sdcs3.common.handler.impl;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.saschat.sdcs3.common.config.Config;
import de.saschat.sdcs3.common.handler.Handler;
import de.saschat.sdcs3.common.message.mc.impl.*;
import de.saschat.sdcs3.common.message.mc.MMessage;
import de.saschat.sdcs3.common.message.sdcs.impl.TextMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class DiscordHandler extends Handler implements EventListener {
    public JDA jda;
    public TextChannel mainChannel;
    public Webhook wh;
    public WebhookClient cl;
    public Config cfg;

    public boolean consoleEnabled;
    public boolean consoleAsInput;
    public long requiredPermissions = 8;
    public TextChannel consoleChannel;

    @Override
    public boolean isEnabled(Config config) {
        return config.get("discord.enabled").equals("true");
    }

    @Override
    public boolean loadIdentity(Config config) {
        cfg = config;
        if (!config.get("discord.enabled").equals("true"))
            return false;
        if (config.get("discord.token") == null)
            return false;
        if (config.get("discord.mainChannel") == null)
            return false;
        String token = config.get("discord.token");
        JDABuilder builder = JDABuilder.createDefault(token, EnumSet.allOf(GatewayIntent.class)).addEventListeners(this);
        try {
            jda = builder.build();
            jda.awaitReady();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        String id = config.get("discord.mainChannel");
        mainChannel = jda.getTextChannelById(id);
        if (mainChannel == null) {
            jda.shutdownNow();
            return false;
        }

        Webhook ret = getWebhook();
        if (ret == null) {
            ret = mainChannel.createWebhook("SDCS3").complete();
        }
        if (ret == null) {
            jda.shutdownNow();
            return false;
        }
        wh = ret;
        cl = WebhookClient.withUrl(wh.getUrl());

        if (config.get("discord.consoleEnabled").equals("true")) {
            System.out.println("Console is enabled in config.");
            String id2 = config.get("discord.consoleChannel");
            consoleChannel = jda.getTextChannelById(id2);
            if (consoleChannel != null) {
                consoleEnabled = true;
                if (config.get("discord.consoleAsInput").equals("true"))
                    consoleAsInput = true;
            } else {
                System.out.println("Failed to create console channel.");
            }
            String c = config.get("commands.requiredPermissions");
            if(c != null) {
                try {
                    long e = Long.parseLong(c);
                    requiredPermissions = e;
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

    public Webhook getWebhook() {
        List<Webhook> whs = mainChannel.retrieveWebhooks().complete();
        for (Webhook wh : whs) {
            if (wh.getName().equals("SDCS3")) {
                return wh;
            }
        }
        System.out.println("Unable to get webhook?");
        return null;
    }

    private char[] bannedCharacters = {(char) 0x202E, (char) 0x202D, (char) 0x202A, (char) 0x202B, (char) 0x202C};

    public String sanitize(String msg) {
        for (char ch : bannedCharacters) {
            msg = msg.replace(Character.toString(ch), "");
        }
        while (msg.matches(".*@everyone.*")) {
            msg = msg.replace("@everyone", "\\@<everyone>");
        }
        while (msg.matches(".*@here.*")) {
            msg = msg.replace("@here", "\\@<here>");
        }
        return msg;
    }

    public void sendReliable(WebhookMessage msg, int rec) {
        if (rec > 5)
            return;
        cl.send(msg).handle((readonlyMessage, throwable) -> {
            if (throwable != null) {
                wh = getWebhook();
                if (wh == null) {
                    System.out.println("Unable to get webhook?");
                    return null;
                }
                cl = WebhookClient.withUrl(wh.getUrl());
                sendReliable(msg, rec + 1);
            }
            return null;
        });
    }


    @Override
    public void send(MMessage _msg) {
        if (_msg instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) _msg;
            WebhookMessageBuilder builder = new WebhookMessageBuilder();

            builder.setAvatarUrl("https://crafatar.com/avatars/" + msg.User.UUID);

            builder.setUsername(msg.User.Username);

            builder.setContent(sanitize(msg.Content));

            WebhookMessage whmsg = builder.build();
            sendReliable(whmsg, 0);
        }
        if (_msg instanceof ServerMessage) {
            ServerMessage.ServerAction action = ((ServerMessage) _msg).Action;
            switch (action) {
                case START:
                    mainChannel.sendMessage(cfg.get("discord.startMessage")).queue();
                    break;
                case STOP:
                    mainChannel.sendMessage(cfg.get("discord.stopMessage")).queue();
                    break;
            }
        }
        if (_msg instanceof ConnectionMessage) {
            ConnectionMessage.ConnectionAction action = ((ConnectionMessage) _msg).Action;
            switch (action) {
                case JOIN:
                    mainChannel.sendMessage(cfg.get("discord.joinMessage").replace("%p", _msg.User.Username)).queue();
                    break;
                case LEAVE:
                    mainChannel.sendMessage(cfg.get("discord.leaveMessage").replace("%p", _msg.User.Username)).queue();
                    break;
            }
        }
        if (_msg instanceof DeathMessage) {
            DeathMessage msg = (DeathMessage) _msg;
            mainChannel.sendMessage(cfg.get("discord.deathMessage").replace("%s", msg.Text)).queue();
        }
        if (_msg instanceof AdvancementMessage) {
            AdvancementMessage msg = (AdvancementMessage) _msg;
            switch (msg.Type) {
                case TASK:
                    mainChannel.sendMessage(cfg.get("discord.advancementMessage").replace("%p", msg.User.Username).replace("%a", msg.AdvancementName)).queue();
                    break;
                case CHALLENGE:
                    mainChannel.sendMessage(cfg.get("discord.challengeMessage").replace("%p", msg.User.Username).replace("%a", msg.AdvancementName)).queue();
                    break;
            }
        }
        if(_msg instanceof AnnouncementMessage) {
            AnnouncementMessage msg = (AnnouncementMessage) _msg;
            mainChannel.sendMessage(msg.Content).queue();
        }
        if (consoleEnabled) {
            if (_msg instanceof ConsoleMessage) {
                ConsoleMessage msg = (ConsoleMessage) _msg;
                String text = "";
                for (String line : msg.Lines) {
                    text += line;
                }
                consoleChannel.sendMessage(text).queue();
            }
        }
    }

    @Override
    public void shutdown() {
        jda.shutdownNow();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            System.out.println("Discord ready!");
        }
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent recv = (MessageReceivedEvent) event;
            if (recv.getAuthor().isBot())
                return;
            if (recv.getChannel().getId().equals(mainChannel.getId())) {

                Message msg = recv.getMessage();
                TextMessage broadcast = new TextMessage();
                broadcast.Content = msg.getContentDisplay();
                broadcast.Name = msg.getAuthor().getName() + "#" + msg.getAuthor().getDiscriminator();

                String nick = recv.getGuild().getMember(msg.getAuthor()).getNickname();
                if (nick != null)
                    broadcast.Nickname = nick;
                else
                    broadcast.Nickname = msg.getAuthor().getName();
                broadcast.Picture = msg.getAuthor().getAvatarUrl();
                broadcast.Service = "Discord";

                List<Message.Attachment> attachments_d = msg.getAttachments();
                List<TextMessage.Attachment> attachments_i = new LinkedList<>();

                for (Message.Attachment a : attachments_d) {
                    TextMessage.Attachment attachment = new TextMessage.Attachment();
                    attachment.Filename = a.getFileName();
                    attachment.Url = a.getUrl();
                    attachments_i.add(attachment);
                }

                broadcast.Attachments = attachments_i;

                broadcastReceiver(broadcast);
            }
            if(consoleAsInput) {
                if(recv.getChannel().getId().equals(consoleChannel.getId())) {
                    Message msg = recv.getMessage();

                    Member m = msg.getGuild().getMember(msg.getAuthor());
                    if(!m.hasPermission(Permission.getPermissions(requiredPermissions)))
                        return;

                    de.saschat.sdcs3.common.message.sdcs.impl.CommandMessage broadcast = new de.saschat.sdcs3.common.message.sdcs.impl.CommandMessage();
                    broadcast.Command = msg.getContentRaw();
                    broadcastReceiver(broadcast);
                }
            }
        }
    }
}
