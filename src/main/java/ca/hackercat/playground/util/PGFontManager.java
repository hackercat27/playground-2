package ca.hackercat.playground.util;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGFileUtils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PGFontManager {
    private PGFontManager() {}

    private static final Logger LOGGER = Logger.get(PGFontManager.class);
    private static final Map<String, Font> map = new HashMap<>();

    public static final float DEFAULT_FONT_SIZE = 12f;

    public static final String OPEN_SANS = "OpenSans";
    public static final String ROBOTO_MONO = "RobotoMono";

    static {
        register("/assets/font/OpenSans-Regular", OPEN_SANS);
        register("/assets/font/RobotoMono-Regular", ROBOTO_MONO);
    }

    public static void register(String fontPath, String name) {

        InputStream fontStream = PGFileUtils.getInputStream(fontPath);

        if (fontStream == null) {
            LOGGER.warn("Unable to load font '" + name + "', fontStream == null!");
        }
        else {
            try {
                register(Font.createFont(Font.TRUETYPE_FONT, fontStream), name);
            } catch (FontFormatException | IOException e) {
                LOGGER.error(e);
            }
        }

    }
    public static void register(Font font, String name) {
        map.put(name, font.deriveFont(DEFAULT_FONT_SIZE));
        LOGGER.log("Registered font '" + name + "'.");
    }

    public static Font get(String name) {
        Font font = map.get(name);

        if (font == null) {
            LOGGER.warn("Returning null Font, '" + name + "' was not found!");
        }

        return font;
    }
    public static Font get(String name, float size) {
        Font f = get(name);
        if (f != null) {
            return f.deriveFont(size);
        }
        return null;
    }

}
