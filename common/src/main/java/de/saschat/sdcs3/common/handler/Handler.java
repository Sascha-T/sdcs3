package de.saschat.sdcs3.common.handler;

import de.saschat.sdcs3.common.config.Config;
import de.saschat.sdcs3.common.message.sdcs.IMessage;
import de.saschat.sdcs3.common.message.mc.MMessage;

import java.util.LinkedList;
import java.util.List;

public abstract class Handler {
    public abstract boolean isEnabled(Config config);
    public abstract boolean loadIdentity(Config config);
    public abstract void send(MMessage msg);
    // Guaranteed to be called on a safe server shutdown;
    public abstract void shutdown();

    private List<Receiver> RECEIVERS = new LinkedList<>();

    public void addReceiver(Receiver receiver) {
        RECEIVERS.add(receiver);
    }
    public void removeReceiver(Receiver receiver) {
        RECEIVERS.remove(receiver);
    }
    protected void broadcastReceiver(IMessage message) {
        for (Receiver REC: RECEIVERS) {
            REC.receive(message);
        }
    }


    public interface Receiver {
        void receive(IMessage message);
    }
}
