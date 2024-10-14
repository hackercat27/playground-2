package ca.hackercat.logging;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {

    private enum Level {
        INFO,
        WARN,
        ERROR
    }

    private static final PrintStream out = System.out;

    private String className;

    private static final String INFO_COLOR = "\u001b[0m";
    private static final String WARN_COLOR = "\u001b[38;2;255;212;168m";
    private static final String ERROR_COLOR = "\u001b[0;31m";
    private static final String PROPERTIES_COLOR = "\u001b[0m";

    private static final String RESET = "\u001b[0m";

    private Logger(String className) {
        this.className = className;
    }

    public static <T> Logger get(Class<T> clazz) {
        return new Logger(clazz.getSimpleName());
    }

    public void log(Object o) {
        log(o.toString());
    }
    public void log(Throwable t) {
        log(t.toString() + "\n" + getStackTrace(t));
    }
    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }
    public void log(String msg) {
        print(Level.INFO, msg);
    }

    public void warn(Object o) {
        warn(o.toString());
    }
    public void warn(Throwable t) {
        warn(t.toString() + "\n" + getStackTrace(t));
    }
    public void warnf(String format, Object... args) {
        warn(String.format(format, args));
    }
    public void warn(String msg) {
        print(Level.WARN, msg);
    }

    public void error(Object o) {
        error(o.toString());
    }
    public void error(Throwable t) {
        error(t.toString() + "\n" + getStackTrace(t));
    }
    public void errorf(String format, Object... args) {
        error(String.format(format, args));
    }
    public void error(String msg) {
        print(Level.ERROR, msg);
    }

    private String getStackTrace(Throwable t) {
        StackTraceElement[] elements = t.getStackTrace();
        StringBuilder str = new StringBuilder();

        for (StackTraceElement element : elements) {
            str.append("    ").append(element.toString()).append("\n");
        }
        return str.toString();
    }

    private void print(Level level, String message) {

        String thread = Thread.currentThread().getName();
        String name = className;

        String info = name.isBlank()? thread + "/Anonymous Class" : thread + "/" + name;

        String logPrefix;
        String color;

        switch (level) {
            case INFO -> {
                logPrefix = "[" + getTime() + "] [INFO/" + info + "] ";
                color = INFO_COLOR;
            }
            case WARN -> {
                logPrefix = "[" + getTime() + "] [WARN/" + info + "] ";
                color = WARN_COLOR;
            }
            case ERROR -> {
                logPrefix = "[" + getTime() + "] [ERROR/" + info + "] ";
                color = ERROR_COLOR;
            }
            default -> {
                logPrefix = "";
                color = "";
            }
        }

        if (message.contains("\n")) {
            StringBuilder replacement = new StringBuilder();
            replacement.append("\n");
            for (int i = 0, length = logPrefix.length(); i < length; i++) {
                replacement.append(" ");
            }
            message = message.replaceAll("\n", replacement.toString());
        }

        out.println(color + logPrefix + message + RESET);
    }

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private String getTime() {
        return dtf.format(LocalDateTime.now());
    }
}
