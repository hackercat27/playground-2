package ca.hackercat.playground;

import ca.hackercat.playground.io.PGWindowState;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

/**
 * Interface representing a customizable frame for a playground application.
 */
public interface PGFrame {

    /**
     * Sets the state of the frame.
     *
     * @param state the state to set
     */
    void setState(PGWindowState state);

    /**
     * Adds a mouse motion listener to the frame.
     *
     * @param mml the mouse motion listener to add
     */
    void addMouseMotionListener(MouseMotionListener mml);

    /**
     * Adds a mouse listener to the frame.
     *
     * @param ml the mouse listener to add
     */
    void addMouseListener(MouseListener ml);

    /**
     * Adds a mouse wheel listener to the frame.
     *
     * @param mwl the mouse wheel listener to add
     */
    void addMouseWheelListener(MouseWheelListener mwl);

    /**
     * Adds a key listener to the frame.
     *
     * @param kl the key listener to add
     */
    void addKeyListener(KeyListener kl);

    /**
     * Adds a window listener to the frame.
     *
     * @param wl the window listener to add
     */
    void addWindowListener(WindowListener wl);

    /**
     * Sets the icon image of the frame.
     *
     * @param image the icon image to set
     */
    void setIconImage(BufferedImage image);

    /**
     * Sets the visibility of the frame.
     *
     * @param visible {@code true} if the frame should be visible, {@code false} otherwise
     */
    void setVisible(boolean visible);

    /**
     * Sets the title of the frame.
     *
     * @param title the title to set
     */
    void setTitle(String title);

    /**
     * Polls the window for any events.
     */
    void poll();

    /**
     * Draws the frame.
     */
    void drawFrame(double targetTickTimeMillis);

    void close();
}