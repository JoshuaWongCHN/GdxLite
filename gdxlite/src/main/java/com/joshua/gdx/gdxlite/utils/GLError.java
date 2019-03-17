package com.joshua.gdx.gdxlite.utils;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.util.Log;

import static android.opengl.GLES20.GL_NO_ERROR;

public class GLError {
    private static final String TAG = "GLError";

    public static void checkError() {
        int error = GLES20.glGetError();
        if (error != GL_NO_ERROR) {
            Throwable t = new Throwable();
            Log.e(TAG, "GL error: " + GLU.gluErrorString(error), t);
        }
    }
}
