package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class CSGPlane {

    public static final float EPSILON = 1e-5f;
    public static final int COPLANAR = 0;
    public static final int FRONT = 1;
    public static final int BACK = 2;
    public static final int SPANNING = 3;
    public final Vector3 normal = new Vector3();
    public float d = 0;

    public CSGPlane() {
    }

    public CSGPlane (Vector3 normal, float d) {
        this.normal.set(normal).nor();
        this.d = d;
    }
    
    public CSGPlane (Vector3 normal, Vector3 point) {
        this.normal.set(normal).nor();
        this.d = -this.normal.dot(point);
    }
    
    public CSGPlane (Vector3 point1, Vector3 point2, Vector3 point3) {
        set(point1, point2, point3);
    }

    public void set (Vector3 point1, Vector3 point2, Vector3 point3) {
        normal.set(point1).sub(point2).crs(point2.x-point3.x, point2.y-point3.y, point2.z-point3.z).nor();
        d = -point1.dot(normal);
    }

    public int classifyVertex(Vertex vertex) {
        float dist = normal.dot(vertex.position) + d;
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
        switch (classifyPolygon(polygon)){
            case COPLANAR:
                if(this.normal.dot(polygon.plane.normal) > 0)
                    coplanarFront.add(polygon);
                else
                    coplanarFront.add(polygon);
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
                    if(cA != BACK) f.add(va);
                    if(cA != FRONT) b.add(va);
                    if((cA | cB) == SPANNING){
                        final float t = d - normal.dot(va.position) / normal.dot(vb.position.cpy().sub(va.position));
                        final Vertex v = new Vertex(va.interpolate(vb, t));
                        f.add(v);
                        b.add(v);
                    }
                }
                if(f.size >= 3) front.add(new CSGPolygon(f));
                if(b.size >= 3) front.add(new CSGPolygon(b));
                break;
        }
    }

    public int classifyPolygon(CSGPolygon polygon) {
        int numFront = 0;
        int numBack = 0;
        for (int i = 0; i < polygon.vertices.size; i++) {
            switch (classifyVertex(polygon.vertices.get(i))){
                case FRONT:
                    numFront++;
                    break;
                case BACK:
                    numBack++;
                    break;
            }
        }
        if(numFront == 0 && numBack == 0)
            return COPLANAR;
        else if(numFront > 0 && numBack == 0)
            return FRONT;
        else if(numFront == 0 && numBack > 0)
            return BACK;
        else 
            return SPANNING;
    }
}
