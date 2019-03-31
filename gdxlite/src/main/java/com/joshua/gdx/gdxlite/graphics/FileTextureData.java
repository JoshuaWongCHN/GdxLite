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

import com.joshua.gdx.gdxlite.utils.FileUtil;

public class FileTextureData implements TextureData {
    private final String filename;
    private final boolean internal;
    private int width = 0;
    private int height = 0;
    private Bitmap bitmap;
    private boolean useMipMaps;
    private boolean isPrepared = false;

    public FileTextureData(String filename, boolean internal, boolean useMipMaps) {
        this.filename = filename;
        this.internal = internal;
        this.useMipMaps = useMipMaps;
        if (bitmap != null) {
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void prepare() {
        if (isPrepared) throw new RuntimeException("Already prepared");
        if (bitmap == null) {
            if (internal) {
                bitmap = FileUtil.internalBitmap(filename);
            } else {
                bitmap = FileUtil.externalBitmap(filename);
            }
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        isPrepared = true;
    }

    @Override
    public Bitmap consumeBitmap() {
        if (!isPrepared) throw new RuntimeException("Call prepare() before calling getBitmap()");
        isPrepared = false;
        Bitmap bitmap = this.bitmap;
        this.bitmap = null;
        return bitmap;
    }

    @Override
    public boolean disposeBitmap() {
        return true;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getFormat() {
        return -1;
    }

    @Override
    public boolean useMipMaps() {
        return useMipMaps;
    }

    @Override
    public TextureDataType getType() {
        return TextureDataType.Bitmap;
    }

    @Override
    public void consumeCustomData(int target) {
        throw new RuntimeException("This TextureData implementation does not upload data itself");
    }

}
