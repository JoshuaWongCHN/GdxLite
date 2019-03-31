package com.joshua.gdx.gdxlite.graphics;

import android.opengl.GLES20;

public enum Format {
    ALPHA(1),
    LUMINANCE_ALPHA(2),
    RGB888(3),
    RGBA8888(4),
    RGB565(5),
    RGBA4444(6);

    final int value;

    Format(int ni) {
        this.value = ni;
    }

    public int toGlFormat() {
        switch (this) {
            case ALPHA:
                return GLES20.GL_ALPHA;
            case LUMINANCE_ALPHA:
                return GLES20.GL_LUMINANCE_ALPHA;
            case RGB888:
            case RGB565:
                return GLES20.GL_RGB;
            case RGBA8888:
            case RGBA4444:
                return GLES20.GL_RGBA;
            default:
                throw new RuntimeException("unknown format: " + value);
        }
    }

    public int toGlType() {
        switch (this) {
            case ALPHA:
            case LUMINANCE_ALPHA:
            case RGB888:
            case RGBA8888:
                return GLES20.GL_UNSIGNED_BYTE;
            case RGB565:
                return GLES20.GL_UNSIGNED_SHORT_5_6_5;
            case RGBA4444:
                return GLES20.GL_UNSIGNED_SHORT_4_4_4_4;
            default:
                throw new RuntimeException("unknown format: " + value);
        }
    }
}
