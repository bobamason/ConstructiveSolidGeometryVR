package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPlane {

    public static final float EPSILON = 1e-4f;
    public static final int COPLANAR = 0;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    public static final int SPANNING = 3;
    public final Vector3 normal = new Vector3();
    public float d = 0;

    public CSGPlane(Vector3 normal, float d) {
        this.normal.set(normal).nor();
        this.d = d;
    }

    @Nullable
    public static CSGPlane fromPoints(Vector3 a, Vector3 b, Vector3 c) {
        Vector3 normal = new Vector3(b).sub(a).crs(c.x - a.x, c.y - a.y, c.z - a.z).nor();
        if (normal.isZero(1e-5f)) return null;
        float d = -normal.dot(a);
        return new CSGPlane(normal, d);
    }

    public static Vector3 calculateNormal(Vector3 a, Vector3 b, Vector3 c) {
        return new Vector3().set(b).sub(a).crs(c.x - a.x, c.y - a.y, c.z - a.z).nor();
    }

    public int classifyVertex(Vertex vertex) {
        float dist = getOffset(vertex);
        if (dist < -EPSILON)
            return BACK;
        else if (dist > EPSILON)
            return FRONT;
        else
            return COPLANAR;
    }

    public float getOffset(Vertex vertex) {
        return vertex.position.dot(normal) + d;
    }

    public void flip() {
        normal.scl(-1);
        d = -d;
    }

    public void splitPolygon(CSGPolygon polygon, Array<CSGPolygon> coplanarFront, Array<CSGPolygon> coplanarBack, Array<CSGPolygon> front, Array<CSGPolygon> back) {
//        if(polygon.plane == null) return;
        switch (classifyPolygon(polygon)) {
            case COPLANAR:
                if (polygon.plane != null) {
                    if (this.normal.dot(polygon.plane.normal) > 0)
                        coplanarFront.add(polygon);
                    else
                        coplanarBack.add(polygon);
                }
                break;
            case FRONT:
                front.add(polygon);
                break;
            case BACK:
                back.add(polygon);
                break;
            case SPANNING:
                final Array<Vertex> f = new Array<>(3);
                final Array<Vertex> b = new Array<>(3);
                final int count = polygon.getVertexCount();
                for (int i = 0; i < count; i++) {
                    final Vertex va = polygon.vertices.get(i);
                    final Vertex vb = polygon.vertices.get((i + 1) % count);
                    int cA = classifyVertex(va);
                    int cB = classifyVertex(vb);
                    if (cA != BACK) f.add(va);
                    if (cA != FRONT) b.add(cA != BACK ? va.copy() : va);
                    if ((cA | cB) == SPANNING) {
                        final float t = -getOffset(va) / normal.dot(vb.position.cpy().sub(va.position));
//                        final float t = Math.abs(-getOffset(va) / normal.dot(vb.position.cpy().sub(va.position)));
                        final Vertex v = new Vertex(va.interpolate(vb, t));
                        f.add(v);
                        b.add(v.copy());
                    }
                }
//                removeDoubles(f);
//                removeDoubles(b);
                if (f.size >= 3) front.add(new CSGPolygon(f));
                if (b.size >= 3) back.add(new CSGPolygon(b));
                break;
        }
    }

    private void removeDoubles(Array<Vertex> vertices) {
        final Array<Vertex> temp = new Array<>();
        for (int i = 0; i < vertices.size; i++) {
            boolean isDouble = false;
            for (int j = 0; j < temp.size; j++) {
                isDouble = vertices.get(i).position.epsilonEquals(temp.get(j).position, 1e-4f);
                break;
            }
            if (!isDouble) {
                temp.add(vertices.get(i).copy());
            }
        }
        vertices.clear();
        vertices.addAll(temp);
    }

    public int classifyPolygon(CSGPolygon polygon) {
        int type = 0;
        for (int i = 0; i < polygon.vertices.size; i++) {
            type |= classifyVertex(polygon.vertices.get(i));
        }
        return type;
    }

    public CSGPlane copy() {
        return new CSGPlane(normal, d);
    }
}
