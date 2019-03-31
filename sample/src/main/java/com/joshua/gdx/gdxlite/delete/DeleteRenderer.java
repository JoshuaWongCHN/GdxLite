package com.joshua.gdx.gdxlite.delete;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.joshua.gdx.gdxlite.graphics.DefaultTextureBinder;
import com.joshua.gdx.gdxlite.graphics.OrthographicCamera;
import com.joshua.gdx.gdxlite.graphics.Texture;
import com.joshua.gdx.gdxlite.graphics.TextureBinder;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.math.Matrix4;

import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DeleteRenderer implements GLSurfaceView.Renderer {
    private Context mContext;
    private TextureBinder mTextureBinder;

    private ShaderProgram mProgram;
    private PlaneVertexArray mVertices;

    private OrthographicCamera mCamera;
    private Texture mTexture0;

    public DeleteRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        mProgram = new ShaderProgram("delete/delete.vert", "delete/delete.frag");
        Logger.getLogger("joshua").info(mProgram.getLog());

        long time = System.currentTimeMillis();
        Logger.getLogger("joshua").info("time:" + (time - System.currentTimeMillis()));

        mTextureBinder = new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1);
        mTexture0 = new Texture("delete/weather.png");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mCamera = new OrthographicCamera(width, height);
    }

    private long mStartTime = 0;

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mVertices == null) {
            mVertices = new PlaneVertexArray(192, 192, 192, 192);
            mStartTime = System.currentTimeMillis();
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float time = (System.currentTimeMillis() - mStartTime) / 1000f;

        mProgram.begin();
        mTextureBinder.begin();
        mProgram.setUniformi("u_tex0", mTextureBinder.bind(mTexture0));
        mProgram.setUniformMatrix("u_projViewTrans", mCamera.combined);
        mProgram.setUniformMatrix("u_worldTrans", new Matrix4().idt());
        mProgram.setUniformf("u_time", time);
        mVertices.draw(mProgram);
        mTextureBinder.end();
        mProgram.end();
    }
}
