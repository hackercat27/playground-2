package ca.hackercat.playground.opengl;

import ca.hackercat.logging.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class GLMesh {

    private static final Logger LOGGER = Logger.get(GLMesh.class);

    private Vector3f[] vertices;
    private Vector2f[] uvs;
    private int[] indices;

    private int vao;
    private int indexBuffer;
    private int positionBuffer;
    private int textureUVBuffer;


    public GLMesh(Vector3f[] vertices, Vector2f[] uvs, int[] indices) {
        this.indices = indices;
        this.vertices = vertices;
        this.uvs = uvs;
        create();
    }
    private void create() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
        float[] positionData = new float[vertices.length * 3];
        for (int i = 0; i < vertices.length; i++) {
            positionData[i * 3] = vertices[i].x();
            positionData[(i * 3) + 1] = vertices[i].y();
            positionData[(i * 3) + 2] = vertices[i].z();
        }
        positionBuffer.put(positionData).flip();
        this.positionBuffer = storeData(positionBuffer, 0, 3);
        MemoryUtil.memFree(positionBuffer);

        if (uvs != null) {
            FloatBuffer textureUVBuffer = MemoryUtil.memAllocFloat(uvs.length * 2);
            float[] textureData = new float[uvs.length * 2];
            for (int i = 0; i < uvs.length; i++) {
                if (uvs[i] == null)
                    uvs[i] = new Vector2f();
                textureData[i * 2] = uvs[i].x();
                textureData[(i * 2) + 1] = uvs[i].y();
            }
            textureUVBuffer.put(textureData).flip();
            this.textureUVBuffer = storeData(textureUVBuffer, 1, 2);
            MemoryUtil.memFree(textureUVBuffer);
        }

        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        this.indexBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(indexBuffer);

        glBindVertexArray(0);

        LOGGER.log(
                "Created mesh (" + this.positionBuffer + ", "
                + this.textureUVBuffer + ", "
                + this.indexBuffer + ") ("
                + vertices.length + ", "
                + (uvs == null? "null" : uvs.length) + ", "
                + indices.length + ")"
        );
    }

    private int storeData(FloatBuffer buffer, int index, int size) {
        int bufferID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return bufferID;
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

//    @Override
    public void close() {
        glDeleteBuffers(positionBuffer);
        glDeleteBuffers(indexBuffer);
        glDeleteBuffers(textureUVBuffer);

        glDeleteVertexArrays(vao);
    }

    public int getVertexArray() {
        return vao;
    }

    public int getPositionBuffer() {
        return positionBuffer;
    }

    public int getIndexBuffer() {
        return indexBuffer;
    }

    public int getTextureUVBuffer() {
        return textureUVBuffer;
    }

    public Vector3f[] getVertices() {
        return vertices;
    }

    public Vector2f[] getUVs() {
        return uvs;
    }

    public int[] getIndices() {
        return indices;
    }
}