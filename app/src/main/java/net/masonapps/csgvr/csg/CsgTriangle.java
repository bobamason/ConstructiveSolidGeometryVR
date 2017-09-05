package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Plane;

/**
 * Created by Bob on 9/5/2017.
 */

public class CsgTriangle {

    public static final int ARRAY_LENGTH = CsgVertex.ARRAY_LENGTH * 3;
    public final Plane plane = new Plane();
    public CsgVertex v1;
    public CsgVertex v2;
    public CsgVertex v3;

    public CsgTriangle(CsgVertex v1, CsgVertex v2, CsgVertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        plane.set(v1.position, v2.position, v3.position);
    }

    public static CsgTriangle fromArray(float[] array, int offset) {
        if (offset + ARRAY_LENGTH > array.length)
            throw new IllegalArgumentException("array length must be at least " + (offset + ARRAY_LENGTH) + " to use an offset of " + offset);

        final CsgVertex v1 = CsgVertex.fromArray(array, offset);
        final CsgVertex v2 = CsgVertex.fromArray(array, offset + CsgVertex.ARRAY_LENGTH);
        final CsgVertex v3 = CsgVertex.fromArray(array, offset + CsgVertex.ARRAY_LENGTH * 2);

        return new CsgTriangle(v1, v2, v3);
    }

    public void flip() {
        final CsgVertex tmp = v1;
        v1 = v3;
        v3 = tmp;

        v1.flip();
        v2.flip();
        v3.flip();

        plane.set(v1.position, v2.position, v3.position);
    }

    public float[] toArray(float[] array, int offset) {
        if (offset + ARRAY_LENGTH > array.length)
            throw new IllegalArgumentException("array length must be at least " + (offset + ARRAY_LENGTH) + " to use an offset of " + offset);

        v1.toArray(array, offset);
        v2.toArray(array, offset + CsgVertex.ARRAY_LENGTH);
        v3.toArray(array, offset + CsgVertex.ARRAY_LENGTH * 2);

        return array;
    }
}
