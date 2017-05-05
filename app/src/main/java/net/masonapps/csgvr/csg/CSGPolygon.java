package net.masonapps.csgvr.csg;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    public Array<Vertex> vertices;
    public final CSGPlane plane = new CSGPlane();

    public CSGPolygon() {
        this(new Array<Vertex>(3));
    }

    public CSGPolygon(Array<Vertex> vertices) {
        this.vertices = vertices;
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
}
