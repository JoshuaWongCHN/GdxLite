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

import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;

/**
 * A single vertex attribute defined by its {@link Usage}, its number of components and its shader alias. The Usage
 * is used
 * for uniquely identifying the vertex attribute from among its {@linkplain VertexAttributes} siblings. The number of
 * components
 * defines how many components the attribute has. The alias defines to which shader attribute this attribute should
 * bind. The alias
 * is used by a {@link Mesh} when drawing with a {@link ShaderProgram}. The alias can be changed at any time.
 *
 * @author mzechner
 */
public final class VertexAttribute {
    /**
     * the number of components this attribute has
     **/
    public final int numComponents;
    /**
     * For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f and +1f (unsigned)
     */
    public final boolean normalized;
    /**
     * the OpenGL type of each component, e.g. {@link GLES20#GL_FLOAT} or {@link GLES20#GL_UNSIGNED_BYTE}
     */
    public final int type;
    /**
     * the offset of this attribute in bytes, don't change this!
     **/
    public int offset;
    /**
     * the alias for the attribute used in a {@link ShaderProgram}
     **/
    public String alias;
    /**
     * optional unit/index specifier, used for texture coordinates and bone weights
     **/
    public int unit;

    /**
     * Constructs a new VertexAttribute. The GL data type is automatically selected based on the usage.
     *
     * @param numComponents the number of components of this attribute, must be between 1 and 4.
     * @param alias         the alias used in a shader for this attribute. Can be changed after construction.
     */
    public VertexAttribute(int numComponents, String alias) {
        this(numComponents, alias, 0);
    }

    /**
     * Constructs a new VertexAttribute. The GL data type is automatically selected based on the usage.
     *
     * @param numComponents the number of components of this attribute, must be between 1 and 4.
     * @param alias         the alias used in a shader for this attribute. Can be changed after construction.
     * @param unit          Optional unit/index specifier, used for texture coordinates and bone weights
     */
    public VertexAttribute(int numComponents, String alias, int unit) {
        this(numComponents, GLES20.GL_FLOAT, false, alias, unit);
    }

    /**
     * Constructs a new VertexAttribute.
     *
     * @param numComponents the number of components of this attribute, must be between 1 and 4.
     * @param type          the OpenGL type of each component, e.g. {@link GLES20#GL_FLOAT} or
     *                      {@link GLES20#GL_UNSIGNED_BYTE}. Since {@link Mesh}
     *                      stores vertex data in 32bit floats, the total size of this attribute (type size times
     *                      number of components) must be a
     *                      multiple of four.
     * @param normalized    For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f
     *                      and +1f (unsigned)
     * @param alias         The alias used in a shader for this attribute. Can be changed after construction.
     */
    public VertexAttribute(int numComponents, int type, boolean normalized, String alias) {
        this(numComponents, type, normalized, alias, 0);
    }

    /**
     * Constructs a new VertexAttribute.
     *
     * @param numComponents the number of components of this attribute, must be between 1 and 4.
     * @param type          the OpenGL type of each component, e.g. {@link GLES20#GL_FLOAT} or
     *                      {@link GLES20#GL_UNSIGNED_BYTE}. Since {@link Mesh}
     *                      stores vertex data in 32bit floats, the total size of this attribute (type size times
     *                      number of components) must be a
     *                      multiple of four bytes.
     * @param normalized    For fixed types, whether the values are normalized to either -1f and +1f (signed) or 0f
     *                      and +1f (unsigned)
     * @param alias         The alias used in a shader for this attribute. Can be changed after construction.
     * @param unit          Optional unit/index specifier, used for texture coordinates and bone weights
     */
    public VertexAttribute(int numComponents, int type, boolean normalized, String alias, int unit) {
        this.numComponents = numComponents;
        this.type = type;
        this.normalized = normalized;
        this.alias = alias;
        this.unit = unit;
    }

    /**
     * @return A copy of this VertexAttribute with the same parameters. The {@link #offset} is not copied and must
     * be recalculated, as is typically done by the {@linkplain VertexAttributes} that owns the VertexAttribute.
     */
    public VertexAttribute copy() {
        return new VertexAttribute(numComponents, type, normalized, alias, unit);
    }

    public static VertexAttribute Position() {
        return new VertexAttribute(3, ShaderProgram.POSITION_ATTRIBUTE);
    }

    public static VertexAttribute TexCoords(int unit) {
        return new VertexAttribute(2, ShaderProgram.TEXCOORD_ATTRIBUTE + unit, unit);
    }

    public static VertexAttribute Normal() {
        return new VertexAttribute(3, ShaderProgram.NORMAL_ATTRIBUTE);
    }

    public static VertexAttribute ColorPacked() {
        return new VertexAttribute(4, GLES20.GL_UNSIGNED_BYTE, true, ShaderProgram.COLOR_ATTRIBUTE);
    }

    public static VertexAttribute ColorUnpacked() {
        return new VertexAttribute(4, GLES20.GL_FLOAT, false, ShaderProgram.COLOR_ATTRIBUTE);
    }

    public static VertexAttribute Tangent() {
        return new VertexAttribute(3, ShaderProgram.TANGENT_ATTRIBUTE);
    }

    public static VertexAttribute Binormal() {
        return new VertexAttribute(3, ShaderProgram.BINORMAL_ATTRIBUTE);
    }

    public static VertexAttribute BoneWeight(int unit) {
        return new VertexAttribute(2, ShaderProgram.BONEWEIGHT_ATTRIBUTE + unit, unit);
    }

    /**
     * Tests to determine if the passed object was created with the same parameters
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof VertexAttribute)) {
            return false;
        }
        return equals((VertexAttribute) obj);
    }

    public boolean equals(final VertexAttribute other) {
        return other != null && numComponents == other.numComponents && type == other.type && normalized == other
                .normalized && alias.equals(other.alias)
                && unit == other.unit;
    }

    /**
     * @return How many bytes this attribute uses.
     */
    public int getSizeInBytes() {
        switch (type) {
            case GLES20.GL_FLOAT:
            case GLES20.GL_FIXED:
                return 4 * numComponents;
            case GLES20.GL_UNSIGNED_BYTE:
            case GLES20.GL_BYTE:
                return numComponents;
            case GLES20.GL_UNSIGNED_SHORT:
            case GLES20.GL_SHORT:
                return 2 * numComponents;
        }
        return 0;
    }
}
