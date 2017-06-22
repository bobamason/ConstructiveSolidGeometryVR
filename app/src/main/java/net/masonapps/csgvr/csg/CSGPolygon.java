package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPolygon {
    public static final float EPSILON = 1e-5f;
    public static final int COPLANAR = 0;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    public static final int SPANNING = 3;
    private static final String TAG = CSGPolygon.class.getSimpleName();
    @Nullable
    public Plane plane = null;
    public Array<Vertex> vertices;

    public CSGPolygon(Array<Vertex> vertices) {
        this.vertices = new Array<>(vertices.size);
        for (Vertex vertex : vertices) {
            this.vertices.add(vertex.copy());
        }
        initPlane();
    }

    public static void removeDuplicates(Array<Vertex> vertices) {
        final Array<Vertex> temp = new Array<>();
        for (int i = 0; i < vertices.size; i++) {
            boolean isDouble = false;
            for (int j = 0; j < temp.size; j++) {
                isDouble = vertices.get(i).position.epsilonEquals(temp.get(j).position, 1e-5f);
                if (isDouble)
                    break;
            }
            if (!isDouble) {
                temp.add(vertices.get(i).copy());
            } else {
                Log.e(CSGPolygon.class.getSimpleName(), "duplicate vertex found" + vertices.get(i).position.toString());
            }
        }
        vertices.clear();
        vertices.addAll(temp);
    }

    public static int classifyVertex(Vertex vertex, Plane plane) {
        float dist = plane.distance(vertex.position);
//        Log.d(TAG + ".classifyVertex()", "dist = " + dist);
        if (dist < -EPSILON)
            return BACK;
        else if (dist > EPSILON)
            return FRONT;
        else
            return COPLANAR;
    }

    private void initPlane() {
        if (vertices.size < 3)
            throw new IllegalStateException("polygon must have 3 or more vertexArray");
        plane = new Plane(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
        if (plane.normal.isZero(1e-5f)) {
            plane = null;
//            Log.e(CSGPolygon.class.getSimpleName(), "a: " + vertices.get(0).position.toString() +
//                    "\n b: " + vertices.get(1).position.toString() +
//                    "\n c: " + vertices.get(2).position.toString() +
//                    "\n num vertexArray: " + vertices.size);
        }
    }

    public void flip() {
        vertices.reverse();
        for (Vertex v : vertices) {
            v.flip();
        }
        if (plane != null) {
            plane.d = -plane.d;
            plane.normal.scl(-1);
        }
    }

    public int getVertexCount() {
        return vertices.size;
    }

    public CSGPolygon copy() {
        return new CSGPolygon(vertices);
    }

    public void split(Plane plane, Array<CSGPolygon> coplanarFront, Array<CSGPolygon> coplanarBack, Array<CSGPolygon> front, Array<CSGPolygon> back) {
        if (plane == null) return;
        switch (classifyPolygon(plane)) {
            case COPLANAR:
//                Log.d(TAG + ".split()", "COPLANAR");
                if (-plane.normal.dot(plane.normal) > 0)
                    coplanarFront.add(this);
                else
                    coplanarBack.add(this);
                break;
            case FRONT:
//                Log.d(TAG + ".split()", "FRONT");
                front.add(this);
                break;
            case BACK:
//                Log.d(TAG + ".split()", "BACK");
                back.add(this);
                break;
            case SPANNING:
//                Log.d(TAG + ".split()", "SPANNING");
                final Array<Vertex> f = new Array<>(3);
                final Array<Vertex> b = new Array<>(3);
                final int count = getVertexCount();
                for (int i = 0; i < count; i++) {
                    final Vertex va = vertices.get(i);
                    final Vertex vb = vertices.get((i + 1) % count);

                    final int aSide = classifyVertex(va, plane);
                    if (aSide != BACK)
                        f.add(va);
                    if (aSide != FRONT)
                        b.add(aSide != BACK ? va.copy() : va);

                    final Vector3 intersection = new Vector3();
                    if (Intersector.intersectSegmentPlane(va.position, vb.position, plane, intersection)) {
                        final Vertex v = new Vertex();
                        v.position.set(intersection);
                        v.normal.set(va.normal.slerp(vb.normal, 0.5f));
                        v.uv.set(va.uv.lerp(vb.uv, 0.5f));
                        f.add(v);
                        b.add(v.copy());
                    }
//                    removeDuplicates(f);
//                    removeDuplicates(b);
                    if (f.size >= 3) front.add(new CSGPolygon(f));
                    if (b.size >= 3) back.add(new CSGPolygon(b));
                }
                break;
        }
    }

    public int classifyPolygon(Plane plane) {
        int frontCount = 0;
        int backCount = 0;
        for (int i = 0; i < vertices.size; i++) {
            final int side = classifyVertex(vertices.get(i), plane);
            if (side == FRONT)
                frontCount++;
            else if (side == BACK)
                backCount++;
        }
        if (frontCount == 0 && backCount == 0)
            return COPLANAR;
        if (frontCount > 0 && backCount == 0)
            return FRONT;
        if (frontCount == 0 && backCount > 0)
            return BACK;
        else
            return SPANNING;
    }
}
