package net.masonapps.csgvr.primitives;

import android.util.Log;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class ConversionUtils {

    public static PolyhedronsSet modelInstanceToPolyhedronsSet(ModelInstance modelInstance) {
        final List<Vector3D> vertexList = new ArrayList<>();
        final List<int[]> facets = new ArrayList<>();
        final double tolerance = 0.00001;
        for (Mesh mesh : modelInstance.model.meshes) {
            final int numVertices = mesh.getNumVertices();
            final int vertexSize = mesh.getVertexSize() / 4;
            final float[] vertices = new float[numVertices * vertexSize];
            mesh.getVertices(vertices);
            final short[] indices = new short[mesh.getNumIndices()];
            mesh.getIndices(indices);
            final float[] mat = modelInstance.transform.val;
            for (int i = 0; i < vertices.length; i += vertexSize) {
                final float x = vertices[i];
                final float y = vertices[i + 1];
                final float z = vertices[i + 2];
                vertexList.add(new Vector3D(x * mat[Matrix4.M00] + y * mat[Matrix4.M01] + z * mat[Matrix4.M02] + mat[Matrix4.M03],
                        x * mat[Matrix4.M10] + y * mat[Matrix4.M11] + z * mat[Matrix4.M12] + mat[Matrix4.M13],
                        x * mat[Matrix4.M20] + y * mat[Matrix4.M21] + z * mat[Matrix4.M22] + mat[Matrix4.M23]));
            }

            for (int i = 0; i < indices.length; i += 3) {
                facets.add(new int[]{indices[i], indices[i + 1], indices[i + 2]});
            }
        }
        return new PolyhedronsSet(vertexList, facets, tolerance);
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

    private static class MeshCreationTreeVisitor implements BSPTreeVisitor<Euclidean3D> {

        private final FloatArray vertices;
        private final ShortArray indices;
        private final EarClippingTriangulator triangulator;
        private int startIndex;

        public MeshCreationTreeVisitor(FloatArray vertices, ShortArray indices) {
            this.vertices = vertices;
            this.indices = indices;
            triangulator = new EarClippingTriangulator();
            startIndex = 0;
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
            final Vector2D[][] loops = ((PolygonsSet) subPlane.getRemainingRegion()).getVertices();
            final FloatArray loopVerts = new FloatArray();
            for (int iL = 0; iL < loops.length; iL++) {
                final Vector2D[] loop = loops[iL];
                loopVerts.clear();
                Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "loop #" + iL + " size: " + loop.length);
                if (loop.length < 3 || loop[0] == null) {
                    Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "skipping loop");
                    continue;
                }
                for (final Vector2D v : loop) {
                    loopVerts.add((float) v.getX());
                    loopVerts.add((float) v.getY());
                    addVertex(plane.toSpace(v), plane.getNormal().scalarMultiply(reverse ? -1 : 1), vertices);
                }
                final ShortArray tempIndices = triangulator.computeTriangles(loopVerts);
                if (!reverse) {
                    tempIndices.reverse();
                }
                for (int i = 0; i < tempIndices.size; i++) {
                    final int index = startIndex + tempIndices.get(i);
//                    Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "index " + index);
                    indices.add(index);
                }
                startIndex = vertices.size / 6;
            }
        }

//        private void handleSubPlane(SubPlane subPlane) {
//            final Plane plane = (Plane) subPlane.getHyperplane();
//            final Vector2D[][] loops = ((PolygonsSet) subPlane.getRemainingRegion()).getVertices();
//            final FloatArray loopVerts = new FloatArray();
//            for (int iL = 0; iL < loops.length; iL++) {
//                final Vector2D[] loop = loops[iL];
//                loopVerts.clear();
//                Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "loop #" + iL + " size: " + loop.length);
//                if (loop.length < 3 || loop[0] == null) {
//                    Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "skipping loop");
//                    continue;
//                }
//                for (int i = 2; i < loop.length; i++) {
//                    if (i == 2) {
//                        addVertex(plane.toSpace(loop[i - 2]), plane.getNormal(), vertices);
//                        indices.add(startIndex);
//                        startIndex++;
//                        addVertex(plane.toSpace(loop[i - 1]), plane.getNormal(), vertices);
//                        indices.add(startIndex);
//                        startIndex++;
//                    } else {
//                        indices.add(startIndex - 2);
//                        indices.add(startIndex - 1);
//                    }
//                    addVertex(plane.toSpace(loop[i]), plane.getNormal(), vertices);
//                    indices.add(startIndex);
//                    startIndex++;
////                    addVertex(plane.toSpace(loop[i]), plane.getNormal(), vertices);
//                }
//            }
//        }

//        private void handlePlusInside(SubPlane plusInside) {
//            final Plane plane1 = (Plane) plusInside.getHyperplane();
//            final Vector2D[][] loops = ((PolygonsSet) plusInside.getRemainingRegion()).getVertices();
//            for (int iL = 0; iL < loops.length; iL++) {
//                final Vector2D[] loop = loops[iL];
//                Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handlePlusInside", "loop #" + iL + " size: " + loop.length);
//                if (loop.length < 3 || loop[0] == null) {
//                    Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handlePlusInside", "skipping loop");
//                    continue;
//                }
//                final int startIndex = startIndex;
//                for (int i = loop.length - 3; i >= 0; i--) {
//                    if (i == loop.length - 3) {
//                        addVertex(plane1.toSpace(loop[i + 2]), plane1.getNormal(), vertices);
//                        indices.add(startIndex);
//                        startIndex++;
//                        addVertex(plane1.toSpace(loop[i + 1]), plane1.getNormal(), vertices);
//                        indices.add(startIndex);
//                        startIndex++;
//                    } else {
//                        indices.add(startIndex);
//                        indices.add(startIndex - 1);
//                    }
//                    addVertex(plane1.toSpace(loop[i]), plane1.getNormal(), vertices);
//                    indices.add(startIndex);
//                    startIndex++;
//                }
//            }
//        }

        @Override
        public void visitLeafNode(BSPTree<Euclidean3D> node) {
        }
    }
}
