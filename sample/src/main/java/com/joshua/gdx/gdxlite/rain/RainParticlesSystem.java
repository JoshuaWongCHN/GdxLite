package com.joshua.gdx.gdxlite.rain;

import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.VertexAttribute;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexArray;
import com.joshua.gdx.gdxlite.math.Vector3;

public class RainParticlesSystem {
    /**
     * attribute vec3 a_position;
     * attribute float a_speed;
     * attribute float a_start;
     * attribute int a_type;
     */
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int SPEED_COMPONENT_COUNT = 1;
    private static final int START_COMPONENT_COUNT = 1;
    private static final int TYPE_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT
            = POSITION_COMPONENT_COUNT
            + SPEED_COMPONENT_COUNT
            + START_COMPONENT_COUNT
            + TYPE_COMPONENT_COUNT;

    private final float[] particles;
    private final VertexArray mVertexArray;
    private final int mMaxParticleCount;

    private int mCurrentParticleCount;
    private int mNextParticle;

    public RainParticlesSystem(int maxParticleCount) {
        this.particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        this.mVertexArray = new VertexArray(particles.length,
                new VertexAttribute(POSITION_COMPONENT_COUNT, "a_position"),
                new VertexAttribute(SPEED_COMPONENT_COUNT, "a_speed"),
                new VertexAttribute(START_COMPONENT_COUNT, "a_start"),
                new VertexAttribute(TYPE_COMPONENT_COUNT, "a_type"));
        mVertexArray.setVertices(particles, 0, particles.length);
        this.mMaxParticleCount = maxParticleCount;
    }

    public void addParticle(Vector3 pos, float speed, float start, int type) {
        final int offset = mNextParticle * TOTAL_COMPONENT_COUNT;

        int currentOffset = offset;
        mNextParticle++;

        if (mCurrentParticleCount < mMaxParticleCount) {
            mCurrentParticleCount++;
        }

        if (mNextParticle == mMaxParticleCount) {
            mNextParticle = 0;
        }

        particles[currentOffset++] = pos.x;
        particles[currentOffset++] = pos.y;
        particles[currentOffset++] = pos.z;


        particles[currentOffset++] = speed;
        particles[currentOffset++] = start;
        particles[currentOffset] = type;

        mVertexArray.updateVertices(offset, particles, offset, TOTAL_COMPONENT_COUNT);
    }

    public void draw(ShaderProgram program) {
        mVertexArray.bind(program);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mCurrentParticleCount);
    }

}
