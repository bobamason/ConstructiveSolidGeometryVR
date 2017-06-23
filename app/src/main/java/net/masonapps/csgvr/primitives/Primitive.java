package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.math.Matrix4;

import net.masonapps.csgvr.modeling.Solid;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive extends Solid {

    public Primitive() {
        super(createPolyhedronsSet());
    }

    @Override
    public PolyhedronsSet getPolyhedronsSet() {
        if (polyhedronsSet == null || !isPolyhedronsSetUpdated) {
            final Matrix4 transform = new Matrix4().set(position, rotation);
            polyhedronsSet = createPolyhedronsSet(transform);
            isPolyhedronsSetUpdated = true;
        }
        return polyhedronsSet;
    }

    protected abstract PolyhedronsSet createPolyhedronsSet();
}
