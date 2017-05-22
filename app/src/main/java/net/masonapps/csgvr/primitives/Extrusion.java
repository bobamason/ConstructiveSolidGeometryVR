package net.masonapps.csgvr.primitives;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.ArrayList;
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

    private static int[] reverseIntArray(int[] array) {
//        int temp;
//        final int halfLen = array.length / 2;
//        for (int i = 0; i < halfLen; i++) {
//            temp = array[i];
//            array[i] = array[i + halfLen];
//            array[i + halfLen] = temp;
//        }
        final int[] newArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[array.length - 1 - i];
        }
        return newArray;
    }
    
    @Override
    public PolyhedronsSet createPolyhedronsSet() {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();
        final Vector2D[][] loops = remainingRegion.getVertices();
        final List<Vector3D> vertices = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        for (Vector2D[] loop : loops) {
            for (Vector2D v2D : loop) {
                if (loop.length < 3 || loop[0] == null)
                    continue;
                final Vector3D vertexPos = plane.toSpace(v2D);
                vertices.add(vertexPos);
            }
        }
        final List<Vector3D> tempVertices = new ArrayList<>();
        for (Vector3D vertexPos : vertices) {
            tempVertices.add(new Vector3D(plane.getNormal().toArray()).scalarMultiply(depth).add(vertexPos));
        }
        vertices.addAll(tempVertices);
        final int n = vertices.size() / 2;
        final int[] f1 = new int[n];
        final int[] f2 = new int[n];
        for (int i = 0; i < n; i++) {
            f1[i] = i;
            f2[i] = i + n;
        }
        facets.add(f1);
        facets.add(reverseIntArray(f2));
        return new PolyhedronsSet(vertices, facets, tolerance);
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
