package net.masonapps.csgvr.csg;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/10/2017.
 */

public class ShapeUtils {

    public static BSPTreeNode createBoxTree() {
        final Array<CSGPolygon> polygons = new Array<>();
        final Array<Vertex> vertices = new Array<>();
        final Vertex vertex = new Vertex();
        //back
        vertex.position.set(-1, -1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, 1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, -1, -1);
        vertices.add(vertex.copy());
        polygons.add(new CSGPolygon(vertices));
        //front
        vertex.position.set(-1, -1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(1, -1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, 1, 1);
        vertices.add(vertex.copy());
        polygons.add(new CSGPolygon(vertices));
        //left
        vertex.position.set(-1, -1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, -1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, 1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, 1, -1);
        vertices.add(vertex.copy());
        polygons.add(new CSGPolygon(vertices));
        //right
        vertex.position.set(1, -1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(1, -1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, 1);
        vertices.add(vertex.copy());
        //top
        vertex.position.set(-1, 1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(1, 1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, 1, -1);
        //bottom
        vertex.position.set(-1, -1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, -1, -1);
        vertices.add(vertex.copy());
        vertex.position.set(1, -1, 1);
        vertices.add(vertex.copy());
        vertex.position.set(-1, -1, 1);
        vertices.add(vertex.copy());
        polygons.add(new CSGPolygon(vertices));
        return new BSPTreeNode(polygons);
    }
}
