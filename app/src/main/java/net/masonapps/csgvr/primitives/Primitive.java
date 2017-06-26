package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.math.Matrix4;

import net.masonapps.csgvr.modeling.Solid;

/**
 * Created by Bob on 5/16/2017.
 */

public abstract class Primitive {
    public Matrix4 transform = new Matrix4();
    protected double tolerance = 1e-10;

    public abstract Solid createSolid();

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
}
