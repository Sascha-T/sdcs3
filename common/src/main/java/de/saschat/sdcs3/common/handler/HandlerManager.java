package de.saschat.sdcs3.common.handler;

import de.saschat.sdcs3.common.config.Config;
import de.saschat.sdcs3.common.handler.impl.DiscordHandler;

import java.util.LinkedList;
import java.util.List;

public class HandlerManager {
    protected static LinkedList<Class<? extends Handler>> HANDLERS = new LinkedList<>();
    static {
        HANDLERS.push(DiscordHandler.class);
    }
    public static void registerHandlerType(Class<? extends Handler> adaptor) {
        HANDLERS.add(0, adaptor);
    }
    public static List<Handler> getAllEnabled(Config cfg) {
        List<Handler> handlers = new LinkedList<>();
        for (Class<? extends Handler> handler: HANDLERS) {
            try {
                Handler r = handler.getConstructor().newInstance();
                if(r.isEnabled(cfg))
                    handlers.add(r);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return handlers;
    }
}
