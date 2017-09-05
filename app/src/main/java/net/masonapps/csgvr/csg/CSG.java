package net.masonapps.csgvr.csg;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 9/4/2017.
 */

public class CSG {

    public static List<CsgTriangle> trianglesFromMesh(Mesh mesh, Matrix4 transform) {
        final List<CsgTriangle> triangles = new ArrayList<>(mesh.getNumIndices() / 3);
        final VertexAttributes attrs = mesh.getVertexAttributes();
        final int vertexSize = mesh.getVertexSize() / 4;
        final float[] vertices = new float[mesh.getNumVertices() * vertexSize];
        mesh.getVertices(vertices);
        final short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);

        for (int i = 0; i < indices.length; i += 3) {
            final CsgVertex v1 = new CsgVertex();
            final CsgVertex v2 = new CsgVertex();
            final CsgVertex v3 = new CsgVertex();
            int i1 = indices[i] * vertexSize;
            int i2 = indices[i + 1] * vertexSize;
            int i3 = indices[i + 2] * vertexSize;

            v1.position.set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]).mul(transform);
            v1.normal.set(vertices[i1 + 3], vertices[i1 + 4], vertices[i1 + 5]).rot(transform);

            v2.position.set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]).mul(transform);
            v2.normal.set(vertices[i2 + 3], vertices[i2 + 4], vertices[i2 + 5]).rot(transform);

            v3.position.set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]).mul(transform);
            v3.normal.set(vertices[i3 + 3], vertices[i3 + 4], vertices[i3 + 5]).rot(transform);

            triangles.add(new CsgTriangle(v1, v2, v3));
        }

        return triangles;
    }

    public static Mesh toMesh(List<CsgTriangle> triangles) {
        final int numTris = triangles.size();
        final Mesh mesh = new Mesh(true, numTris * 3, numTris * 3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()));
        final int vertexSize = 6;
        final float[] vertices = new float[numTris * vertexSize * 3];

        for (int i = 0; i < numTris; i++) {
            final CsgTriangle t = triangles.get(i);
            vertices[i * vertexSize] = t.v1.position.x;
            vertices[i * vertexSize + 1] = t.v1.position.y;
            vertices[i * vertexSize + 2] = t.v1.position.z;
            vertices[i * vertexSize + 3] = t.v1.normal.x;
            vertices[i * vertexSize + 4] = t.v1.normal.y;
            vertices[i * vertexSize + 5] = t.v1.normal.z;

            vertices[i * vertexSize + vertexSize] = t.v2.position.x;
            vertices[i * vertexSize + vertexSize + 1] = t.v2.position.y;
            vertices[i * vertexSize + vertexSize + 2] = t.v2.position.z;
            vertices[i * vertexSize + vertexSize + 3] = t.v2.normal.x;
            vertices[i * vertexSize + vertexSize + 4] = t.v2.normal.y;
            vertices[i * vertexSize + vertexSize + 5] = t.v2.normal.z;

            vertices[i * vertexSize + 2 * vertexSize] = t.v3.position.x;
            vertices[i * vertexSize + 2 * vertexSize + 1] = t.v3.position.y;
            vertices[i * vertexSize + 2 * vertexSize + 2] = t.v3.position.z;
            vertices[i * vertexSize + 2 * vertexSize + 3] = t.v3.normal.x;
            vertices[i * vertexSize + 2 * vertexSize + 4] = t.v3.normal.y;
            vertices[i * vertexSize + 2 * vertexSize + 5] = t.v3.normal.z;
        }

        mesh.setVertices(vertices);

        final short[] indices = new short[numTris * 3];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = (short) i;
        }
        mesh.setIndices(indices);
        return mesh;
    }

    public static List<CsgTriangle> union(List<CsgTriangle> aTriangles, List<CsgTriangle> bTriangles) {
        BspTree.Node a = new BspTree.Node(aTriangles);
        BspTree.Node b = new BspTree.Node(bTriangles);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        return new BspTree.Node(b.getAllTriangles()).getAllTriangles();
    }
}
