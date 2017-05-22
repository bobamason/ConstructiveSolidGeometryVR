package net.masonapps.csgvr.primitives;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive {
    public double tolerance = 1e-5;
    public Vector3D position = new Vector3D(0, 0, 0);
    public Rotation rotation = new Rotation(1, 0, 0, 0, false);

    public abstract PolyhedronsSet createPolyhedronsSet();
}
