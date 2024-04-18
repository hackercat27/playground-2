package ca.hackercat.playground;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGKeyboard;
import ca.hackercat.playground.io.PGMouse;
import ca.hackercat.playground.io.sound.PGSoundEventManager;
import ca.hackercat.playground.io.sound.PGSoundManager;
import ca.hackercat.playground.math.PGMath;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;

public class PGWindow {

    private static final Logger LOGGER = Logger.get(PGWindow.class);

    public static final int EXIT_NORMAL = 0;

    private JFrame frame;
    private PGPanel panel;

    private Object threadLock = new Object();

    private double tps = 30;
    private double fps = 60;

    // OpenAL pointers
    private long audioContext;
    private long audioDevice;

    private ALCCapabilities alcCapabilities;
    private ALCapabilities alCapabilities;

    private boolean autoScale = true;

    // TODO: the fact that these need to be package private,
    //  at least to me, feels really janky and it should be fixed
    int width;
    int height;

    int internalWidth = 854;
    int internalHeight = 480;

    private int x;
    private int y;

    private boolean fullscreen = false;

    private String title;

    private long lastTickTimeMillis;
    private long lastFrameLengthMillis;

    private List<PGObject> objects = new ArrayList<>();

    public PGWindow() {
        this(854, 480);
    }

    public PGWindow(String title) {
        this(854, 480, title);
    }

    public PGWindow(int width, int height) {
        this(width, height, null);
    }

    public PGWindow(int width, int height, String title) {
        this.width = width;
        this.height = height;
        setTitle(title);
        init();
    }

    private void init() {
        frame = new JFrame();
        panel = new PGPanel(this, width, height);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        createOpenALContext();

        start();
    }

    private void createOpenALContext() {

        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDevice);

        int[] attributes = new int[] {0};
        audioContext = alcCreateContext(audioDevice, attributes);

        alcMakeContextCurrent(audioContext);

        alcCapabilities = ALC.createCapabilities(audioDevice);
        alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            LOGGER.error("Audio library is not supported.");
        }
    }

    private void destroyOpenALContext() {

        alcMakeContextCurrent(audioContext);

        alcCloseDevice(audioDevice);
        alcDestroyContext(audioContext);

    }

    private void start() {
        PGMouse.setWindow(this);
        PGKeyboard.setWindow(this);

        panel.addMouseListener(PGMouse.get());
        panel.addMouseWheelListener(PGMouse.get());
        panel.addMouseMotionListener(PGMouse.get());
        frame.addKeyListener(PGKeyboard.get());

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }

            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });


        Runnable renderer = new Runnable() {
            @Override
            public void run() {

                long targetFrameTimeMillis = (long) (1000 / fps);
                while (true) {
                    panel.repaint();
                    try {
                        long remainingTime = targetFrameTimeMillis - lastFrameLengthMillis;
                        if (remainingTime > 0) {
                            Thread.sleep(remainingTime);
                        }

                    }
                    catch (InterruptedException ignored) {}
                }
            }
        };
        Runnable updater = new Runnable() {
            @Override
            public void run() {

                long endTimeMillis;
                long startTimeMillis;
                while (true) {
                        long targetTickTimeMillis = (long) (1000 / tps);

                        startTimeMillis = System.currentTimeMillis();

                        try {
                            update(1d / tps);

                            endTimeMillis = System.currentTimeMillis();

                            lastTickTimeMillis = endTimeMillis;

                            long tickTime = endTimeMillis - startTimeMillis;

                            long remainingTime = targetTickTimeMillis - tickTime;
                            if (remainingTime > 0) {
                                Thread.sleep(remainingTime);
                            }
                        }
                        catch (InterruptedException ignored) {}
                }
            }
        };

        new Thread(renderer, "engine-renderer").start();
        new Thread(updater, "engine-update").start();
    }

    public void update(double deltaTime) {

        frame.setTitle(title);
        Point pos = frame.getLocation();
        this.x = pos.x;
        this.y = pos.y;

        if (this.objects == null) {
            return;
        }

        synchronized (threadLock) {

            List<PGObject> objects = new ArrayList<>(this.objects);
            List<PGObject> forRemoval = new LinkedList<>();

            objects.sort(new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    // again more redundant stuff
                    int layer1 = (o1 instanceof PGObject obj1)? obj1.getOrder() : 0;
                    int layer2 = (o2 instanceof PGObject obj2)? obj2.getOrder() : 0;
                    return -Integer.compare(layer1, layer2);
                }
            });

            for (PGObject o : objects) {

                if (o instanceof Updatable u) {
                    u.update(deltaTime);
                }

                if (o != null && o.isGarbage()) {
                    o.onDispose();
                    forRemoval.add(o);
                }

            }

            this.objects.removeAll(forRemoval);

            // TODO: implement this in a better, more generic way
            if (PGKeyboard.get().isKeyPressed(KeyEvent.VK_F11)) {
                fullscreen = !fullscreen;
                frame.dispose();

                if (fullscreen) {
                    frame.setUndecorated(true);
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
                else {
                    frame.setUndecorated(false);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setExtendedState(JFrame.NORMAL);
                }
                frame.setVisible(true);
            }

            PGMouse.update();
            PGKeyboard.update();
            PGSoundManager.cleanSounds();
        }
    }

    private void exit() {
        for (Object o : objects) {
            if (o instanceof ExitListener e) {
                e.onExit();
            }
        }

        destroyOpenALContext();

        System.exit(EXIT_NORMAL);
    }

    public void render(Graphics2D g2) {

        synchronized (threadLock) {

            long startTimeMillis = System.currentTimeMillis();
            long endTimeMillis;

            for (Object o : objects) {
                if (o instanceof Renderable r) {
                    double t = getTickProgress();

                    t = PGMath.clamp(t, 0, 1);

                    g2.setColor(Color.WHITE);
                    r.render(g2, t);
                }
            }

            endTimeMillis = System.currentTimeMillis();

            lastFrameLengthMillis = endTimeMillis - startTimeMillis;
        }
    }

    public double getTickProgress() {
        double targetTickTimeMillis = 1000d / tps;
        return PGMath.clamp(0, 1, (double) (System.currentTimeMillis() - lastTickTimeMillis) / targetTickTimeMillis);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setIcon(BufferedImage icon) {
        frame.setIconImage(icon);
    }

    public void add(PGObject o) {
        objects.add(o);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getInternalWidth() {
        return internalWidth;
    }
    public int getInternalHeight() {
        return internalHeight;
    }

    public void setTPS(double tps) {
        this.tps = tps;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public boolean isAutoScale() {
        return autoScale;
    }
}
