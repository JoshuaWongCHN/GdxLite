package com.joshua.gdx.gdxlite.delete;

import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.VertexAttribute;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexArray;

import java.util.Random;

public class PlaneVertexArray extends VertexArray {
    public PlaneVertexArray(float width, float height, int widthSegments, int heightSegments) {
        super((widthSegments + 1) * (heightSegments + 1),
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0),
                new VertexAttribute(2, "a_velocity"),
                new VertexAttribute(2, "a_acceleration"),
                new VertexAttribute(1, "a_start"));
        float[] vertices = planeGeometry(width, height, widthSegments, heightSegments);
        setVertices(vertices, 0, vertices.length);
    }

    public void draw(ShaderProgram program) {
        bind(program);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, getNumVertices());
    }

    private float[] planeGeometry(float width, float height, int widthSegments, int heightSegments) {
        float[] vertices = new float[(widthSegments + 1) * (heightSegments + 1) * 10];
        float width_half = width / 2;
        float height_half = height / 2;

        int gridX = widthSegments + 1;
        int gridY = heightSegments + 1;

        float ceilWidth = width / widthSegments;
        float ceilHeight = height / heightSegments;

        Random random = new Random();

        int ix, iy;

        // generate vertices, normals and uvs

        for (iy = 0; iy < gridY; iy++) {
            float y = iy * ceilHeight - height_half;
            for (ix = 0; ix < gridX; ix++) {
                float x = ix * ceilWidth - width_half;
                int pos = (iy * gridY + ix) * 10;
                // a_position
                vertices[pos] = x;
                vertices[pos + 1] = -y;
                vertices[pos + 2] = 0;
                // a_texCoord0
                vertices[pos + 3] = ix * 1f / widthSegments;
                vertices[pos + 4] = 1 - (iy * 1f / heightSegments);
                // a_velocity
                vertices[pos + 5] = 10 * (float) (Math.pow(-1, Math.ceil(random.nextFloat() * 1000)) * 20 * random
                        .nextFloat());
                vertices[pos + 6] = -150 + 500 * random.nextFloat();
                // a_acceleration
                vertices[pos + 7] = 0;
                vertices[pos + 8] = -980f;
                // a_start
                vertices[pos + 9] = 2f;
            }
        }
        return vertices;
    }
}
