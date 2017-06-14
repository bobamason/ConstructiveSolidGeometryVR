package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.math.Matrix4;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive extends Solid {

    @Override
    public PolyhedronsSet getPolyhedronsSet() {
        if (polyhedronsSet == null || !updated) {
            final Matrix4 transform = new Matrix4(position, rotation, scale);
            polyhedronsSet = createPolyhedronsSet(transform);
        }
        return polyhedronsSet;
    }

    protected abstract PolyhedronsSet createPolyhedronsSet(Matrix4 transform);
}
