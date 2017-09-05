package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob on 8/10/2015.
 */
public class Entity implements Disposable {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Matrix4 inverseTransform = new Matrix4();
    protected final Vector3 scale = new Vector3(1f, 1f, 1f);
    private final Vector3 dimensions = new Vector3();
    private final Vector3 center = new Vector3();
    private final float radius;
    public ModelInstance modelInstance;
    @Nullable
    protected BaseShader shader = null;
    protected boolean updated = false;
    private BoundingBox bounds = new BoundingBox();
    private boolean renderingEnabled = true;
    private boolean lightingEnabled = true;

    public Entity(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        modelInstance.calculateBoundingBox(bounds);
        bounds.getDimensions(dimensions);
        bounds.getCenter(center);
        radius = dimensions.len();
//        radius = dimensions.len() / 2f;
    }

    public boolean isVisible(Camera camera) {
        if (!renderingEnabled) return false;
        if (!updated) recalculateTransform();
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final boolean inFrustum = camera.frustum.sphereInFrustum(tmp.set(position).add(center), radius);
        Pools.free(tmp);
        return inFrustum;
    }

    public boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    public void setRenderingEnabled(boolean renderingEnabled) {
        this.renderingEnabled = renderingEnabled;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    public void setShader(@Nullable BaseShader shader) {
        this.shader = shader;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public float getRadius() {
        return radius;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Vector3 getDimensions() {
        return dimensions;
    }

    @Override
    public void dispose() {
        if (shader != null)
            shader.dispose();
        shader = null;
    }

    public boolean intersectsRayBoundsFast(Ray ray) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBoundsFast = Intersector.intersectRayBoundsFast(tmpRay, getBounds());

        Pools.free(tmpRay);
        return intersectRayBoundsFast;
    }

    public boolean intersectsRayBounds(Ray ray, Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBounds = Intersector.intersectRayBounds(tmpRay, getBounds(), hitPoint);

        Pools.free(tmpRay);
        return intersectRayBounds;
    }

    public boolean intersectsRaySphere(Ray ray, Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRaySphere = Intersector.intersectRaySphere(tmpRay, getCenter(), getRadius(), hitPoint);

        Pools.free(tmpRay);
        return intersectRaySphere;
    }

    public Matrix4 getTransform() {
        return getTransform(new Matrix4());
    }

    public Matrix4 getTransform(Matrix4 out) {
        return out.set(modelInstance.transform);
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
    }

    public void setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
    }

    public void scaleX(float x) {
        scale.x *= x;
        invalidate();
    }

    public void scaleY(float y) {
        scale.y *= y;
        invalidate();
    }

    public void scaleZ(float z) {
        scale.z *= z;
        invalidate();
    }

    public void scale(float x, float y, float z) {
        scale.scl(x, y, z);
        invalidate();
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public void setScaleX(float x) {
        scale.x = x;
        invalidate();
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public void setScaleY(float y) {
        scale.y = y;
        invalidate();
    }

    public float getScaleZ() {
        return this.scale.z;
    }

    public void setScaleZ(float z) {
        scale.z = z;
        invalidate();
    }

    public void setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
    }

    public void setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
    }

    public void setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
    }

    public void rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
    }

    public void rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
    }

    public void rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
    }

    public void lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
    }

    public void translateX(float units) {
        this.position.x += units;
        invalidate();
    }

    public float getX() {
        return this.position.x;
    }

    public void setX(float x) {
        this.position.x = x;
        invalidate();
    }

    public void translateY(float units) {
        this.position.y += units;
        invalidate();
    }

    public float getY() {
        return this.position.y;
    }

    public void setY(float y) {
        this.position.y = y;
        invalidate();
    }

    public void translateZ(float units) {
        this.position.z += units;
        invalidate();
    }

    public float getZ() {
        return this.position.z;
    }

    public void setZ(float z) {
        this.position.z = z;
        invalidate();
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        invalidate();
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        invalidate();
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
    }

    public void invalidate() {
        updated = false;
    }

    public void recalculateTransform() {
        modelInstance.transform.set(position, rotation, scale);
        inverseTransform.set(position, rotation, scale);
        updated = true;
    }

    public static class EntityConstructor extends World.Constructor<Entity> {

        public EntityConstructor(Model model) {
            super(model);
        }

        @Override
        public Entity construct(float x, float y, float z) {
            return new Entity(new ModelInstance(model, x, y, z));
        }

        @Override
        public Entity construct(Matrix4 transform) {
            return new Entity(new ModelInstance(model, transform));
        }

        @Override
        public void dispose() {
            try {
                model.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
