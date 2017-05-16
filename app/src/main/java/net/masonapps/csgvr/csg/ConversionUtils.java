package net.masonapps.csgvr.csg;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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

    @SuppressWarnings("unchecked")
    public static Mesh polyhedronsSetToMesh(PolyhedronsSet polyhedronsSet) {
        final FloatArray vertices = new FloatArray();
        final ShortArray indices = new ShortArray();

        polyhedronsSet.getTree(true).visit(new BSPTreeVisitor<Euclidean3D>() {
            @Override
            public Order visitOrder(BSPTree<Euclidean3D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            @Override
            public void visitInternalNode(BSPTree<Euclidean3D> node) {
                int index = 0;
                BoundaryAttribute<Euclidean3D> attribute =
                        (BoundaryAttribute<Euclidean3D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    final SubPlane plusOutside = (SubPlane) attribute.getPlusOutside();
                    final Plane plane1 = (Plane) plusOutside.getHyperplane();
                    final Vector2D[][] vertices1 = ((PolygonsSet) plusOutside.getRemainingRegion()).getVertices();
                    for (int i = 0; i < vertices1[0].length; i++) {
                        final Vector3D pos = plane1.toSpace(vertices1[0][i]);
                        final Vector3D normal = plane1.getNormal();
                        vertices.add((float) pos.getX());
                        vertices.add((float) pos.getY());
                        vertices.add((float) pos.getZ());
                        vertices.add((float) normal.getX());
                        vertices.add((float) normal.getY());
                        vertices.add((float) normal.getZ());
                        indices.add(index);
                        index++;
                    }
                }
                if (attribute.getPlusInside() != null) {
                    final SubPlane plusInside = (SubPlane) attribute.getPlusInside();
                    final Plane plane2 = (Plane) plusInside.getHyperplane();
                    final Vector2D[][] vertices2 = ((PolygonsSet) plusInside.getRemainingRegion()).getVertices();
                    for (int i = 0; i < vertices2[0].length; i++) {
                        final Vector3D pos = plane2.toSpace(vertices2[0][i]);
                        final Vector3D normal = plane2.getNormal();
                        vertices.add((float) pos.getX());
                        vertices.add((float) pos.getY());
                        vertices.add((float) pos.getZ());
                        vertices.add((float) normal.getX());
                        vertices.add((float) normal.getY());
                        vertices.add((float) normal.getZ());
                        indices.add(index);
                        index++;
                    }
                }
            }

            @Override
            public void visitLeafNode(BSPTree<Euclidean3D> node) {
            }
        });
        final Mesh mesh = new Mesh(false, vertices.size, indices.size, VertexAttribute.Position(), VertexAttribute.Normal());
        mesh.setIndices(indices.toArray());
        final float[] vertArray = vertices.toArray();
        mesh.setVertices(vertArray);
        return mesh;
    }
}
