package com.joshua.gdx.gdxlite.objects;

import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.VertexAttribute;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexArray;

public class Image extends VertexArray {
    private final static float[] VERTICES = {
            -1f, 1f, 0f, 0f, 1f,
            1f, 1f, 0f, 1f, 1f,
            -1f, -1f, 0f, 0f, 0f,
            1f, -1f, 0f, 1f, 0f
    };

    public Image(float width, float height) {
        super(4, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        float[] vertices = {
                -width, height, 0f, 0f, 1f,
                width, height, 0f, 1f, 1f,
                -width, -height, 0f, 0f, 0f,
                width, -height, 0f, 1f, 0f
        };
        setVertices(vertices, 0, vertices.length);
    }

    public void draw(ShaderProgram program) {
        bind(program);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
