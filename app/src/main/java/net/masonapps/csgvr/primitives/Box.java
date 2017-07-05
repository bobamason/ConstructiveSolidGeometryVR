package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.csgvr.modeling.Solid;
import net.masonapps.csgvr.utils.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class Box extends Primitive {

    public Box(float width, float height, float depth) {
        setDimensions(width, height, depth);
    }

    @Override
    protected ModelInstance createModelInstance() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Model model = modelBuilder.createBox(1f, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        return new ModelInstance(model);
    }

    @Override
    public Solid createSolid() {
        List<Vector3D> vertices = new ArrayList<>();
        List<int[]> facets = new ArrayList<>();

        final float hw = 0.5f;
        final float hh = 0.5f;
        final float hd = 0.5f;
        final Vector3 temp = new Vector3();
        //0
        temp.set(-hw, hh, hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //1
        temp.set(hw, hh, hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //2
        temp.set(hw, hh, -hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //3
        temp.set(-hw, hh, -hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //4
        temp.set(-hw, -hh, -hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //5
        temp.set(hw, -hh, -hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //6
        temp.set(hw, -hh, hd).mul(modelInstance.transform);
        vertices.add(ConversionUtils.convertVector(temp));
        //7
        temp.set(-hw, -hh, hd).mul(modelInstance.transform);
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

        return new Solid(new PolyhedronsSet(vertices, facets, 1e-10));
    }

    public void setDimensions(float width, float height, float depth) {
        setScale(width, height, depth);
    }

    public void setWidth(float width) {
        setScaleX(width);
    }

    public void setHeight(float height) {
        setScaleY(height);
    }

    public void setDepth(float depth) {
        setScaleZ(depth);
    }
}
