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

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.joshua.gdx.gdxlite.graphics.glutils.GLTool;

import java.text.Format;

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
 * You can draw {@link Bitmap}s to a texture at any time. The changes will be automatically uploaded to texture
 * memory. This is of
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

    TextureData data;

    public Texture(String filename) {
        this(filename, true, false);
    }

    public Texture(String filename, boolean useMipMaps) {
        this(filename, true, useMipMaps);
    }

    public Texture(String filename, boolean internal, boolean useMipMaps) {
        this(TextureData.Factory.loadFromFile(filename, internal, useMipMaps));
    }

    public Texture(Bitmap bitmap) {
        this(new BitmapTextureData(bitmap, false, false));
    }

    public Texture(Bitmap bitmap, boolean useMipMaps) {
        this(new BitmapTextureData(bitmap, useMipMaps, false));
    }

    public Texture(Bitmap bitmap, Format format, boolean useMipMaps) {
        this(new BitmapTextureData(bitmap, useMipMaps, false));
    }

    public Texture(TextureData data) {
        this(GLES20.GL_TEXTURE_2D, GLTool.glGenTexture(), data);
    }

    protected Texture(int glTarget, int glHandle, TextureData data) {
        super(glTarget, glHandle);
        load(data);
    }

    public void load(TextureData data) {
        this.data = data;

        if (!data.isPrepared()) data.prepare();

        bind();
        uploadImageData(GLES20.GL_TEXTURE_2D, data);

        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        GLES20.glBindTexture(glTarget, 0);
    }

    /**
     * Used internally to reload after context loss. Creates a new GL handle then calls {@link #load(TextureData)}.
     * Use this only
     * if you know what you do!
     */
    @Override
    protected void reload() {
        glHandle = GLTool.glGenTexture();
        load(data);
    }

    /**
     * Draws the given {@link Bitmap} to the texture at position x, y. No clipping is performed so you have to make
     * sure that you
     * draw only inside the texture region. Note that this will only draw to mipmap level 0!
     *
     * @param bitmap The Bitmap
     * @param x      The x coordinate in pixels
     * @param y      The y coordinate in pixels
     */
    public void draw(Bitmap bitmap, int x, int y) {
        bind();

        GLUtils.texSubImage2D(glTarget, 0, x, y, bitmap);
    }

    @Override
    public int getWidth() {
        return data.getWidth();
    }

    @Override
    public int getHeight() {
        return data.getHeight();
    }

    @Override
    public int getDepth() {
        return 0;
    }

    public TextureData getTextureData() {
        return data;
    }

    /**
     * Disposes all resources associated with the texture
     */
    public void dispose() {
        // this is a hack. reason: we have to set the glHandle to 0 for textures that are
        // reloaded through the asset manager as we first remove (and thus dispose) the texture
        // and then reload it. the glHandle is set to 0 in invalidateAllTextures prior to
        // removal from the asset manager.
        if (glHandle == 0) return;
        delete();
    }

    public String toString() {
        if (data instanceof FileTextureData) return data.toString();
        return super.toString();
    }

    public static int BitmapToGLFormat(Bitmap.Config config) {
        switch (config) {
            case ALPHA_8:
                return GLES20.GL_ALPHA;
            case RGB_565:
                return GLES20.GL_RGB;
            case ARGB_8888:
            case ARGB_4444:
                return GLES20.GL_RGBA;
            default:
                throw new RuntimeException("unknown format: " + config);
        }
    }

    public static int BitmapToGLType(Bitmap.Config config) {
        switch (config) {
            case ALPHA_8:
            case ARGB_8888:
                return GLES20.GL_UNSIGNED_BYTE;
            case RGB_565:
                return GLES20.GL_UNSIGNED_SHORT_5_6_5;
            case ARGB_4444:
                return GLES20.GL_UNSIGNED_SHORT_4_4_4_4;
            default:
                throw new RuntimeException("unknown format: " + config);
        }
    }
}
