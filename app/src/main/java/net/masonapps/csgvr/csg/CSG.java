package net.masonapps.csgvr.csg;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Created by Bob on 5/8/2017.
 */

public class CSG {

    private BSPTreeNode tree;

    public CSG(BSPTreeNode tree) {
        this.tree = tree;
    }

    public static CSG fromMesh(Mesh mesh, Matrix4 transform) {
        final Array<CSGPolygon> polygons = new Array<>();
        final int numVertices = mesh.getNumVertices();
        final int vertexSize = 8;
//        if(vertexSize != 8) throw new IllegalArgumentException("mesh must has position, normal, and uv");
        final float[] vertices = new float[numVertices * vertexSize];
        mesh.getVertices(vertices);
        final Array<Vertex> tmpVerts = new Array<>(3);
        final Vertex a = new Vertex();
        final Vertex b = new Vertex();
        final Vertex c = new Vertex();

        final int step = vertexSize * 3;
        final int end = vertices.length - (step - 1);
        for (int i = 0; i < end; i += step) {
            tmpVerts.clear();
            final int offsetA = 0;
            a.position.set(vertices[i + offsetA], vertices[i + 1 + offsetA], vertices[i + 2 + offsetA]).mul(transform);
            a.normal.set(vertices[i + 3 + offsetA], vertices[i + 4 + offsetA], vertices[i + 5 + offsetA]);
            a.uv.set(vertices[i + 6 + offsetA], vertices[i + 7 + offsetA]);

            final int offsetB = vertexSize;
            b.position.set(vertices[i + offsetB], vertices[i + 1 + offsetB], vertices[i + 2 + offsetB]).mul(transform);
            b.normal.set(vertices[i + 3 + offsetB], vertices[i + 4 + offsetB], vertices[i + 5 + offsetB]);
            b.uv.set(vertices[i + 6 + offsetB], vertices[i + 7 + offsetB]);

            final int offsetC = vertexSize * 2;
            c.position.set(vertices[i + offsetC], vertices[i + 1 + offsetC], vertices[i + 2 + offsetC]).mul(transform);
            c.normal.set(vertices[i + 3 + offsetC], vertices[i + 4 + offsetC], vertices[i + 5 + offsetC]);
            c.uv.set(vertices[i + 6 + offsetC], vertices[i + 7 + offsetC]);

            tmpVerts.add(a);
            tmpVerts.add(b);
            tmpVerts.add(c);
            polygons.add(new CSGPolygon(tmpVerts));
        }
        return new CSG(new BSPTreeNode(polygons));
    }

    public static Mesh toMesh(CSG csg) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Array<CSGPolygon> polygons = new Array<>();
        csg.tree.getAllPolygons(polygons);
        final Vertex tempVertex = new Vertex();
        int index = -1;
        for (CSGPolygon polygon : polygons) {
            for (int i = 2; i < polygon.getVertexCount(); i++) {
                if (i < 3) {
                    tempVertex.set(polygon.vertices.get(i - 2));
                    vertices.add(tempVertex.position.x);
                    vertices.add(tempVertex.position.y);
                    vertices.add(tempVertex.position.z);
                    vertices.add(tempVertex.normal.x);
                    vertices.add(tempVertex.normal.y);
                    vertices.add(tempVertex.normal.z);
                    vertices.add(tempVertex.uv.x);
                    vertices.add(tempVertex.uv.y);
                    indices.add(index++);

                    tempVertex.set(polygon.vertices.get(i - 1));
                    vertices.add(tempVertex.position.x);
                    vertices.add(tempVertex.position.y);
                    vertices.add(tempVertex.position.z);
                    vertices.add(tempVertex.normal.x);
                    vertices.add(tempVertex.normal.y);
                    vertices.add(tempVertex.normal.z);
                    vertices.add(tempVertex.uv.x);
                    vertices.add(tempVertex.uv.y);
                    indices.add(index++);
                } else {
                    indices.add(index - 1);
                    indices.add(index);
                }
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                vertices.add(tempVertex.normal.x);
                vertices.add(tempVertex.normal.y);
                vertices.add(tempVertex.normal.z);
                vertices.add(tempVertex.uv.x);
                vertices.add(tempVertex.uv.y);
                indices.add(index++);
            }
        }
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position());
        mesh.setIndices(indices.toArray());
        mesh.setVertices(vertices.toArray());
        return mesh;
    }

    public static Mesh toLineMesh(CSG csg) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Array<CSGPolygon> polygons = new Array<>();
        csg.tree.getAllPolygons(polygons);
        final Vertex tempVertex = new Vertex();
        int index = -1;
        for (CSGPolygon polygon : polygons) {
            for (int i = 1; i < polygon.getVertexCount(); i++) {
                if (i < 2) {
                    tempVertex.set(polygon.vertices.get(i - 1));
                    vertices.add(tempVertex.position.x);
                    vertices.add(tempVertex.position.y);
                    vertices.add(tempVertex.position.z);
                    indices.add(index++);
                } else {
                    indices.add(index);
                }
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                indices.add(index++);
            }
        }
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position());
        mesh.setIndices(indices.toArray());
        mesh.setVertices(vertices.toArray());
        return mesh;
    }

    public static Mesh toPoints(CSG csg) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Array<CSGPolygon> polygons = new Array<>();
        csg.tree.getAllPolygons(polygons);
        final Vertex tempVertex = new Vertex();
        int index = -1;
        for (CSGPolygon polygon : polygons) {
            for (int i = 1; i < polygon.getVertexCount(); i++) {
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                indices.add(index++);
            }
        }
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position());
        mesh.setIndices(indices.toArray());
        mesh.setVertices(vertices.toArray());
        return mesh;
    }

    public CSG union(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        a.build(b.getAllPolygons());
        return new CSG(a);
    }

    public CSG subtract(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.getAllPolygons());
        a.invert();
        return new CSG(a);
    }

    public CSG intersect(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.getAllPolygons());
        return new CSG(a);
    }
}
