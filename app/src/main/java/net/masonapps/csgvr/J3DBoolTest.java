package net.masonapps.csgvr;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.ui.Grid;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by Bob on 6/21/2017.
 */

public class J3DBoolTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    //    private TransformManipulator transformManipulator;
    private Grid grid;

    @Override
    public void create() {
        final CameraInputController cameraController = new CameraInputController(camera);
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

        grid = Grid.newInstance(10f);
        grid.setRenderingEnabled(true);
        grid.setToPlane(new Plane(new Vector3D(0, 1, 0), 1e-10));

        final Material material = new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(50f));
//        transformManipulator = new TransformManipulator(modelInstance.transform);
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
        update();

        Gdx.gl.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glClearColor(0.15f, 0.25f, 0.35f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        light.setDirection(camera.direction);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        if (grid.isRenderingEnabled())
            modelBatch.render(grid.modelInstance);
//        transformManipulator.render(modelBatch);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    private void update() {
        if (Gdx.input.isTouched()) {
            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
//            transformManipulator.rayTest(ray);
        }
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
