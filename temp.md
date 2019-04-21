package com.joshua.gdx.gdxlite.feedback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.joshua.gdx.gdxlite.Renderer;
import com.joshua.gdx.gdxlite.graphics.glutils.GLTool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES30.*;

public class FeedbackRenderer extends Renderer {
    private static final String TAG = "RainRenderer";
    private static final float[] VERTICES = {
            0f, 0f, 0f, 0f
    };
    private TFShaderProgram mProgram;
    private FloatBuffer mBuffer;

    private int mSrcVbo, mDstVbo;

    public FeedbackRenderer(Context context) {
        super(context);
    }

    @Override
    public void create() {
        GLES20.glClearColor(1f, 1f, 1f, 1f);
        mProgram = new TFShaderProgram("feedback/feedback.vert", "feedback/feedback.frag");
        Log.d(TAG, "onSurfaceCreated: " + mProgram.getLog());

        mBuffer = ByteBuffer.allocate(VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mBuffer.put(VERTICES);
        mBuffer.position(0);

        mSrcVbo = GLTool.glGenBuffer();
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, mSrcVbo);
        GLES30.glBufferData(GLES20.GL_ARRAY_BUFFER, mBuffer.capacity() * 4, mBuffer, GLES30.GL_STATIC_DRAW);
        mDstVbo = GLTool.glGenBuffer();
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, mDstVbo);
        GLES30.glBufferData(GLES20.GL_ARRAY_BUFFER, mBuffer.capacity() * 4, mBuffer, GLES30.GL_STATIC_DRAW);
    }

    public void setupVertexAttributes(int vboID) {
        glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboID);
        mProgram.enableVertexAttribute("a_position");
        mProgram.setVertexAttribute("a_position", 3, GL_FLOAT, false, 16, 0);
        mProgram.enableVertexAttribute("a_size");
        mProgram.setVertexAttribute("a_size", 1, GL_FLOAT, false, 16, 12);
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mProgram.begin();
        setupVertexAttributes(mSrcVbo);

        // Set transform feedback buffer
        glBindBuffer(GL_TRANSFORM_FEEDBACK_BUFFER, mDstVbo);
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, mDstVbo);

        // Turn off rasterization - we are not drawing
        glEnable(GL_RASTERIZER_DISCARD);

        // Emit particles using transform feedback
        glBeginTransformFeedback(GL_POINTS);
        glDrawArrays(GL_POINTS, 0, 1);
        glEndTransformFeedback();

        // Create a sync object to ensure transform feedback results are completed before the draw that uses them.
        glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

        // Restore state
        glDisable(GL_RASTERIZER_DISCARD);
        glUseProgram(0);
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        mProgram.end();

        glBindBuffer(GL_ARRAY_BUFFER, mSrcVbo);
        ByteBuffer buffer = (ByteBuffer) glMapBufferRange(GL_ARRAY_BUFFER, 0, 16, GL_MAP_WRITE_BIT);
        FloatBuffer floatBuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        StringBuilder sb = new StringBuilder();
        for (float f : floatBuffer.array()) {
            sb.append(String.format("%.2f", f)).append(" ");
        }
        Log.d("joshua", sb.toString());
        glUnmapBuffer(GL_ARRAY_BUFFER);
    }
}
