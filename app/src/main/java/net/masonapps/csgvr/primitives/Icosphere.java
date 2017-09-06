package net.masonapps.csgvr.primitives;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.csgvr.modeling.Solid;
import net.masonapps.csgvr.utils.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 6/13/2017.
 */

public class Icosphere extends Primitive {


    List<int[]> facets = new ArrayList<>();
    private float radius = 1f;
    private int subdivisions = 3;
    private Matrix4 transform = new Matrix4();

    public Icosphere(float radius) {
        this.radius = radius;
    }

    @Override
    protected ModelInstance createModelInstance() {
        return null;
    }

    @Override
    public Solid createSolid() {
        List<Vector3> tempVerts = new ArrayList<>();
        List<Vector3D> vertices = new ArrayList<>();

        float t = (float) (0.5 + Math.sqrt(5.0) / 2.0);
        transform.scale(radius, radius, radius);

        tempVerts.add(new Vector3(-1, t, 0).nor());
        tempVerts.add(new Vector3(1, t, 0).nor());
        tempVerts.add(new Vector3(-1, -t, 0).nor());
        tempVerts.add(new Vector3(1, -t, 0).nor());

        tempVerts.add(new Vector3(0, -1, t).nor());
        tempVerts.add(new Vector3(0, 1, t).nor());
        tempVerts.add(new Vector3(0, -1, -t).nor());
        tempVerts.add(new Vector3(0, 1, -t).nor());

        tempVerts.add(new Vector3(t, 0, -1).nor());
        tempVerts.add(new Vector3(t, 0, 1).nor());
        tempVerts.add(new Vector3(-t, 0, -1).nor());
        tempVerts.add(new Vector3(-t, 0, 1).nor());

        facets.add(new int[]{0, 11, 5});
        facets.add(new int[]{0, 5, 1});
        facets.add(new int[]{0, 1, 7});
        facets.add(new int[]{0, 7, 10});
        facets.add(new int[]{0, 10, 11});

        facets.add(new int[]{1, 5, 9});
        facets.add(new int[]{5, 11, 4});
        facets.add(new int[]{11, 10, 2});
        facets.add(new int[]{10, 7, 6});
        facets.add(new int[]{7, 1, 8});

        facets.add(new int[]{3, 9, 4});
        facets.add(new int[]{3, 4, 2});
        facets.add(new int[]{3, 2, 6});
        facets.add(new int[]{3, 6, 8});
        facets.add(new int[]{3, 8, 9});

        facets.add(new int[]{4, 9, 5});
        facets.add(new int[]{2, 4, 11});
        facets.add(new int[]{6, 2, 10});
        facets.add(new int[]{8, 6, 7});
        facets.add(new int[]{9, 8, 1});

        for (int i = 0; i < subdivisions; i++) {
            final ArrayList<int[]> list = new ArrayList<>();
            list.addAll(facets);
            subdivide(tempVerts, list);
        }

        for (int i = 0; i < tempVerts.size(); i++) {
            vertices.add(ConversionUtils.convertVector(tempVerts.get(i).mul(transform)));
        }
        tempVerts.clear();
        return new Solid(new PolyhedronsSet(vertices, facets, tolerance));
    }

    private void subdivide(List<Vector3> vertices, List<int[]> faces) {
        int i = 0;
        for (int[] facet : faces) {
            final Vector3 a = vertices.get(facet[0]);
            final Vector3 b = vertices.get(facet[1]);
            final Vector3 c = vertices.get(facet[2]);

            final Vector3 ab = new Vector3(a).lerp(b, 0.5f).nor();
            final Vector3 bc = new Vector3(b).lerp(c, 0.5f).nor();
            final Vector3 ca = new Vector3(c).lerp(a, 0.5f).nor();

            int iab = vertices.indexOf(ab);
            if (iab == -1) {
                iab = i++;
                vertices.add(ab);
            }

            int ibc = vertices.indexOf(bc);
            if (ibc == -1) {
                ibc = i++;
                vertices.add(bc);
            }

            int ica = vertices.indexOf(ca);
            if (ica == -1) {
                ica = i++;
                vertices.add(ca);
            }

            int ia = vertices.indexOf(a);
            if (ia == -1) {
                ia = i++;
                vertices.add(a);
            }

            int ib = vertices.indexOf(b);
            if (ib == -1) {
                ib = i++;
                vertices.add(b);
            }

            int ic = vertices.indexOf(c);
            if (ic == -1) {
                ic = i++;
                vertices.add(c);
            }

            facets.add(new int[]{ia, iab, ica});
            facets.add(new int[]{ib, ibc, iab});
            facets.add(new int[]{ic, ica, ibc});
            facets.add(new int[]{iab, ibc, ica});
        }
    }
}
