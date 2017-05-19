package net.masonapps.csgvr.primitives;

import android.util.Log;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.BoundaryProjection;
import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bob on 5/16/2017.
 */

public class ConversionUtils {

    public static Vector3D convertVector3(Vector3 v) {
        return new Vector3D(v.x, v.y, v.z);
    }

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
            final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();
            final double tolerance = remainingRegion.getTolerance();
            final Vector2D[][] loops = remainingRegion.getVertices();
            final HashMap<PolygonsSet, List<Vector2D>> polygonsMap = new HashMap<>();
            final Collection<SubHyperplane<Euclidean2D>> lines = new ArrayList<>();

            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "loop count " + loops.length);

            if (loops.length == 1) {
                final Vector2D[] loop = loops[0];
                addLoopVertices(plane, loop, reverse);
            } else {

                for (Vector2D[] loop : loops) {
                    if (loop.length < 3 || loop[0] == null)
                        continue;

                    lines.clear();
                    for (int i = 0; i < loop.length; i++) {
                        lines.add(new SubLine(loop[i], loop[(i + 1) % loop.length], tolerance));
                    }
                    boolean isHole = false;
                    final PolygonsSet p = new PolygonsSet(lines, tolerance);
                    for (PolygonsSet polygonsSet : polygonsMap.keySet()) {
                        isHole = true;
                        for (Vector2D v : loop) {
                            if (polygonsSet.checkPoint(v) != Region.Location.INSIDE)
                                isHole = false;
                            break;
                        }
                        if (isHole) {
                            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "hole found!!!!!!");
                            BoundaryProjection<Euclidean2D> projection = polygonsSet.projectToBoundary(loop[loop.length - 1]);
                            if (projection.getProjected() == null) break;
                            List<Vector2D> list = polygonsMap.get(polygonsSet);
                            list.add((Vector2D) projection.getProjected());
                            for (int j = loop.length - 1; j >= 0; j--) {
                                list.add(loop[j]);
                            }
                            list.add((Vector2D) projection.getProjected());
                            break;
                        }
                    }
                    if (!isHole) {
                        Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "no hole found");
                        polygonsMap.put(p, new ArrayList<>(Arrays.asList(loop)));
                    }
                }

                for (List<Vector2D> list : polygonsMap.values()) {
                    final Vector2D[] array = new Vector2D[list.size()];
                    addLoopVertices(plane, list.toArray(array), reverse);
                }

            }
        }

        private void addLoopVertices(Plane plane, Vector2D[] loop, boolean reverse) {
            final FloatArray tempVerts = new FloatArray();
            for (Vector2D v : loop) {
                addVertex(plane.toSpace(v), plane.getNormal().scalarMultiply(reverse ? -1 : 1), this.vertices);
                tempVerts.add((float) v.getX());
                tempVerts.add((float) v.getY());
            }


            Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "vertex count " + tempVerts.size / 2);

            final ShortArray tempIndices = triangulator.computeTriangles(tempVerts);
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
