package net.masonapps.csgvr.primitives;

import android.util.Log;
import android.util.SparseIntArray;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class ConversionUtils {

    public static Vector2D convertVector(Vector2 v) {
        return new Vector2D(v.x, v.y);
    }

    public static Vector2 convertVector(Vector2D v) {
        return new Vector2((float) v.getX(), (float) v.getY());
    }

    public static Vector3D convertVector(Vector3 v) {
        return new Vector3D(v.x, v.y, v.z);
    }

    public static Vector3 convertVector(Vector3D v) {
        return new Vector3((float) v.getX(), (float) v.getY(), (float) v.getZ());
    }

    public static Vector3D mulVector3D(Matrix4 matrix, Vector3D v) {
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();
        final float l_mat[] = matrix.val;
        return new Vector3D(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03], x
                * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13], x * l_mat[Matrix4.M20] + y
                * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]);
    }

    public static Mesh polyhedronsSetToMesh(PolyhedronsSet polyhedronsSet) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        polyhedronsSet.getTree(true).visit(new MeshCreationTreeVisitor(vertices, indices));
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.Normal());
        mesh.setIndices(indices.toArray());
        final float[] vertArray = vertices.toArray();
        mesh.setVertices(vertArray);
        return mesh;
    }

    private static void addVertex(Vector3D position, Vector3D normal, FloatArray vertices) {
        vertices.add((float) position.getX());
        vertices.add((float) position.getY());
        vertices.add((float) position.getZ());
        vertices.add((float) normal.getX());
        vertices.add((float) normal.getY());
        vertices.add((float) normal.getZ());
    }

    public static ModelInstance polyhedronsSetToModelInstance(PolyhedronsSet polyhedronsSet, Material material) {
        final Mesh mesh = ConversionUtils.polyhedronsSetToMesh(polyhedronsSet);
        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("mesh", mesh, GL20.GL_TRIANGLES, material);
        return new ModelInstance(modelBuilder.end());
    }

    public static PolyhedronsSet meshToPolyhedronSet(Mesh mesh) {
        final int numVertices = mesh.getNumVertices();
        final int vertexSize = mesh.getVertexSize() / 4;
        float[] vertices = new float[numVertices * vertexSize];
        mesh.getVertices(vertices);
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        final List<Vector3D> vector3DList = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        final ArrayList<Integer> outIndices = new ArrayList<>();
        removeDoubles(vertices, indices, vertexSize, vector3DList, outIndices);
        for (int i = 0; i < outIndices.size(); i += 3) {
            int ia = outIndices.get(i);
            int ib = outIndices.get(i + 1);
            int ic = outIndices.get(i + 2);
            facets.add(new int[]{ia, ib, ic});
        }
        return new PolyhedronsSet(vector3DList, facets, 1e-10);
    }

    private static void removeDoubles(float[] vertices, short[] indices, int vertexSize, List<Vector3D> outVecs, List<Integer> outIndices) {
        final double tolerance = 1e-5;
        SparseIntArray indexMap = new SparseIntArray();
        for (int i = 0; i < vertices.length / vertexSize; i++) {
            boolean isDouble = false;
            final Vector3D v = new Vector3D(vertices[i * vertexSize], vertices[i * vertexSize + 1], vertices[i * vertexSize + 2]);
            for (int j = 0; j < outVecs.size(); j++) {
                if (outVecs.get(j).distance(v) <= tolerance) {
                    Log.d("remove doubles", "double found i: " + i + " j: " + j + " vec: " + v.toString());
                    indexMap.put(i, j);
                    isDouble = true;
                    break;
                }
            }
            if (!isDouble) {
                indexMap.put(i, outVecs.size());
                outVecs.add(v);
            }
        }

        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            outIndices.add(indexMap.get(index, index));
        }
    }
    
    private static class MeshCreationTreeVisitor implements BSPTreeVisitor<Euclidean3D> {

        public static final int VERTEX_SIZE = 6;
        private final FloatArray vertices;
        private final ShortArray indices;
        private final DelaunayTriangulator triangulator;
        private int startIndex;

        public MeshCreationTreeVisitor(FloatArray vertices, ShortArray indices) {
            this.vertices = vertices;
            this.indices = indices;
            triangulator = new DelaunayTriangulator();
            startIndex = 0;
        }

        private static Vector3D computeCentroid(FloatArray vertices, int ia, int ib, int ic) {
            double x = (vertices.get(ia) + vertices.get(ib) + vertices.get(ic)) / 3.0;
            double y = (vertices.get(ia + 1) + vertices.get(ib + 1) + vertices.get(ic + 1)) / 3.0;
            double z = (vertices.get(ia + 2) + vertices.get(ib + 2) + vertices.get(ic + 2)) / 3.0;
            return new Vector3D(x, y, z);
        }

        @Override
        public Order visitOrder(BSPTree<Euclidean3D> node) {
            return Order.PLUS_SUB_MINUS;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visitInternalNode(BSPTree<Euclidean3D> node) {
//            if (startIndex > 12) return;
            BoundaryAttribute<Euclidean3D> attribute =
                    (BoundaryAttribute<Euclidean3D>) node.getAttribute();
            if (attribute.getPlusOutside() != null) {
                final SubPlane plusOutside = (SubPlane) attribute.getPlusOutside();
                Log.i(MeshCreationTreeVisitor.class.getSimpleName(), "handing plusOutside SubPlane");
                handleSubPlane(plusOutside, false);
            }
            if (attribute.getPlusInside() != null) {
                final SubPlane plusInside = (SubPlane) attribute.getPlusInside();
                Log.i(MeshCreationTreeVisitor.class.getSimpleName(), "handing plusInside SubPlane");
                handleSubPlane(plusInside, true);
            }
        }

        private void handleSubPlane(SubPlane subPlane, boolean reverse) {
            final Plane plane = (Plane) subPlane.getHyperplane();
            final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();
            final double tolerance = remainingRegion.getTolerance();
            final Vector2D[][] loops = remainingRegion.getVertices();

            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "loop count " + loops.length);

            final FloatArray tempVerts = new FloatArray();
            for (Vector2D[] loop : loops) {

                for (Vector2D v : loop) {
                    addVertex(plane.toSpace(v), plane.getNormal().scalarMultiply(reverse ? -1 : 1), this.vertices);
                    tempVerts.add((float) v.getX());
                    tempVerts.add((float) v.getY());
                }
            }


            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "vertex count " + tempVerts.size / 2);

            final ShortArray tempIndices = triangulator.computeTriangles(tempVerts, false);
            if (!reverse) {
                tempIndices.reverse();
            }
            for (int j = 0; j < tempIndices.size; j += 3) {
                final int ia = startIndex + tempIndices.get(j);
                final int ib = startIndex + tempIndices.get(j + 1);
                final int ic = startIndex + tempIndices.get(j + 2);
                Vector3D centroid = computeCentroid(vertices, ia * VERTEX_SIZE, ib * VERTEX_SIZE, ic * VERTEX_SIZE);
                if (remainingRegion.checkPoint(plane.toSubSpace(centroid)) == Region.Location.INSIDE) {
                    indices.add(ia);
                    indices.add(ib);
                    indices.add(ic);
                }
            }
            startIndex = this.vertices.size / VERTEX_SIZE;
        }

        private void addLoopVertices(Plane plane, Vector2D[] loop, boolean reverse) {
            final FloatArray tempVerts = new FloatArray();
            for (Vector2D v : loop) {
                addVertex(plane.toSpace(v), plane.getNormal().scalarMultiply(reverse ? -1 : 1), this.vertices);
                tempVerts.add((float) v.getX());
                tempVerts.add((float) v.getY());
            }


            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "vertex count " + tempVerts.size / 2);

            final ShortArray tempIndices = triangulator.computeTriangles(tempVerts, false);
            if (!reverse) {
                tempIndices.reverse();
            }
            for (int j = 0; j < tempIndices.size; j++) {
                final int index = startIndex + tempIndices.get(j);
                indices.add(index);
            }
            startIndex = this.vertices.size / 6;
        }

        @Override
        public void visitLeafNode(BSPTree<Euclidean3D> node) {
        }
    }
}
