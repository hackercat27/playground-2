package ca.hackercat.playground.math;

import java.awt.geom.AffineTransform;

public class PGTransformUtils {

    private static AffineTransform t = new AffineTransform();
    public static AffineTransform getTransform(double x, double y, double scaleX, double scaleY, double rotation) {
        t.setToIdentity();
        t.scale(scaleX, scaleY);
        t.rotate(rotation);
        t.translate(x, y);
        return t;
    }

}
