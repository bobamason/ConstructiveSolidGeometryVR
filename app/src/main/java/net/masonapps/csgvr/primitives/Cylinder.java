package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

import net.masonapps.csgvr.modeling.Solid;
import net.masonapps.csgvr.utils.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;

/**
 * Created by Bob on 6/5/2017.
 */

public class Cylinder extends Primitive {

    private int divisions;

    public Cylinder() {
        this(1, 1);
    }

    public Cylinder(float radius, float height) {
        setRadius(radius);
        setHeight(height);
        this.divisions = 16;
    }

    @Override
    protected ModelInstance createModelInstance() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Model model = modelBuilder.createCylinder(1f, 1f, 1f, divisions, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        return new ModelInstance(model);
    }

    @Override
    public Solid createSolid() {
        double height = 1f;
        double radius = 1f;
        final Plane[] planes = new Plane[divisions + 2];
        Matrix4 rotMatrix = new Matrix4(modelInstance.transform.getRotation(new Quaternion()));
        planes[0] = new Plane(ConversionUtils.mulVector3D(modelInstance.transform, new Vector3D(0, height / 2.0, 0)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(0, 1, 0)), tolerance);
        planes[1] = new Plane(ConversionUtils.mulVector3D(modelInstance.transform, new Vector3D(0, -height / 2.0, 0)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(0, -1, 0)), tolerance);
        for (int i = 0; i < divisions; i++) {
            double a = (double) i / divisions * Math.PI * 2.0;
            double x = radius * Math.cos(a);
            double y = 0;
            double z = radius * Math.sin(a);
            planes[i + 2] = new Plane(ConversionUtils.mulVector3D(modelInstance.transform, new Vector3D(x, y, z)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(x, y, z)), tolerance);
        }
        return new Solid((PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(planes));
    }

    public int getDivisions() {
        return divisions;
    }

    public void setDivisions(int divisions) {
        this.divisions = divisions;
    }

    public void setRadius(float radius) {
        setScaleX(radius);
        setScaleZ(radius);
    }

    public void setHeight(float height) {
        setScaleY(height);
    }
}