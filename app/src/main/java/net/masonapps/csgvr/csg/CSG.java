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

    public BSPTreeNode tree;

    public CSG(BSPTreeNode tree) {
        this.tree = tree;
    }

    public static CSG fromMesh(Mesh mesh, Matrix4 transform) {
        return new CSG(new BSPTreeNode(polygonsFromMesh(mesh, transform)));
    }

    public static Array<CSGPolygon> polygonsFromMesh(Mesh mesh, Matrix4 transform) {
        final Array<CSGPolygon> polygons = new Array<>();
        final int numVertices = mesh.getNumVertices();
        final int vertexSize = 8;
//        if(vertexSize != 8) throw new IllegalArgumentException("mesh must has position, normal, and uv");
        final float[] vertices = new float[numVertices * vertexSize];
        mesh.getVertices(vertices);
        final short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        final Array<Vertex> tmpVerts = new Array<>(3);
        final Vertex a = new Vertex();
        final Vertex b = new Vertex();
        final Vertex c = new Vertex();

        for (int i = 0; i < indices.length; i += 3) {
            tmpVerts.clear();
//            Log.i("start", i / 3 + "");
            int iA = indices[i] * vertexSize;
//            Log.i("iA", indices[i] + "");
            a.position.set(vertices[iA], vertices[iA + 1], vertices[iA + 2]).mul(transform);
            a.normal.set(vertices[iA + 3], vertices[iA + 4], vertices[iA + 5]);
            a.uv.set(vertices[iA + 6], vertices[iA + 7]);

            int iB = indices[i + 1] * vertexSize;
//            Log.i("iB", indices[i + 1] + "");
            b.position.set(vertices[iB], vertices[iB + 1], vertices[iB + 2]).mul(transform);
            b.normal.set(vertices[iB + 3], vertices[iB + 4], vertices[iB + 5]);
            b.uv.set(vertices[iB + 6], vertices[iB + 7]);

            int iC = indices[i + 2] * vertexSize;
//            Log.i("iC", indices[i + 2] + "");
            c.position.set(vertices[iC], vertices[iC + 1], vertices[iC + 2]).mul(transform);
            c.normal.set(vertices[iC + 3], vertices[iC + 4], vertices[iC + 5]);
            c.uv.set(vertices[iC + 6], vertices[iC + 7]);
//            Log.i("end", i / 3 + "");

            tmpVerts.add(a);
            tmpVerts.add(b);
            tmpVerts.add(c);
            polygons.add(new CSGPolygon(tmpVerts));
        }
        return polygons;
    }

    public static Mesh toMesh(CSG csg) {
        return polygonsToMesh(csg.tree.getAllPolygons());
    }

    public static Mesh polygonsToMesh(Array<CSGPolygon> polygons) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Vertex tempVertex = new Vertex();
        int index = -1;
        for (CSGPolygon polygon : polygons) {
//            for (int i = 2; i < polygon.getVertexCount(); i++) {
//                if (i < 3) {
            tempVertex.set(polygon.vertices.get(0));
            vertices.add(tempVertex.position.x);
            vertices.add(tempVertex.position.y);
            vertices.add(tempVertex.position.z);
            vertices.add(tempVertex.normal.x);
            vertices.add(tempVertex.normal.y);
            vertices.add(tempVertex.normal.z);
            vertices.add(tempVertex.uv.x);
            vertices.add(tempVertex.uv.y);
            indices.add(index++);

            tempVertex.set(polygon.vertices.get(1));
            vertices.add(tempVertex.position.x);
            vertices.add(tempVertex.position.y);
            vertices.add(tempVertex.position.z);
            vertices.add(tempVertex.normal.x);
            vertices.add(tempVertex.normal.y);
            vertices.add(tempVertex.normal.z);
            vertices.add(tempVertex.uv.x);
            vertices.add(tempVertex.uv.y);
            indices.add(index++);
//                } else {
//                    indices.add(index - 1);
//                    indices.add(index);
//                }
            tempVertex.set(polygon.vertices.get(2));
            vertices.add(tempVertex.position.x);
            vertices.add(tempVertex.position.y);
            vertices.add(tempVertex.position.z);
            vertices.add(tempVertex.normal.x);
            vertices.add(tempVertex.normal.y);
            vertices.add(tempVertex.normal.z);
            vertices.add(tempVertex.uv.x);
            vertices.add(tempVertex.uv.y);
            indices.add(index++);
//            }
        }
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        mesh.setIndices(indices.toArray());
        mesh.setVertices(vertices.toArray());
        return mesh;
    }

    public static Mesh toLineMesh(CSG csg) {
        return polygonsToLineMesh(csg.tree.getAllPolygons());
    }

    public static Mesh polygonsToLineMesh(Array<CSGPolygon> polygons) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Vertex tempVertex = new Vertex();
        int index = -1;
        for (int p = 0; p < polygons.size; p++) {
            final CSGPolygon polygon = polygons.get(p);
            final int vertexCount = polygon.getVertexCount();
            for (int i = 0; i < vertexCount; i++) {
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                indices.add(index++);
//                Log.i("index start", index + "");
                if (i >= 1) {
                    indices.add(index);
//                    Log.i("index repeat", index + "");
                }
                final int end = vertexCount - 1;
                if (i == end) {
                    indices.add(index - end);
//                    Log.i("index end", (index - end) + "");
                }
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
