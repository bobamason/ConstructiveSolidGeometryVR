package net.masonapps.csgvr.csg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 9/5/2017.
 */

public class CsgVertex {
    public static final int ARRAY_LENGTH = 12;
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3();
    public final Vector2 uv = new Vector2();
    public final Color color = new Color();

    public CsgVertex() {
    }

    public CsgVertex(CsgVertex other) {
        position.set(other.position);
        normal.set(other.normal);
        uv.set(other.uv);
        color.set(other.color);
    }

    public static CsgVertex fromArray(float[] array, int offset) {
        if (offset + ARRAY_LENGTH > array.length)
            throw new IllegalArgumentException("array length must be at least " + (offset + ARRAY_LENGTH) + " to use an offset of " + offset);
        final CsgVertex v = new CsgVertex();
        v.position.set(array[offset], array[offset + 1], array[offset + 2]);
        v.normal.set(array[offset + 3], array[offset + 4], array[offset + 5]).nor();
        v.uv.set(array[offset + 6], array[offset + 7]);
        v.color.set(array[offset + 8], array[offset + 9], array[offset + 10], array[offset + 11]);
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

        array[6] = uv.x;
        array[7] = uv.y;

        array[8] = color.r;
        array[9] = color.g;
        array[10] = color.b;
        array[11] = color.a;
        return array;
    }

    public void flip() {
        normal.scl(-1);
    }
}
