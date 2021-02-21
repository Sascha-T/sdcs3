package de.saschat.sdcs3;

import de.saschat.sdcs3.common.message.mc.impl.ConsoleMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Plugin(
    name = "ConsoleAppender",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE)
public class ConsoleAppender extends AbstractAppender {
    Queue<String> lines = new ConcurrentLinkedQueue<String>();
    int lineSize = 0;
    Timer timer = new Timer();

    protected ConsoleAppender(String name, Filter filter) {
        super(name, filter, null);

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addAppender(this);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                flush();
            }
        }, 5000, 5000);
        this.start();
    }

    @PluginFactory
    public static ConsoleAppender createAppender(
        @PluginAttribute("name") String name,
        @PluginElement("Filter") Filter filter) {
        return new ConsoleAppender(name, filter);
    }

    public synchronized void flush() {
        List<String> toSend = new LinkedList<>();
        int len = 0;
        for (String a: lines) {
            if(len + a.length() < 2000) {
                toSend.add(a);
                len += a.length();
                lines.remove(a);
            } else {
                break;
            }
        }
        if(toSend.size() == 0)
            return;
        ConsoleMessage msg = new ConsoleMessage();
        msg.Lines = toSend.toArray(new String[0]);
        SDCS3.INSTANCE.common.sendMessage(msg);
        lineSize -= len;
    }

    @Override
    public synchronized void append(LogEvent event) {
        String line = event.getMessage().getFormattedMessage();

        LocalDateTime myDateObj = LocalDateTime.ofEpochSecond(event.getTimeMillis() / 1000, 0, ZoneOffset.UTC);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        String formattedDate = myDateObj.format(myFormatObj);
        lineSize += line.length();
        lines.add("[" + formattedDate + "] [" + event.getLoggerName() + "]" + line + "\n");

        if(lineSize > 2000) {
            flush();
        }
    }
}
