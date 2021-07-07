package de.jvstvshd.foxesbot.utils;

import de.jvstvshd.foxesbot.FoxesBot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public record Logging(Logger logger, FoxesBot bot) {

    public void init() {
        System.setOut(new PrintStream(new CustomOutputStream(logger, Level.INFO)));
        System.setErr(new PrintStream(new CustomOutputStream(logger, Level.FATAL)));
    }
}
