package net.masonapps.csgvr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob on 6/1/2017.
 */

public class Arrow {

    private static final Matrix4 tempM = new Matrix4();
    private static final Ray tempRay = new Ray();
    public final ModelInstance modelInstance;
    private BoundingBox bounds;

    public Arrow(ModelBuilder modelBuilder, Vector3 v, Color color) {
        modelInstance = new ModelInstance(modelBuilder.createArrow(
                0f, 0f, 0f,
                v.x, v.y, v.z,
                0.1f, 0.1f, 8,
                GL20.GL_TRIANGLES,
                new Material(ColorAttribute.createDiffuse(color), new DepthTestAttribute(0, false), new BlendingAttribute()),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));
        bounds = new BoundingBox();
        modelInstance.calculateBoundingBox(bounds);
    }

    public boolean performRayTest(Ray ray) {
        return Intersector.intersectRayBoundsFast(tempRay.set(ray).mul(tempM.set(modelInstance.transform).inv()), bounds);
    }
}
