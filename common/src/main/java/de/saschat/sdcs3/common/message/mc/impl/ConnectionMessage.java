package de.saschat.sdcs3.common.message.mc.impl;

import de.saschat.sdcs3.common.message.mc.MMessage;

public class ConnectionMessage extends MMessage {
    public ConnectionMessage.ConnectionAction Action;
    public enum ConnectionAction {
        JOIN, LEAVE
    }
}
