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

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.joshua.gdx.gdxlite.graphics.GLTexture;
import com.joshua.gdx.gdxlite.utils.Array;
import com.joshua.gdx.gdxlite.utils.BufferUtils;
import com.joshua.gdx.gdxlite.utils.Disposable;

import java.nio.IntBuffer;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses.
 * It will
 * automatically create a gltexture for the color attachment and a renderbuffer for the depth buffer. You can get a
 * hold of the
 * gltexture by {@link GLFrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 *
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to
 * another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 *
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 *
 * @author mzechner, realitix
 */
public abstract class GLFrameBuffer<T extends GLTexture> implements Disposable {
    /**
     * the frame buffers
     **/
    protected final static int GL_DEPTH24_STENCIL8_OES = 0x88F0;

    /**
     * the color buffer texture
     **/
    protected Array<T> textureAttachments = new Array<T>();

    /**
     * the default framebuffer handle, a.k.a screen.
     */
    protected static int defaultFramebufferHandle;
    /**
     * true if we have polled for the default handle already.
     */
    protected static boolean defaultFramebufferHandleInitialized = false;

    /**
     * the framebuffer handle
     **/
    protected int framebufferHandle;
    /**
     * the depthbuffer render object handle
     **/
    protected int depthbufferHandle;
    /**
     * the stencilbuffer render object handle
     **/
    protected int stencilbufferHandle;
    /**
     * the depth stencil packed render buffer object handle
     **/
    protected int depthStencilPackedBufferHandle;
    /**
     * if has depth stencil packed buffer
     **/
    protected boolean hasDepthStencilPackedBuffer;

    /**
     * if multiple texture attachments are present
     **/
    protected boolean isMRT;

    protected GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder;

    GLFrameBuffer() {
    }

    /**
     * Creates a GLFrameBuffer from the specifications provided by bufferBuilder
     **/
    protected GLFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<T>> bufferBuilder) {
        this.bufferBuilder = bufferBuilder;
        build();
    }

    /**
     * Convenience method to return the first Texture attachment present in the fbo
     **/
    public T getColorBufferTexture() {
        return textureAttachments.first();
    }

    /**
     * Return the Texture attachments attached to the fbo
     **/
    public Array<T> getTextureAttachments() {
        return textureAttachments;
    }

    /**
     * Override this method in a derived class to set up the backing texture as you like.
     */
    protected abstract T createTexture(FrameBufferTextureAttachmentSpec attachmentSpec);

    /**
     * Override this method in a derived class to dispose the backing texture as you like.
     */
    protected abstract void disposeColorTexture(T colorTexture);

    /**
     * Override this method in a derived class to attach the backing texture to the GL framebuffer object.
     */
    protected abstract void attachFrameBufferColorTexture(T texture);

    protected void build() {
        checkValidBuilder();

        // iOS uses a different framebuffer handle! (not necessarily 0)
        if (!defaultFramebufferHandleInitialized) {
            defaultFramebufferHandleInitialized = true;
            defaultFramebufferHandle = 0;
        }

        framebufferHandle = GLTool.glGenFramebuffer();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferHandle);

        int width = bufferBuilder.width;
        int height = bufferBuilder.height;

        if (bufferBuilder.hasDepthRenderBuffer) {
            depthbufferHandle = GLTool.glGenRenderbuffer();
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthbufferHandle);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, bufferBuilder.depthRenderBufferSpec.internalFormat,
                    width,
                    height);
        }

        if (bufferBuilder.hasStencilRenderBuffer) {
            stencilbufferHandle = GLTool.glGenRenderbuffer();
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, stencilbufferHandle);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, bufferBuilder.stencilRenderBufferSpec.internalFormat,
                    width, height);
        }

        if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
            depthStencilPackedBufferHandle = GLTool.glGenRenderbuffer();
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, bufferBuilder.packedStencilDepthRenderBufferSpec
                            .internalFormat, width,
                    height);
        }

        isMRT = bufferBuilder.textureAttachmentSpecs.size > 1;
        int colorTextureCounter = 0;
        if (isMRT) {
            for (FrameBufferTextureAttachmentSpec attachmentSpec : bufferBuilder.textureAttachmentSpecs) {
                T texture = createTexture(attachmentSpec);
                textureAttachments.add(texture);
                if (attachmentSpec.isColorTexture()) {
                    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0 +
                                    colorTextureCounter,
                            GLES20.GL_TEXTURE_2D,
                            texture.getTextureObjectHandle(), 0);
                    colorTextureCounter++;
                } else if (attachmentSpec.isDepth) {
                    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                            GLES20.GL_TEXTURE_2D,
                            texture.getTextureObjectHandle(), 0);
                } else if (attachmentSpec.isStencil) {
                    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT,
                            GLES20.GL_TEXTURE_2D,
                            texture.getTextureObjectHandle(), 0);
                }
            }
        } else {
            T texture = createTexture(bufferBuilder.textureAttachmentSpecs.first());
            textureAttachments.add(texture);
            GLES20.glBindTexture(texture.glTarget, texture.getTextureObjectHandle());
        }

        if (isMRT) {
            IntBuffer buffer = BufferUtils.newIntBuffer(colorTextureCounter);
            for (int i = 0; i < colorTextureCounter; i++) {
                buffer.put(GLES20.GL_COLOR_ATTACHMENT0 + i);
            }
            buffer.position(0);
            GLES30.glDrawBuffers(colorTextureCounter, buffer);
        } else {
            attachFrameBufferColorTexture(textureAttachments.first());
        }

        if (bufferBuilder.hasDepthRenderBuffer) {
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,
                    depthbufferHandle);
        }

        if (bufferBuilder.hasStencilRenderBuffer) {
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    stencilbufferHandle);
        }

        if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES30.GL_DEPTH_STENCIL_ATTACHMENT,
                    GLES20.GL_RENDERBUFFER,
                    depthStencilPackedBufferHandle);
        }

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        for (T texture : textureAttachments) {
            GLES20.glBindTexture(texture.glTarget, 0);
        }

        int result = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

//        if (result == GLES20.GL_FRAMEBUFFER_UNSUPPORTED && bufferBuilder.hasDepthRenderBuffer && bufferBuilder
//                .hasStencilRenderBuffer
//                && (GLES.supportsExtension("GL_OES_packed_depth_stencil")
//                || Gdx.graphics.supportsExtension("GL_EXT_packed_depth_stencil"))) {
//            if (bufferBuilder.hasDepthRenderBuffer) {
//                GLES20.glDeleteRenderbuffer(depthbufferHandle);
//                depthbufferHandle = 0;
//            }
//            if (bufferBuilder.hasStencilRenderBuffer) {
//                GLES20.glDeleteRenderbuffer(stencilbufferHandle);
//                stencilbufferHandle = 0;
//            }
//            if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
//                GLES20.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
//                depthStencilPackedBufferHandle = 0;
//            }
//
//            depthStencilPackedBufferHandle = GLES20.glGenRenderbuffer();
//            hasDepthStencilPackedBuffer = true;
//            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthStencilPackedBufferHandle);
//            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GL_DEPTH24_STENCIL8_OES, width, height);
//            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
//
//            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
// GLES20.GL_RENDERBUFFER,
//                    depthStencilPackedBufferHandle);
//            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT,
// GLES20.GL_RENDERBUFFER,
//                    depthStencilPackedBufferHandle);
//            result = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
//        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, defaultFramebufferHandle);

        if (result != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            for (T texture : textureAttachments) {
                disposeColorTexture(texture);
            }

            if (hasDepthStencilPackedBuffer) {
                GLTool.glDeleteBuffer(depthStencilPackedBufferHandle);
            } else {
                if (bufferBuilder.hasDepthRenderBuffer) GLTool.glDeleteRenderbuffer(depthbufferHandle);
                if (bufferBuilder.hasStencilRenderBuffer) GLTool.glDeleteRenderbuffer(stencilbufferHandle);
            }

            GLTool.glDeleteFramebuffer(framebufferHandle);

            if (result == GLES20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
            if (result == GLES20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
                throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
            if (result == GLES20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
                throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
            if (result == GLES20.GL_FRAMEBUFFER_UNSUPPORTED)
                throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of " +
                        "formats");
            throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
        }
    }

    private void checkValidBuilder() {
        boolean runningGL30 = true;

        if (!runningGL30) {
            if (bufferBuilder.hasPackedStencilDepthRenderBuffer) {
                throw new RuntimeException("Packed Stencil/Render render buffers are not available on GLES 2.0");
            }
            if (bufferBuilder.textureAttachmentSpecs.size > 1) {
                throw new RuntimeException("Multiple render targets not available on GLES 2.0");
            }
            for (FrameBufferTextureAttachmentSpec spec : bufferBuilder.textureAttachmentSpecs) {
                if (spec.isDepth)
                    throw new RuntimeException("Depth texture FrameBuffer Attachment not available on GLES 2.0");
                if (spec.isStencil)
                    throw new RuntimeException("Stencil texture FrameBuffer Attachment not available on GLES 2.0");
                if (spec.isFloat) {
//                    if (!Gdx.graphics.supportsExtension("OES_texture_float")) {
//                        throw new RuntimeException("Float texture FrameBuffer Attachment not available on GLES 2.0");
//                    }
                }
            }
        }
    }

    /**
     * Releases all resources associated with the FrameBuffer.
     */
    @Override
    public void dispose() {
        for (T texture : textureAttachments) {
            disposeColorTexture(texture);
        }

        if (hasDepthStencilPackedBuffer) {
            GLTool.glDeleteRenderbuffer(depthStencilPackedBufferHandle);
        } else {
            if (bufferBuilder.hasDepthRenderBuffer) GLTool.glDeleteRenderbuffer(depthbufferHandle);
            if (bufferBuilder.hasStencilRenderBuffer) GLTool.glDeleteRenderbuffer(stencilbufferHandle);
        }

        GLTool.glDeleteFramebuffer(framebufferHandle);
    }

    /**
     * Makes the frame buffer current so everything gets drawn to it.
     */
    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferHandle);
    }

    /**
     * Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on.
     */
    public static void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, defaultFramebufferHandle);
    }

    /**
     * Binds the frame buffer and sets the viewport accordingly, so everything gets drawn to it.
     */
    public void begin() {
        bind();
        setFrameBufferViewport();
    }

    /**
     * Sets viewport to the dimensions of framebuffer. Called by {@link #begin()}.
     */
    protected void setFrameBufferViewport() {
        GLES20.glViewport(0, 0, bufferBuilder.width, bufferBuilder.height);
    }

    /**
     * Unbinds the framebuffer and sets viewport sizes, all drawing will be performed to the normal framebuffer from
     * here on.
     *
     * @param x      the x-axis position of the viewport in pixels
     * @param y      the y-asis position of the viewport in pixels
     * @param width  the width of the viewport in pixels
     * @param height the height of the viewport in pixels
     */
    public void end(int x, int y, int width, int height) {
        unbind();
        GLES20.glViewport(x, y, width, height);
    }

    /**
     * @return The OpenGL handle of the framebuffer (see {@link GLTool#glGenFramebuffer()})
     */
    public int getFramebufferHandle() {
        return framebufferHandle;
    }

    /**
     * @return The OpenGL handle of the (optional) depth buffer (see {@link GLTool#glGenRenderbuffer()}). May return 0
     * even if depth
     * buffer enabled
     */
    public int getDepthBufferHandle() {
        return depthbufferHandle;
    }

    /**
     * @return The OpenGL handle of the (optional) stencil buffer (see {@link GLTool#glGenRenderbuffer()}). May return
     * 0 even if
     * stencil buffer enabled
     */
    public int getStencilBufferHandle() {
        return stencilbufferHandle;
    }

    /**
     * @return The OpenGL handle of the packed depth & stencil buffer (GL_DEPTH24_STENCIL8_OES) or 0 if not used.
     **/
    protected int getDepthStencilPackedBuffer() {
        return depthStencilPackedBufferHandle;
    }

    /**
     * @return the height of the framebuffer in pixels
     */
    public int getHeight() {
        return bufferBuilder.height;
    }

    /**
     * @return the width of the framebuffer in pixels
     */
    public int getWidth() {
        return bufferBuilder.width;
    }

    protected static class FrameBufferTextureAttachmentSpec {
        int internalFormat, format, type;
        boolean isFloat, isGpuOnly;
        boolean isDepth;
        boolean isStencil;

        public FrameBufferTextureAttachmentSpec(int internalformat, int format, int type) {
            this.internalFormat = internalformat;
            this.format = format;
            this.type = type;
        }

        public boolean isColorTexture() {
            return !isDepth && !isStencil;
        }
    }

    protected static class FrameBufferRenderBufferAttachmentSpec {
        int internalFormat;

        public FrameBufferRenderBufferAttachmentSpec(int internalFormat) {
            this.internalFormat = internalFormat;
        }
    }

    protected static abstract class GLFrameBufferBuilder<U extends GLFrameBuffer<? extends GLTexture>> {
        protected int width, height;

        protected Array<FrameBufferTextureAttachmentSpec> textureAttachmentSpecs = new Array<>();

        protected FrameBufferRenderBufferAttachmentSpec stencilRenderBufferSpec;
        protected FrameBufferRenderBufferAttachmentSpec depthRenderBufferSpec;
        protected FrameBufferRenderBufferAttachmentSpec packedStencilDepthRenderBufferSpec;

        protected boolean hasStencilRenderBuffer;
        protected boolean hasDepthRenderBuffer;
        protected boolean hasPackedStencilDepthRenderBuffer;

        public GLFrameBufferBuilder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public GLFrameBufferBuilder<U> addColorTextureAttachment(int internalFormat, int format, int type) {
            textureAttachmentSpecs.add(new FrameBufferTextureAttachmentSpec(internalFormat, format, type));
            return this;
        }

        public GLFrameBufferBuilder<U> addBasicColorTextureAttachment (Bitmap.Config config) {
            int glFormat = BitmapConfigToGlFormat(config);
            int glType = BitmapConfigToGlType(config);
            return addColorTextureAttachment(glFormat, glFormat, glType);
        }

        private int BitmapConfigToGlFormat(Bitmap.Config config){
            switch (config) {
                case ALPHA_8:
                    return GLES20.GL_ALPHA;
//                case GDX2D_FORMAT_LUMINANCE_ALPHA:
//                    return GL20.GL_LUMINANCE_ALPHA;
//                case GDX2D_FORMAT_RGB888:
                case RGB_565:
                    return GLES20.GL_RGB;
                case ARGB_8888:
                case ARGB_4444:
                    return GLES20.GL_RGBA;
                default:
                    throw new RuntimeException("unknown format: " + config);
            }
        }
        private int BitmapConfigToGlType(Bitmap.Config config){
            switch (config) {
                case ALPHA_8:
//                case GDX2D_FORMAT_LUMINANCE_ALPHA:
//                case GDX2D_FORMAT_RGB888:
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

        public GLFrameBufferBuilder<U> addFloatAttachment(int internalFormat, int format, int type, boolean gpuOnly) {
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat, format, type);
            spec.isFloat = true;
            spec.isGpuOnly = gpuOnly;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addDepthTextureAttachment(int internalFormat, int type) {
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat,
                    GLES20.GL_DEPTH_COMPONENT,
                    type);
            spec.isDepth = true;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilTextureAttachment(int internalFormat, int type) {
            FrameBufferTextureAttachmentSpec spec = new FrameBufferTextureAttachmentSpec(internalFormat,
                    GLES20.GL_STENCIL_ATTACHMENT,
                    type);
            spec.isStencil = true;
            textureAttachmentSpecs.add(spec);
            return this;
        }

        public GLFrameBufferBuilder<U> addDepthRenderBuffer(int internalFormat) {
            depthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasDepthRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilRenderBuffer(int internalFormat) {
            stencilRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasStencilRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addStencilDepthPackedRenderBuffer(int internalFormat) {
            packedStencilDepthRenderBufferSpec = new FrameBufferRenderBufferAttachmentSpec(internalFormat);
            hasPackedStencilDepthRenderBuffer = true;
            return this;
        }

        public GLFrameBufferBuilder<U> addBasicDepthRenderBuffer() {
            return addDepthRenderBuffer(GLES20.GL_DEPTH_COMPONENT16);
        }

        public GLFrameBufferBuilder<U> addBasicStencilRenderBuffer() {
            return addStencilRenderBuffer(GLES20.GL_STENCIL_INDEX8);
        }

        public GLFrameBufferBuilder<U> addBasicStencilDepthPackedRenderBuffer() {
            return addStencilDepthPackedRenderBuffer(GLES30.GL_DEPTH24_STENCIL8);
        }

        public abstract U build();
    }

    public static class FrameBufferBuilder extends GLFrameBufferBuilder<FrameBuffer> {
        public FrameBufferBuilder(int width, int height) {
            super(width, height);
        }

        @Override
        public FrameBuffer build() {
            return new FrameBuffer(this);
        }
    }

}
