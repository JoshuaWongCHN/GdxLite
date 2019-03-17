/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.joshua.gdx.gdxlite.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.glutils.GLTool;
import com.joshua.gdx.gdxlite.utils.FileUtil;

/**
 * A Texture wraps a standard OpenGL ES texture.
 * <p>
 * A Texture can be managed. If the OpenGL context is lost all managed textures get invalidated. This happens when a
 * user switches
 * to another application or receives an incoming call. Managed textures get reloaded automatically.
 * <p>
 * A Texture has to be bound via the {@link Texture#bind()} method in order for it to be applied to geometry. The
 * texture will be
 * bound to the currently active texture unit specified via {@link GLES20#glActiveTexture(int)}.
 * <p>
 * This is of
 * course not extremely fast so use it with care. It also only works with unmanaged textures.
 * <p>
 * A Texture must be disposed when it is no longer used
 *
 * @author badlogicgames@gmail.com
 */
public class Texture extends GLTexture {
    public enum TextureFilter {
        /**
         * Fetch the nearest texel that best maps to the pixel on screen.
         */
        Nearest(GLES20.GL_NEAREST),

        /**
         * Fetch four nearest texels that best maps to the pixel on screen.
         */
        Linear(GLES20.GL_LINEAR),

        /**
         * @see TextureFilter#MipMapLinearLinear
         */
        MipMap(GLES20.GL_LINEAR_MIPMAP_LINEAR),

        /**
         * Fetch the best fitting image from the mip map chain based on the pixel/texel ratio and then sample the
         * texels with a
         * nearest filter.
         */
        MipMapNearestNearest(GLES20.GL_NEAREST_MIPMAP_NEAREST),

        /**
         * Fetch the best fitting image from the mip map chain based on the pixel/texel ratio and then sample the
         * texels with a
         * linear filter.
         */
        MipMapLinearNearest(GLES20.GL_LINEAR_MIPMAP_NEAREST),

        /**
         * Fetch the two best fitting images from the mip map chain and then sample the nearest texel from each of the
         * two images,
         * combining them to the final output pixel.
         */
        MipMapNearestLinear(GLES20.GL_NEAREST_MIPMAP_LINEAR),

        /**
         * Fetch the two best fitting images from the mip map chain and then sample the four nearest texels from each
         * of the two
         * images, combining them to the final output pixel.
         */
        MipMapLinearLinear(GLES20.GL_LINEAR_MIPMAP_LINEAR);

        final int glEnum;

        TextureFilter(int glEnum) {
            this.glEnum = glEnum;
        }

        public boolean isMipMap() {
            return glEnum != GLES20.GL_NEAREST && glEnum != GLES20.GL_LINEAR;
        }

        public int getGLEnum() {
            return glEnum;
        }

    }

    public enum TextureWrap {
        MirroredRepeat(GLES20.GL_MIRRORED_REPEAT), ClampToEdge(GLES20.GL_CLAMP_TO_EDGE), Repeat(GLES20.GL_REPEAT);

        final int glEnum;

        TextureWrap(int glEnum) {
            this.glEnum = glEnum;
        }

        public int getGLEnum() {
            return glEnum;
        }

    }

    private Bitmap mBitmap;

    public Texture() {
        this(GLES20.GL_TEXTURE_2D, GLTool.glGenTexture(), null);
    }

    public Texture(Context context, String internalPath) {
        super(GLES20.GL_TEXTURE_2D, GLTool.glGenTexture());
        Bitmap bitmap = FileUtil.loadBitmapFromAssets(context, internalPath);
        load(bitmap);
        bitmap.recycle();
    }

    public Texture(String path) {
        super(GLES20.GL_TEXTURE_2D, GLTool.glGenTexture());
        Bitmap bitmap = FileUtil.loadBitmapFromSdcard(path);
        load(bitmap);
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    public Texture(Bitmap bitmap) {
        this(GLES20.GL_TEXTURE_2D, GLTool.glGenTexture(), bitmap);
    }

    private Texture(int glTarget, int glHandle, Bitmap bitmap) {
        super(glTarget, glHandle);
        load(bitmap);
    }

    private void load(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        this.mBitmap = bitmap;

        bind();
        uploadImageData(GLES20.GL_TEXTURE_2D, bitmap);

        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        GLES20.glBindTexture(glTarget, 0);
    }

    @Override
    protected void reload() {
        glHandle = GLTool.glGenTexture();
        load(mBitmap);
    }

    @Override
    public int getWidth() {
        return mBitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return mBitmap.getHeight();
    }

    @Override
    public int getDepth() {
        return 0;
    }

    public void dispose() {
        if (glHandle == 0) return;
        delete();
    }
}
