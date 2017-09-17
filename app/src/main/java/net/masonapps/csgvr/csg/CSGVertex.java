package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 9/5/2017.
 */

public class CSGVertex {
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3();

    public CSGVertex() {
    }

    public CSGVertex(CSGVertex other) {
        position.set(other.position);
        normal.set(other.normal);
    }

    public void flip() {
        normal.scl(-1);
    }

    public CSGVertex copy() {
        return new CSGVertex(this);
    }

    public CSGVertex interpolate(CSGVertex other, float t) {
        final CSGVertex vertex = new CSGVertex(this);
        vertex.position.lerp(other.position, t);
        vertex.normal.lerp(other.normal, t).nor();
        return vertex;
    }
}
