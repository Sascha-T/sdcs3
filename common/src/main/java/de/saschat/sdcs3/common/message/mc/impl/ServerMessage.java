package de.saschat.sdcs3.common.message.mc.impl;

import de.saschat.sdcs3.common.message.mc.MMessage;

public class ServerMessage extends MMessage {
    public ServerAction Action;
    public enum ServerAction {
        START, STOP
    }
}
