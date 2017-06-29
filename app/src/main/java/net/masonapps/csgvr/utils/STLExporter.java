package net.masonapps.csgvr.utils;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * Created by Bob on 6/28/2017.
 */

public class STLExporter {

    private final float[] vertices;
    private final short[] indices;
    private final int vertexSize;
    public Matrix4 transform = new Matrix4();

    public STLExporter(float[] vertices, short[] indices, int vertexSize) {
        this.vertices = vertices;
        this.indices = indices;
        this.vertexSize = vertexSize;
    }

    public static STLExporter fromPolyhedronsSets(FileType fileType, PolyhedronsSet... polyhedronsSets) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();
        PolyhedronsSet polyhedronsSet = polyhedronsSets[0];
        if (polyhedronsSets.length > 1) {
            for (int i = 1; i < polyhedronsSets.length; i++) {
                polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, polyhedronsSets[i]);
            }
        }
        polyhedronsSet.getTree(true).visit(new MeshCreationTreeVisitor(vertices, indices, (Vector3D) polyhedronsSet.getBarycenter()));
        return new STLExporter(vertices.toArray(), indices.toArray(), 6);
    }

    public void writeToFile(File file) throws IOException {
        writeToOutputStream(new FileOutputStream(file));
    }

    private void writeToOutputStream(OutputStream outputStream) throws IOException {
        writeToOutputStream(outputStream, FileType.ASCII);
    }

    public void writeToFile(File file, FileType fileType) throws IOException {
        writeToOutputStream(new FileOutputStream(file), fileType);
    }

    public void writeToOutputStream(OutputStream outputStream, FileType fileType) throws IOException {
        switch (fileType) {
            case ASCII:
                writeASCII(outputStream);
                break;
            case BINARY:
                writeBinary(outputStream);
                break;
        }
    }

    private void writeASCII(OutputStream outputStream) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        writer.write("solid");
        writer.newLine();

        final Vector3 v1 = new Vector3();
        final Vector3 v2 = new Vector3();
        final Vector3 v3 = new Vector3();
        final Vector3 normal = new Vector3();

        for (int i = 0; i < indices.length; i += 3) {
            int i1 = indices[i] * vertexSize;
            int i2 = indices[i + 1] * vertexSize;
            int i3 = indices[i + 2] * vertexSize;
            v1.set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]).mul(transform);
            v2.set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]).mul(transform);
            v3.set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]).mul(transform);
            normal.set(v2).sub(v1).crs(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z).rot(transform).nor();

            writer.write(String.format(Locale.US, "facet normal %f %f %f", normal.x, normal.y, normal.z));
            writer.newLine();

            writer.write("outer loop");
            writer.newLine();

            writer.write(String.format(Locale.US, "vertex %f %f %f", v1.x, v1.y, v1.z));
            writer.newLine();
            writer.write(String.format(Locale.US, "vertex %f %f %f", v2.x, v2.y, v2.z));
            writer.newLine();
            writer.write(String.format(Locale.US, "vertex %f %f %f", v3.x, v3.y, v3.z));
            writer.newLine();

            writer.write("endloop");
            writer.newLine();

            writer.write("endfacet");
            writer.newLine();
        }

        writer.write("endsolid");
        writer.newLine();

        writer.close();
    }

    private void writeBinary(OutputStream outputStream) throws IOException {
        final DataOutputStream stream = new DataOutputStream(outputStream);

        for (int i = 0; i < 80; i++) {
            stream.writeByte(0);
        }

        stream.write(indices.length / 3);

        final Vector3 v1 = new Vector3();
        final Vector3 v2 = new Vector3();
        final Vector3 v3 = new Vector3();
        final Vector3 normal = new Vector3();

        for (int i = 0; i < indices.length; i += 3) {
            int i1 = indices[i] * vertexSize;
            int i2 = indices[i + 1] * vertexSize;
            int i3 = indices[i + 2] * vertexSize;
            v1.set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]);
            v2.set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]);
            v3.set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]);
            normal.set(v2).sub(v1).crs(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z).nor();

            stream.writeFloat(normal.x);
            stream.writeFloat(normal.y);
            stream.writeFloat(normal.z);

            stream.writeFloat(v1.x);
            stream.writeFloat(v1.y);
            stream.writeFloat(v1.z);

            stream.writeFloat(v2.x);
            stream.writeFloat(v2.y);
            stream.writeFloat(v2.z);

            stream.writeFloat(v3.x);
            stream.writeFloat(v3.y);
            stream.writeFloat(v3.z);

            stream.writeShort(0);
        }
    }

    public enum FileType {
        ASCII, BINARY
    }
}
