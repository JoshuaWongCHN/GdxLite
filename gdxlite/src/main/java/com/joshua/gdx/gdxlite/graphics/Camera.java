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

import com.joshua.gdx.gdxlite.math.Frustum;
import com.joshua.gdx.gdxlite.math.Matrix4;
import com.joshua.gdx.gdxlite.math.Quaternion;
import com.joshua.gdx.gdxlite.math.Vector3;

/**
 * Base class for {@link OrthographicCamera} and {@link PerspectiveCamera}.
 *
 * @author mzechner
 */
public abstract class Camera {
    /**
     * the position of the camera
     **/
    public final Vector3 position = new Vector3();
    /**
     * the unit length direction vector of the camera
     **/
    public final Vector3 direction = new Vector3(0, 0, -1);
    /**
     * the unit length up vector of the camera
     **/
    public final Vector3 up = new Vector3(0, 1, 0);

    /**
     * the projection matrix
     **/
    public final Matrix4 projection = new Matrix4();
    /**
     * the view matrix
     **/
    public final Matrix4 view = new Matrix4();
    /**
     * the combined projection and view matrix
     **/
    public final Matrix4 combined = new Matrix4();
    /**
     * the inverse combined projection and view matrix
     **/
    public final Matrix4 invProjectionView = new Matrix4();

    /**
     * the near clipping plane distance, has to be positive
     **/
    public float near = 1;
    /**
     * the far clipping plane distance, has to be positive
     **/
    public float far = 100;

    /**
     * the viewport width
     **/
    public float viewportWidth = 0;
    /**
     * the viewport height
     **/
    public float viewportHeight = 0;

    /**
     * the frustum
     **/
    public final Frustum frustum = new Frustum();

    private final Vector3 tmpVec = new Vector3();

    /**
     * Recalculates the projection and view matrix of this camera and the {@link Frustum} planes. Use this after
     * you've manipulated
     * any of the attributes of the camera.
     */
    public abstract void update();

    /**
     * Recalculates the projection and view matrix of this camera and the {@link Frustum} planes if
     * <code>updateFrustum</code> is
     * true. Use this after you've manipulated any of the attributes of the camera.
     */
    public abstract void update(boolean updateFrustum);

    /**
     * Recalculates the direction of the camera to look at the point (x, y, z). This function assumes the up vector is
     * normalized.
     *
     * @param x the x-coordinate of the point to look at
     * @param y the y-coordinate of the point to look at
     * @param z the z-coordinate of the point to look at
     */
    public void lookAt(float x, float y, float z) {
        tmpVec.set(x, y, z).sub(position).nor();
        if (!tmpVec.isZero()) {
            float dot = tmpVec.dot(up); // up and direction must ALWAYS be orthonormal vectors
            if (Math.abs(dot - 1) < 0.000000001f) {
                // Collinear
                up.set(direction).scl(-1);
            } else if (Math.abs(dot + 1) < 0.000000001f) {
                // Collinear opposite
                up.set(direction);
            }
            direction.set(tmpVec);
            normalizeUp();
        }
    }

    /**
     * Recalculates the direction of the camera to look at the point (x, y, z).
     *
     * @param target the point to look at
     */
    public void lookAt(Vector3 target) {
        lookAt(target.x, target.y, target.z);
    }

    /**
     * Normalizes the up vector by first calculating the right vector via a cross product between direction and up,
     * and then
     * recalculating the up vector via a cross product between right and direction.
     */
    public void normalizeUp() {
        tmpVec.set(direction).crs(up).nor();
        up.set(tmpVec).crs(direction).nor();
    }

    /**
     * Rotates the direction and up vector of this camera by the given angle around the given axis. The direction and
     * up vector
     * will not be orthogonalized.
     *
     * @param angle the angle
     * @param axisX the x-component of the axis
     * @param axisY the y-component of the axis
     * @param axisZ the z-component of the axis
     */
    public void rotate(float angle, float axisX, float axisY, float axisZ) {
        direction.rotate(angle, axisX, axisY, axisZ);
        up.rotate(angle, axisX, axisY, axisZ);
    }

    /**
     * Rotates the direction and up vector of this camera by the given angle around the given axis. The direction and
     * up vector
     * will not be orthogonalized.
     *
     * @param axis  the axis to rotate around
     * @param angle the angle, in degrees
     */
    public void rotate(Vector3 axis, float angle) {
        direction.rotate(axis, angle);
        up.rotate(axis, angle);
    }

    /**
     * Rotates the direction and up vector of this camera by the given rotation matrix. The direction and up vector
     * will not be
     * orthogonalized.
     *
     * @param transform The rotation matrix
     */
    public void rotate(final Matrix4 transform) {
        direction.rot(transform);
        up.rot(transform);
    }

    /**
     * Rotates the direction and up vector of this camera by the given {@link Quaternion}. The direction and up
     * vector will not be
     * orthogonalized.
     *
     * @param quat The quaternion
     */
    public void rotate(final Quaternion quat) {
        quat.transform(direction);
        quat.transform(up);
    }

    /**
     * Rotates the direction and up vector of this camera by the given angle around the given axis, with the axis
     * attached to given
     * point. The direction and up vector will not be orthogonalized.
     *
     * @param point the point to attach the axis to
     * @param axis  the axis to rotate around
     * @param angle the angle, in degrees
     */
    public void rotateAround(Vector3 point, Vector3 axis, float angle) {
        tmpVec.set(point);
        tmpVec.sub(position);
        translate(tmpVec);
        rotate(axis, angle);
        tmpVec.rotate(axis, angle);
        translate(-tmpVec.x, -tmpVec.y, -tmpVec.z);
    }

    /**
     * Transform the position, direction and up vector by the given matrix
     *
     * @param transform The transform matrix
     */
    public void transform(final Matrix4 transform) {
        position.mul(transform);
        rotate(transform);
    }

    /**
     * Moves the camera by the given amount on each axis.
     *
     * @param x the displacement on the x-axis
     * @param y the displacement on the y-axis
     * @param z the displacement on the z-axis
     */
    public void translate(float x, float y, float z) {
        position.add(x, y, z);
    }

    /**
     * Moves the camera by the given vector.
     *
     * @param vec the displacement vector
     */
    public void translate(Vector3 vec) {
        position.add(vec);
    }
}
