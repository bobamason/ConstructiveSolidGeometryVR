package net.masonapps.csgvr.csg;

import android.util.Log;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Bob on 9/15/2017.
 */

public class CSGPlane {
    public static final float EPSILON = 1e-5f;
    public static final int COPLANAR = 0;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    public static final int SPANNING = 3;
    public Vector3 normal;
    public float w;

    public CSGPlane(Vector3 normal, float w) {
        this.normal = normal;
        this.w = w;
    }

    public static CSGPlane fromPoints(Vector3 a, Vector3 b, Vector3 c) {
        final Vector3 n = new Vector3(b).sub(a).crs(new Vector3(c).sub(a)).nor();
        return new CSGPlane(n, n.dot(a));
    }

    public CSGPlane copy() {
        return new CSGPlane(new Vector3(normal), w);
    }

    public void flip() {
        this.normal.scl(-1);
        w = -w;
    }

    public void splitPolygon(CSGPolygon polygon, List<CSGPolygon> coplanarFront, List<CSGPolygon> coplanarBack, List<CSGPolygon> front, List<CSGPolygon> back) {
        final String tag = "CSGPlane::splitPolygon";
        int polygonType = 0;
        int[] types = new int[polygon.vertices.size()];

        for (int i = 0; i < polygon.vertices.size(); i++) {
            float t = this.normal.dot(polygon.vertices.get(i).position) - this.w;
            int type;
            if (t < -EPSILON) type = BACK;
            else if (t > EPSILON) type = FRONT;
            else type = COPLANAR;
            polygonType |= type;
            types[i] = type;
        }

        Log.d(tag, "polygon side: " + polygonType + " vertex sides: " + Arrays.toString(types));

        switch (polygonType) {
            case COPLANAR:
                if (this.normal.dot(polygon.plane.normal) > 0f) {
                    coplanarFront.add(polygon);
                    Log.d(tag, "added to list coplanarFront");
                } else {
                    coplanarBack.add(polygon);
                    Log.d(tag, "added to list coplanarBack");
                }
                break;
            case FRONT:
                front.add(polygon);
                Log.d(tag, "added to list front");
                break;
            case BACK:
                back.add(polygon);
                Log.d(tag, "added to list back");
                break;
            case SPANNING:
                Log.d(tag, "splitting spanning polygon");
                List<CSGVertex> f = new ArrayList<>();
                List<CSGVertex> b = new ArrayList<>();
                for (int i = 0; i < polygon.vertices.size(); i++) {
                    final int j = (i + 1) % polygon.vertices.size();
                    final int ti = types[i];
                    final int tj = types[j];
                    final CSGVertex vi = polygon.vertices.get(i);
                    final CSGVertex vj = polygon.vertices.get(j);
                    if (ti != BACK) f.add(vi);
                    if (ti != FRONT) b.add(ti != BACK ? vi.copy() : vi);
                    if ((ti | tj) == SPANNING) {
                        final Vector3 tmp = Pools.obtain(Vector3.class);
                        final float t = (this.w - this.normal.dot(vi.position)) / this.normal.dot(tmp.set(vj.position).sub(vi.position));
                        CSGVertex v = vi.interpolate(vj, t);
                        f.add(v);
                        b.add(v.copy());
                        Pools.free(tmp);
                    }
                }
                if (f.size() >= 3) {
                    final CSGPolygon frontPolygon = new CSGPolygon(f, polygon.shared);
                    if (polygon.isValid()) {
                        front.add(frontPolygon);
                        Log.d(tag, "added to list front");
                    }
                }
                if (b.size() >= 3) {
                    final CSGPolygon backPolygon = new CSGPolygon(b, polygon.shared);
                    if (backPolygon.isValid()) {
                        back.add(backPolygon);
                        Log.d(tag, "added to list back");
                    }
                }
                break;
        }
    }
}
