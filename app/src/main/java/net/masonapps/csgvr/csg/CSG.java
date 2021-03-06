package net.masonapps.csgvr.csg;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Bob on 9/4/2017.
 */

public class CSG {

    private List<CSGPolygon> polygons;

    public CSG() {
        polygons = new ArrayList<>();
    }

    public CSG(List<CSGPolygon> polygons) {
        this.polygons = polygons;
    }

    public static CSG cube(Vector3 center, float r) {
        Matrix4 transform = new Matrix4().translate(center).scale(r * 2f, r * 2f, r * 2f);
        ModelBuilder mb = new ModelBuilder();
        Model model = mb.createBox(1f, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        Mesh mesh = model.meshes.get(0);
        return new CSG(meshToPolygons(mesh, transform));
    }

    public static List<CSGPolygon> meshToPolygons(Mesh mesh, Matrix4 transform) {
        List<CSGPolygon> polygons = new ArrayList<>();
        final int vertexSize = mesh.getVertexSize() / 4;
        float[] vertices = new float[mesh.getNumVertices() * vertexSize];
        mesh.getVertices(vertices);
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);
        for (int i = 0; i < indices.length; i += 3) {
            final ArrayList<CSGVertex> csgVertices = new ArrayList<>(3);
            csgVertices.add(createVertex(vertices, indices[i] * vertexSize, transform));
            csgVertices.add(createVertex(vertices, indices[i + 1] * vertexSize, transform));
            csgVertices.add(createVertex(vertices, indices[i + 2] * vertexSize, transform));
            polygons.add(new CSGPolygon(csgVertices, new SharedProperties()));
        }
        return polygons;
    }

    private static CSGVertex createVertex(float[] vertices, int start, Matrix4 transform) {
        final CSGVertex vertex = new CSGVertex();
        vertex.position.set(vertices[start], vertices[start + 1], vertices[start + 2]).mul(transform);
        vertex.normal.set(vertices[start + 3], vertices[start + 4], vertices[start + 5]).rot(transform);
        return vertex;
    }

    public static CSG cylinder(Vector3 center, float height, float radius) {
        Matrix4 transform = new Matrix4().translate(center);
        List<CSGPolygon> polygons = new ArrayList<>();
        List<CSGVertex> top = new ArrayList<>();
        final float aStep = 360f / 16;
        for (int i = 0; i < 16; i++) {
            final CSGVertex v = new CSGVertex();
            v.position.set(MathUtils.cosDeg(i * aStep) * radius, height / 2f, -MathUtils.sinDeg(i * aStep) * radius).mul(transform);
            v.normal.set(0, 1, 0).rot(transform).nor();
            top.add(v);
        }
        polygons.add(new CSGPolygon(top, new SharedProperties()));

        List<CSGVertex> bottom = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            final CSGVertex v = new CSGVertex();
            v.position.set(MathUtils.cosDeg(360f - i * aStep) * radius, -height / 2f, -MathUtils.sinDeg(360f - i * aStep) * radius).mul(transform);
            v.normal.set(0, -1, 0).rot(transform).nor();
            bottom.add(v);
        }
        polygons.add(new CSGPolygon(bottom, new SharedProperties()));

        for (int i = 0; i < 16; i++) {
            final int j = (i + 1) % 16;
            List<CSGVertex> verts = new ArrayList<>();

            final CSGVertex v1 = new CSGVertex();
            v1.position.set(MathUtils.cosDeg(i * aStep) * radius, height / 2f, -MathUtils.sinDeg(i * aStep) * radius).mul(transform);
            v1.normal.set(v1.position.x, 0, v1.position.z).rot(transform).nor();
            verts.add(v1);

            final CSGVertex v2 = new CSGVertex();
            v2.position.set(v1.position).sub(0, height, 0).mul(transform);
            v2.normal.set(v2.position.x, 0, v2.position.z).rot(transform).nor();
            verts.add(v2);

            final CSGVertex v3 = new CSGVertex();
            v3.position.set(MathUtils.cosDeg(j * aStep) * radius, -height / 2f, -MathUtils.sinDeg(j * aStep) * radius).mul(transform);
            v3.normal.set(v3.position.x, 0, v3.position.z).rot(transform).nor();
            verts.add(v3);

            final CSGVertex v4 = new CSGVertex();
            v4.position.set(v3.position).add(0, height, 0).mul(transform);
            v4.normal.set(v4.position.x, 0, v4.position.z).rot(transform).nor();
            verts.add(v4);

            polygons.add(new CSGPolygon(verts, new SharedProperties()));
        }

        return new CSG(polygons);
    }

    public static CSG sphere(Vector3 center, float r) {
        Matrix4 transform = new Matrix4().translate(center).scale(r * 2f, r * 2f, r * 2f);
        List<CSGPolygon> polygons = new ArrayList<>();
        List<CSGVertex> verts = new ArrayList<>();
        return new CSG(polygons);
    }

    public CSG copy() {
        final List<CSGPolygon> polygonList = polygons.stream().map(CSGPolygon::copy).collect(Collectors.toList());
        return new CSG(polygonList);
    }

    public List<CSGPolygon> getPolygons() {
        return polygons;
    }

    public CSG union(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return new CSG(a.allPolygons());
    }

    public CSG subtract(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        a.invert();
        return new CSG(a.allPolygons());
    }

    public CSG intersect(CSG csg) {
        final BspNode a = new BspNode(this.copy().polygons);
        final BspNode b = new BspNode(csg.copy().polygons);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.allPolygons());
        a.invert();
        return new CSG(a.allPolygons());
    }

    public CSG inverse() {
        final CSG copy = this.copy();
        copy.polygons.forEach(CSGPolygon::flip);
        return copy;
    }

    public Model toModel(ModelBuilder mb, Color color) {
        mb.begin();
        MeshPartBuilder builder = mb.part("", GL20.GL_TRIANGLES, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()), new Material(ColorAttribute.createAmbient(color), ColorAttribute.createDiffuse(color)));
        for (CSGPolygon polygon : polygons) {
            final List<CSGVertex> vertices = polygon.vertices;
            if (vertices.size() >= 3) {
                final Vector3 start = vertices.get(0).position;
                for (int i = 1; i < vertices.size(); i++) {
                    builder.triangle(start, vertices.get(i - 1).position, vertices.get(i).position);
                }
            }
        }
        return mb.end();
    }
}
