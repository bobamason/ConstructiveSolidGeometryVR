package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.geometry.euclidean.twod.Segment;
import org.apache.commons.math3.geometry.euclidean.twod.SubLine;
import org.apache.commons.math3.geometry.partitioning.BSPTree;
import org.apache.commons.math3.geometry.partitioning.BSPTreeVisitor;
import org.apache.commons.math3.geometry.partitioning.BoundaryAttribute;

/**
 * Created by Bob on 5/26/2017.
 */

public class PolyhedronsetToLineModel {

    public static ModelInstance convert(PolyhedronsSet polyhedronsSet) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        polyhedronsSet.getTree(true).visit(new LineVisitor(vertices, indices));

        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.ColorPacked());
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());

        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.part("", mesh, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.WHITE)));

        return new ModelInstance(modelBuilder.end());
    }

    private static class LineVisitor implements BSPTreeVisitor<Euclidean3D> {
        private final FloatArray vertices;
        private final ShortArray indices;
        private int index = 0;
        private float c = 0;

        public LineVisitor(FloatArray vertices, ShortArray indices) {

            this.vertices = vertices;
            this.indices = indices;
        }

        @Override
        public BSPTreeVisitor.Order visitOrder(BSPTree<Euclidean3D> node) {
            return BSPTreeVisitor.Order.PLUS_SUB_MINUS;
        }

        @Override
        public void visitInternalNode(BSPTree<Euclidean3D> node) {
            BoundaryAttribute<Euclidean3D> attribute =
                    (BoundaryAttribute<Euclidean3D>) node.getAttribute();
            if (attribute.getPlusOutside() != null) {
                final SubPlane plusOutside = (SubPlane) attribute.getPlusOutside();
                handleSubPlane(plusOutside, false);
            }
            if (attribute.getPlusInside() != null) {
                final SubPlane plusInside = (SubPlane) attribute.getPlusInside();
                handleSubPlane(plusInside, true);
            }
        }

        private void handleSubPlane(final SubPlane subPlane, final boolean reverse) {
            c = 0f;
            final Plane plane = (Plane) subPlane.getHyperplane();
            final BSPTree<Euclidean2D> tree = subPlane.getRemainingRegion().getTree(true);
            tree.visit(new BSPTreeVisitor<Euclidean2D>() {

                @Override
                public Order visitOrder(BSPTree<Euclidean2D> node) {
                    return reverse ? Order.PLUS_SUB_MINUS : Order.MINUS_SUB_PLUS;
                }

                @Override
                public void visitInternalNode(BSPTree<Euclidean2D> node) {
                    final BoundaryAttribute<Euclidean2D> attribute = (BoundaryAttribute<Euclidean2D>) node.getAttribute();
                    final SubLine plusOutside = (SubLine) attribute.getPlusOutside();
                    handleSubLine(plusOutside, plane);
                    final SubLine plusInside = (SubLine) attribute.getPlusInside();
                    handleSubLine(plusInside, plane);
                }

                @Override
                public void visitLeafNode(BSPTree<Euclidean2D> node) {

                }
            });
        }

        @Override
        public void visitLeafNode(BSPTree<Euclidean3D> node) {

        }

        private void handleSubLine(SubLine subLine, Plane plane) {
            if (subLine == null) return;
            for (Segment segment : subLine.getSegments()) {
                final Vector3D start = plane.toSpace(segment.getStart());
                final Vector3D end = plane.toSpace(segment.getEnd());
                final Vector3D nor = plane.getNormal().scalarMultiply(0.25f);
                vertices.add((float) (start.getX() + nor.getX()));
                vertices.add((float) (start.getY() + nor.getY()));
                vertices.add((float) (start.getZ() + nor.getZ()));
                c += 0.125f;
                c %= 1f;
                vertices.add(new Color(1f - c, 0f, c, 1).toFloatBits());
                indices.add(index++);

                vertices.add((float) (end.getX() + nor.getX()));
                vertices.add((float) (end.getY() + nor.getY()));
                vertices.add((float) (end.getZ() + nor.getZ()));
//                final float c2 = (index * 0.1f) % 1f;
                vertices.add(new Color(1f - c, 0f, c, 1).toFloatBits());
                indices.add(index++);
            }
        }
    }
}
