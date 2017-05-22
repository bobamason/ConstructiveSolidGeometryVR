package net.masonapps.csgvr.primitives;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

import java.util.ArrayList;
import java.util.Collection;

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
        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();
        final Vector2D[][] loops = remainingRegion.getVertices();
        final Collection<SubHyperplane<Euclidean3D>> subPlanes = new ArrayList<>();
        final Collection<SubHyperplane<Euclidean2D>> subLines = new ArrayList<>();
        final PolygonsSet polygonsSet = new PolygonsSet(subLines, tolerance);
        subPlanes.add(new SubPlane(plane, remainingRegion));
        subPlanes.add(new SubPlane(new Plane(new Vector3D(), plane.getNormal(), tolerance), remainingRegion));
        return new PolyhedronsSet(subPlanes, tolerance);
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
