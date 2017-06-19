package net.masonapps.csgvr.primitives;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.masonapps.libgdxgooglevr.GdxVr;

/**
 * Created by Bob on 6/13/2017.
 */

public class Solid implements Disposable {
    private static final Vector3 dir = new Vector3();
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Vector3 scale = new Vector3(1, 1, 1);
    private final Quaternion rotator = new Quaternion();
    public double tolerance = 1e-10;
    public Material material = new Material();
    @Nullable
    protected PolyhedronsSet polyhedronsSet = null;
    @Nullable
    protected ModelInstance modelInstance = null;
    protected boolean updated = false;
    private BoundingBox boundingBox = new BoundingBox();
    private Ray tempRay = new Ray();

    @Nullable
    public PolyhedronsSet getPolyhedronsSet() {
        return polyhedronsSet;
    }

    public void setPolyhedronsSet(@Nullable PolyhedronsSet polyhedronsSet) {
        this.polyhedronsSet = polyhedronsSet;
    }

    @Nullable
    public ModelInstance getModelInstance(boolean rebuild) {
        if (rebuild) {
            if (polyhedronsSet != null) {
                if (modelInstance != null)
                    modelInstance.model.dispose();
                modelInstance = ConversionUtils.polyhedronsSetToModelInstance(getPolyhedronsSet(), material);
            } else {
                modelInstance = null;
            }
        }
        if (!updated) {
            updateTransformAndBounds();
        }
        return modelInstance;
    }

    private void updateTransformAndBounds() {
        if (modelInstance == null) return;
        modelInstance.transform.set(position, rotation, scale);
        modelInstance.calculateBoundingBox(boundingBox);
    }

    public boolean castRay(Ray ray, Vector3 hitPoint) {
        if (!updated) {
            updateTransformAndBounds();
        }
        if (modelInstance != null && polyhedronsSet != null) {
            if (Intersector.intersectRayBoundsFast(ray, boundingBox)) {
                tempRay.set(GdxVr.input.getInputRay());
                final Vector3D point = ConversionUtils.convertVector(tempRay.origin);
                final Vector3D point2 = ConversionUtils.convertVector(tempRay.direction).add(point);
                final SubPlane plane3D = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
                if (plane3D != null) {
                    final com.badlogic.gdx.math.Plane plane = ConversionUtils.convertPlane((Plane) plane3D.getHyperplane());
                    return Intersector.intersectRayPlane(ray, plane, hitPoint);
                }
            }
        }
        return false;
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
    }

    public void setScale(Vector3 scale) {
        this.scale.set(scale);
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

    private void invalidate() {
        updated = false;
    }

    @Override
    public void dispose() {
        if (modelInstance != null)
            modelInstance.model.dispose();
    }
}
