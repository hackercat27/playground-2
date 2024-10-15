package ca.hackercat.playground;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGKeyboard;
import ca.hackercat.playground.io.PGMouse;
import ca.hackercat.playground.io.PGWindowState;
import ca.hackercat.playground.io.sound.PGSoundManager;
import ca.hackercat.playground.math.PGMath;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.openal.ALC10.*;

public class PGWindow {

    private static final Logger LOGGER = Logger.get(PGWindow.class);

    public static final int EXIT_NORMAL = 0;

    private PGFrame frame;
//    private PGPanel panel;

    private final Object threadLock = new Object();

    private double targetTPS = 60;
    private double targetFPS = 240;

    private int currentTPS;
    private int currentFPS;

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

    private PGWindowState state = PGWindowState.WINDOWED;

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
        init();
        setTitle(title);

    }

    private void init() {

        LOGGER.log("Initializing Swing frame.");
        frame = new PGFrameSwing(this, width, height, title);


        frame.setVisible(true);

        createOpenALContext();
    }

    private void createOpenALContext() {
        LOGGER.log("Creating OpenAL context");

        String defaultDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        LOGGER.log("got default device");
        audioDevice = alcOpenDevice(defaultDevice);
        LOGGER.log("opened device");

        int[] attributes = new int[] {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        LOGGER.log("created context");

        alcMakeContextCurrent(audioContext);
        LOGGER.log("made context current");

        alcCapabilities = ALC.createCapabilities(audioDevice);
        LOGGER.log("created alc capabilities");
        alCapabilities = AL.createCapabilities(alcCapabilities);
        LOGGER.log("created al capabilities");

        if (!alCapabilities.OpenAL10) {
            LOGGER.error("Audio library is not supported.");
        }

        LOGGER.log("Finished initializing!");

    }

    private void destroyOpenALContext() {

        if (audioDevice != 0) {
            alcMakeContextCurrent(audioContext);
            alcCloseDevice(audioDevice);
            audioDevice = 0;
        }
        if (audioContext != 0) {
            alcDestroyContext(audioContext);
            audioContext = 0;
        }

    }

    public void start() {
        PGMouse.setWindow(this);
        PGKeyboard.setWindow(this);

        frame.addMouseListener(PGMouse.get());
        frame.addMouseWheelListener(PGMouse.get());
        frame.addMouseMotionListener(PGMouse.get());
        frame.addKeyListener(PGKeyboard.get());

        final boolean[] shouldClose = {false};

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
                shouldClose[0] = false;
            }

            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {}
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });


        Runnable updater = () -> {

            long lastTPSRefresh = System.currentTimeMillis();
            PGWindow.this.currentTPS = (int) targetTPS; // so its not 0

            int ticks = 0;

            while (!shouldClose[0]) {
                double targetTickTimeMillis = 1000d / targetTPS;

                double deltaTime;
                long startTimeMillis;
                long endTimeMillis;

                startTimeMillis = System.currentTimeMillis();

                update(1d / targetTPS);
                ticks++;

                if (lastTPSRefresh - System.currentTimeMillis() >= 1000) {
                    lastTPSRefresh = System.currentTimeMillis();
                    currentTPS = ticks;
                    ticks = 0;
                }


                endTimeMillis = System.currentTimeMillis();

                deltaTime = startTimeMillis - endTimeMillis;

                double millisUntilNextTick = targetTickTimeMillis - deltaTime;
                long waitTime = PGMath.round(millisUntilNextTick);

                if (waitTime > 0) {
                    try {
                        Thread.sleep(waitTime);
                    }
                    catch (InterruptedException e) {
                        LOGGER.warn(e);
                    }
                }

            }
        };
        Runnable renderer = () -> {


            long endTimeMillis;
            long startTimeMillis;

//            double millisUntilNextTick = 0;
            double millisUntilNextFrame = 0;

            long lastFPSRefresh = System.currentTimeMillis();
            int frames = (int) targetFPS;

            while (!shouldClose[0]) {
                double targetFrameTimeMillis = 1000d / targetFPS;

                frames++;

                if (lastFPSRefresh - System.currentTimeMillis() >= 1000) {
                    lastFPSRefresh = System.currentTimeMillis();
                    currentFPS = frames;
                    frames = 0;

                }

                frame.poll();
                frame.drawFrame(targetFrameTimeMillis);
            }

        };

        new Thread(renderer, "engine-renderer").start();
        new Thread(updater, "engine-update").start();
//        renderer.run(); // bad but "i'll fix it later"
    }

    public void update(double deltaTime) {

        // probably unnecessary?
//        frame.setTitle(title);

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

            PGMouse.update();
            PGKeyboard.update();
            PGSoundManager.cleanSounds();

            lastTickTimeMillis = System.currentTimeMillis();
        }
    }

    public void setWindowState(PGWindowState state) {
        // wrapper method
        frame.setState(state);
    }

    private void exit() {
        for (Object o : objects) {
            if (o instanceof ExitListener e) {
                e.onExit();
            }
        }

        destroyOpenALContext();

        if (frame != null) {
            frame.close();
        }

        System.exit(EXIT_NORMAL);
    }

    public void render(Graphics2D g2) {

        synchronized (threadLock) {

            long startTimeMillis = System.currentTimeMillis();
            long endTimeMillis;

            double t = getTickProgress();

            for (Object o : objects) {
                if (o instanceof Renderable r) {
                    g2.setColor(Color.WHITE);
                    r.render(g2, t);
                }
            }

            endTimeMillis = System.currentTimeMillis();

            lastFrameLengthMillis = endTimeMillis - startTimeMillis;
        }
    }

    public double getTickProgress() {
        double targetTickTimeMillis = 1000d / targetTPS;
        return (double) (System.currentTimeMillis() - lastTickTimeMillis) / targetTickTimeMillis;
    }

    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        this.title = title;
        frame.setTitle(title);
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

    public int getInternalWidth() {
        return internalWidth;
    }
    public int getInternalHeight() {
        return internalHeight;
    }

    public void setTPS(double tps) {
        this.targetTPS = tps;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public PGWindowState getState() {
        return state;
    }

    public int getTPS() {
        return currentTPS;
    }

    public int getFPS() {
        return currentFPS;
    }
}
