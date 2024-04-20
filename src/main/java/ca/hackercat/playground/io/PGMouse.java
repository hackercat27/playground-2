package ca.hackercat.playground.io;


import ca.hackercat.logging.Logger;
import ca.hackercat.playground.PGWindow;
import ca.hackercat.playground.math.PGMath;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class PGMouse implements MouseListener, MouseMotionListener, MouseWheelListener {


    private static final int BUTTON_COUNT = 10;

    public static final int BUTTON_LEFT = MouseEvent.BUTTON1;
    public static final int BUTTON_RIGHT = MouseEvent.BUTTON3;
    public static final int BUTTON_MIDDLE = MouseEvent.BUTTON2;

    public static final int BUTTON_WHEEL_UP = BUTTON_COUNT + 1;
    public static final int BUTTON_WHEEL_DOWN = BUTTON_COUNT + 2;

    private boolean locked;

    private static final Logger LOGGER = Logger.get(PGMouse.class);
    private static PGMouse instance;
    public static PGMouse get() {
        if (instance == null) {
            instance = new PGMouse();
        }
        return instance;
    }

    private boolean[] lastHeld = new boolean[BUTTON_COUNT];
    private boolean[] held = new boolean[BUTTON_COUNT];

    private static PGWindow window;

    public static void setWindow(PGWindow w) {
        PGMouse.window = w;
    }

    private double x = 0;
    private double y = 0;
    private double lockedDX = 0;
    private double lockedDY = 0;
    private double vScroll = 0;

    private double lastX = 0;
    private double lastY = 0;
    private double lastScroll;

    private boolean ignoreMouseEvents = false;

    public static void update() {
        for (int i = 0; i < BUTTON_COUNT; i++) {
            get().lastHeld[i] = get().held[i];
        }

        PGMouse m = get();

        m.lastX = m.x;
        m.lastY = m.y;

//        if (m.locked) {
//            m.ignoreMouseEvents = true;
//            m.robot.mouseMove(window.getX() + window.getWidth() / 2, window.getY() + window.getHeight() / 2);
//            m.ignoreMouseEvents = false;
//
//            Component x = m.mouse.getComponent(Component.Identifier.Axis.X);
//            Component y = m.mouse.getComponent(Component.Identifier.Axis.Y);
//
//            m.lockedDX = x.getPollData();
//            m.lockedDY = y.getPollData();
//        }


//            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
//
//            Controller firstMouse = null;
//            for (int i = 0; i < controllers.length && firstMouse == null; i++) {
//                if (controllers[i].getType() == Controller.Type.MOUSE) {
//                    // Found a mouse
//                    firstMouse = controllers[i];
//                }
//            }
//
//
//
//            if (firstMouse != null) {
//                Component[] components = firstMouse.getComponents();
//                StringBuilder buffer = new StringBuilder();
//
//                for (int i = 0; i < components.length; i++) {
//
//                    if (i > 0) {
//                        buffer.append(", ");
//                    }
//                    buffer.append(components[i].getName());
//                    buffer.append(": ");
//                    if (components[i].isAnalog()) {
//                        /* Get the value at the last poll of this component */
//                        buffer.append(components[i].getPollData());
//                    } else {
//                        if (components[i].getPollData() == 1.0f) {
//                            buffer.append("On");
//                        } else {
//                            buffer.append("Off");
//                        }
//                    }
//                }
//                LOGGER.log(buffer);
//            }
//            else {
//                LOGGER.log("Null");
//            }

//        }
        m.lastScroll = m.vScroll;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (ignoreMouseEvents) {
            return;
        }
        int button = e.getButton();
        if (button < 0 || button >= BUTTON_COUNT) {
            LOGGER.error("Button '" + button + "' out of range!");
            return;
        }
        held[button] = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (ignoreMouseEvents) {
            return;
        }
        int button = e.getButton();
        if (button < 0 || button >= BUTTON_COUNT) {
            LOGGER.error("Button '" + button + "' out of range!");
            return;
        }
        held[button] = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (ignoreMouseEvents) {
            return;
        }
        updatePos(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (ignoreMouseEvents) {
            return;
        }
        updatePos(e);
    }

    private void updatePos(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (ignoreMouseEvents) {
            return;
        }
        double delta = e.getPreciseWheelRotation();
        vScroll += delta;
    }

    public double getX() {
        double scale;
        if (window.isAutoScale()) {
            scale = (double) window.getHeight() / window.getInternalHeight();
        }
        else {
            scale = 1;
        }
        return x / scale;
    }
    public double getY() {
        double scale;
        if (window.isAutoScale()) {
            scale = (double) window.getHeight() / window.getInternalHeight();
        }
        else {
            scale = 1;
        }
        return y / scale;
    }

    public boolean isButtonHeld(int button) {
        if (button < 0 || button >= BUTTON_COUNT) {
            LOGGER.error("Button '" + button + "' out of range!");
            return false;
        }
        return held[button];
    }

    public boolean isButtonPressed(int button) {
        if (button == BUTTON_WHEEL_UP) {
            return PGMath.round(getDScroll()) < 0;
        }
        if (button == BUTTON_WHEEL_DOWN) {
            return PGMath.round(getDScroll()) > 0;
        }
        if (button < 0 || button >= BUTTON_COUNT) {
            LOGGER.error("Button '" + button + "' out of range!");
            return false;
        }
        return held[button] && !lastHeld[button];
    }
    public boolean isButtonReleased(int button) {
        if (button < 0 || button >= BUTTON_COUNT) {
            LOGGER.error("Button '" + button + "' out of range!");
            return false;
        }
        return !held[button] && lastHeld[button];
    }

    public double getDX() {
        if (locked) {
            return lockedDX;
        }
        return x - lastX;
    }
    public double getDY() {
        if (locked) {
            return lockedDY;
        }
        return y - lastY;
    }

    public double getScroll() {
        return vScroll;
    }

    public double getDScroll() {
        return vScroll - lastScroll;
    }

    // TODO: Figure out how to implement me
    /**
     * This is not currently implemented.
     *
     * @param grabbed The desired mouse grabbed state
     */
    public void setGrabbed(boolean grabbed) {
        this.locked = grabbed;
    }

    public boolean isGrabbed() {
        return locked;
    }
}
