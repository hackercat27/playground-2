package ca.hackercat.playground.util;

import ca.hackercat.logging.Logger;

public class PGArgumentParser {

    private static final Logger LOGGER = Logger.get(PGArgumentParser.class);

    private String[] args;

    public PGArgumentParser(String[] args) {
        this.args = args;
    }

    public String getArg(String flag) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(flag)) {
                return args[i + 1];
            }
        }
        return null;
    }
    public int getInt(String flag) {
        try {
            return Integer.parseInt(getArg(flag));
        }
        catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return 0;
    }
    public long getLong(String flag) {
        try {
            return Long.parseLong(getArg(flag));
        }
        catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return 0;
    }
    public double getDouble(String flag) {
        try {
            return Double.parseDouble(getArg(flag));
        }
        catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return 0;
    }
    public float getFloat(String flag) {
        try {
            return Float.parseFloat(getArg(flag));
        }
        catch (NumberFormatException e) {
            LOGGER.error(e);
        }
        return 0;
    }
    public boolean hasFlag(String flag) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(flag)) {
                return true;
            }
        }
        return false;
    }

}
