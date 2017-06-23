package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.csgvr.utils.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class Box extends Primitive {

    public float width;
    public float height;
    public float depth;

    public Box() {
        this(1, 1, 1);
    }

    public Box(float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    @Override
    protected PolyhedronsSet createPolyhedronsSet(Matrix4 transform) {
        List<Vector3D> vertices = new ArrayList<>();
        List<int[]> facets = new ArrayList<>();

        final float hw = width / 2.0f;
        final float hh = height / 2.0f;
        final float hd = depth / 2.0f;
        final Vector3 temp = new Vector3();
        //0
        temp.set(-hw, hh, hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //1
        temp.set(hw, hh, hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //2
        temp.set(hw, hh, -hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //3
        temp.set(-hw, hh, -hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //4
        temp.set(-hw, -hh, -hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //5
        temp.set(hw, -hh, -hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //6
        temp.set(hw, -hh, hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //7
        temp.set(-hw, -hh, hd).mul(transform);
        vertices.add(ConversionUtils.convertVector(temp));
        
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

        return new PolyhedronsSet(vertices, facets, tolerance);
    }
}
