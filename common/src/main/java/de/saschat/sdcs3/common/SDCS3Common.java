package de.saschat.sdcs3.common;

import de.saschat.sdcs3.common.config.Config;
import de.saschat.sdcs3.common.config.ConfigAdaptorArguments;
import de.saschat.sdcs3.common.config.ConfigManager;
import de.saschat.sdcs3.common.handler.Handler;
import de.saschat.sdcs3.common.handler.HandlerManager;
import de.saschat.sdcs3.common.message.sdcs.IMessage;
import de.saschat.sdcs3.common.message.mc.MMessage;

import java.util.LinkedList;
import java.util.List;

public class SDCS3Common {
    public Config CONFIG;
    public List<Handler> HANDLERS = new LinkedList<>();
    public List<Handler.Receiver> RECEIVERS = new LinkedList<>();
    private ConfigAdaptorArguments arg;

    public void initialize(ConfigAdaptorArguments arguments) {
        arg = arguments;
        try {
            CONFIG = ConfigManager.getConfig().newInstance();
            CONFIG.load(arguments, true);

            List<Handler> handlers = HandlerManager.getAllEnabled(CONFIG);
            for (Handler r: handlers) {
                boolean loaded = r.loadIdentity(CONFIG);
                if(loaded) {
                    r.addReceiver(this::receiveMessage);
                    HANDLERS.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("SDCS3 Common - Loaded!");
        if(HANDLERS.size() == 0)
            System.out.println("   - No handlers loaded. Is this intentional?");
    }

    public static boolean shutdown = false;
    public void shutdown() {
        if(!shutdown) {
            shutdown = true;
            CONFIG.save(arg);
            for (Handler r: HANDLERS) {
                r.shutdown();
                HANDLERS.remove(r);
            }
            System.gc();
        }
    }

    // @TODO: Implement crosstalk between platforms (ex. Discord -> Telegram)
    public void sendMessage(MMessage msg) {
        for (Handler r: HANDLERS) {
            r.send(msg);
        }
    }
    public void receiveMessage(IMessage message) {
        for (Handler.Receiver r: RECEIVERS) {
            r.receive(message);
        }
    }

    public void addReceiver(Handler.Receiver receiver) {
        RECEIVERS.add(receiver);
    }
    public void removeReceiver(Handler.Receiver receiver) {
        RECEIVERS.remove(receiver);
    }
}
