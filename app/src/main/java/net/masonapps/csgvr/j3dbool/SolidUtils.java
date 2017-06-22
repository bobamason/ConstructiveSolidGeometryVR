package net.masonapps.csgvr.j3dbool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;

/**
 * Created by Bob on 6/22/2017.
 */

public class SolidUtils {

    public static Solid meshToSolid(Mesh mesh, Color color) {
        final Vertex[] vertexArray = new Vertex[mesh.getNumVertices()];
        final int vertexSize = mesh.getVertexSize() / 4;
        final float[] vertices = new float[mesh.getNumVertices() * vertexSize];
        final short[] indices = new short[mesh.getNumIndices()];
        mesh.getVertices(vertices);
        mesh.getIndices(indices);
        for (int i = 0; i < vertexArray.length; i++) {
            final int index = i * vertexSize;
            final float x = vertices[index];
            final float y = vertices[index + 1];
            final float z = vertices[index + 2];
            final Vertex vertex = new Vertex(x, y, z, color);
            final float nx = vertices[index + 3];
            final float ny = vertices[index + 4];
            final float nz = vertices[index + 5];
            vertex.normal.set(nx, ny, nz);
            vertexArray[i] = vertex;
        }
        return new Solid(vertexArray, indices);
    }

    public static Solid createBox(float w, float h, float d, Color color) {
        final Vertex[] vertexArray = new Vertex[8];
        final short[] indices = new short[36];

        final float hw = w / 2.0f;
        final float hh = h / 2.0f;
        final float hd = d / 2.0f;
        //0
        vertexArray[0] = new Vertex(-hw, hh, hd, color);
        //1
        vertexArray[1] = new Vertex(hw, hh, hd, color);
        //2
        vertexArray[2] = new Vertex(hw, hh, -hd, color);
        //3
        vertexArray[3] = new Vertex(-hw, hh, -hd, color);
        //4
        vertexArray[4] = new Vertex(-hw, -hh, -hd, color);
        //5
        vertexArray[5] = new Vertex(hw, -hh, -hd, color);
        //6
        vertexArray[6] = new Vertex(hw, -hh, hd, color);
        //7
        vertexArray[7] = new Vertex(-hw, -hh, hd, color);

        //top
        int offset = 0;
        indices[offset] = 0;
        indices[offset + 1] = 1;
        indices[offset + 2] = 2;
        indices[offset + 3] = 0;
        indices[offset + 4] = 2;
        indices[offset + 5] = 3;
//        facets.add(new int[]{0, 1, 2, 3});

        //bottom
        offset = 6;
        indices[offset] = 4;
        indices[offset + 1] = 5;
        indices[offset + 2] = 6;
        indices[offset + 3] = 4;
        indices[offset + 4] = 6;
        indices[offset + 5] = 7;
//        facets.add(new int[]{4, 5, 6, 7});

        //front
        offset = 6 * 2;
        indices[offset] = 7;
        indices[offset + 1] = 6;
        indices[offset + 2] = 1;
        indices[offset + 3] = 7;
        indices[offset + 4] = 1;
        indices[offset + 5] = 0;
//        facets.add(new int[]{7, 6, 1, 0});

        //back
        offset = 6 * 3;
        indices[offset] = 5;
        indices[offset + 1] = 4;
        indices[offset + 2] = 3;
        indices[offset + 3] = 5;
        indices[offset + 4] = 3;
        indices[offset + 5] = 2;
//        facets.add(new int[]{5, 4, 3, 2});

        //left
        offset = 6 * 4;
        indices[offset] = 4;
        indices[offset + 1] = 7;
        indices[offset + 2] = 0;
        indices[offset + 3] = 4;
        indices[offset + 4] = 0;
        indices[offset + 5] = 3;
//        facets.add(new int[]{4, 7, 0, 3});

        //right
        offset = 6 * 5;
        indices[offset] = 6;
        indices[offset + 1] = 5;
        indices[offset + 2] = 2;
        indices[offset + 3] = 6;
        indices[offset + 4] = 2;
        indices[offset + 5] = 1;
//        facets.add(new int[]{6, 5, 2, 1});

        return new Solid(vertexArray, indices);
    }
}
