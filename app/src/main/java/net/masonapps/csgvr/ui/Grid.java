package net.masonapps.csgvr.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.csgvr.primitives.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob on 6/6/2017.
 */

public class Grid extends Entity {

    private Grid(ModelInstance modelInstance) {
        super(modelInstance);
        setLightingEnabled(false);
    }

    public static final Grid newInstance() {

        final ModelBuilder modelBuilder = new ModelBuilder();
        float r = 1f;
        final Texture texture = new Texture("grid.png");
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        final Material material = new Material(TextureAttribute.createDiffuse(texture), new BlendingAttribute(), FloatAttribute.createAlphaTest(0.5f), new IntAttribute(IntAttribute.CullFace, 0));
        final Model rect = modelBuilder.createRect(
                -r, -r, 0f,
                r, -r, 0f,
                r, r, 0f,
                -r, r, 0f,
                0f, 0f, 1f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
        rect.meshes.get(0).transformUV(new Matrix3().scale(r * 20f, r * 20f));
        return new Grid(new ModelInstance(rect));
    }

    public void setToPlane(Plane plane) {
        final Vector3 orgin = ConversionUtils.convertVector(plane.getOrigin());
        final Vector3 u = ConversionUtils.convertVector(plane.getU());
        final Vector3 v = ConversionUtils.convertVector(plane.getV());
        final Matrix4 rotMat = new Matrix4().setToLookAt(v.crs(u), u).tra();
        modelInstance.transform.setToTranslation(orgin).mul(rotMat);
    }
}
