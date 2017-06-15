package net.masonapps.csgvr;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.csg.CSG;
import net.masonapps.csgvr.csg.CSGPolygon;
import net.masonapps.csgvr.csg.Vertex;

/**
 * Created by Bob on 6/1/2017.
 */

public class SimpleCSGTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private CameraInputController cameraController;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private CSG csg1;
    private CSG csg2;
    private DirectionalLight light;
    private ModelBuilder modelBuilder;

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
        light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        environment.add(light);

        modelBuilder = new ModelBuilder();

        final ModelInstance s1 = new ModelInstance(createModel(modelBuilder, 1f));
        final ModelInstance s2 = new ModelInstance(createModel(modelBuilder, 0.5f), 0.75f, 0.75f, 0.75f);
        csg1 = CSG.fromMesh(s1.model.meshes.get(0), s1.transform);
        csg2 = CSG.fromMesh(s2.model.meshes.get(0), s2.transform);
//        instances.add(s1);
//        instances.add(s2);


        final Material material = new Material(ColorAttribute.createDiffuse(Color.GOLD), ColorAttribute.createSpecular(Color.GOLD), FloatAttribute.createShininess(50f));
        final ModelInstance modelInstance = CSG.toModelInstance(csg1, material);

        instances.add(modelInstance);

//            modelBuilder.begin();
//            final Mesh mesh2 = CSG.toMesh();
//            modelBuilder.part("mesh", mesh2, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.OLIVE)));
//            instances.add(new ModelInstance(modelBuilder.end()));
    }

    @Override
    public void resize(int width, int height) {
        camera.position.set(2f, 1f, 2f);
        camera.near = 0.1f;
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
        light.setDirection(camera.direction);
        modelBatch.begin(camera);
//        modelBatch.render(instances, environment);
        modelBatch.render(instances);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        renderCSGTree(csg1, Color.WHITE);
        if (Gdx.input.isTouched()) {
            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        }
    }

    private void renderCSGTree(CSG csg, Color color) {
        final float s = 0.1f;
        shapeRenderer.begin();
        shapeRenderer.setColor(color);
        final Array<CSGPolygon> polygons = new Array<>();
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
