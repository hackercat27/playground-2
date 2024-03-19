package ca.hackercat.playground.io;


import ca.hackercat.logging.Logger;
import ca.hackercat.playground.PGWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PGKeyboard implements KeyListener {

    private static final Logger LOGGER = Logger.get(PGKeyboard.class);

    private PGKeyboard() {}
    private static PGKeyboard instance;
    public static PGKeyboard get() {
        if (instance == null) {
            instance = new PGKeyboard();
        }
        return instance;
    }

    private static PGWindow window;

    public static void setWindow(PGWindow window) {
        PGKeyboard.window = window;
    }

    public static void update() {
        for (int i = 0; i < KEY_COUNT; i++) {
            get().lastHeld[i] = get().held[i];
        }
    }

    private final static int KEY_COUNT = 4096;

    private final boolean[] lastHeld = new boolean[KEY_COUNT];
    private final boolean[] held = new boolean[KEY_COUNT];


    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KEY_COUNT || code < 0) {
            LOGGER.error("Code '" + code + "' is out of range!");
            return;
        }

        held[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KEY_COUNT || code < 0) {
            LOGGER.error("Code '" + code + "' is out of range!");
            return;
        }
        held[code] = false;
    }

    public boolean isKeyHeld(int code) {
        if (code >= KEY_COUNT || code < 0) {
            LOGGER.error("Code '" + code + "' is out of range!");
            return false;
        }
        return held[code];
    }

    public boolean isKeyPressed(int code) {
        if (code < 0 || code >= KEY_COUNT) {
            LOGGER.error("Code '" + code + "' is out of range!");
            return false;
        }

        return held[code] && !lastHeld[code];

    }
}
