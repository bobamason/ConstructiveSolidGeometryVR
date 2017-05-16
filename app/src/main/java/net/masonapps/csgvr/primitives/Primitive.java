package net.masonapps.csgvr.primitives;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive {
    public double tolerance = 1e-5;
    public Vector3D position = new Vector3D(0, 0, 0);
    public Rotation rotation = new Rotation(1, 0, 0, 0, false);

    public PolyhedronsSet createPolyhedronsSet() {
        final List<Vector3D> vertices = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        buildShape(vertices, facets);
        return new PolyhedronsSet(vertices, facets, tolerance);
    }

    protected abstract void buildShape(List<Vector3D> vertices, List<int[]> facets);
}
