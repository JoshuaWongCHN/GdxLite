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

import com.joshua.gdx.gdxlite.graphics.Texture.TextureFilter;
import com.joshua.gdx.gdxlite.graphics.Texture.TextureWrap;
import com.joshua.gdx.gdxlite.graphics.TextureData.TextureDataType;
import com.joshua.gdx.gdxlite.graphics.glutils.GLTool;
import com.joshua.gdx.gdxlite.utils.Disposable;


/**
 * Class representing an OpenGL texture by its target and handle. Keeps track of its state like the TextureFilter and
 * TextureWrap.
 * Also provides some (protected) static methods to create TextureData and upload image data.
 *
 * @author badlogic, Xoppa
 */
public abstract class GLTexture implements Disposable {
    /**
     * The target of this texture, used when binding the texture, e.g. GL_TEXTURE_2D
     */
    public final int glTarget;
    protected int glHandle;
    protected TextureFilter minFilter = TextureFilter.Nearest;
    protected TextureFilter magFilter = TextureFilter.Nearest;
    protected TextureWrap uWrap = TextureWrap.ClampToEdge;
    protected TextureWrap vWrap = TextureWrap.ClampToEdge;

    /**
     * @return the width of the texture in pixels
     */
    public abstract int getWidth();

    /**
     * @return the height of the texture in pixels
     */
    public abstract int getHeight();

    /**
     * @return the depth of the texture in pixels
     */
    public abstract int getDepth();

    /**
     * Generates a new OpenGL texture with the specified target.
     */
    public GLTexture(int glTarget) {
        this(glTarget, GLTool.glGenTexture());
    }

    public GLTexture(int glTarget, int glHandle) {
        this.glTarget = glTarget;
        this.glHandle = glHandle;
    }

    protected abstract void reload();

    /**
     * Binds this texture. The texture will be bound to the currently active texture unit specified via
     * {@link GLES20#glActiveTexture(int)}.
     */
    public void bind() {
        GLES20.glBindTexture(glTarget, glHandle);
    }

    /**
     * Binds the texture to the given texture unit. Sets the currently active texture unit via
     * {@link GLES20#glActiveTexture(int)}.
     *
     * @param unit the unit (0 to MAX_TEXTURE_UNITS).
     */
    public void bind(int unit) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
        GLES20.glBindTexture(glTarget, glHandle);
    }

    /**
     * @return The {@link TextureFilter} used for minification.
     */
    public TextureFilter getMinFilter() {
        return minFilter;
    }

    /**
     * @return The {@link TextureFilter} used for magnification.
     */
    public TextureFilter getMagFilter() {
        return magFilter;
    }

    /**
     * @return The {@link TextureWrap} used for horizontal (U) texture coordinates.
     */
    public TextureWrap getUWrap() {
        return uWrap;
    }

    /**
     * @return The {@link TextureWrap} used for vertical (V) texture coordinates.
     */
    public TextureWrap getVWrap() {
        return vWrap;
    }

    /**
     * @return The OpenGL handle for this texture.
     */
    public int getTextureObjectHandle() {
        return glHandle;
    }

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
     *
     * @param u the u wrap
     * @param v the v wrap
     */
    public void unsafeSetWrap(TextureWrap u, TextureWrap v) {
        unsafeSetWrap(u, v, false);
    }

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. Assumes the texture is bound and active!
     *
     * @param u     the u wrap
     * @param v     the v wrap
     * @param force True to always set the values, even if they are the same as the current values.
     */
    public void unsafeSetWrap(TextureWrap u, TextureWrap v, boolean force) {
        if (u != null && (force || uWrap != u)) {
            GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_WRAP_S, u.getGLEnum());
            uWrap = u;
        }
        if (v != null && (force || vWrap != v)) {
            GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_WRAP_T, v.getGLEnum());
            vWrap = v;
        }
    }

    /**
     * Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
     *
     * @param u the u wrap
     * @param v the v wrap
     */
    public void setWrap(TextureWrap u, TextureWrap v) {
        this.uWrap = u;
        this.vWrap = v;
        bind();
        GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_WRAP_S, u.getGLEnum());
        GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_WRAP_T, v.getGLEnum());
    }

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is
     * bound and active!
     *
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     */
    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter) {
        unsafeSetFilter(minFilter, magFilter, false);
    }

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. Assumes the texture is
     * bound and active!
     *
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     * @param force     True to always set the values, even if they are the same as the current values.
     */
    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter, boolean force) {
        if (minFilter != null && (force || this.minFilter != minFilter)) {
            GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
            this.minFilter = minFilter;
        }
        if (magFilter != null && (force || this.magFilter != magFilter)) {
            GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
            this.magFilter = magFilter;
        }
    }

    /**
     * Sets the {@link TextureFilter} for this texture for minification and magnification. This will bind this texture!
     *
     * @param minFilter the minification filter
     * @param magFilter the magnification filter
     */
    public void setFilter(TextureFilter minFilter, TextureFilter magFilter) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        bind();
        GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
        GLES20.glTexParameteri(glTarget, GLES20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
    }

    /**
     * Destroys the OpenGL Texture as specified by the glHandle.
     */
    protected void delete() {
        if (glHandle != 0) {
            GLTool.glDeleteTexture(glHandle);
            glHandle = 0;
        }
    }

    @Override
    public void dispose() {
        delete();
    }

    protected static void uploadImageData(int target, TextureData data) {
        uploadImageData(target, data, 0);
    }

    public static void uploadImageData(int target, TextureData data, int miplevel) {
        if (data == null) {
            // FIXME: remove texture on target?
            return;
        }

        if (!data.isPrepared()) data.prepare();

        final TextureDataType type = data.getType();
        if (type == TextureDataType.Custom) {
            data.consumeCustomData(target);
            return;
        }

        Bitmap bitmap = data.consumeBitmap();
        boolean disposeBitmap = data.disposeBitmap();

        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        GLUtils.texImage2D(target, miplevel, bitmap, 0);
        if (data.useMipMaps()) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
        if (disposeBitmap) bitmap.recycle();
    }
}
