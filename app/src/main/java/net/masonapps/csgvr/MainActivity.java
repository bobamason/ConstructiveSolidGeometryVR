package net.masonapps.csgvr;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.csg.CSG;
import net.masonapps.csgvr.csg.CSGPolygon;
import net.masonapps.csgvr.csg.Vertex;

public class MainActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new CSGTest());
    }

    private static class CSGTest implements ApplicationListener {

        final Vector3 center = new Vector3();
        private final Array<ModelInstance> instances = new Array<>();
        private final PerspectiveCamera camera = new PerspectiveCamera();
        private CameraInputController cameraController;
        private ModelBatch modelBatch;
        private Environment environment;
        private ShapeRenderer shapeRenderer;
        private CSG csg1;
        private CSG csg2;
        private Array<CSGPolygon> polygons = new Array<>();
        private CSG union;
        private CSG difference;
        private CSG intersection;

        private static Model createModel(ModelBuilder modelBuilder, float radius) {
            modelBuilder.begin();
            final MeshPartBuilder part = modelBuilder.part("model", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, new Material(ColorAttribute.createDiffuse(Color.BLUE)));
            BoxShapeBuilder.build(part, radius * 2f, radius * 2f, radius * 2f);
            return modelBuilder.end();
        }

        @Override
        public void create() {
            cameraController = new CameraInputController(camera);
            Gdx.input.setInputProcessor(cameraController);
            environment = new Environment();
            modelBatch = new ModelBatch();
            shapeRenderer = new ShapeRenderer();
            shapeRenderer.setAutoShapeType(true);
            environment.set(new ColorAttribute(ColorAttribute.Ambient, Color.GRAY));
            final DirectionalLight light = new DirectionalLight();
            light.setDirection(new Vector3(1, -1, -1).nor());
            environment.add(light);

            final ModelBuilder modelBuilder = new ModelBuilder();

            final ModelInstance s1 = new ModelInstance(createModel(modelBuilder, 1f));
            final ModelInstance s2 = new ModelInstance(createModel(modelBuilder, 0.5f), 0.75f, 0.75f, 0.75f);
//            instances.add(s1);
//            instances.add(s2);
//            final BSPTreeNode boxTree = ShapeUtils.createBoxTree();
//            csg1 = new CSG(boxTree);
            csg1 = CSG.fromMesh(s1.model.meshes.get(0), s1.transform);
            csg2 = CSG.fromMesh(s2.model.meshes.get(0), s2.transform);
            union = csg1.union(csg2);
            difference = csg1.subtract(csg2);
            intersection = csg1.intersect(csg2);

//            modelBuilder.begin();
//            final Mesh mesh = CSG.toMesh(csg1);
//            modelBuilder.part("mesh", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.SKY)));
//            instances.add(new ModelInstance(modelBuilder.end()));

//            modelBuilder.begin();
//            final Mesh mesh2 = CSG.toMesh(csg2);
//            modelBuilder.part("mesh", mesh2, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.OLIVE)));
//            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh lineMesh = CSG.toLineMesh(csg1);
            modelBuilder.part("mesh", lineMesh, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.BLUE)));
            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh lineMesh2 = CSG.toLineMesh(csg2);
            modelBuilder.part("mesh", lineMesh2, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.LIME)));
            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh uMesh = CSG.toMesh(union);
            modelBuilder.part("mesh", uMesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.BROWN)));
            instances.add(new ModelInstance(modelBuilder.end()));

//            modelBuilder.begin();
//            final Mesh uLineMesh = CSG.toLineMesh(union);
//            modelBuilder.part("mesh", uLineMesh, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
//            instances.add(new ModelInstance(modelBuilder.end()));
        }

        @Override
        public void resize(int width, int height) {
            camera.position.set(0, 0.5f, 4f);
            camera.up.set(0, 1, 0);
            camera.lookAt(0, 0, 0);
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }

        @Override
        public void render() {
            Gdx.gl.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
            Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            modelBatch.begin(camera);
//            modelBatch.render(instances, environment);
            modelBatch.render(instances);
            modelBatch.end();

            shapeRenderer.setProjectionMatrix(camera.combined);
            renderCSGTree(csg1, Color.BLUE);
            renderCSGTree(csg2, Color.LIME);
            renderCSGTree(union, Color.WHITE);
//            renderCSGTree(intersection, Color.YELLOW);
//            renderCSGTree(difference, Color.RED);
        }

        private void renderCSGTree(CSG csg, Color color) {
            final float s = 0.1f;
            shapeRenderer.begin();
            shapeRenderer.setColor(color);
            polygons.clear();
            csg.tree.getAllPolygons(polygons);
            for (CSGPolygon polygon : polygons) {
                for (Vertex vertex : polygon.vertices) {
                    final Vector3 v = vertex.position;
                    final Vector3 n;
                    if (polygon.plane != null) {
                        n = polygon.plane.normal;
                        shapeRenderer.line(v.x, v.y, v.z, v.x + n.x * s, v.y + n.y * s, v.z + n.z * s);
                    }
                }
            }
            shapeRenderer.end();
        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {
            modelBatch.dispose();
            shapeRenderer.dispose();
        }
    }
}
