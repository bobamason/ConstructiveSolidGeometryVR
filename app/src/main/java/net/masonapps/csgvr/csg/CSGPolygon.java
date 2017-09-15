package net.masonapps.csgvr.csg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob on 9/15/2017.
 */

public class CSGPolygon {

    public final CSGPlane plane;
    public List<CSGVertex> vertices;
    public SharedProperties shared;

    public CSGPolygon(List<CSGVertex> vertices, SharedProperties shared) {
        this.vertices = vertices;
        plane = CSGPlane.fromPoints(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
        this.shared = shared;
    }
    
    public CSGPolygon copy() {
        final List<CSGVertex> vertices = this.vertices.stream().map(CSGVertex::copy).collect(Collectors.toList());
        return new CSGPolygon(vertices, shared);
    }

    public void flip() {
        Collections.reverse(this.vertices);
        this.vertices.forEach(CSGVertex::flip);
        this.plane.flip();
    }
}
