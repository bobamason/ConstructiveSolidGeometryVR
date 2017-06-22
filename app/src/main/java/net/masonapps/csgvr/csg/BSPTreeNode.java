package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class BSPTreeNode {

    private static final String TAG = BSPTreeNode.class.getSimpleName();
    public Array<CSGPolygon> polygons;
    @Nullable
    public Plane divider = null;
    @Nullable
    public BSPTreeNode front = null;
    @Nullable
    public BSPTreeNode back = null;

    public BSPTreeNode() {
        this.polygons = new Array<>();
    }

    public BSPTreeNode(Array<CSGPolygon> polygons) {
        this();
        build(polygons);
    }

    public void build(Array<CSGPolygon> polygons) {
        Log.d(TAG, ".build()");
        if (polygons.size == 0) return;
        Log.d(TAG + ".build()", "polygons.size = " + polygons.size);
        if (divider == null) {
            for (CSGPolygon polygon : polygons) {
                if (polygon.plane != null) {
                    divider = new Plane(polygon.plane.getNormal(), polygon.plane.getD());
                    break;
                }
            }
        }
        if (divider == null) {
            Log.d(TAG + ".build()", "divider is null");
            return;
        }
        Array<CSGPolygon> frontPolygons = new Array<>();
        Array<CSGPolygon> backPolygons = new Array<>();
        Log.d(TAG + ".build()", "divider normal = " + divider.normal.toString() + " d = " + divider.d);
        for (int i = 0; i < polygons.size; i++) {
            polygons.get(i).split(divider, this.polygons, this.polygons, frontPolygons, backPolygons);
        }
        Log.d(TAG + ".build()", "this.polygons.size = " + this.polygons.size);
        if (frontPolygons.size > 0) {
            if (front == null) {
                Log.d(TAG + ".build()", "creating front node");
                front = new BSPTreeNode();
            }
            Log.d(TAG + ".build()", "adding front node polygons - " + frontPolygons.size);
            front.build(frontPolygons);
        }
        if (backPolygons.size > 0) {
            if (back == null) {
                Log.d(TAG + ".build()", "creating back node");
                back = new BSPTreeNode();
            }
            Log.d(TAG + ".build()", "adding back node polygons - " + backPolygons.size);
            back.build(backPolygons);
        }
    }

//    public boolean isConvex() {
//        for (int i = 0; i < polygons.size; i++) {
//            for (int j = 0; j < polygons.size; j++) {
//                final Plane polygonPlane = polygons.get(i).plane;
//                if (polygonPlane != null) {
//                    if (i != j && polygonPlane.classifyPolygon(polygons.get(j)) != CSGPlane.BACK)
//                        return false;
//                }
//            }
//        }
//        return true;
//    }

    public Array<CSGPolygon> clipPolygons(Array<CSGPolygon> polygons) {
        Log.d(TAG, ".clipPolygons()");
        final Array<CSGPolygon> outPolygons = new Array<>();
        if (divider == null) {
            outPolygons.addAll(polygons);
            return outPolygons;
        }
        Log.d(TAG + ".clipPolygons()", "this.polygons.size " + this.polygons.size);

        Array<CSGPolygon> frontPolygons = new Array<>();
        Array<CSGPolygon> backPolygons = new Array<>();

        for (int i = 0; i < polygons.size; i++) {
            polygons.get(i).split(divider, frontPolygons, backPolygons, frontPolygons, backPolygons);
        }

        if (front != null)
            frontPolygons = front.clipPolygons(frontPolygons);
        if (back != null)
            backPolygons = back.clipPolygons(backPolygons);
        else
            backPolygons.clear();

        Log.d(TAG + ".clipPolygons()", "adding frontPolygons - " + frontPolygons.size);
        outPolygons.addAll(frontPolygons);
        Log.d(TAG + ".clipPolygons()", "adding backPolygons - " + backPolygons.size);
        outPolygons.addAll(backPolygons);
        return outPolygons;
    }

    public void clipTo(BSPTreeNode other) {
        Log.d(TAG, ".clipTo()");
        polygons = other.clipPolygons(polygons);
        if (front != null)
            front.clipTo(other);
        if (back != null)
            back.clipTo(other);
    }

    public BSPTreeNode invert() {
        Log.d(TAG, ".invert()");
        if (divider != null) {
            divider.normal.scl(-1);
            divider.d = -divider.d;
        }
        for (CSGPolygon polygon : polygons) {
            polygon.flip();
        }
        if (front != null)
            front.invert();
        if (back != null)
            back.invert();
        BSPTreeNode temp = front;
        front = back;
        back = temp;
        return this;
    }

    public Array<CSGPolygon> getAllPolygons() {
        return getAllPolygons(new Array<CSGPolygon>());
    }

    public Array<CSGPolygon> getAllPolygons(Array<CSGPolygon> out) {
        Log.d(TAG, ".getAllPolygons()");
        for (CSGPolygon polygon : polygons) {
            out.add(polygon.copy());
        }
        if (front != null)
            front.getAllPolygons(out);
        if (back != null)
            back.getAllPolygons(out);
        return out;
    }

    public BSPTreeNode copy() {
        Log.d(TAG, ".copy()");
        final BSPTreeNode node = new BSPTreeNode();
        node.divider = divider == null ? null : new Plane(divider.getNormal(), divider.getD());
        for (CSGPolygon polygon : polygons) {
            node.polygons.add(polygon.copy());
        }
        node.front = front == null ? null : front.copy();
        node.back = back == null ? null : back.copy();
        return node;
    }
}
