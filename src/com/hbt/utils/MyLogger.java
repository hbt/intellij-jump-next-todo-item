package com.hbt.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class MyLogger {
    public static Logger log;

    public static Logger getLogger() {
        if (log == null) {

            log = (Logger) Logger.getInstance(MyLogger.class);
            log.setLevel(Level.ALL);
            ConsoleAppender ca = new ConsoleAppender();
            PatternLayout layout = new PatternLayout("%d{HH:mm:ss} %-5p %c{1}:%L - %m%n");
            ca.setLayout(layout);
            ca.activateOptions();
            log.addAppender(ca);
        }

        return log;
    }
}
