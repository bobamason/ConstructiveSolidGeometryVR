package net.masonapps.csgvr.primitives;

import android.support.annotation.NonNull;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

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
    
    @Override
    public PolyhedronsSet createPolyhedronsSet() {
//        return createUsingSubHyperplanes();
        return createUsingFacets();
    }

    @NonNull
    private PolyhedronsSet createUsingFacets() {
        List<Vector3D> vertices = new ArrayList<>();
        List<int[]> facets = new ArrayList<>();

        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();

        final List<SubHyperplane<Euclidean3D>> subHyperplanes = new ArrayList<>();
        final Vector3D offset = plane.getNormal().scalarMultiply(depth);
        Vector3D point = null;
        final Vector2D[][] loops = remainingRegion.getVertices();

        return new PolyhedronsSet(vertices, facets, tolerance);
    }

    @NonNull
    private PolyhedronsSet createUsingSubHyperplanes() {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();

        final List<SubHyperplane<Euclidean3D>> subHyperplanes = new ArrayList<>();
        final Vector3D offset = plane.getNormal().scalarMultiply(depth);
        Vector3D point = null;
        final Vector2D[][] loops = remainingRegion.getVertices();

        for (Vector2D[] loop : loops) {
            if (loop.length < 3 || loop[0] == null)
                continue;
            for (int i = 0; i < loop.length; i++) {
                final List<SubHyperplane<Euclidean2D>> subLines = new ArrayList<>();
                Vector3D p1, p2, p3, p4;
                if (depth < -tolerance) {
                    p1 = plane.toSpace(loop[i]);
                    p2 = plane.toSpace(loop[(i + 1) % loop.length]);
                    p3 = p1.add(offset);
                    p4 = p2.add(offset);
                } else if (depth > tolerance) {
                    p3 = plane.toSpace(loop[i]);
                    p4 = plane.toSpace(loop[(i + 1) % loop.length]);
                    p1 = p3.add(offset);
                    p2 = p4.add(offset);
                } else {
                    return new PolyhedronsSet(tolerance);
                }
                if (point == null)
                    point = new Vector3D(p3.getX(), p3.getY(), p3.getZ());
                final Plane hyperplane = new Plane(p1, p2, p3, tolerance);
                final Vector2D subP1 = hyperplane.toSubSpace(p1);
                final Vector2D subP2 = hyperplane.toSubSpace(p2);
                final Vector2D subP3 = hyperplane.toSubSpace(p3);
                final Vector2D subP4 = hyperplane.toSubSpace(p4);
                subLines.add(new SubLine(subP1, subP2, tolerance));
                subLines.add(new SubLine(subP1, subP3, tolerance));
                subLines.add(new SubLine(subP3, subP4, tolerance));
                subLines.add(new SubLine(subP4, subP2, tolerance));
                final PolygonsSet region = new PolygonsSet(subLines, tolerance);
                subHyperplanes.add(new SubPlane(hyperplane, region));
            }
        }
        subHyperplanes.add(new SubPlane(plane, remainingRegion));
        subHyperplanes.add(new SubPlane(new Plane(point, plane.getNormal().scalarMultiply(-1), tolerance), remainingRegion));
        return new PolyhedronsSet(subHyperplanes, tolerance);
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
