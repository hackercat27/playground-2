package ca.hackercat.playground.io;

import ca.hackercat.logging.Logger;
import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class PGFileUtils {

    private static BufferedImage MISSING_TEXTURE = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    private static final Logger LOGGER = Logger.get(PGFileUtils.class);

    static {
        Graphics2D g2 = MISSING_TEXTURE.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, MISSING_TEXTURE.getWidth(), MISSING_TEXTURE.getHeight());
        g2.setColor(Color.MAGENTA);
        g2.fillRect(MISSING_TEXTURE.getWidth() / 2, 0, MISSING_TEXTURE.getWidth() / 2, MISSING_TEXTURE.getHeight() / 2);
        g2.fillRect(0, MISSING_TEXTURE.getHeight() / 2, MISSING_TEXTURE.getWidth() / 2, MISSING_TEXTURE.getHeight() / 2);
        g2.dispose();
    }

    /**
     * Returns the InputStream associated with the given path.
     * <p>
     * Path separators should be forward slashes. If the first character
     * of the path is a forward slash, then the returned InputStream will
     * be from the program resources. Else, the returned InputStream will
     * be from the system's filesystem.
     *
     * @param path The path to represent the InputStream.
     * @return The InputStream associated with the given path.
     */
    public static InputStream getInputStream(String path) {
        if (path == null) {
            return null;
        }

        InputStream in;

        if (path.startsWith("/")) {
            in = PGFileUtils.class.getResourceAsStream(path);
        }
        else {
            try {
                in = new FileInputStream(path);
            }
            catch (FileNotFoundException e) {
                LOGGER.error(e);
                return null;
            }
        }

        if (in == null) {
            LOGGER.error("InputStream from filepath '" + path + "' == null!");
        }
        return in;
    }

    public static BufferedImage getImage(String path) {
        try {
            InputStream in = getInputStream(path);
            if (in == null) {
                return MISSING_TEXTURE;
            }
            return ImageIO.read(in);
        }
        catch (IOException | NullPointerException e) {
            LOGGER.error(e);
        }
        return MISSING_TEXTURE;
    }

    public static String getContents(String path) {
        StringBuilder strb = new StringBuilder();
        InputStream in = getInputStream(path);
        if (in == null) {
            return null;
        }
        Scanner scan = new Scanner(in);
        scan.useDelimiter("");
        while (scan.hasNext()) {
            strb.append(scan.next());
        }
        return strb.toString();
    }


    public static void writeImage(BufferedImage out, String outPath) {

        try {
            ImageIO.write(out, "PNG", new File(outPath));
        }
        catch (IOException e) {
            LOGGER.error(e);
        }

    }

    public static void write(String out, String outPath) {
        if (out == null) {
            LOGGER.warn("out == null!");
            return;
        }

        makeDirectories(new File(outPath));

        try (FileWriter fw = new FileWriter(outPath)) {
            fw.write(out);
        }
        catch (IOException e) {
            LOGGER.error(e);
        }

    }

    public static void makeDirectories(File file) {
//        File dir = new File(file.getAbsolutePath().replaceAll("/.*?$", ""));
//        dir.mkdirs();
    }
}
