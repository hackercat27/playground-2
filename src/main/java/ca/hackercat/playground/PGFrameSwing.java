package ca.hackercat.playground;

import ca.hackercat.playground.io.PGWindowState;
import javax.swing.JFrame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

public class PGFrameSwing implements PGFrame {

    private JFrame frame;
    private PGPanel panel;
    private PGWindowState state = PGWindowState.WINDOWED;

    private boolean swing;

    private boolean visible;

    private double lastFrameLengthMillis;

    public PGFrameSwing(PGWindow window, int width, int height, String title) {

        frame = new JFrame();
        panel = new PGPanel(window, width, height);

        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void setState(PGWindowState state) {

        if (state == this.state) {
            // no change, so don't change anything
            return;
        }

        this.state = state;

        frame.dispose();

        // TODO: fullscreen is broken rn need to fix
//        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                                                   .getDefaultScreenDevice()
//                                                   .getDefaultConfiguration()
//                                                   .getDevice();
//        if (state != PGWindowState.FULLSCREEN) {
//            device.setFullScreenWindow(null);
//        }

        switch (state) {
            case WINDOWED -> {
                frame.setUndecorated(false);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setExtendedState(JFrame.NORMAL);
                frame.setVisible(true);
            }
            case WINDOWED_BORDERLESS -> {
                frame.setUndecorated(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
//            case FULLSCREEN -> {
//                device.setFullScreenWindow(frame);
//            }
        }
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener mml) {
        frame.addMouseMotionListener(mml);
    }
    @Override
    public void addMouseListener(MouseListener ml) {
        frame.addMouseListener(ml);
    }
    @Override
    public void addMouseWheelListener(MouseWheelListener mwl) {
        frame.addMouseWheelListener(mwl);
    }
    @Override
    public void addKeyListener(KeyListener kl) {
        frame.addKeyListener(kl);
    }
    @Override
    public void addWindowListener(WindowListener wl) {
        frame.addWindowListener(wl);
    }
    @Override
    public void setIconImage(BufferedImage image) {
        frame.setIconImage(image);
    }

    @Override
    public void setVisible(boolean visible) {

        if (visible == this.visible) {
            // no change
            return;
        }

        this.visible = visible;

        if (!visible) {
            frame.dispose();
        }
        else {
            frame.setVisible(true);
        }
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }

    @Override
    public void poll() {
    }

    @Override
    public void drawFrame(double targetTickTimeMillis) {

        panel.repaint();

    }

    @Override
    public void close() {

    }

    public void add(Component component) {
        frame.add(component);
    }
}
