package net.masonapps.csgvr.primitives;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class Box extends Primitive {

    public double width;
    public double height;
    public double depth;

    public Box() {
        this(1, 1, 1);
    }

    public Box(double width, double height, double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    protected void buildShape(List<Vector3D> vertices, List<int[]> facets) {
        //top
        //bottom
        //front
        //back
        //left
        //right
    }
}
