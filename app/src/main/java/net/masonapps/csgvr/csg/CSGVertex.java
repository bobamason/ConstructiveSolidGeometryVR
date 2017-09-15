package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 9/5/2017.
 */

public class CSGVertex {
    public static final int ARRAY_LENGTH = 6;
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3();

    public CSGVertex() {
    }

    public CSGVertex(CSGVertex other) {
        position.set(other.position);
        normal.set(other.normal);
    }

    public static CSGVertex fromArray(float[] array, int offset) {
        if (offset + ARRAY_LENGTH > array.length)
            throw new IllegalArgumentException("array length must be at least " + (offset + ARRAY_LENGTH) + " to use an offset of " + offset);
        final CSGVertex v = new CSGVertex();
        v.position.set(array[offset], array[offset + 1], array[offset + 2]);
        v.normal.set(array[offset + 3], array[offset + 4], array[offset + 5]).nor();
        return v;
    }

    public float[] toArray(float[] array, int offset) {
        if (offset + ARRAY_LENGTH > array.length)
            throw new IllegalArgumentException("array length must be at least " + (offset + ARRAY_LENGTH) + " to use an offset of " + offset);
        array[0] = position.x;
        array[1] = position.y;
        array[2] = position.z;

        array[3] = normal.x;
        array[4] = normal.y;
        array[5] = normal.z;
        return array;
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
