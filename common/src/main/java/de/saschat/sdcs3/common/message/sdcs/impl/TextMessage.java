package de.saschat.sdcs3.common.message.sdcs.impl;

import de.saschat.sdcs3.common.message.sdcs.IMessage;

import java.util.List;

public class TextMessage extends IMessage {
    public String Content;
    public List<Attachment> Attachments;
    public static class Attachment {
        public String Url;
        public String Filename;
    }
}
