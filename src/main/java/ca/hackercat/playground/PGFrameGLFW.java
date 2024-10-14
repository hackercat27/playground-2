package ca.hackercat.playground;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGWindowState;
import ca.hackercat.playground.opengl.GLGraphics2D;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import javax.swing.JFrame;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PGFrameGLFW implements PGFrame {

    private static final int nullptr = 0;

    private static final int KEY_TYPED = -3;
    private static final int KEY_PRESSED = -2;
    private static final int KEY_RELEASED = -1;
    private static final int MOUSE_ENTERED = 0;
    private static final int MOUSE_EXITED = 1;
    private static final int MOUSE_RELEASED = 2;
    private static final int MOUSE_PRESSED = 3;
    private static final int MOUSE_CLICKED = 4;
    private static final int MOUSE_MOVED = 5;
    private static final int MOUSE_DRAGGED = 6;
    private static final int WINDOW_ICONIFIED = 7;
    private static final int WINDOW_CLOSING = 8;
    private static final int WINDOW_CLOSED = 9;
    private static final int WINDOW_OPENED = 10;
    private static final int WINDOW_DEICONIFIED = 11;
    private static final int WINDOW_ACTIVATED = 12;
    private static final int WINDOW_DEACTIVATED = 13;

    private static final Logger LOGGER = Logger.get(PGFrameGLFW.class);

    private long window = nullptr;

    // pretty annoying because to create awt events they need a window source,
    // but we. like. don't have one of those since we're using glfw and hacking
    // the rest of playground to think that it's swing instead. bruh
    private Window dummyWindow;
    private Component dummyComponent;

    private final List<KeyListener> kls = new ArrayList<>();
    private final List<MouseListener> mls = new ArrayList<>();
    private final List<MouseMotionListener> mmls = new ArrayList<>();
    private final List<MouseWheelListener> mwls = new ArrayList<>();
    private final List<WindowListener> wls = new ArrayList<>();

    private GLFWWindowCloseCallback windowCloseCallback;
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWScrollCallback scrollCallback;
    private GLFWCursorEnterCallback cursorEnterCallback;
    private GLFWWindowSizeCallback windowSizeCallback;

    private GLFWVidMode videoMode;
    private GLCapabilities glCapabilities;

    private int time = 1;

    private int width = 640;
    private int height = 480;

    private GLGraphics2D graphics2D;
    private PGWindow parent;

    public PGFrameGLFW(PGWindow parent, int width, int height, String title) {

        dummyWindow = new Window(new JFrame());
        dummyComponent = new JFrame();

        this.parent = parent;

        this.width = width;
        this.height = height;

        initGLFW(width, height, title);
        initOpenGL();

        graphics2D = new GLGraphics2D(parent);

    }

    private void initGLFW(int width, int height, String name) {

        if (!Thread.currentThread().getName().equals("main")) {
            LOGGER.warn("GLFW will be initialized on a non-main thread.");
        }

        boolean init = glfwInit();

        if (!init) {
            LOGGER.error("Error initializing GLFW.");
            System.exit(-1);
        }

        window = glfwCreateWindow(width, height, name == null? "" : name, 0, 0);


        if (window == nullptr) {
            LOGGER.error("Window was not created");
            return;
        }

        videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode == null) {
            LOGGER.error("GLFW video mode == null!");
            return;
        }

        glfwSetWindowPos(window, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);

        glfwShowWindow(window);

        glfwMakeContextCurrent(window);

        initCallbacks();
    }

    private void initCallbacks() {

        windowCloseCallback = new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                WindowEvent e = new WindowEvent(dummyWindow, WindowEvent.WINDOW_CLOSING);
                alertListeners(e, WINDOW_CLOSING);
            }
        };
        windowSizeCallback = new GLFWWindowSizeCallback() {

            @Override
            public void invoke(long window, int w, int h) {
                width = w;
                height = h;
            }
        };
        scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double x, double y) {
//                MouseWheelEvent e = new MouseWheelEvent(dummyComponent, MouseWheelEvent.MOUSE_WHEEL,
//                                                        System.currentTimeMillis(), 0,
//                                                        (int) PGMath.round(x), (int) PGMath.round(y),
//                                                        0, false, MouseEvent.NOBUTTON
//                );
//                alertListeners(e);
            }
        };
        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                int swingMods = 0;

                int swingKey = convertKeyCode(key);

//                if (mods != 0) {
//                    LOGGER.log(Integer.toBinaryString(mods) + " - " + mods);
//                }
//                LOGGER.logf("key %d scancode %d action %s", key, scancode, action);

                int swingAction = action == GLFW_RELEASE? KeyEvent.KEY_RELEASED
                        : action == GLFW_PRESS? KeyEvent.KEY_PRESSED : 0;

                KeyEvent e = new KeyEvent(dummyComponent,
                                          swingAction,
                                          ++time, swingMods, swingKey, KeyEvent.getKeyText(swingKey).charAt(0)
                );
                alertListeners(e, action == GLFW_RELEASE? KEY_RELEASED : KEY_PRESSED);
            }
        };

        glfwSetWindowCloseCallback(window, windowCloseCallback);
        glfwSetScrollCallback(window, scrollCallback);
        glfwSetKeyCallback(window, keyCallback);
        glfwSetCursorPosCallback(window, cursorPosCallback);
        glfwSetMouseButtonCallback(window, mouseButtonCallback);
        glfwSetCursorEnterCallback(window, cursorEnterCallback);
        glfwSetWindowSizeCallback(window, windowSizeCallback);


    }

    private void releaseCallbacks() {

        if (windowCloseCallback != null)
            windowCloseCallback.free();
        if (scrollCallback != null)
            scrollCallback.free();
        if (cursorPosCallback != null)
            cursorPosCallback.free();
        if (mouseButtonCallback != null)
            mouseButtonCallback.free();
        if (keyCallback != null)
            keyCallback.free();
        if (cursorEnterCallback != null)
            cursorEnterCallback.free();
        if (windowSizeCallback != null)
            windowSizeCallback.free();

    }

    private void initOpenGL() {
        glCapabilities = GL.createCapabilities();
    }

    @Override
    public void setState(PGWindowState state) {
        if (window == nullptr) {
            return;
        }
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener mml) {
        synchronized (mmls) {
            mmls.add(mml);
        }
    }

    @Override
    public void addMouseListener(MouseListener ml) {
        synchronized (mls) {
            mls.add(ml);
        }
    }

    @Override
    public void addMouseWheelListener(MouseWheelListener mwl) {
        synchronized (mwls) {
            mwls.add(mwl);
        }
    }

    @Override
    public void addKeyListener(KeyListener kl) {
        synchronized (kls) {
            kls.add(kl);
        }
    }

    @Override
    public void addWindowListener(WindowListener wl) {
        synchronized (wls) {
            wls.add(wl);
        }
    }

    @Override
    public void setIconImage(BufferedImage image) {

    }

    @Override
    public void setVisible(boolean visible) {

    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void poll() {
        if (window == nullptr) {
            return;
        }
        glfwPollEvents();
    }

    @Override
    public void drawFrame(double targetFrameTimeMillis) {
        if (window == nullptr) {
            return;
        }

        glViewport(0, 0, getWidth(), getHeight());
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);


        AffineTransform af = new AffineTransform();
        // default OpenGL viewport is a 1x1 screen. adjust it to be a width x height screen.

        af.translate(-1, 1);
        af.scale(getWidth() / 2d, -getHeight() / 2d);

        graphics2D.setTransform(af);

        parent.width = getWidth();
        parent.height = getHeight();

        parent.internalWidth = getWidth();
        parent.internalHeight = getHeight();

        parent.render(graphics2D);

//        LOGGER.log("Swapping buffers");
        glfwSwapBuffers(window);

    }

    @Override
    public void close() {
        LOGGER.log("Cleaning...");
        if (window == nullptr) {
            return;
        }


        releaseCallbacks();

        glfwTerminate();

        window = nullptr;
    }

    private void alertListeners(KeyEvent e, int type) {
        synchronized (kls) {
            for (KeyListener kl : kls) {
                switch (type) {
                    case KEY_TYPED -> kl.keyTyped(e);
                    case KEY_PRESSED -> kl.keyPressed(e);
                    case KEY_RELEASED -> kl.keyReleased(e);
                }
            }
        }
    }

    private void alertListeners(MouseEvent e, int type) {
        switch (type) {
            case MOUSE_ENTERED, MOUSE_EXITED, MOUSE_CLICKED, MOUSE_PRESSED, MOUSE_RELEASED -> {
                synchronized (mls) {
                    for (MouseListener ml : mls) {
                        switch (type) {
                            case MOUSE_ENTERED -> ml.mouseEntered(e);
                            case MOUSE_EXITED -> ml.mouseExited(e);
                            case MOUSE_CLICKED -> ml.mouseClicked(e);
                            case MOUSE_PRESSED -> ml.mousePressed(e);
                            case MOUSE_RELEASED -> ml.mouseReleased(e);
                        }
                    }
                }
            }
            default -> {
                synchronized (mmls) {
                    for (MouseMotionListener mml : mmls) {
                        switch (type) {
                            case MOUSE_DRAGGED -> mml.mouseDragged(e);
                            case MOUSE_MOVED -> mml.mouseMoved(e);
                        }
                    }
                }
            }
        }
    }

    private void alertListeners(MouseWheelEvent e) {
        synchronized (mwls) {
            for (MouseWheelListener mwl : mwls) {
                mwl.mouseWheelMoved(e);
            }
        }
    }

    private void alertListeners(WindowEvent e, int type) {
        synchronized (wls) {
            for (WindowListener wl : wls) {
                switch (type) {
                    case WINDOW_OPENED -> wl.windowOpened(e);
                    case WINDOW_CLOSING -> wl.windowClosing(e);
                    case WINDOW_CLOSED -> wl.windowClosed(e);
                    case WINDOW_ACTIVATED -> wl.windowActivated(e);
                    case WINDOW_DEACTIVATED -> wl.windowDeactivated(e);
                    case WINDOW_ICONIFIED -> wl.windowIconified(e);
                    case WINDOW_DEICONIFIED -> wl.windowDeiconified(e);
                }
            }
        }
    }

    private int getHeight() {
        return height;
    }

    private int getWidth() {
        return width;
    }

    private static int convertKeyCode(int glfwKey) {

        // provided courtesy of chatgpt 3.5
        // funny ai code
        switch (glfwKey) {
            case GLFW.GLFW_KEY_SPACE: return KeyEvent.VK_SPACE;
            case GLFW.GLFW_KEY_APOSTROPHE: return KeyEvent.VK_QUOTE;
            case GLFW.GLFW_KEY_COMMA: return KeyEvent.VK_COMMA;
            case GLFW.GLFW_KEY_MINUS: return KeyEvent.VK_MINUS;
            case GLFW.GLFW_KEY_PERIOD: return KeyEvent.VK_PERIOD;
            case GLFW.GLFW_KEY_SLASH: return KeyEvent.VK_SLASH;
            case GLFW.GLFW_KEY_0: return KeyEvent.VK_0;
            case GLFW.GLFW_KEY_1: return KeyEvent.VK_1;
            case GLFW.GLFW_KEY_2: return KeyEvent.VK_2;
            case GLFW.GLFW_KEY_3: return KeyEvent.VK_3;
            case GLFW.GLFW_KEY_4: return KeyEvent.VK_4;
            case GLFW.GLFW_KEY_5: return KeyEvent.VK_5;
            case GLFW.GLFW_KEY_6: return KeyEvent.VK_6;
            case GLFW.GLFW_KEY_7: return KeyEvent.VK_7;
            case GLFW.GLFW_KEY_8: return KeyEvent.VK_8;
            case GLFW.GLFW_KEY_9: return KeyEvent.VK_9;
            case GLFW.GLFW_KEY_SEMICOLON: return KeyEvent.VK_SEMICOLON;
            case GLFW.GLFW_KEY_EQUAL: return KeyEvent.VK_EQUALS;
            case GLFW.GLFW_KEY_A: return KeyEvent.VK_A;
            case GLFW.GLFW_KEY_B: return KeyEvent.VK_B;
            case GLFW.GLFW_KEY_C: return KeyEvent.VK_C;
            case GLFW.GLFW_KEY_D: return KeyEvent.VK_D;
            case GLFW.GLFW_KEY_E: return KeyEvent.VK_E;
            case GLFW.GLFW_KEY_F: return KeyEvent.VK_F;
            case GLFW.GLFW_KEY_G: return KeyEvent.VK_G;
            case GLFW.GLFW_KEY_H: return KeyEvent.VK_H;
            case GLFW.GLFW_KEY_I: return KeyEvent.VK_I;
            case GLFW.GLFW_KEY_J: return KeyEvent.VK_J;
            case GLFW.GLFW_KEY_K: return KeyEvent.VK_K;
            case GLFW.GLFW_KEY_L: return KeyEvent.VK_L;
            case GLFW.GLFW_KEY_M: return KeyEvent.VK_M;
            case GLFW.GLFW_KEY_N: return KeyEvent.VK_N;
            case GLFW.GLFW_KEY_O: return KeyEvent.VK_O;
            case GLFW.GLFW_KEY_P: return KeyEvent.VK_P;
            case GLFW.GLFW_KEY_Q: return KeyEvent.VK_Q;
            case GLFW.GLFW_KEY_R: return KeyEvent.VK_R;
            case GLFW.GLFW_KEY_S: return KeyEvent.VK_S;
            case GLFW.GLFW_KEY_T: return KeyEvent.VK_T;
            case GLFW.GLFW_KEY_U: return KeyEvent.VK_U;
            case GLFW.GLFW_KEY_V: return KeyEvent.VK_V;
            case GLFW.GLFW_KEY_W: return KeyEvent.VK_W;
            case GLFW.GLFW_KEY_X: return KeyEvent.VK_X;
            case GLFW.GLFW_KEY_Y: return KeyEvent.VK_Y;
            case GLFW.GLFW_KEY_Z: return KeyEvent.VK_Z;
            case GLFW.GLFW_KEY_LEFT_BRACKET: return KeyEvent.VK_OPEN_BRACKET;
            case GLFW.GLFW_KEY_BACKSLASH: return KeyEvent.VK_BACK_SLASH;
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return KeyEvent.VK_CLOSE_BRACKET;
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return KeyEvent.VK_BACK_QUOTE;
            case GLFW.GLFW_KEY_ESCAPE: return KeyEvent.VK_ESCAPE;
            case GLFW.GLFW_KEY_ENTER: return KeyEvent.VK_ENTER;
            case GLFW.GLFW_KEY_TAB: return KeyEvent.VK_TAB;
            case GLFW.GLFW_KEY_BACKSPACE: return KeyEvent.VK_BACK_SPACE;
            case GLFW.GLFW_KEY_INSERT: return KeyEvent.VK_INSERT;
            case GLFW.GLFW_KEY_DELETE: return KeyEvent.VK_DELETE;
            case GLFW.GLFW_KEY_RIGHT: return KeyEvent.VK_RIGHT;
            case GLFW.GLFW_KEY_LEFT: return KeyEvent.VK_LEFT;
            case GLFW.GLFW_KEY_DOWN: return KeyEvent.VK_DOWN;
            case GLFW.GLFW_KEY_UP: return KeyEvent.VK_UP;
            case GLFW.GLFW_KEY_PAGE_UP: return KeyEvent.VK_PAGE_UP;
            case GLFW.GLFW_KEY_PAGE_DOWN: return KeyEvent.VK_PAGE_DOWN;
            case GLFW.GLFW_KEY_HOME: return KeyEvent.VK_HOME;
            case GLFW.GLFW_KEY_END: return KeyEvent.VK_END;
            case GLFW.GLFW_KEY_CAPS_LOCK: return KeyEvent.VK_CAPS_LOCK;
            case GLFW.GLFW_KEY_SCROLL_LOCK: return KeyEvent.VK_SCROLL_LOCK;
            case GLFW.GLFW_KEY_NUM_LOCK: return KeyEvent.VK_NUM_LOCK;
            case GLFW.GLFW_KEY_PRINT_SCREEN: return KeyEvent.VK_PRINTSCREEN;
            case GLFW.GLFW_KEY_PAUSE: return KeyEvent.VK_PAUSE;
            case GLFW.GLFW_KEY_F1: return KeyEvent.VK_F1;
            case GLFW.GLFW_KEY_F2: return KeyEvent.VK_F2;
            case GLFW.GLFW_KEY_F3: return KeyEvent.VK_F3;
            case GLFW.GLFW_KEY_F4: return KeyEvent.VK_F4;
            case GLFW.GLFW_KEY_F5: return KeyEvent.VK_F5;
            case GLFW.GLFW_KEY_F6: return KeyEvent.VK_F6;
            case GLFW.GLFW_KEY_F7: return KeyEvent.VK_F7;
            case GLFW.GLFW_KEY_F8: return KeyEvent.VK_F8;
            case GLFW.GLFW_KEY_F9: return KeyEvent.VK_F9;
            case GLFW.GLFW_KEY_F10: return KeyEvent.VK_F10;
            case GLFW.GLFW_KEY_F11: return KeyEvent.VK_F11;
            case GLFW.GLFW_KEY_F12: return KeyEvent.VK_F12;
            case GLFW.GLFW_KEY_F13: return KeyEvent.VK_F13;
            case GLFW.GLFW_KEY_F14: return KeyEvent.VK_F14;
            case GLFW.GLFW_KEY_F15: return KeyEvent.VK_F15;
            case GLFW.GLFW_KEY_F16: return KeyEvent.VK_F16;
            case GLFW.GLFW_KEY_F17: return KeyEvent.VK_F17;
            case GLFW.GLFW_KEY_F18: return KeyEvent.VK_F18;
            case GLFW.GLFW_KEY_F19: return KeyEvent.VK_F19;
            case GLFW.GLFW_KEY_F20: return KeyEvent.VK_F20;
            case GLFW.GLFW_KEY_F21: return KeyEvent.VK_F21;
            case GLFW.GLFW_KEY_F22: return KeyEvent.VK_F22;
            case GLFW.GLFW_KEY_F23: return KeyEvent.VK_F23;
            case GLFW.GLFW_KEY_F24: return KeyEvent.VK_F24;
            case GLFW.GLFW_KEY_KP_0: return KeyEvent.VK_NUMPAD0;
            case GLFW.GLFW_KEY_KP_1: return KeyEvent.VK_NUMPAD1;
            case GLFW.GLFW_KEY_KP_2: return KeyEvent.VK_NUMPAD2;
            case GLFW.GLFW_KEY_KP_3: return KeyEvent.VK_NUMPAD3;
            case GLFW.GLFW_KEY_KP_4: return KeyEvent.VK_NUMPAD4;
            case GLFW.GLFW_KEY_KP_5: return KeyEvent.VK_NUMPAD5;
            case GLFW.GLFW_KEY_KP_6: return KeyEvent.VK_NUMPAD6;
            case GLFW.GLFW_KEY_KP_7: return KeyEvent.VK_NUMPAD7;
            case GLFW.GLFW_KEY_KP_8: return KeyEvent.VK_NUMPAD8;
            case GLFW.GLFW_KEY_KP_9: return KeyEvent.VK_NUMPAD9;
            case GLFW.GLFW_KEY_KP_DECIMAL: return KeyEvent.VK_DECIMAL;
            case GLFW.GLFW_KEY_KP_DIVIDE: return KeyEvent.VK_DIVIDE;
            case GLFW.GLFW_KEY_KP_MULTIPLY: return KeyEvent.VK_MULTIPLY;
            case GLFW.GLFW_KEY_KP_SUBTRACT: return KeyEvent.VK_SUBTRACT;
            case GLFW.GLFW_KEY_KP_ADD: return KeyEvent.VK_ADD;
            case GLFW.GLFW_KEY_KP_ENTER: return KeyEvent.VK_ENTER;
            case GLFW.GLFW_KEY_KP_EQUAL: return KeyEvent.VK_EQUALS;
            case GLFW.GLFW_KEY_LEFT_SHIFT: return KeyEvent.VK_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL: return KeyEvent.VK_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT: return KeyEvent.VK_ALT;
            case GLFW.GLFW_KEY_LEFT_SUPER: return KeyEvent.VK_WINDOWS;
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return KeyEvent.VK_SHIFT;
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return KeyEvent.VK_CONTROL;
            case GLFW.GLFW_KEY_RIGHT_ALT: return KeyEvent.VK_ALT_GRAPH;
            case GLFW.GLFW_KEY_RIGHT_SUPER: return KeyEvent.VK_WINDOWS;
            case GLFW.GLFW_KEY_MENU: return KeyEvent.VK_CONTEXT_MENU;
            default: return KeyEvent.VK_UNDEFINED;
        }
    }
}
