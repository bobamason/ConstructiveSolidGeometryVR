package net.masonapps.csgvr.csg;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 9/4/2017.
 */

public class BspTree {

    public static class Node {

        public Plane divider = new Plane();
        @Nullable
        public Node frontNode = null;
        @Nullable
        public Node backNode = null;
        public List<CsgTriangle> triangles = new ArrayList<>();

        public Node(List<CsgTriangle> triangles) {
            if (triangles.isEmpty())
                throw new IllegalArgumentException("triangles list must have at least one triangle");
            divider.set(triangles.get(0).plane);
            this.triangles.add(triangles.get(0));
            if (triangles.size() == 1)
                return;
            final List<CsgTriangle> front = new ArrayList<>();
            final List<CsgTriangle> back = new ArrayList<>();

            final float[] array = new float[CsgTriangle.ARRAY_LENGTH];
            final Intersector.SplitTriangle splitTriangle = new Intersector.SplitTriangle(CsgVertex.ARRAY_LENGTH);
            for (int j = 1; j < triangles.size(); j++) {
                Intersector.splitTriangle(triangles.get(j).toArray(array, 0), divider, splitTriangle);
                for (int i = 0; i < splitTriangle.numFront; i++) {
                    front.add(CsgTriangle.fromArray(splitTriangle.front, i * CsgTriangle.ARRAY_LENGTH));
                }
                for (int i = 0; i < splitTriangle.numBack; i++) {
                    back.add(CsgTriangle.fromArray(splitTriangle.back, i * CsgTriangle.ARRAY_LENGTH));
                }
            }

            if (!front.isEmpty())
                frontNode = new Node(front);
            if (!back.isEmpty())
                backNode = new Node(back);
        }

        public void invert() {
            for (CsgTriangle triangle : triangles) {
                triangle.flip();
            }
            divider.normal.scl(-1);
            divider.d = -divider.d;
            if (frontNode != null)
                frontNode.invert();
            if (backNode != null)
                backNode.invert();
            final Node tmp = frontNode;
            frontNode = backNode;
            backNode = tmp;
        }

        public List<CsgTriangle> clipTriangles(List<CsgTriangle> clipTriangles) {
            if (divider.normal.epsilonEquals(Vector3.Zero, 1e-5f))
                return clipTriangles;
            List<CsgTriangle> front = new ArrayList<>();
            List<CsgTriangle> back = new ArrayList<>();
            final float[] array = new float[CsgTriangle.ARRAY_LENGTH];
            final Intersector.SplitTriangle splitTriangle = new Intersector.SplitTriangle(CsgVertex.ARRAY_LENGTH);
            for (CsgTriangle triangle : clipTriangles) {
                Intersector.splitTriangle(triangle.toArray(array, 0), divider, splitTriangle);
                for (int i = 0; i < splitTriangle.numFront; i++) {
                    front.add(CsgTriangle.fromArray(splitTriangle.front, i * CsgTriangle.ARRAY_LENGTH));
                }
                for (int i = 0; i < splitTriangle.numBack; i++) {
                    back.add(CsgTriangle.fromArray(splitTriangle.back, i * CsgTriangle.ARRAY_LENGTH));
                }
            }

            if (frontNode != null)
                front = frontNode.clipTriangles(front);
            if (backNode != null) {
                back = backNode.clipTriangles(back);
                front.addAll(back);
            }
            return front;
        }

        public void clipTo(Node other) {
            triangles = other.clipTriangles(triangles);
            if (frontNode != null)
                frontNode.clipTo(other);
            if (backNode != null)
                backNode.clipTo(other);
        }

        public List<CsgTriangle> getAllTriangles() {
            final List<CsgTriangle> outTriangles = new ArrayList<>();
            outTriangles.addAll(triangles);
            if (frontNode != null)
                outTriangles.addAll(frontNode.getAllTriangles());
            if (backNode != null)
                outTriangles.addAll(backNode.getAllTriangles());
            return outTriangles;
        }
    }
}
