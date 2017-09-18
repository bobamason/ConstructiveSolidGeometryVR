package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob on 9/4/2017.
 */

public class BspNode {
    private CSGPlane plane;
    @Nullable
    private BspNode front = null;
    @Nullable
    private BspNode back = null;

    private List<CSGPolygon> polygons;

    public BspNode(@Nullable List<CSGPolygon> polygons) {
        this.polygons = new ArrayList<>();
        if (polygons != null) build(polygons);
    }

    public BspNode() {
        this(null);
    }

    public void build(List<CSGPolygon> polygons) {
        List<CSGPolygon> polygonList = polygons.stream().filter(CSGPolygon::isValid).distinct().collect(Collectors.toList());
        Log.d("BspNode::build", "polygonList size: " + polygonList.size());
        if (polygonList.isEmpty()) return;

        if (this.plane == null) {
            this.plane = polygonList.get(0).plane.copy();
        }
        
        List<CSGPolygon> f = new ArrayList<>();
        List<CSGPolygon> b = new ArrayList<>();
        polygonList.forEach(polygon -> this.plane.splitPolygon(polygon, this.polygons, this.polygons, f, b));
        if (!f.isEmpty()) {
            if (front == null) front = new BspNode();
            front.build(f);
        }
        if (!b.isEmpty()) {
            if (back == null) back = new BspNode();
            back.build(b);
        }
    }

    public BspNode copy() {
        final BspNode node = new BspNode();
        node.plane = this.plane.copy();
        node.front = this.front != null ? this.front.copy() : null;
        node.back = this.back != null ? this.back.copy() : null;
        this.polygons.forEach(p -> node.polygons.add(p.copy()));
        return node;
    }

    public void invert() {
        polygons.forEach(CSGPolygon::flip);
        if (plane != null)
            plane.flip();
        if (front != null)
            front.invert();
        if (back != null)
            back.invert();
        BspNode temp = front;
        front = back;
        back = temp;
    }

    public List<CSGPolygon> clipPolygons(List<CSGPolygon> polygons) {
        if (plane == null)
            return new ArrayList<>(polygons);

        List<CSGPolygon> f = new ArrayList<>();
        List<CSGPolygon> b = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {
            plane.splitPolygon(polygons.get(i), f, b, f, b);
        }

        if (front != null)
            f = front.clipPolygons(f);

        if (back != null)
            b = back.clipPolygons(b);
        else
            b = new ArrayList<>(0);

        final List<CSGPolygon> out = new ArrayList<>();
        out.addAll(f);
        out.addAll(b);
        return out;
    }

    public void clipTo(BspNode node) {
        polygons = node.clipPolygons(polygons);
        if (front != null)
            front.clipTo(node);

        if (back != null)
            back.clipTo(node);
    }

    public List<CSGPolygon> allPolygons() {
        final List<CSGPolygon> p = new ArrayList<>(polygons);
        if (front != null)
            p.addAll(front.allPolygons());
        if (back != null)
            p.addAll(back.allPolygons());
        return p;
    }
}
