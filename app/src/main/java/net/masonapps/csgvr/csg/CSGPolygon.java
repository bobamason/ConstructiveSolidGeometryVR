package net.masonapps.csgvr.csg;

import java.util.ArrayList;
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
    private boolean isValid;

    public CSGPolygon(List<CSGVertex> vertices, SharedProperties shared) {
        this.vertices = vertices;
        plane = CSGPlane.fromPoints(vertices.get(0).position, vertices.get(1).position, vertices.get(2).position);
        isValid = !plane.normal.isZero(CSGPlane.EPSILON);
        this.shared = shared;
    }

    public static void mergeDuplicateVertices(List<CSGVertex> vertices) {
        List<CSGVertex> out = new ArrayList<>();
        boolean isDuplicate;

        for (int i = 0; i < vertices.size(); i++) {
            isDuplicate = false;
            final CSGVertex v1 = vertices.get(i);

            for (int j = 0; j < out.size(); j++) {
                final CSGVertex v2 = vertices.get(j);
                if (v1.position.epsilonEquals(v2.position, CSGPlane.EPSILON)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (!isDuplicate)
                out.add(v1);
        }

        vertices.clear();
        vertices.addAll(out);
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

    public boolean isValid() {
        return isValid;
    }
}
