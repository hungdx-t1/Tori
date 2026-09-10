package com.dianxin.tori.server.logger;

import com.dianxin.tori.server.gui.ToriServerGui;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Plugin(name = "GuiLogAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class GuiLogAppender extends AbstractAppender {

    protected GuiLogAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @PluginFactory
    public static GuiLogAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout) {

        if (layout == null) {
            layout = PatternLayout.newBuilder()
                    .withPattern("[%d{HH:mm:ss}] %-5level [%t] [%c{1}] - %msg%n")
                    .withCharset(StandardCharsets.UTF_8)
                    .build();
        }
        return new GuiLogAppender(name != null ? name : "GuiLogAppender", filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        byte[] bytes = getLayout().toByteArray(event);
        String message = new String(bytes, StandardCharsets.UTF_8);

        // Remove ANSI color codes (terminal color codes) so that the text in the TextArea is clean and neat
        String cleanMessage = message.replaceAll("\u001B\\[[;\\d]*m", "");

        ToriServerGui.appendText(cleanMessage);
    }
}