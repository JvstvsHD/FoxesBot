package de.jvstvshd.foxesbot.utils;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * A custom output stream for logging with the logging with {@link Logger}
 */
public class CustomOutputStream extends OutputStream {
    Logger logger;
    private final Level levels;
    StringBuilder stringBuilder;
    private TextChannel channel = null;

    public CustomOutputStream(Logger logger, Level levelMap) {
        this.logger = logger;
        this.levels = levelMap;
        stringBuilder = new StringBuilder();
    }

    @Override
    public final void write(int i) {
        char c = (char) i;
        if (c == '\r' || c == '\n') {
            if (stringBuilder.length() > 0) {
                logger.log(levels, stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        } else
            stringBuilder.append(c);
    }
}