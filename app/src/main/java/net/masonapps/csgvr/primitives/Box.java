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
        final double hw = width / 2.0;
        final double hh = height / 2.0;
        final double hd = depth / 2.0;
        //0
        vertices.add(new Vector3D(-hw, hh, hd));
        //1
        vertices.add(new Vector3D(hw, hh, hd));
        //2
        vertices.add(new Vector3D(hw, hh, -hd));
        //3
        vertices.add(new Vector3D(-hw, hh, -hd));

        //4
        vertices.add(new Vector3D(-hw, -hh, -hd));
        //5
        vertices.add(new Vector3D(hw, -hh, -hd));
        //6
        vertices.add(new Vector3D(hw, -hh, hd));
        //7
        vertices.add(new Vector3D(-hw, -hh, hd));
        //top
        facets.add(new int[]{0, 1, 2, 3});
        //bottom
        facets.add(new int[]{4, 5, 6, 7});
        //front
        facets.add(new int[]{7, 6, 1, 0});
        //back
        facets.add(new int[]{5, 4, 3, 2});
        //left
        facets.add(new int[]{4, 7, 0, 3});
        //right
        facets.add(new int[]{6, 5, 2, 1});
    }
}
