package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Vector3;
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
        this.vertices = vertices;
        updatePlane();
    }

    public void updatePlane() {
        if (vertices.size < 3) return;
        Vector3 a = vertices.get(0).position;
        Vector3 b = vertices.get(1).position;
        Vector3 c = vertices.get(2).position;
        plane.normal.set(b.cpy().sub(a)).crs(c.cpy().sub(a));
        plane.d = plane.normal.dot(a);
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
