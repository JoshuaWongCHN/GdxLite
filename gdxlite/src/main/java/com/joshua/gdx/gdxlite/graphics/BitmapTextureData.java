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

public class BitmapTextureData implements TextureData {
    private Bitmap Bitmap;
    private boolean useMipMaps;
    private boolean disposeBitmap;
    private boolean managed;

    public BitmapTextureData(Bitmap Bitmap, boolean useMipMaps, boolean disposeBitmap) {
        this(Bitmap, useMipMaps, disposeBitmap, false);
    }

    public BitmapTextureData(Bitmap Bitmap, boolean useMipMaps, boolean disposeBitmap, boolean managed) {
        this.Bitmap = Bitmap;
        this.useMipMaps = useMipMaps;
        this.disposeBitmap = disposeBitmap;
        this.managed = managed;
    }

    @Override
    public boolean disposeBitmap() {
        return disposeBitmap;
    }

    @Override
    public Bitmap consumeBitmap() {
        return Bitmap;
    }

    @Override
    public int getWidth() {
        return Bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return Bitmap.getHeight();
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

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void prepare() {
        throw new RuntimeException("prepare() must not be called on a BitmapTextureData instance as it is already " +
                "prepared.");
    }
}
