package ca.hackercat.playground;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class PGPanel extends JPanel {

    private PGWindow w;

    public PGPanel(PGWindow w, int width, int height) {
        setPreferredSize(new Dimension(width, height));
        this.w = w;
    }

    @Override
    public void paintComponent(Graphics g) {

        w.width = getWidth();
        w.height = getHeight();

        double scale;

        if (w.isAutoScale()) {
            scale = (double) getHeight() / w.getInternalHeight();
        }
        else {
            scale = 1;
        }

        w.internalWidth = (int) (getWidth() / scale) + 1;

        if (!w.isAutoScale()) {
            w.internalHeight = getHeight();
        }


        if (g instanceof Graphics2D g2) {

//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform old = g2.getTransform();

            AffineTransform transform = g2.getTransform();
            transform.scale(scale, scale);
            g2.setTransform(transform);
            w.render(g2);

            g2.setTransform(old);
        }


    }

}
