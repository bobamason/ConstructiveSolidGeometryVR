package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    @Nullable
    public CSGPlane plane = null;
    public Array<Vertex> vertices = new Array<>(3);

    public CSGPolygon() {
    }

    public CSGPolygon(Array<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            this.vertices.add(vertex.copy());
        }
        updatePlane();
    }

    public void updatePlane() {
        if (vertices.size < 3) return;
        plane = new CSGPlane();
        plane.set(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
    }

    public void flip() {
        if (plane != null)
            plane.flip();
        final Array<Vertex> tmpVert = new Array<>(vertices.size);
        for (int i = vertices.size - 1; i >= 0; i--) {
            final Vertex vertex = vertices.get(i);
            vertex.flip();
            tmpVert.add(vertex);
        }
        vertices = tmpVert;
    }

    public int getVertexCount() {
        return vertices.size;
    }

    public CSGPolygon copy() {
        return new CSGPolygon(vertices);
    }
}
