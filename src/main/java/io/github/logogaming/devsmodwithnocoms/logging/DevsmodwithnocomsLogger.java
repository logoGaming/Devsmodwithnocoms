package io.github.logogaming.devsmodwithnocoms.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DevsmodwithnocomsLogger {

    private static final String PREFIX_COLOR_HEX = "85DDFF";
    private static final String PREFIX_TEXT = "〘Devsmodwithnocoms〙";
    private static final String ANSI_RESET = "[0m";

    private static final Logger LOGGER = LoggerFactory.getLogger("devsmodwithnocoms");

    private DevsmodwithnocomsLogger() {
    }

    public static void debug(String message) {
        LOGGER.debug(format("A8A8A8", message));
    }

    public static void info(String message) {
        LOGGER.info(format("A8A8A8", message));
    }

    public static void warn(String message) {
        LOGGER.warn(format("FFFF99", message));
    }

    public static void error(String message) {
        LOGGER.error(format("FA795F", message));
    }

    private static String format(String messageColorHex, String message) {
        return ansiColor(PREFIX_COLOR_HEX) + PREFIX_TEXT + ANSI_RESET
                + ansiColor(messageColorHex) + message + ANSI_RESET;
    }

    private static String ansiColor(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return "[38;2;" + r + ";" + g + ";" + b + "m";
    }
}
