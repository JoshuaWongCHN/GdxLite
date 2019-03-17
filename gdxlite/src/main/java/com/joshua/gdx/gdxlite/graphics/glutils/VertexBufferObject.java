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

package com.joshua.gdx.gdxlite.graphics.glutils;

import android.opengl.GLES20;

import com.joshua.gdx.gdxlite.graphics.VertexAttribute;
import com.joshua.gdx.gdxlite.graphics.VertexAttributes;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * <p>
 * The data is bound via glVertexAttribPointer() according to the attribute aliases specified via
 * {@link VertexAttributes}
 * in the constructor.
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 *
 * @author mzechner, Dave Clayton <contact@redskyforge.com>
 */
public class VertexBufferObject implements VertexData {
    private VertexAttributes attributes;
    private FloatBuffer buffer;
    private ByteBuffer byteBuffer;
    private boolean ownsBuffer;
    private int bufferHandle;
    private int usage;
    boolean isDirty = false;
    boolean isBound = false;

    /**
     * Constructs a new interleaved VertexBufferObject.
     *
     * @param isStatic    whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes  the {@link VertexAttribute}s.
     */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttribute... attributes) {
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    /**
     * Constructs a new interleaved VertexBufferObject.
     *
     * @param isStatic    whether the vertex data is static.
     * @param numVertices the maximum number of vertices
     * @param attributes  the {@link VertexAttributes}.
     */
    public VertexBufferObject(boolean isStatic, int numVertices, VertexAttributes attributes) {
        bufferHandle = GLTool.glGenBuffer();

        ByteBuffer data = ByteBuffer.allocateDirect(attributes.vertexSize * numVertices).order(ByteOrder.nativeOrder());
        data.limit(0);
        setBuffer(data, true, attributes);
        setUsage(isStatic ? GLES20.GL_STATIC_DRAW : GLES20.GL_DYNAMIC_DRAW);
    }

    protected VertexBufferObject(int usage, ByteBuffer data, boolean ownsBuffer, VertexAttributes attributes) {
        bufferHandle = GLTool.glGenBuffer();

        setBuffer(data, ownsBuffer, attributes);
        setUsage(usage);
    }

    @Override
    public VertexAttributes getAttributes() {
        return attributes;
    }

    @Override
    public int getNumVertices() {
        return buffer.limit() * 4 / attributes.vertexSize;
    }

    @Override
    public int getNumMaxVertices() {
        return byteBuffer.capacity() / attributes.vertexSize;
    }

    @Override
    public FloatBuffer getBuffer() {
        isDirty = true;
        return buffer;
    }

    /**
     * Low level method to reset the buffer and attributes to the specified values. Use with care!
     *
     * @param data
     * @param ownsBuffer
     * @param value
     */
    protected void setBuffer(Buffer data, boolean ownsBuffer, VertexAttributes value) {
        if (isBound) throw new RuntimeException("Cannot change attributes while VBO is bound");
        attributes = value;
        if (data instanceof ByteBuffer)
            byteBuffer = (ByteBuffer) data;
        else
            throw new RuntimeException("Only ByteBuffer is currently supported");
        this.ownsBuffer = ownsBuffer;

        final int l = byteBuffer.limit();
        byteBuffer.limit(byteBuffer.capacity());
        buffer = byteBuffer.asFloatBuffer();
        byteBuffer.limit(l);
        buffer.limit(l / 4);
    }

    private void bufferChanged() {
        if (isBound) {
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }
    }

    @Override
    public void setVertices(float[] vertices, int offset, int count) {
        isDirty = true;
        buffer.put(vertices, offset, count);
        buffer.position(0);
        buffer.limit(count);
        bufferChanged();
    }

    @Override
    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
        isDirty = true;
        buffer.position(targetOffset);
        buffer.put(vertices, sourceOffset, count);
        buffer.position(0);
        bufferChanged();
    }

    /**
     * @return The GL enum used in the call to {@link GLES20#glBufferData(int, int, Buffer, int)}, e.g.
     * GL_STATIC_DRAW or
     * GL_DYNAMIC_DRAW
     */
    protected int getUsage() {
        return usage;
    }

    /**
     * Set the GL enum used in the call to {@link GLES20#glBufferData(int, int, Buffer, int)}, can only be called when
     * the
     * VBO is not bound.
     */
    protected void setUsage(int value) {
        if (isBound) throw new RuntimeException("Cannot change usage while VBO is bound");
        usage = value;
    }

    /**
     * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
     *
     * @param shader the shader
     */
    @Override
    public void bind(ShaderProgram shader) {
        bind(shader, null);
    }

    @Override
    public void bind(ShaderProgram shader, int[] locations) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandle);
        if (isDirty) {
            byteBuffer.limit(buffer.limit() * 4);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
            isDirty = false;
        }

        final int numAttributes = attributes.size();
        if (locations == null) {
            for (int i = 0; i < numAttributes; i++) {
                final VertexAttribute attribute = attributes.get(i);
                final int location = shader.getAttributeLocation(attribute.alias);
                if (location < 0) continue;
                shader.enableVertexAttribute(location);

                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
                        attributes.vertexSize, attribute.offset);
            }

        } else {
            for (int i = 0; i < numAttributes; i++) {
                final VertexAttribute attribute = attributes.get(i);
                final int location = locations[i];
                if (location < 0) continue;
                shader.enableVertexAttribute(location);

                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
                        attributes.vertexSize, attribute.offset);
            }
        }
        isBound = true;
    }

    /**
     * Unbinds this VertexBufferObject.
     *
     * @param shader the shader
     */
    @Override
    public void unbind(final ShaderProgram shader) {
        unbind(shader, null);
    }

    @Override
    public void unbind(final ShaderProgram shader, final int[] locations) {
        final int numAttributes = attributes.size();
        if (locations == null) {
            for (int i = 0; i < numAttributes; i++) {
                shader.disableVertexAttribute(attributes.get(i).alias);
            }
        } else {
            for (int i = 0; i < numAttributes; i++) {
                final int location = locations[i];
                if (location >= 0) shader.disableVertexAttribute(location);
            }
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        isBound = false;
    }

    /**
     * Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
     */
    @Override
    public void invalidate() {
        bufferHandle = GLTool.glGenBuffer();
        isDirty = true;
    }

    /**
     * Disposes of all resources this VertexBufferObject uses.
     */
    @Override
    public void dispose() {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLTool.glDeleteBuffer(bufferHandle);
        bufferHandle = 0;
    }
}
