package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class BSPTreeNode {

    public Array<CSGPolygon> polygons;
    @Nullable
    public CSGPlane divider = null;
    @Nullable
    public BSPTreeNode front = null;
    @Nullable
    public BSPTreeNode back = null;

    public BSPTreeNode() {
        this.polygons = new Array<>();
    }

    public void build(Array<CSGPolygon> polygons) {
        if (polygons.size == 0) return;
        divider = polygons.get(0).plane.copy();
        Array<CSGPolygon> frontPolygons = new Array<>();
        Array<CSGPolygon> backPolygons = new Array<>();
        for (int i = 0; i < polygons.size; i++) {
            divider.splitPolygon(polygons.get(i), polygons, polygons, frontPolygons, backPolygons);
        }
        if (frontPolygons.size > 0) {
            if (front == null)
                front = new BSPTreeNode();
            front.build(frontPolygons);
        }
        if (backPolygons.size > 0) {
            if (back == null)
                back = new BSPTreeNode();
            back.build(backPolygons);
        }
    }

    public boolean isConvex() {
        for (int i = 0; i < polygons.size; i++) {
            for (int j = 0; j < polygons.size; j++) {
                if (i != j && polygons.get(i).plane.classifyPolygon(polygons.get(j)) != CSGPlane.BACK)
                    return false;
            }
        }
        return true;
    }

    public void add(CSGPolygon polygon) {

    }

    public Array<CSGPolygon> clipPolygons(Array<CSGPolygon> polygons) {
        final Array<CSGPolygon> outPolygons = new Array<>();
        if (divider == null) {
            outPolygons.addAll(polygons);
            return outPolygons;
        }

        Array<CSGPolygon> frontPolygons = new Array<>();
        Array<CSGPolygon> backPolygons = new Array<>();

        for (int i = 0; i < polygons.size; i++) {
            divider.splitPolygon(polygons.get(i), frontPolygons, backPolygons, frontPolygons, backPolygons);
        }

        if (front != null)
            frontPolygons = front.clipPolygons(frontPolygons);
        if (back != null)
            backPolygons = back.clipPolygons(backPolygons);
        else
            backPolygons.clear();

        outPolygons.addAll(frontPolygons);
        outPolygons.addAll(backPolygons);
        return outPolygons;
    }

    public void clipTo(BSPTreeNode other) {
        polygons = other.clipPolygons(polygons);
        if (front != null)
            front.clipTo(other);
        if (back != null)
            back.clipTo(other);
    }

    public BSPTreeNode invert() {
        if (divider != null)
            divider.flip();
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
        final BSPTreeNode node = new BSPTreeNode();
        node.divider = divider == null ? null : divider.copy();
        for (CSGPolygon polygon : polygons) {
            node.polygons.add(polygon.copy());
        }
        node.front = front == null ? null : front.copy();
        node.back = back == null ? null : back.copy();
        return node;
    }
}
