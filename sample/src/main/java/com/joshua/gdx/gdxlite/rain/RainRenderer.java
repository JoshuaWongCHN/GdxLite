package com.joshua.gdx.gdxlite.rain;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.joshua.gdx.gdxlite.graphics.DefaultTextureBinder;
import com.joshua.gdx.gdxlite.graphics.PerspectiveCamera;
import com.joshua.gdx.gdxlite.graphics.Texture;
import com.joshua.gdx.gdxlite.graphics.TextureBinder;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.math.Matrix4;
import com.joshua.gdx.gdxlite.math.Vector3;
import com.joshua.gdx.gdxlite.utils.GLError;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RainRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "RainRenderer";

    private final Context mContext;

    private ShaderProgram mBgProgram;
    private BgVertexArray mVertex;

    private ShaderProgram mRainProgram;
    private RainParticlesSystem mParticlesSystem;
    private RainParticleShooter mParticleShooter;

    private PerspectiveCamera mCamera;
    private TextureBinder mTextureBinder;
    private Texture mTextureBg;
    private Texture mTexture0;

    public RainRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1f, 1f, 1f, 1f);

        mBgProgram = new ShaderProgram(mContext, "rain/bg.vert", "rain/bg.frag");
        Log.d(TAG, "onSurfaceCreated: " + mBgProgram.getLog());
        mVertex = new BgVertexArray();

        mRainProgram = new ShaderProgram(mContext, "rain/rain.vert", "rain/rain.frag");
        Log.d(TAG, "onSurfaceCreated: " + mRainProgram.getLog());
        mParticlesSystem = new RainParticlesSystem(4000);
        GLError.checkError();

        mTextureBinder = new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1);
        mTextureBg = new Texture(mContext, "rain/bg_first_heavyrain.png");
        mTexture0 = new Texture(mContext, "rain/particle-ball1.png");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mCamera = new PerspectiveCamera(90f, width, height);
        mCamera.near = 0.1f;
        mCamera.far = 10f;
        mCamera.position.set(-0.1f, -0.1f, 0.3f);
        mCamera.lookAt(0f, 0f, 0f);
        mCamera.update();

        Log.d(TAG, "onSurfaceChanged: " + mCamera.combined.toString());
    }

    private long mStartTime = 0;
    private Vector3 mDir = new Vector3(-0.1f, -1f, 0f).nor();

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mParticleShooter == null) {
            mParticleShooter = new RainParticleShooter(new Vector3(0f, .5f, 0f), 0.5f, 0.3f, 1f);
            mStartTime = System.currentTimeMillis();
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mBgProgram.begin();
        mBgProgram.setUniformi("u_tex0", mTextureBinder.bind(mTextureBg));
        mVertex.draw(mBgProgram);
        mBgProgram.end();

        float time = (System.currentTimeMillis() - mStartTime) / 1000f;
        mRainProgram.begin();
        GLError.checkError();
        mTextureBinder.begin();
        mRainProgram.setUniformMatrix("u_projViewTrans", mCamera.combined);
//        Log.d(TAG, "onDrawFrame: " + mCamera.combined.toString());
        mRainProgram.setUniformMatrix("u_worldTrans", new Matrix4().idt());
        mRainProgram.setUniformf("u_time", time);
        mRainProgram.setUniformf("u_dir", mDir);
        if (mRainProgram.hasUniform("u_tex0"))
            mRainProgram.setUniformi("u_tex0", mTextureBinder.bind(mTexture0));
        mParticleShooter.addParticles(mParticlesSystem, time, 5);
        mParticlesSystem.draw(mRainProgram);
        mTextureBinder.end();
        mRainProgram.end();
    }
}
