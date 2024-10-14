package ca.hackercat.playground.io;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.ExitListener;
import ca.hackercat.playground.PGObject;
import xmlwise.Plist;
import xmlwise.XmlParseException;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PGInputManager implements PGObject, ExitListener {

    private static final Logger LOGGER = Logger.get(PGInputManager.class);

    private PGKeyboard kl = PGKeyboard.get();
    private PGMouse ml = PGMouse.get();
    private PGGamepad cl = PGGamepad.get(1);

    private static final String keybindsPath = "binds.plist";

    private Map<String, String> keybinds;

    private static PGInputManager instance;

    public static PGInputManager get() {
        if (instance == null) {
            instance = new PGInputManager();
        }
        return instance;
    }

    private PGInputManager() {

        try {
            keybinds = new HashMap<>();
            Map<String, Object> k = Plist.fromXml(Objects.requireNonNullElse(PGFileUtils.getContents(keybindsPath), ""));

            for (String str : k.keySet()) {
                Object o = k.get(str);
                if (o instanceof String s) keybinds.put(str, s);
            }
        }
        catch (XmlParseException e) {
            LOGGER.error("Failed to init keybinds, loading from defaults...");
            initDefaultBinds();
            saveKeybinds();
        }

    }

    private void initDefaultBinds() {
        keybinds = new HashMap<>();

        setBind("toggle_fullscreen", "+f11");
    }

    public void saveKeybinds() {
        if (keybinds == null) {
            return;
        }
        String plist = Plist.toPlist(keybinds);
        PGFileUtils.write(plist, keybindsPath);
        LOGGER.log("Saved keybinds to '" + keybindsPath + "'");
    }

    public boolean actionAsserted(String action) {
        return isKeyActionAsserted(getKey(action));
    }

    public void setBind(String action, String key) {
        if (keybinds == null) {
            return;
        }
        keybinds.put(action, key);
    }
    public String getKey(String action) {
        if (keybinds == null) {
            return null;
        }
        return keybinds.get(action);
    }


    private boolean isKeyActionAsserted(String keyBind) {
        if (keyBind == null) {
            return false;
        }

        String[] ands = keyBind.split("\\|");

        for (String bind : ands) {

            String[] keys = bind.split("&");


            boolean worked = true;
            for (String key : keys) {
                if (!isKeyPressed(key)) {
                    worked = false;
                }
            }

            if (worked) {
                return true;
            }

        }
        return false;
    }

    private boolean isKeyPressed(String key) {


        enum PressType {
            HOLD, PRESS, RELEASE
        }


        String keyName = key.replaceAll("[+-]", "").toLowerCase(Locale.ROOT);


        PressType type;

        if (key.startsWith("+")) {
            type = PressType.PRESS;
        }
        else if (key.startsWith("-")) {
            type = PressType.RELEASE;
        }
        else {
            type = PressType.HOLD;
        }

        return switch (keyName) {
            case "mwheelup" -> ml.isButtonPressed(PGMouse.BUTTON_WHEEL_UP);
            case "mwheeldown" -> ml.isButtonPressed(PGMouse.BUTTON_WHEEL_DOWN);
            case "mouse1" ->
                    type == PressType.PRESS? ml.isButtonPressed(PGMouse.BUTTON_LEFT) : ml.isButtonHeld(PGMouse.BUTTON_LEFT);
            case "mouse2" -> type == PressType.PRESS? ml.isButtonPressed(PGMouse.BUTTON_MIDDLE) : ml.isButtonHeld(PGMouse.BUTTON_MIDDLE);
            case "mouse3" ->
                    type == PressType.PRESS? ml.isButtonPressed(PGMouse.BUTTON_RIGHT) : ml.isButtonHeld(PGMouse.BUTTON_RIGHT);
            case "mouse4" -> type == PressType.PRESS? ml.isButtonPressed(PGMouse.BUTTON4) : ml.isButtonHeld(PGMouse.BUTTON4);
            case "mouse5" -> type == PressType.PRESS? ml.isButtonPressed(PGMouse.BUTTON5) : ml.isButtonHeld(PGMouse.BUTTON5);

            default -> {

                Field[] fields = KeyEvent.class.getFields();

                for (Field field : fields) {

                    String name = field.getName();

                    String keyNameProper = "VK_" + keyName.toUpperCase(Locale.ROOT);

                    if (keyNameProper.equals(name)) {
                        try {
                            int code = (Integer) field.get(null);

                            switch (type) {
                                case PRESS, RELEASE -> {
                                    yield kl.isKeyPressed(code);
                                }
                                case HOLD -> {
                                    yield kl.isKeyHeld(code);
                                }
                            }
                        }
                        catch (IllegalAccessException e) {
                            LOGGER.error(e);
                        }
                    }

                }

                yield false;
            }
        };

    }


    public double getCursorX() {
        return ml.getX();
    }
    public double getCursorY() {
        return ml.getY();
    }


    @Override
    public void onExit() {
        saveKeybinds();
    }
}
