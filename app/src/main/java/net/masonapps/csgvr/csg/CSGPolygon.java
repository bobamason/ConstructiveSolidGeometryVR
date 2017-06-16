package net.masonapps.csgvr.csg;

import android.util.Log;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    public CSGPlane plane;
    public Array<Vertex> vertices = new Array<>(3);

    public CSGPolygon() {
    }

    public CSGPolygon(Array<Vertex> vertices) {
        this.vertices = vertices;
//        this.vertices.addAll(vertices);
        initPlane();
    }

    private void initPlane() {
        if (vertices.size < 3)
            throw new IllegalStateException("polygon must have 3 or more vertices");
//        Vector3 normal = new Vector3();
//        float n = 0f;
//        for (int i = 2; i < vertices.size; i++) {
//            normal.add(CSGPlane.calculateNormal(vertices.get(i - 2).position, vertices.get(i - 1).position, vertices.get(i).position));
//            n += 1f;
//        }
//        normal.scl(1f / n).nor();
//        plane = new CSGPlane(normal, -normal.dot(vertices.get(0).position));
        plane = new CSGPlane(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
        if (plane.normal.isZero(1e-5f)) {
            Log.e(CSGPolygon.class.getSimpleName(), "a: " + vertices.get(0).position.toString() +
                    "\n b: " + vertices.get(1).position.toString() +
                    "\n c: " + vertices.get(2).position.toString() +
                    "\n num vertices: " + vertices.size);
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
