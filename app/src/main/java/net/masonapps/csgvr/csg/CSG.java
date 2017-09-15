package net.masonapps.csgvr.csg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob on 9/4/2017.
 */

public class CSG {

    private List<CSGPolygon> polygons;

    public CSG() {
        polygons = new ArrayList<>();
    }

    public CSG(List<CSGPolygon> polygons) {
        this.polygons = polygons;
    }

    public static CSG cube() {
        return new CSG();
    }

    public static CSG cylinder() {
        return new CSG();
    }

    public static CSG sphere() {
        return new CSG();
    }

    public CSG copy() {
        final List<CSGPolygon> polygonList = polygons.stream().map(CSGPolygon::copy).collect(Collectors.toList());
        return new CSG(polygonList);
    }

    public List<CSGPolygon> getPolygons() {
        return polygons;
    }

    public CSG union(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return new CSG(a.allPolygons());
    }

    public CSG subtract(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        a.invert();
        return new CSG(a.allPolygons());
    }

    public CSG intersect(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.allPolygons());
        a.invert();
        return new CSG(a.allPolygons());
    }

    public CSG inverse() {
        final CSG copy = this.copy();
        copy.polygons.forEach(CSGPolygon::flip);
        return copy;
    }
}
