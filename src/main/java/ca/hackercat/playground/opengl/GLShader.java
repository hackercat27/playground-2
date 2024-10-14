package ca.hackercat.playground.opengl;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGFileUtils;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class GLShader {


    private static class Uniform {
        public String name;
        public Object value;
        public Uniform(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final Logger LOGGER = Logger.get(GLShader.class);

    // make these available so that if programs using this
    // engine want to use them they don't need to be in memory twice
    public static final GLShader DEFAULT_SHADER = new GLShader("DEFAULT_SHADER", "/assets/playground/shader/default.vsh", "/assets/playground/shader/default.fsh");
//    public static final GLShader COLOR_SHADER = new GLShader("COLOR_SHADER", "/assets/shaders/color.vsh", "/assets/shaders/color.fsh");
//    public static final GLShader TEXT_SHADER = new GLShader("TEXT_SHADER", "/assets/shaders/text.vsh", "/assets/shaders/text.fsh");

    private String vertexSource, fragmentSource;
    private String vertexPath, fragmentPath;
    private String name;
    private int vertexID, fragmentID, programID;
    private boolean disposable = false;

    private final List<Uniform> uniformQueue = new ArrayList<>();

    public GLShader(String vertexPath, String fragmentPath) {
        this(null, vertexPath, fragmentPath);
    }
    public GLShader(String name, String vertexPath, String fragmentPath) {
        this.fragmentPath = fragmentPath;
        this.vertexSource = PGFileUtils.getContents(vertexPath);
        this.fragmentSource = PGFileUtils.getContents(fragmentPath);
        this.vertexPath = vertexPath;

        if (name == null) {
            this.name = this.toString();
        }
        else {
            this.name = name;
        }

        create();
    }

    private void create() {
        programID = glCreateProgram();
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        if (glGetShaderi(vertexID, GL_COMPILE_STATUS) == GL_FALSE) {
            LOGGER.error(vertexPath + " couldn't compile\n"
                         + glGetShaderInfoLog(vertexID));
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);
        if (glGetShaderi(fragmentID, GL_COMPILE_STATUS) == GL_FALSE) {
            LOGGER.error(fragmentPath + " couldn't compile\n"
                         + glGetShaderInfoLog(fragmentID));
        }

        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);

        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            LOGGER.error("Shader " + name + " initialization error - Couldn't link program\n" + glGetProgramInfoLog(programID));
        }
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
            LOGGER.error("Shader " + name + " initialization error Program is invalid\n" + glGetProgramInfoLog(programID));
        }

        LOGGER.log("Created shader " + name);

//        AssetManager.register(this);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programID, name);
    }

    public void queueUniform(String name, float value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, double value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, int value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, boolean value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector2f value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector3f value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector4f value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Matrix4f value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector2d value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector3d value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Vector4d value) {
        uniformQueue.add(new Uniform(name, value));
    }
    public void queueUniform(String name, Matrix4d value) {
        uniformQueue.add(new Uniform(name, value));
    }

    public void setUniform(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }
    public void setUniform(String name, double value) {
        setUniform(name, (float) value);
    }
    public void setUniform(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }
    public void setUniform(String name, boolean value) {
        glUniform1i(getUniformLocation(name), value? 1 : 0);
    }
    public void setUniform(String name, Vector2f value) {
        glUniform2f(getUniformLocation(name), value.x(), value.y());
    }
    public void setUniform(String name, Vector2d value) {
        glUniform2f(getUniformLocation(name), (float) value.x(), (float) value.y());
    }
    public void setUniform(String name, Vector3f value) {
        glUniform3f(getUniformLocation(name), value.x(), value.y(), value.z());
    }
    public void setUniform(String name, Vector3d value) {
        glUniform3f(getUniformLocation(name), (float) value.x(), (float) value.y(), (float) value.z());
    }
    public void setUniform(String name, Vector4f value) {
        glUniform4f(getUniformLocation(name), value.x(), value.y(), value.z(), value.w());
    }
    public void setUniform(String name, Vector4d value) {
        glUniform4f(getUniformLocation(name), (float) value.x(), (float) value.y(), (float) value.z(), (float) value.w());
    }
    public void setUniform(String name, Matrix4f value) {
        FloatBuffer matBuffer = MemoryUtil.memAllocFloat(16);
        value.get(matBuffer);
        glUniformMatrix4fv(getUniformLocation(name), false, matBuffer);

        MemoryUtil.memFree(matBuffer);
    }
    public void setUniform(String name, Matrix4d value) {
        setUniform(name, new Matrix4f(value));
    }

    private void setGenericUniform(String name, Object value) {
        if (value instanceof Float f) {
            setUniform(name, f);
        }
        else if (value instanceof Double d) {
            setUniform(name, d);
        }
        else if (value instanceof Boolean b) {
            setUniform(name, b);
        }
        else if (value instanceof Integer i) {
            setUniform(name, i);
        }
        else if (value instanceof Vector2f vec2) {
            setUniform(name, vec2);
        }
        else if (value instanceof Vector3f vec3) {
            setUniform(name, vec3);
        }
        else if (value instanceof Vector4f vec4) {
            setUniform(name, vec4);
        }
        else if (value instanceof Matrix4f mat4) {
            setUniform(name, mat4);
        }
        else if (value instanceof Vector2d vec2) {
            setUniform(name, vec2);
        }
        else if (value instanceof Vector3d vec3) {
            setUniform(name, vec3);
        }
        else if (value instanceof Vector4d vec4) {
            setUniform(name, vec4);
        }
        else if (value instanceof Matrix4d mat4) {
            setUniform(name, mat4);
        }

    }

    public void applyUniforms() {
        for (Uniform u : uniformQueue) {
            setGenericUniform(u.name, u.value);
//            LOGGER.log("Set uniform " + u.name + " to " + u.value);
        }
        uniformQueue.clear();
    }

    public void bind() {
        glUseProgram(programID);
    }
    public void unbind() {
        if (!uniformQueue.isEmpty()) {
            LOGGER.warn("Suspicious unbind without applying uniforms, automatically flushing...");
            applyUniforms();
        }
        glUseProgram(0);
    }

//    @Override
//    public boolean isDisposable() {
//        return disposable;
//    }
//
//    @Override
//    public void dispose() {
//        disposable = true;
//    }

    public void close() {
        glDetachShader(programID, vertexID);
        glDetachShader(programID, fragmentID);
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
        glDeleteProgram(programID);
    }

}
