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
import android.opengl.GLUtils;

import com.joshua.gdx.gdxlite.graphics.Texture.TextureFilter;
import com.joshua.gdx.gdxlite.graphics.Texture.TextureWrap;
import com.joshua.gdx.gdxlite.graphics.glutils.GLTool;
import com.joshua.gdx.gdxlite.math.Vector3;
import com.joshua.gdx.gdxlite.utils.FileUtil;

/**
 * Wraps a standard OpenGL ES Cubemap. Must be disposed when it is no longer used.
 *
 * @author Xoppa
 */
public class Cubemap extends GLTexture {
    /**
     * Enum to identify each side of a Cubemap
     */
    public enum CubemapSide {
        /**
         * The positive X and first side of the cubemap
         */
        PositiveX(0, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, -1, 0, 1, 0, 0),
        /**
         * The negative X and second side of the cubemap
         */
        NegativeX(1, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, -1, 0, -1, 0, 0),
        /**
         * The positive Y and third side of the cubemap
         */
        PositiveY(2, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, 0, 1, 0, 1, 0),
        /**
         * The negative Y and fourth side of the cubemap
         */
        NegativeY(3, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, 0, -1, 0, -1, 0),
        /**
         * The positive Z and fifth side of the cubemap
         */
        PositiveZ(4, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, -1, 0, 0, 0, 1),
        /**
         * The negative Z and sixth side of the cubemap
         */
        NegativeZ(5, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, -1, 0, 0, 0, -1);

        /**
         * The zero based index of the side in the cubemap
         */
        public final int index;
        /**
         * The OpenGL target (used for glTexImage2D) of the side.
         */
        public final int glEnum;
        /**
         * The up vector to target the side.
         */
        public final Vector3 up;
        /**
         * The direction vector to target the side.
         */
        public final Vector3 direction;

        CubemapSide(int index, int glEnum, float upX, float upY, float upZ, float directionX, float directionY, float
                directionZ) {
            this.index = index;
            this.glEnum = glEnum;
            this.up = new Vector3(upX, upY, upZ);
            this.direction = new Vector3(directionX, directionY, directionZ);
        }

        /**
         * @return The OpenGL target (used for glTexImage2D) of the side.
         */
        public int getGLEnum() {
            return glEnum;
        }

        /**
         * @return The up vector of the side.
         */
        public Vector3 getUp(Vector3 out) {
            return out.set(up);
        }

        /**
         * @return The direction vector of the side.
         */
        public Vector3 getDirection(Vector3 out) {
            return out.set(direction);
        }
    }

    protected Bitmap[] data;

    /**
     * Construct a Cubemap with the specified texture files for the sides, optionally generating mipmaps.
     */
    public Cubemap(Context context, String positiveX, String negativeX, String positiveY, String negativeY, String
            positiveZ, String negativeZ) {
        this(FileUtil.internalBitmap(positiveX),
                FileUtil.internalBitmap( negativeX),
                FileUtil.internalBitmap( positiveY),
                FileUtil.internalBitmap( negativeY),
                FileUtil.internalBitmap( positiveZ),
                FileUtil.internalBitmap( negativeZ));
    }

    public Cubemap(Bitmap positiveX, Bitmap negativeX, Bitmap positiveY, Bitmap negativeY,
                   Bitmap positiveZ, Bitmap negativeZ) {
        super(GLES20.GL_TEXTURE_CUBE_MAP);
        minFilter = TextureFilter.Nearest;
        magFilter = TextureFilter.Nearest;
        uWrap = TextureWrap.ClampToEdge;
        vWrap = TextureWrap.ClampToEdge;

        data = new Bitmap[6];
        data[0] = positiveX;
        data[1] = negativeX;
        data[2] = positiveY;
        data[3] = negativeY;
        data[4] = positiveZ;
        data[5] = negativeZ;
        load(data);
    }

    private void load(Bitmap[] data) {
        bind();
        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        consumeCubemapData(data);
        GLES20.glBindTexture(glTarget, 0);
    }

    private void consumeCubemapData(Bitmap[] data) {
        for (int i = 0; i < data.length; i++) {
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, data[i], 0);
        }
    }

    @Override
    protected void reload() {
        glHandle = GLTool.glGenTexture();
        load(data);
    }

    @Override
    public int getWidth() {
        int tmp, width = 0;
        if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.PositiveY.index] != null && (tmp = data[CubemapSide.PositiveY.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.NegativeY.index] != null && (tmp = data[CubemapSide.NegativeY.index].getWidth()) > width)
            width = tmp;
        return width;
    }

    @Override
    public int getHeight() {
        int tmp, height = 0;
        if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getHeight()) > height)
            height = tmp;
        return height;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    /**
     * Disposes all resources associated with the cubemap
     */
    @Override
    public void dispose() {
        if (glHandle == 0) return;
        delete();
    }
}
