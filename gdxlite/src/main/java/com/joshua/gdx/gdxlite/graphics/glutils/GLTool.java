package com.joshua.gdx.gdxlite.graphics.glutils;

import android.opengl.GLES20;
import android.util.Log;

import static com.joshua.gdx.gdxlite.utils.GLConstants.*;

public class GLTool {
    private static final String TAG = "GLTool";

    public static int glGenBuffer() {
        int[] ids = new int[1];
        GLES20.glGenBuffers(1, ids, 0);
        if (ids[0] == INVALID_BUFFER) {
            Log.e(TAG, "Generate VBO failed!");
        }
        return ids[0];
    }

    public static void glDeleteBuffer(int buffer) {
        int[] ids = {buffer};
        GLES20.glDeleteBuffers(1, ids, 0);
    }

    public static int glGenTexture() {
        int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        if (ids[0] == INVALID_TEXTURE) {
            Log.e(TAG, "Generate Texture failed!");
        }
        return ids[0];
    }

    public static void glDeleteTexture(int handle) {
        int[] ids = {handle};
        GLES20.glDeleteTextures(1, ids, 0);
    }

    public static int glGenFramebuffer() {
        int[] ids = new int[1];
        GLES20.glGenFramebuffers(1, ids, 0);
        if (ids[0] == INVALID_BUFFER) {
            Log.e(TAG, "Generate frame buffer failed!");
        }
        return ids[0];
    }

    public static void glDeleteFramebuffer(int handle) {
        int[] ids = {handle};
        GLES20.glDeleteFramebuffers(1, ids, 0);
    }

    public static int glGenRenderbuffer() {
        int[] ids = new int[1];
        GLES20.glGenRenderbuffers(1, ids, 0);
        if (ids[0] == INVALID_BUFFER) {
            Log.e(TAG, "Generate render buffer failed!");
        }
        return ids[0];
    }

    public static void glDeleteRenderbuffer(int handle) {
        int[] ids = {handle};
        GLES20.glDeleteRenderbuffers(1, ids, 0);
    }
}
