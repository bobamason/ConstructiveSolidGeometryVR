package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    @Nullable
    public CSGPlane plane = null;
    public Array<Vertex> vertices = new Array<>(3);

    public CSGPolygon(Array<Vertex> vertices) {
        this.vertices = vertices;
//        this.vertexArray.addAll(vertexArray);
        initPlane();
    }

    private void initPlane() {
        if (vertices.size < 3)
            throw new IllegalStateException("polygon must have 3 or more vertexArray");
        plane = CSGPlane.fromPoints(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
        if (plane == null) {
            Log.e(CSGPolygon.class.getSimpleName(), "a: " + vertices.get(0).position.toString() +
                    "\n b: " + vertices.get(1).position.toString() +
                    "\n c: " + vertices.get(2).position.toString() +
                    "\n num vertexArray: " + vertices.size);
        }
    }

    public void flip() {
        vertices.reverse();
        for (Vertex v : vertices) {
            v.flip();
        }
        if (plane != null)
            plane.flip();
    }

    public int getVertexCount() {
        return vertices.size;
    }

    public CSGPolygon copy() {
        return new CSGPolygon(vertices);
    }
}
