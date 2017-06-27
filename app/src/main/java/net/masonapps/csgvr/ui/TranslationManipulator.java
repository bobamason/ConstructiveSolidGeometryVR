package net.masonapps.csgvr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.csgvr.modeling.Solid;

/**
 * Created by Bob on 6/1/2017.
 */

public class TranslationManipulator {

    @Nullable
    private Solid solid = null;
    //    private Vector3 up = new Vector3();
//    private Vector3 right = new Vector3();
//    private Vector3 forward = new Vector3();
    private Arrow xArrow, yArrow, zArrow;
    private Vector3 temp = new Vector3();
    private Plane planeXY = new Plane(Vector3.Z, 0);
    private Plane planeXZ = new Plane(Vector3.Y, 0);
    private Plane planeYZ = new Plane(Vector3.X, 0);
    private Vector3 hitPoint = new Vector3();
    private Axis focusedAxis = Axis.NONE;
    private float startVal = 0f;
    private float currentVal = 0f;

    public TranslationManipulator() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        this.xArrow = new Arrow(modelBuilder, Vector3.X, Color.GREEN);
        this.yArrow = new Arrow(modelBuilder, Vector3.Y, Color.RED);
        this.zArrow = new Arrow(modelBuilder, Vector3.Z, Color.BLUE);
    }

    public void updateTransform() {
        if (solid == null) return;
        solid.modelInstance.transform.getTranslation(temp);
        xArrow.modelInstance.transform.setToTranslation(temp).translate(solid.getBoundingBox().max.x, 0, 0);
        yArrow.modelInstance.transform.setToTranslation(temp).translate(0, solid.getBoundingBox().max.y, 0);
        zArrow.modelInstance.transform.setToTranslation(temp).translate(0, 0, solid.getBoundingBox().max.z);
    }

    public boolean inputDown(Ray ray) {
        if (solid == null) return false;
        focusedAxis = getFocusedAxis(ray);
        if (focusedAxis == TranslationManipulator.Axis.X) {
            Intersector.intersectRayPlane(ray, planeYZ, hitPoint);
            startVal = hitPoint.x;
            return true;
        } else if (focusedAxis == TranslationManipulator.Axis.Y) {
            Intersector.intersectRayPlane(ray, planeXZ, hitPoint);
            startVal = hitPoint.y;
            return true;
        } else if (focusedAxis == TranslationManipulator.Axis.Z) {
            Intersector.intersectRayPlane(ray, planeXY, hitPoint);
            startVal = hitPoint.z;
            return true;
        }
        return false;
    }

    private Axis getFocusedAxis(Ray ray) {
        if (xArrow.performRayTest(ray)) {
            return Axis.X;
        }
        if (yArrow.performRayTest(ray)) {
            return Axis.Y;
        }
        if (zArrow.performRayTest(ray)) {
            return Axis.Z;
        }
        return Axis.NONE;
    }

    public void render(ModelBatch modelBatch) {
        if (solid == null) return;
        updateTransform();
        modelBatch.render(xArrow.modelInstance);
        modelBatch.render(yArrow.modelInstance);
        modelBatch.render(zArrow.modelInstance);
    }

    public void setSolid(@Nullable Solid solid) {
        this.solid = solid;
    }

    public void inputUp() {
        focusedAxis = Axis.NONE;
    }

    public enum Axis {
        NONE, X, Y, Z
    }
}
