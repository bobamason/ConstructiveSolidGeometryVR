package net.masonapps.csgvr.utils;

import android.util.Log;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.Region;

/**
 * Created by Bob on 6/28/2017.
 */
class MeshCreationTreeVisitor implements BSPTreeVisitor<Euclidean3D> {

    public static final int VERTEX_SIZE = 6;
    private final FloatArray vertices;
    private final ShortArray indices;
    private final DelaunayTriangulator triangulator;
    private final Vector3D center;
    private int startIndex;

    public MeshCreationTreeVisitor(FloatArray vertices, ShortArray indices, Vector3D center) {
        this.vertices = vertices;
        this.indices = indices;
        this.center = center;
        triangulator = new DelaunayTriangulator();
        startIndex = 0;
    }

    private static void addVertex(Vector3D position, Vector3D normal, FloatArray vertices) {
        vertices.add((float) position.getX());
        vertices.add((float) position.getY());
        vertices.add((float) position.getZ());
        vertices.add((float) normal.getX());
        vertices.add((float) normal.getY());
        vertices.add((float) normal.getZ());
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
        final Vector2D[][] loops = remainingRegion.getVertices();

        Log.i(MeshCreationTreeVisitor.class.getSimpleName() + "->handleSubPlane", "loop count " + loops.length);

        final FloatArray tempVerts = new FloatArray();
        for (Vector2D[] loop : loops) {

            for (Vector2D v : loop) {
                addVertex(plane.toSpace(v).subtract(center), plane.getNormal().scalarMultiply(reverse ? -1 : 1), this.vertices);
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
