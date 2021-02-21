package de.saschat.sdcs3.common.config;

import de.saschat.sdcs3.common.config.impl.JSONConfig;

import java.util.LinkedList;

public class ConfigManager {
    protected static LinkedList<Class<? extends Config>> CONFIGS = new LinkedList<>();
    static {
        CONFIGS.push(JSONConfig.class);
    }
    public static Class<? extends Config> getConfig() {
        return CONFIGS.getFirst();
    }
    public static void registerConfigType(Class<? extends Config> adaptor) {
        CONFIGS.add(0, adaptor);
    }
}
