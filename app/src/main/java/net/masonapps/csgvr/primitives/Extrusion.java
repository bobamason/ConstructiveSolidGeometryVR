package net.masonapps.csgvr.primitives;

import android.support.annotation.NonNull;

import com.badlogic.gdx.math.Matrix4;

import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;
import org.apache.commons.math3.geometry.partitioning.Hyperplane;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.apache.commons.math3.geometry.partitioning.SubHyperplane;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 5/19/2017.
 */

public class Extrusion extends Primitive {

    private double depth;
    private SubPlane subPlane;

    public Extrusion(SubPlane subPlane) {
        this.subPlane = subPlane;
        this.depth = 0.0;
    }

    public Extrusion(SubPlane subPlane, double depth) {
        this.subPlane = subPlane;
        this.depth = depth;
    }
    
    @Override
    protected PolyhedronsSet createPolyhedronsSet(Matrix4 transform) {
        return createUsingSubHyperplanes();
//        return createUsingFacets();
    }

    @NonNull
    private PolyhedronsSet createUsingFacets() {
        List<Vector3D> vertices = new ArrayList<>();
        List<int[]> facets = new ArrayList<>();

        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();

        final List<SubHyperplane<Euclidean3D>> subHyperplanes = new ArrayList<>();
        final Vector3D offset = plane.getNormal().scalarMultiply(depth);
        Vector3D point = null;
        final Vector2D[][] loops = remainingRegion.getVertices();

        return new PolyhedronsSet(vertices, facets, tolerance);
    }

    @NonNull
    private PolyhedronsSet createUsingSubHyperplanes() {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final PolygonsSet remainingRegion = (PolygonsSet) subPlane.getRemainingRegion();

        final ArrayList<Hyperplane<Euclidean3D>> planes = new ArrayList<>();
        final Vector3D normal = plane.getNormal();
        final Vector3D offset = normal.scalarMultiply(depth);
        final Vector2D[][] loops = remainingRegion.getVertices();

        remainingRegion.getTree(true).visit(new BSPTreeVisitor<Euclidean2D>() {
            @Override
            public Order visitOrder(BSPTree<Euclidean2D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Euclidean2D> node) {
                final BoundaryAttribute<Euclidean2D> attribute = (BoundaryAttribute<Euclidean2D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    final SubLine plusOutside = (SubLine) attribute.getPlusOutside();
                    final Vector3D p1 = plane.toSpace(((Line) plusOutside.getHyperplane()).toSpace(new Vector1D(0)));
                    final Vector3D p2 = plane.toSpace(((Line) plusOutside.getHyperplane()).toSpace(new Vector1D(1)));
                    planes.add(new Plane(p1, p2, p1.add(normal), tolerance));
                }
                if (attribute.getPlusInside() != null) {
                    final SubLine plusInside = (SubLine) attribute.getPlusOutside();
                    final Vector3D p1 = plane.toSpace(((Line) plusInside.getHyperplane()).toSpace(new Vector1D(0)));
                    final Vector3D p2 = plane.toSpace(((Line) plusInside.getHyperplane()).toSpace(new Vector1D(1)));
                    planes.add(new Plane(p1.add(normal), p2, p1, tolerance));
                }
            }

            @Override
            public void visitLeafNode(BSPTree<Euclidean2D> node) {

            }
        });

        for (Vector2D[] loop : loops) {
            if (loop.length < 3 || loop[0] == null)
                continue;
//            for (int i = 0; i < loop.length; i++) {
//                Vector3D p1, p2, p3;
//                p1 = plane.toSpace(loop[i]);
//                p2 = plane.toSpace(loop[(i + 1) % loop.length]);
//                p3 = p2.add(offset);
////            }
//                final Plane hyperplane = new Plane(p1, p2, p3, tolerance);
//                planes.add(hyperplane);
//            }
            final Vector3D v1 = plane.toSpace(loop[0]);
            final Vector3D v2 = plane.toSpace(loop[1]);
            final Vector3D v3 = plane.toSpace(loop[2]);
            if (depth > tolerance) {
                final Plane hyperplane1 = new Plane(v3, v2, v1, tolerance);
                planes.add(hyperplane1);
                final Plane hyperplane2 = new Plane(v1.add(offset), v2.add(offset), v3.add(offset), tolerance);
                planes.add(hyperplane2);
                } else {
                final Plane hyperplane1 = new Plane(v1, v2, v3, tolerance);
                planes.add(hyperplane1);
                final Plane hyperplane2 = new Plane(v3.add(offset), v2.add(offset), v1.add(offset), tolerance);
                planes.add(hyperplane2);
                }
        }
        final Hyperplane<Euclidean3D>[] hyperplaneArray = new Hyperplane[planes.size()];
        for (int i = 0; i < hyperplaneArray.length; i++) {
            hyperplaneArray[i] = planes.get(i);
        }
        return (PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(hyperplaneArray);
    }

    public SubPlane getSubPlane() {
        return subPlane;
    }

    public void setSubPlane(SubPlane subPlane) {
        this.subPlane = subPlane;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }
}
