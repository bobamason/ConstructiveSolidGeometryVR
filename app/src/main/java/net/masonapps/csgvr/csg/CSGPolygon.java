package net.masonapps.csgvr.csg;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    public final CSGPlane plane = new CSGPlane();
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
        plane.set(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
    }

    public void flip() {
        plane.flip();
        if(vertices.size == 0) return;
        final Array<Vertex> tmpVert = new Array<>(vertices.size);
        for (int i = 0; i < vertices.size; i++) {
            final Vertex vertex = vertices.get(vertices.size - 1 - i);
            vertex.flip();
            tmpVert.add(vertex);
        }
        vertices = tmpVert;
    }
    
    public int getVertexCount(){
        return vertices.size;
    }

    public CSGPolygon copy() {
        final CSGPolygon polygon = new CSGPolygon();
        for (Vertex vertex : vertices) {
            polygon.vertices.add(vertex.copy());
        }
        return polygon;
    }
}
