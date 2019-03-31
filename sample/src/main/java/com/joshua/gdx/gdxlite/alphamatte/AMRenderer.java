package com.joshua.gdx.gdxlite.alphamatte;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.joshua.gdx.gdxlite.graphics.DefaultTextureBinder;
import com.joshua.gdx.gdxlite.graphics.OrthographicCamera;
import com.joshua.gdx.gdxlite.graphics.Texture;
import com.joshua.gdx.gdxlite.graphics.TextureBinder;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.objects.Image;

import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AMRenderer implements GLSurfaceView.Renderer {
    private Context mContext;
    private TextureBinder mTextureBinder;

    private ShaderProgram mTextureProgram;
    private ShaderProgram mGlassProgram;
//    private PlaneVertexArray mVertices;
    private Image mText;
    private Image mMask;

    private OrthographicCamera mCamera;
    private Texture mTextureText;
    private Texture mTextureMask;


    public AMRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(0f, 0f, 0f, 0f);

        mTextureProgram = new ShaderProgram("alphamatte/alphamatte.vert", "alphamatte/alphamatte.frag");
        Logger.getLogger("joshua").info(mTextureProgram.getLog());
        long time = System.currentTimeMillis();
        Logger.getLogger("joshua").info("time:" + (time - System.currentTimeMillis()));

        mTextureBinder = new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1);
        mTextureText = new Texture("alphamatte/text.png");
        mTextureMask = new Texture("alphamatte/mask.png");
        mTextureMask.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mText = new Image(1000f/width, 250f/height);
        mMask = new Image(1000f/width, 250f/height);

        mCamera = new OrthographicCamera(width, height);
        mStartTime = System.currentTimeMillis();
    }

    private long mStartTime = 0;

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float time = (System.currentTimeMillis() - mStartTime) / 1000f;

        mTextureProgram.begin();
        mTextureBinder.begin();
        mTextureProgram.setUniformf("u_time", time);
        mTextureProgram.setUniformi("u_tex0", mTextureBinder.bind(mTextureText));
        mTextureProgram.setUniformi("u_tex1", mTextureBinder.bind(mTextureMask));
        mText.draw(mTextureProgram);
        mTextureBinder.end();
        mTextureProgram.end();
    }
}
