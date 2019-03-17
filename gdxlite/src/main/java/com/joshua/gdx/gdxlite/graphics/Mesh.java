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

import com.joshua.gdx.gdxlite.graphics.glutils.IndexArray;
import com.joshua.gdx.gdxlite.graphics.glutils.IndexBufferObject;
import com.joshua.gdx.gdxlite.graphics.glutils.IndexBufferObjectSubData;
import com.joshua.gdx.gdxlite.graphics.glutils.IndexData;
import com.joshua.gdx.gdxlite.graphics.glutils.ShaderProgram;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexArray;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexBufferObject;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexBufferObjectSubData;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexBufferObjectWithVAO;
import com.joshua.gdx.gdxlite.graphics.glutils.VertexData;
import com.joshua.gdx.gdxlite.utils.Disposable;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * <p>
 * A Mesh holds vertices composed of attributes specified by a {@link VertexAttributes} instance. The vertices are
 * held either in
 * VRAM in form of vertex buffer objects or in RAM in form of vertex arrays. The former variant is more performant
 * and is
 * preferred over vertex arrays if hardware supports it.
 * </p>
 *
 * <p>
 * Meshes are automatically managed. If the OpenGL context is lost all vertex buffer objects get invalidated and must
 * be reloaded
 * when the context is recreated. This only happens on Android when a user switches to another application or
 * receives an incoming
 * call. A managed Mesh will be reloaded automagically so you don't have to do this manually.
 * </p>
 *
 * <p>
 * A Mesh consists of vertices and optionally indices which specify which vertices define a triangle. Each vertex is
 * composed of
 * attributes such as position, normal, color or texture coordinate. Note that not all of this attributes must be
 * given, except
 * for position which is non-optional. Each attribute has an alias which is used when rendering a Mesh in OpenGL ES
 * 2.0. The alias
 * is used to bind a specific vertex attribute to a shader attribute. The shader source and the alias of the
 * attribute must match
 * exactly for this to work.
 * </p>
 *
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Xoppa
 */
public class Mesh implements Disposable {
    public enum VertexDataType {
        VertexArray, VertexBufferObject, VertexBufferObjectSubData, VertexBufferObjectWithVAO
    }

    final VertexData vertices;
    final IndexData indices;
    boolean autoBind = true;
    final boolean isVertexArray;

    protected Mesh(VertexData vertices, IndexData indices, boolean isVertexArray) {
        this.vertices = vertices;
        this.indices = indices;
        this.isVertexArray = isVertexArray;
    }

    /**
     * Creates a new Mesh with the given attributes.
     *
     * @param isStatic    whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices  the maximum number of indices this mesh can hold
     * @param attributes  the {@link VertexAttribute}s. Each vertex attribute defines one property of a vertex such as
     *                    position,
     *                    normal or texture coordinate
     */
    public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        vertices = makeVertexBuffer(isStatic, maxVertices, new VertexAttributes(attributes));
        indices = new IndexBufferObject(isStatic, maxIndices);
        isVertexArray = false;

    }

    /**
     * Creates a new Mesh with the given attributes.
     *
     * @param isStatic    whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices  the maximum number of indices this mesh can hold
     * @param attributes  the {@link VertexAttributes}. Each vertex attribute defines one property of a vertex such as
     *                    position,
     *                    normal or texture coordinate
     */
    public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        vertices = makeVertexBuffer(isStatic, maxVertices, attributes);
        indices = new IndexBufferObject(isStatic, maxIndices);
        isVertexArray = false;
    }

    /**
     * Creates a new Mesh with the given attributes. Adds extra optimizations for dynamic (frequently modified) meshes.
     *
     * @param staticVertices whether vertices of this mesh are static or not. Allows for internal optimizations.
     * @param staticIndices  whether indices of this mesh are static or not. Allows for internal optimizations.
     * @param maxVertices    the maximum number of vertices this mesh can hold
     * @param maxIndices     the maximum number of indices this mesh can hold
     * @param attributes     the {@link VertexAttributes}. Each vertex attribute defines one property of a vertex such
     *                       as position,
     *                       normal or texture coordinate
     * @author Jaroslaw Wisniewski <j.wisniewski@appsisle.com>
     **/
    public Mesh(boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttributes
            attributes) {
        vertices = makeVertexBuffer(staticVertices, maxVertices, attributes);
        indices = new IndexBufferObject(staticIndices, maxIndices);
        isVertexArray = false;
    }

    private VertexData makeVertexBuffer(boolean isStatic, int maxVertices, VertexAttributes vertexAttributes) {
//		ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
//		ConfigurationInfo info = am.getDeviceConfigurationInfo();
//		String v = info.getGlEsVersion(); 
//		if (v.equalsIgnoreCase("3.0")) {
        //todo
        if (true) {
            return new VertexBufferObjectWithVAO(isStatic, maxVertices, vertexAttributes);
        } else {
            return new VertexBufferObject(isStatic, maxVertices, vertexAttributes);
        }
    }

    /**
     * Creates a new Mesh with the given attributes. This is an expert method with no error checking. Use at your own
     * risk.
     *
     * @param type        the {@link VertexDataType} to be used, VBO or VA.
     * @param isStatic    whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices  the maximum number of indices this mesh can hold
     * @param attributes  the {@link VertexAttribute}s. Each vertex attribute defines one property of a vertex such as
     *                    position,
     *                    normal or texture coordinate
     */
    public Mesh(VertexDataType type, boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        this(type, isStatic, maxVertices, maxIndices, new VertexAttributes(attributes));
    }

    /**
     * Creates a new Mesh with the given attributes. This is an expert method with no error checking. Use at your own
     * risk.
     *
     * @param type        the {@link VertexDataType} to be used, VBO or VA.
     * @param isStatic    whether this mesh is static or not. Allows for internal optimizations.
     * @param maxVertices the maximum number of vertices this mesh can hold
     * @param maxIndices  the maximum number of indices this mesh can hold
     * @param attributes  the {@link VertexAttributes}.
     */
    public Mesh(VertexDataType type, boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        switch (type) {
            case VertexBufferObject:
                vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
                indices = new IndexBufferObject(isStatic, maxIndices);
                isVertexArray = false;
                break;
            case VertexBufferObjectSubData:
                vertices = new VertexBufferObjectSubData(isStatic, maxVertices, attributes);
                indices = new IndexBufferObjectSubData(isStatic, maxIndices);
                isVertexArray = false;
                break;
            case VertexBufferObjectWithVAO:
                vertices = new VertexBufferObjectWithVAO(isStatic, maxVertices, attributes);
                indices = new IndexBufferObjectSubData(isStatic, maxIndices);
                isVertexArray = false;
                break;
            case VertexArray:
            default:
                vertices = new VertexArray(maxVertices, attributes);
                indices = new IndexArray(maxIndices);
                isVertexArray = true;
                break;
        }
    }

    /**
     * Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
     *
     * @param vertices the vertices.
     * @return the mesh for invocation chaining.
     */
    public Mesh setVertices(float[] vertices) {
        this.vertices.setVertices(vertices, 0, vertices.length);

        return this;
    }

    /**
     * Sets the vertices of this Mesh. The attributes are assumed to be given in float format.
     *
     * @param vertices the vertices.
     * @param offset   the offset into the vertices array
     * @param count    the number of floats to use
     * @return the mesh for invocation chaining.
     */
    public Mesh setVertices(float[] vertices, int offset, int count) {
        this.vertices.setVertices(vertices, offset, count);

        return this;
    }

    /**
     * Update (a portion of) the vertices. Does not resize the backing buffer.
     *
     * @param targetOffset the offset in number of floats of the mesh part.
     * @param source       the vertex data to update the mesh part with
     */
    public Mesh updateVertices(int targetOffset, float[] source) {
        return updateVertices(targetOffset, source, 0, source.length);
    }

    /**
     * Update (a portion of) the vertices. Does not resize the backing buffer.
     *
     * @param targetOffset the offset in number of floats of the mesh part.
     * @param source       the vertex data to update the mesh part with
     * @param sourceOffset the offset in number of floats within the source array
     * @param count        the number of floats to update
     */
    public Mesh updateVertices(int targetOffset, float[] source, int sourceOffset, int count) {
        this.vertices.updateVertices(targetOffset, source, sourceOffset, count);
        return this;
    }

    /**
     * Copies the vertices from the Mesh to the float array. The float array must be large enough to hold all the
     * Mesh's vertices.
     *
     * @param vertices the array to copy the vertices to
     */
    public float[] getVertices(float[] vertices) {
        return getVertices(0, -1, vertices);
    }

    /**
     * Copies the the remaining vertices from the Mesh to the float array. The float array must be large enough to
     * hold the
     * remaining vertices.
     *
     * @param srcOffset the offset (in number of floats) of the vertices in the mesh to copy
     * @param vertices  the array to copy the vertices to
     */
    public float[] getVertices(int srcOffset, float[] vertices) {
        return getVertices(srcOffset, -1, vertices);
    }

    /**
     * Copies the specified vertices from the Mesh to the float array. The float array must be large enough to hold
     * count vertices.
     *
     * @param srcOffset the offset (in number of floats) of the vertices in the mesh to copy
     * @param count     the amount of floats to copy
     * @param vertices  the array to copy the vertices to
     */
    public float[] getVertices(int srcOffset, int count, float[] vertices) {
        return getVertices(srcOffset, count, vertices, 0);
    }

    /**
     * Copies the specified vertices from the Mesh to the float array. The float array must be large enough to hold
     * destOffset+count vertices.
     *
     * @param srcOffset  the offset (in number of floats) of the vertices in the mesh to copy
     * @param count      the amount of floats to copy
     * @param vertices   the array to copy the vertices to
     * @param destOffset the offset (in floats) in the vertices array to start copying
     */
    public float[] getVertices(int srcOffset, int count, float[] vertices, int destOffset) {
        // TODO: Perhaps this method should be vertexSize aware??
        final int max = getNumVertices() * getVertexSize() / 4;
        if (count == -1) {
            count = max - srcOffset;
            if (count > vertices.length - destOffset) count = vertices.length - destOffset;
        }
        if (srcOffset < 0 || count <= 0 || (srcOffset + count) > max || destOffset < 0 || destOffset >= vertices.length)
            throw new IndexOutOfBoundsException();
        if ((vertices.length - destOffset) < count)
            throw new IllegalArgumentException("not enough room in vertices array, has " + vertices.length + " " +
                    "floats, needs "
                    + count);
        int pos = getVerticesBuffer().position();
        getVerticesBuffer().position(srcOffset);
        getVerticesBuffer().get(vertices, destOffset, count);
        getVerticesBuffer().position(pos);
        return vertices;
    }

    /**
     * Sets the indices of this Mesh
     *
     * @param indices the indices
     * @return the mesh for invocation chaining.
     */
    public Mesh setIndices(short[] indices) {
        this.indices.setIndices(indices, 0, indices.length);

        return this;
    }

    /**
     * Sets the indices of this Mesh.
     *
     * @param indices the indices
     * @param offset  the offset into the indices array
     * @param count   the number of indices to copy
     * @return the mesh for invocation chaining.
     */
    public Mesh setIndices(short[] indices, int offset, int count) {
        this.indices.setIndices(indices, offset, count);

        return this;
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold all the
     * Mesh's indices.
     *
     * @param indices the array to copy the indices to
     */
    public void getIndices(short[] indices) {
        getIndices(indices, 0);
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold destOffset +
     * all the
     * Mesh's indices.
     *
     * @param indices    the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(short[] indices, int destOffset) {
        getIndices(0, indices, destOffset);
    }

    /**
     * Copies the remaining indices from the Mesh to the short array. The short array must be large enough to hold
     * destOffset + all
     * the remaining indices.
     *
     * @param srcOffset  the zero-based offset of the first index to fetch
     * @param indices    the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(int srcOffset, short[] indices, int destOffset) {
        getIndices(srcOffset, -1, indices, destOffset);
    }

    /**
     * Copies the indices from the Mesh to the short array. The short array must be large enough to hold destOffset +
     * count
     * indices.
     *
     * @param srcOffset  the zero-based offset of the first index to fetch
     * @param count      the total amount of indices to copy
     * @param indices    the array to copy the indices to
     * @param destOffset the offset in the indices array to start copying
     */
    public void getIndices(int srcOffset, int count, short[] indices, int destOffset) {
        int max = getNumIndices();
        if (count < 0) count = max - srcOffset;
        if (srcOffset < 0 || srcOffset >= max || srcOffset + count > max)
            throw new IllegalArgumentException("Invalid range specified, offset: " + srcOffset + ", count: " + count
                    + ", max: "
                    + max);
        if ((indices.length - destOffset) < count)
            throw new IllegalArgumentException("not enough room in indices array, has " + indices.length + " shorts, " +
                    "needs " + count);
        int pos = getIndicesBuffer().position();
        getIndicesBuffer().position(srcOffset);
        getIndicesBuffer().get(indices, destOffset, count);
        getIndicesBuffer().position(pos);
    }

    /**
     * @return the number of defined indices
     */
    public int getNumIndices() {
        return indices.getNumIndices();
    }

    /**
     * @return the number of defined vertices
     */
    public int getNumVertices() {
        return vertices.getNumVertices();
    }

    /**
     * @return the maximum number of vertices this mesh can hold
     */
    public int getMaxVertices() {
        return vertices.getNumMaxVertices();
    }

    /**
     * @return the maximum number of indices this mesh can hold
     */
    public int getMaxIndices() {
        return indices.getNumMaxIndices();
    }

    /**
     * @return the size of a single vertex in bytes
     */
    public int getVertexSize() {
        return vertices.getAttributes().vertexSize;
    }

    /**
     * Sets whether to bind the underlying {@link VertexArray} or {@link VertexBufferObject} automatically on a call
     * to one of the
     * render methods. Usually you want to use autobind. Manual binding is an expert functionality. There is a driver
     * bug on the
     * MSM720xa chips that will fuck up memory if you manipulate the vertices and indices of a Mesh multiple times
     * while it is
     * bound. Keep this in mind.
     *
     * @param autoBind whether to autobind meshes.
     */
    public void setAutoBind(boolean autoBind) {
        this.autoBind = autoBind;
    }

    /**
     * Binds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} if indices where given. Use this
     * with OpenGL
     * ES 2.0 and when auto-bind is disabled.
     *
     * @param shader the shader (does not bind the shader)
     */
    public void bind(final ShaderProgram shader) {
        bind(shader, null);
    }

    /**
     * Binds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} if indices where given. Use this
     * with OpenGL
     * ES 2.0 and when auto-bind is disabled.
     *
     * @param shader    the shader (does not bind the shader)
     * @param locations array containing the attribute locations.
     */
    public void bind(final ShaderProgram shader, final int[] locations) {
        vertices.bind(shader, locations);
        if (indices.getNumIndices() > 0) indices.bind();
    }

    /**
     * Unbinds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} is indices were given. Use this
     * with OpenGL
     * ES 1.x and when auto-bind is disabled.
     *
     * @param shader the shader (does not unbind the shader)
     */
    public void unbind(final ShaderProgram shader) {
        unbind(shader, null);
    }

    /**
     * Unbinds the underlying {@link VertexBufferObject} and {@link IndexBufferObject} is indices were given. Use this
     * with OpenGL
     * ES 1.x and when auto-bind is disabled.
     *
     * @param shader    the shader (does not unbind the shader)
     * @param locations array containing the attribute locations.
     */
    public void unbind(final ShaderProgram shader, final int[] locations) {
        vertices.unbind(shader, locations);
        if (indices.getNumIndices() > 0) indices.unbind();
    }

    /**
     * <p>
     * Renders the mesh using the given primitive type. If indices are set for this mesh then getNumIndices() /
     * #vertices per
     * primitive primitives are rendered. If no indices are set then getNumVertices() / #vertices per primitive are
     * rendered.
     * </p>
     *
     * <p>
     * This method will automatically bind each vertex attribute as specified at construction time via
     * {@link VertexAttributes} to
     * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
     * </p>
     *
     * <p>
     * This method must only be called after the {@link ShaderProgram#begin()} method has been called!
     * </p>
     *
     * <p>
     * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x
     * is used.
     * </p>
     *
     * @param primitiveType the primitive type
     */
    public void render(ShaderProgram shader, int primitiveType) {
        render(shader, primitiveType, 0, indices.getNumMaxIndices() > 0 ? getNumIndices() : getNumVertices(),
                autoBind);
    }

    /**
     * <p>
     * Renders the mesh using the given primitive type. offset specifies the offset into either the vertex buffer or
     * the index
     * buffer depending on whether indices are defined. count specifies the number of vertices or indices to use thus
     * count /
     * #vertices per primitive primitives are rendered.
     * </p>
     *
     * <p>
     * This method will automatically bind each vertex attribute as specified at construction time via
     * {@link VertexAttributes} to
     * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
     * </p>
     *
     * <p>
     * This method must only be called after the {@link ShaderProgram#begin()} method has been called!
     * </p>
     *
     * <p>
     * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x
     * is used.
     * </p>
     *
     * @param shader        the shader to be used
     * @param primitiveType the primitive type
     * @param offset        the offset into the vertex or index buffer
     * @param count         number of vertices or indices to use
     */
    public void render(ShaderProgram shader, int primitiveType, int offset, int count) {
        render(shader, primitiveType, offset, count, autoBind);
    }

    /**
     * <p>
     * Renders the mesh using the given primitive type. offset specifies the offset into either the vertex buffer or
     * the index
     * buffer depending on whether indices are defined. count specifies the number of vertices or indices to use thus
     * count /
     * #vertices per primitive primitives are rendered.
     * </p>
     *
     * <p>
     * This method will automatically bind each vertex attribute as specified at construction time via
     * {@link VertexAttributes} to
     * the respective shader attributes. The binding is based on the alias defined for each VertexAttribute.
     * </p>
     *
     * <p>
     * This method must only be called after the {@link ShaderProgram#begin()} method has been called!
     * </p>
     *
     * <p>
     * This method is intended for use with OpenGL ES 2.0 and will throw an IllegalStateException when OpenGL ES 1.x
     * is used.
     * </p>
     *
     * @param shader        the shader to be used
     * @param primitiveType the primitive type
     * @param offset        the offset into the vertex or index buffer
     * @param count         number of vertices or indices to use
     * @param autoBind      overrides the autoBind member of this Mesh
     */
    public void render(ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
        if (count == 0) return;

        if (autoBind) bind(shader);

        if (isVertexArray) {
            if (indices.getNumIndices() > 0) {
                ShortBuffer buffer = indices.getBuffer();
                int oldPosition = buffer.position();
                int oldLimit = buffer.limit();
                buffer.position(offset);
                buffer.limit(offset + count);
                GLES20.glDrawElements(primitiveType, count, GLES20.GL_UNSIGNED_SHORT, buffer);
                buffer.position(oldPosition);
                buffer.limit(oldLimit);
            } else {
                GLES20.glDrawArrays(primitiveType, offset, count);
            }
        } else {
            if (indices.getNumIndices() > 0) {
                if (count + offset > indices.getNumMaxIndices()) {
                    throw new RuntimeException("Mesh attempting to access memory outside of the index buffer " +
                            "(count: "
                            + count + ", offset: " + offset + ", max: " + indices.getNumMaxIndices() + ")");
                }

                GLES20.glDrawElements(primitiveType, count, GLES20.GL_UNSIGNED_SHORT, offset * 2);
            } else {
                GLES20.glDrawArrays(primitiveType, offset, count);
            }
        }

        if (autoBind) unbind(shader);
    }

    /**
     * Frees all resources associated with this Mesh
     */
    public void dispose() {
        vertices.dispose();
        indices.dispose();
    }

    /**
     * @return the vertex attributes of this Mesh
     */
    public VertexAttributes getVertexAttributes() {
        return vertices.getAttributes();
    }

    /**
     * @return the backing FloatBuffer holding the vertices. Does not have to be a direct buffer on Android!
     */
    public FloatBuffer getVerticesBuffer() {
        return vertices.getBuffer();
    }

    /**
     * @return the backing shortbuffer holding the indices. Does not have to be a direct buffer on Android!
     */
    public ShortBuffer getIndicesBuffer() {
        return indices.getBuffer();
    }
}
