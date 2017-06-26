package net.masonapps.csgvr.primitives;

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

    public float radius;
    public float height;
    public int divisions;

    public Cylinder() {
        this(1, 1);
    }

    public Cylinder(float radius, float height) {
        this.radius = radius;
        this.height = height;
        this.divisions = 16;
    }

    @Override
    public Solid createSolid() {
        final Plane[] planes = new Plane[divisions + 2];
        Matrix4 rotMatrix = new Matrix4(transform.getRotation(new Quaternion()));
        planes[0] = new Plane(ConversionUtils.mulVector3D(transform, new Vector3D(0, height / 2.0, 0)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(0, 1, 0)), tolerance);
        planes[1] = new Plane(ConversionUtils.mulVector3D(transform, new Vector3D(0, -height / 2.0, 0)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(0, -1, 0)), tolerance);
        for (int i = 0; i < divisions; i++) {
            double a = (double) i / divisions * Math.PI * 2.0;
            double x = radius * Math.cos(a);
            double y = 0;
            double z = radius * Math.sin(a);
            planes[i + 2] = new Plane(ConversionUtils.mulVector3D(transform, new Vector3D(x, y, z)), ConversionUtils.mulVector3D(rotMatrix, new Vector3D(x, y, z)), tolerance);
        }
        return new Solid((PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(planes));
    }
}