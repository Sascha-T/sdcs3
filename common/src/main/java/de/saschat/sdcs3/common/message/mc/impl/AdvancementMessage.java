package de.saschat.sdcs3.common.message.mc.impl;

import de.saschat.sdcs3.common.message.mc.MMessage;

public class AdvancementMessage extends MMessage {
    public String AdvancementName;
    public AdvancementType Type;
    public enum AdvancementType {
        TASK,
        CHALLENGE
    }
}
