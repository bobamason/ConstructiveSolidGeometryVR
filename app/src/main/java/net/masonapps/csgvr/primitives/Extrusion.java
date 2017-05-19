package net.masonapps.csgvr.primitives;

import android.util.Log;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Bob on 5/19/2017.
 */

public class Extrusion extends Primitive {

    private double depth;
    private SubPlane subPlane;

    public Extrusion(SubPlane subPlane) {
        this.subPlane = subPlane;
        this.depth = 0.0;
    }

    public Extrusion(SubPlane subPlane, double depth) {
        this.subPlane = subPlane;
        this.depth = depth;
    }

    @Override
    protected void buildShape(List<Vector3D> vertices, List<int[]> facets) {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();
        final Vector2D[][] loops = remainingRegion.getVertices();
        int numVerts = 0;
        for (Vector2D[] loop : loops) {
            if (loop.length < 3 || loop[0] == null)
                continue;
            for (Vector2D v : loop) {
                vertices.add(plane.toSpace(v));
                final Vector3D offset = plane.getNormal().scalarMultiply(depth);
                vertices.add(plane.toSpace(v).add(offset));
                numVerts++;
            }
        }
        final boolean isDepthPositive = depth > 0.0;
        final int[] f1 = new int[numVerts];
        final int[] f2 = new int[numVerts];
        for (int i = 0; i < numVerts; i += 2) {
            f1[i] = isDepthPositive ? numVerts - i : i;
            f2[i] = isDepthPositive ? numVerts - i : i;
        }
        facets.add(f1);
        Log.i(Extrusion.class.getSimpleName() + "->buildShape", "f1: " + Arrays.toString(f1));
        facets.add(f2);
        Log.i(Extrusion.class.getSimpleName() + "->buildShape", "f2: " + Arrays.toString(f2));

        for (int i = 0; i < numVerts; i++) {
            final int i2 = (i + 1) % numVerts;
            int[] facet;
            if (isDepthPositive)
                facet = new int[]{f1[i], f1[i2], f2[i2], f2[i]};
            else
                facet = new int[]{f1[i], f1[i2], f2[i2], f2[i]};
            facets.add(facet);
            Log.i(Extrusion.class.getSimpleName() + "->buildShape", "link " + i + " - " + i2 + " : " + Arrays.toString(facet));
        }
    }

    public SubPlane getSubPlane() {
        return subPlane;
    }

    public void setSubPlane(SubPlane subPlane) {
        this.subPlane = subPlane;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }
}
