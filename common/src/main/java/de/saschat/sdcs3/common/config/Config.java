package de.saschat.sdcs3.common.config;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;

public abstract class Config {
    public Config() {}
    protected HashMap<String, String> MEMORY = new HashMap<>();

    public abstract String get_raw(String key);
    public abstract void set_raw(String key, String value);

    public String get(String key) {
        String ret = get_raw(key);
        if(ret == null) {
            String def = DEFAULT_CONFIG.get(key);
            if(def == null) {
                return null;
            }
            ret = def;
            set_raw(key, def);
        }
        return ret;
    }
    public void set(String key, String value) {
        set_raw(key, value);
    }

    public abstract void save(ConfigAdaptorArguments a);
    public abstract void load(ConfigAdaptorArguments a, boolean loadDefault);
    public abstract void load_memory(HashMap<String, String> config);

    public static HashMap<String, String> DEFAULT_CONFIG = new HashMap<>();
    static {
        DEFAULT_CONFIG.put("discord.enabled", "false");
        DEFAULT_CONFIG.put("discord.token", "put token here");
        DEFAULT_CONFIG.put("discord.mainChannel", "put main channel here");
        DEFAULT_CONFIG.put("discord.consoleEnabled", "false");
        DEFAULT_CONFIG.put("discord.consoleChannel", "put console channel");
        DEFAULT_CONFIG.put("discord.consoleAsInput", "true");

        DEFAULT_CONFIG.put("discord.startMessage", ":white_check_mark: Server has started.");
        DEFAULT_CONFIG.put("discord.stopMessage", ":octagonal_sign: Server has stopped.");
        DEFAULT_CONFIG.put("discord.joinMessage", ":inbox_tray: **%p** has joined the game.");
        DEFAULT_CONFIG.put("discord.leaveMessage", ":outbox_tray: **%p** has left the game.");
        DEFAULT_CONFIG.put("discord.deathMessage", ":skull: **%s**");
        DEFAULT_CONFIG.put("discord.advancementMessage", ":star: **%p** has made the advancement **%a**");
        DEFAULT_CONFIG.put("discord.challengeMessage", ":star2: **%p** has completed the challenge **%a**");

        DEFAULT_CONFIG.put("commands.enabled", "false");
        DEFAULT_CONFIG.put("commands.requiredPermissions", "8");
        DEFAULT_CONFIG.put("commands.prefix", "!cmd");
    }
}
