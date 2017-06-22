package net.masonapps.csgvr.csg;

import android.util.Log;
import android.util.SparseIntArray;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import java.util.Arrays;

/**
 * Created by Bob on 5/8/2017.
 */

public class CSG {
    private static final EarClippingTriangulator triangulator = new EarClippingTriangulator();

    public BSPTreeNode tree;

    public CSG(BSPTreeNode tree) {
        this.tree = tree;
    }

    public static CSG fromMesh(Mesh mesh, Matrix4 transform) {
        return new CSG(new BSPTreeNode(meshToPolygons(mesh, transform)));
    }

    public static Array<CSGPolygon> meshToPolygons(Mesh mesh, Matrix4 transform) {
        final Array<CSGPolygon> polygons = new Array<>();
        final int numVertices = mesh.getNumVertices();
        final int vertexSize = mesh.getVertexSize() / 4;
        float[] vertices = new float[numVertices * vertexSize];
        mesh.getVertices(vertices);
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);

        final ShortArray newIndices = new ShortArray();
        final Array<Vertex> newVertices = new Array<>();
        removeDuplicateVertices(vertices, indices, vertexSize, newVertices, newIndices);
        vertices = new float[newVertices.size * vertexSize];
        for (int i = 0; i < newVertices.size; i++) {
            final Vertex vertex = newVertices.get(i);
            final int index = i * vertexSize;
            vertices[index] = vertex.position.x;
            vertices[index + 1] = vertex.position.y;
            vertices[index + 2] = vertex.position.z;

            vertices[index + 3] = vertex.normal.x;
            vertices[index + 4] = vertex.normal.y;
            vertices[index + 5] = vertex.normal.z;

            vertices[index + 6] = vertex.uv.x;
            vertices[index + 7] = vertex.uv.y;
        }
        indices = newIndices.toArray();

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
//            CSGPolygon.removeDuplicates(tmpVerts);
//            if (tmpVerts.size > 3)
            polygons.add(new CSGPolygon(tmpVerts));
        }
        return polygons;
    }

    private static void removeDuplicateVertices(float[] vertices, short[] indices, int vertexSize, Array<Vertex> newVertices, ShortArray newIndices) {
        final float tolerance = 1e-5f;
        SparseIntArray indexMap = new SparseIntArray();
        for (int i = 0; i < vertices.length / vertexSize; i++) {
            boolean isDouble = false;
            final Vertex v = new Vertex();
            int index = i * vertexSize;
            v.position.set(vertices[index], vertices[index + 1], vertices[index + 2]);
            v.normal.set(vertices[index + 3], vertices[index + 4], vertices[index + 5]);
            v.uv.set(vertices[index + 6], vertices[index + 7]);
            for (int j = 0; j < newVertices.size; j++) {
                if (newVertices.get(j).position.dst(v.position) <= tolerance) {
                    Log.d("remove duplicate", "duplicate found i: " + i + " j: " + j);
                    indexMap.put(i, j);
                    isDouble = true;
                    break;
                }
                }
            if (!isDouble) {
                indexMap.put(i, newVertices.size);
                newVertices.add(v);
                }
            }

        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            newIndices.add(indexMap.get(index, index));
        }
        }

    public static Mesh toMesh(CSG csg) {
        return polygonsToMesh(csg.tree.getAllPolygons());
    }

    public static Mesh polygonsToMesh(Array<CSGPolygon> polygons) {
        final FloatArray vertices = new FloatArray();
        final FloatArray tempVerts = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Vertex tempVertex = new Vertex();
        int startIndex = 0;
        for (CSGPolygon polygon : polygons) {
            tempVerts.clear();
            for (int i = 0; i < polygon.getVertexCount(); i++) {
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                vertices.add(tempVertex.normal.x);
                vertices.add(tempVertex.normal.y);
                vertices.add(tempVertex.normal.z);
                vertices.add(tempVertex.uv.x);
                vertices.add(tempVertex.uv.y);
            }
            final ShortArray tempIndices = triangulator.computeTriangles(tempVerts);
            tempIndices.reverse();
            for (int j = 0; j < tempIndices.size; j++) {
                final int index = startIndex + tempIndices.get(j);
                indices.add(index);
            }
            startIndex = vertices.size / 8;
        }
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
        final float[] vertexArray = vertices.toArray();
        mesh.setVertices(vertexArray);
        mesh.setIndices(indices.toArray());
        Log.i("polygonsToMesh", Arrays.toString(vertexArray));
        return mesh;
    }

    public static Mesh toLineMesh(CSG csg) {
        return polygonsToLineMesh(csg.tree.getAllPolygons());
    }

    public static Mesh polygonsToLineMesh(Array<CSGPolygon> polygons) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        final Vertex tempVertex = new Vertex();
        int index = 0;
        for (int p = 0; p < polygons.size; p++) {
            final CSGPolygon polygon = polygons.get(p);
            final int vertexCount = polygon.getVertexCount();
            for (int i = 0; i < vertexCount; i++) {
                tempVertex.set(polygon.vertices.get(i));
                vertices.add(tempVertex.position.x);
                vertices.add(tempVertex.position.y);
                vertices.add(tempVertex.position.z);
                indices.add(index);
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
                index++;
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

    public static ModelInstance toModelInstance(CSG csg, Material material) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("", toMesh(csg), GL20.GL_TRIANGLES, material);
        return new ModelInstance(modelBuilder.end());
    }

    public CSG union(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
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
        a.invert();
        return new CSG(a);
    }
}
