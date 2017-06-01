package net.masonapps.csgvr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob on 6/1/2017.
 */

public class TransformManipulator {

    private final Matrix4 transform;
    private Vector3 position = new Vector3();
    //    private Vector3 up = new Vector3();
//    private Vector3 right = new Vector3();
//    private Vector3 forward = new Vector3();
    private Arrow xArrow, yArrow, zArrow;
    private Vector3 temp = new Vector3();

    public TransformManipulator(Matrix4 transform) {
        this.transform = transform;
        final ModelBuilder modelBuilder = new ModelBuilder();
        this.xArrow = new Arrow(modelBuilder, Vector3.X, Color.GREEN);
        this.yArrow = new Arrow(modelBuilder, Vector3.Y, Color.RED);
        this.zArrow = new Arrow(modelBuilder, Vector3.Z, Color.BLUE);
    }
    
    public void updateTransform() {
        transform.getTranslation(position);
        xArrow.modelInstance.transform.setToTranslation(position);
        yArrow.modelInstance.transform.setToTranslation(position);
        zArrow.modelInstance.transform.setToTranslation(position);
    }

    public boolean rayTest(Ray ray) {
        if (Intersector.intersectRaySphere(ray, transform.getTranslation(position), 1f, null)) {
            if (xArrow.performRayTest(ray)) {
                xArrow.modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE));
                return true;
            }
            if (yArrow.performRayTest(ray)) {
                yArrow.modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE));
                return true;
            }
            if (zArrow.performRayTest(ray)) {
                zArrow.modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE));
                return true;
            }
        }
        return false;
    }

    public void render(ModelBatch modelBatch) {
        updateTransform();
        modelBatch.render(xArrow.modelInstance);
        modelBatch.render(yArrow.modelInstance);
        modelBatch.render(zArrow.modelInstance);
    }
}
