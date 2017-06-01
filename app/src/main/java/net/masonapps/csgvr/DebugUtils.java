package net.masonapps.csgvr;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

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

public class DebugUtils {

    public static void renderPolygonTree(final PolyhedronsSet polyhedronsSet, final ShapeRenderer shapeRenderer) {
        polyhedronsSet.getTree(true).visit(new BSPTreeVisitor<Euclidean3D>() {
            @Override
            public Order visitOrder(BSPTree<Euclidean3D> node) {
                return Order.PLUS_SUB_MINUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Euclidean3D> node) {
                BoundaryAttribute<Euclidean3D> attribute =
                        (BoundaryAttribute<Euclidean3D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    final SubPlane plusOutside = (SubPlane) attribute.getPlusOutside();
                    renderBSPTree2D(shapeRenderer, plusOutside, false);
                }
                if (attribute.getPlusInside() != null) {
                    final SubPlane plusInside = (SubPlane) attribute.getPlusInside();
                    renderBSPTree2D(shapeRenderer, plusInside, true);
                }
            }

            @Override
            public void visitLeafNode(BSPTree<Euclidean3D> node) {

            }
        });
    }

    private static void renderBSPTree2D(final ShapeRenderer shapeRenderer, final SubPlane subPlane, final boolean reverse) {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final BSPTree<Euclidean2D> tree = subPlane.getRemainingRegion().getTree(true);
        tree.visit(new BSPTreeVisitor<Euclidean2D>() {

            float n = 0;

            @Override
            public Order visitOrder(BSPTree<Euclidean2D> node) {
                return reverse ? Order.PLUS_SUB_MINUS : Order.SUB_MINUS_PLUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Euclidean2D> node) {
                n += 1f;
                final Color color = new Color(1f - n * 0.1f, 1f - n * 0.1f * 0.5f, n * 0.1f, 1f);
                shapeRenderer.begin();
                final BoundaryAttribute<Euclidean2D> attribute = (BoundaryAttribute<Euclidean2D>) node.getAttribute();
                final SubLine plusOutside = (SubLine) attribute.getPlusOutside();
                renderSubLine(plusOutside, plane, color);
                final SubLine plusInside = (SubLine) attribute.getPlusInside();
                renderSubLine(plusInside, plane, color);
                shapeRenderer.end();
            }

            private void renderSubLine(org.apache.commons.math3.geometry.euclidean.twod.SubLine subLine, Plane plane, Color color) {
                if (subLine == null) return;
                shapeRenderer.setColor(color);
                for (Segment segment : subLine.getSegments()) {
                    final Vector3D start = plane.toSpace(segment.getStart());
                    final Vector3D end = plane.toSpace(segment.getEnd());
                    shapeRenderer.line((float) start.getX(), (float) start.getY(), (float) start.getZ(), (float) end.getX(), (float) end.getY(), (float) end.getZ());
                }
            }

            @Override
            public void visitLeafNode(BSPTree<Euclidean2D> node) {

            }
        });
    }

    public static Model createEdgeModel(Model model, Color color) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        final MeshPartBuilder part = modelBuilder.part("edges", GL20.GL_LINES, VertexAttributes.Usage.Position, new Material(ColorAttribute.createDiffuse(color)));
        for (Mesh mesh : model.meshes) {
            final int vertexSize = mesh.getVertexSize() / 4;
            final float[] vertices = new float[mesh.getNumVertices() * vertexSize];
            mesh.getVertices(vertices);
            final short[] indices = new short[mesh.getNumIndices()];
            mesh.getIndices(indices);
            for (int i = 0; i < indices.length; i += 3) {
                final int a = indices[i] * vertexSize;
                final int b = indices[i + 1] * vertexSize;
                final int c = indices[i + 2] * vertexSize;
                part.line(vertices[a], vertices[a + 1], vertices[a + 2], vertices[b], vertices[b + 1], vertices[b + 2]);
                part.line(vertices[b], vertices[b + 1], vertices[b + 2], vertices[c], vertices[c + 1], vertices[c + 2]);
                part.line(vertices[c], vertices[c + 1], vertices[c + 2], vertices[a], vertices[a + 1], vertices[a + 2]);
            }
        }
        return modelBuilder.end();
    }
}
