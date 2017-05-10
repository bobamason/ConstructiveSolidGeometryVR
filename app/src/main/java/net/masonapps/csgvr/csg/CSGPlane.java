package net.masonapps.csgvr.csg;

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

    public CSGPlane() {
    }

    public CSGPlane(Vector3 normal, float d) {
        this.normal.set(normal).nor();
        this.d = d;
    }

    public CSGPlane(Vector3 a, Vector3 b, Vector3 c) {
        set(a, b, c);
    }

    public void set(Vector3 a, Vector3 b, Vector3 c) {
        normal.set(b).sub(a).crs(c.x - a.x, c.y - a.y, c.z - a.z).nor();
        d = normal.dot(a);
    }

    public int classifyVertex(Vertex vertex) {
        float dist = normal.dot(vertex.position) - d;
        if (dist < -EPSILON)
            return BACK;
        else if (dist > EPSILON)
            return FRONT;
        else
            return COPLANAR;
    }

    public void flip() {
        normal.scl(-1);
        d = -d;
    }

    public void splitPolygon(CSGPolygon polygon, Array<CSGPolygon> coplanarFront, Array<CSGPolygon> coplanarBack, Array<CSGPolygon> front, Array<CSGPolygon> back) {
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
                for (int i = 0; i < polygon.getVertexCount(); i++) {
                    final Vertex va = polygon.vertices.get(i);
                    final Vertex vb = polygon.vertices.get((i + 1) % polygon.getVertexCount());
                    int cA = classifyVertex(va);
                    int cB = classifyVertex(vb);
                    if (cA != BACK) f.add(va);
                    if (cA != FRONT) b.add(cA != BACK ? va.copy() : va);
                    if ((cA | cB) == SPANNING) {
                        final float t = (d - normal.dot(va.position)) / normal.dot(vb.position.cpy().sub(va.position));
                        final Vertex v = new Vertex(va.interpolate(vb, t));
                        f.add(v);
                        b.add(v);
                    }
                }
                if (f.size >= 3) front.add(new CSGPolygon(f));
                if (b.size >= 3) back.add(new CSGPolygon(b));
                break;
        }
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
