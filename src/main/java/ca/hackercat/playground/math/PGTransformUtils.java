package ca.hackercat.playground.math;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.PGWindow;
import org.joml.*;

import java.awt.geom.AffineTransform;

public class PGTransformUtils {

    private static final Logger LOGGER = Logger.get(PGTransformUtils.class);

    private static AffineTransform t = new AffineTransform();
    public static AffineTransform getTransform(double x, double y, double scaleX, double scaleY, double rotation) {
        t.setToIdentity();
        t.scale(scaleX, scaleY);
        t.rotate(rotation);
        t.translate(x, y);
        return t;
    }

    // LWJGL/OpenGL



    public static Matrix4f getTransformationMatrix(AffineTransform affineTransform) {


        double[] matrix = new double[6];
        affineTransform.getMatrix(matrix);

        float scaleX = (float) matrix[0];
        float shearY = (float) matrix[1];
        float shearX = (float) matrix[2];
        float scaleY = (float) matrix[3];
        float translateX = (float) matrix[4];
        float translateY = (float) matrix[5];

        return new Matrix4f(scaleX, shearY, 0, 0,
                            shearX, scaleY, 0, 0,
                            0, 0, 1, 0,
                            translateX, translateY, 0, 1);
    }

    public static Matrix4f getTransformationMatrix(Vector3f position, Quaternionf rotation, float scale) {
        return new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
    }

    public static Matrix4f getTransformationMatrix(Vector3f position, Quaternionf rotation, Vector3f scale) {
        return new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
    }

    public static Matrix4f getCameraTransformationMatrix(Vector3f position, Quaternionf rotation, Vector3f scale) {
        return new Matrix4f()
                .scale(scale)
                .rotate(rotation)
                .translate(-position.x, -position.y, -position.z);
    }
    public static Matrix4d getCameraTransformationMatrix(Vector3d position, Quaterniond rotation, Vector3d scale) {
        return new Matrix4d()
                .scale(scale)
                .rotate(rotation)
                .translate(-position.x, -position.y, -position.z);
    }


    public static Matrix4f getOrthographicMatrix(PGWindow window) {
        Matrix4f matrix = new Matrix4f().identity();

        float near = 0.1f;
        float far = 1000f;

        float ratio = (float) window.getInternalWidth() / window.getInternalHeight();

        float scale = 1;

        return matrix.ortho(-ratio * scale, ratio * scale, -scale, scale, near, far);
    }
    public static Matrix4f getProjectionMatrix(PGWindow window, float fovDegrees) {

        float ratio = (float) window.getInternalWidth() / window.getInternalHeight();

        float near = 0.01f;
        float far = 1000f;

        Matrix4f matrix = new Matrix4f().identity();
        return matrix.perspective(PGMath.toRadians(fovDegrees), ratio, near, far);
    }
}
