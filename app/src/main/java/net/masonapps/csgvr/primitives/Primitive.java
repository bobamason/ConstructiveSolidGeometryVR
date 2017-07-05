package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.csgvr.modeling.Solid;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive {
    private static final Vector3 dir = new Vector3();
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Vector3 scale = new Vector3();
    private final Quaternion rotator = new Quaternion();
    public double tolerance = 1e-10;
    protected boolean isTransformUpdated = false;
    protected ModelInstance modelInstance;
    private BoundingBox boundingBox = new BoundingBox();
    private Ray tempRay = new Ray();

    public Primitive() {
        modelInstance = createModelInstance();
        modelInstance.calculateBoundingBox(boundingBox);
    }

    protected abstract ModelInstance createModelInstance();

    protected void updateTransform() {
        if (isTransformUpdated) return;

        modelInstance.transform.set(position, rotation, scale);

        modelInstance.calculateBoundingBox(boundingBox);

        isTransformUpdated = true;
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
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        invalidate();
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
    }

    public void lookAt(Vector3 position, Vector3 up) {
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
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

    public void setScale(Vector3 scale) {
        this.scale.set(scale);
        invalidate();
    }

    public void setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
        invalidate();
    }

    public void setScale(float s) {
        this.scale.set(s, s, s);
        invalidate();
    }

    public void setScaleX(float x) {
        this.scale.x = x;
        invalidate();
    }

    public void setScaleY(float y) {
        this.scale.y = y;
        invalidate();
    }

    public void setScaleZ(float z) {
        this.scale.z = z;
        invalidate();
    }

    public void scaleX(float x) {
        this.scale.x *= x;
        invalidate();
    }

    public void scaleY(float y) {
        this.scale.y *= y;
        invalidate();
    }

    public void scaleZ(float z) {
        this.scale.z *= z;
        invalidate();
    }

    protected void invalidate() {
        isTransformUpdated = false;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public abstract Solid createSolid();

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
}
