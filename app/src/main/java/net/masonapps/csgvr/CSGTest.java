package net.masonapps.csgvr;

import android.support.annotation.Nullable;

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

import net.masonapps.csgvr.primitives.Box;
import net.masonapps.csgvr.primitives.ConversionUtils;
import net.masonapps.csgvr.ui.TransformManipulator;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;

/**
 * Created by Bob on 5/19/2017.
 */
class CSGTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private CameraInputController cameraController;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    private PolyhedronsSet polyhedronsSet;
    @Nullable
    private SubPlane focusedPlane = null;
    private TransformManipulator transformManipulator;

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

        final PolyhedronsSet ps1 = new Box(2, 0.25, 1).createPolyhedronsSet();
        final PolyhedronsSet ps2 = new Box(0.25, 1.0, 0.25).createPolyhedronsSet().translate(new Vector3D(-0.5, 0, 0));
        final PolyhedronsSet ps3 = new Box(0.25, 1.0, 0.25).createPolyhedronsSet().translate(new Vector3D(0.5, 0, 0));
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().difference(new RegionFactory<Euclidean3D>().difference(ps1, ps2), ps3);

//        instances.add(PolyhedronsetToLineModel.convert(polyhedronsSet));

        final Material material = new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(50f));
        final ModelInstance modelInstance = ConversionUtils.polyhedronsSetToModelInstance(polyhedronsSet, material);
        instances.add(modelInstance);
        transformManipulator = new TransformManipulator(modelInstance.transform);
        instances.add(new ModelInstance(DebugUtils.createEdgeModel(modelInstance.model, Color.BLACK)));
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
        modelBatch.render(instances, environment);
//        modelBatch.render(instances);
        transformManipulator.render(modelBatch);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        if (Gdx.input.isTouched()) {
            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            transformManipulator.rayTest(ray);
            final Vector3D point = ConversionUtils.convertVector3(ray.origin);
            final Vector3D point2 = ConversionUtils.convertVector3(ray.direction).add(point);
            final SubPlane subPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
            if (subPlane != null)
                focusedPlane = subPlane;
//            if (focusedPlane != null) {
//                final Mesh mesh1 = ConversionUtils.polyhedronsSetToMesh(new Extrusion(focusedPlane, 0.2f).createPolyhedronsSet());
//                modelBuilder.part("mesh", mesh1, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.SKY)));
//                instances.add(new ModelInstance(modelBuilder.end()));
//            }
        }

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);
        
        if (focusedPlane != null) {
            shapeRenderer.setColor(Color.LIME);
            renderSubPlane(focusedPlane);
        }
    }

    private void renderSubPlane(SubPlane subPlane) {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final Vector2D[][] loops = ((PolygonsSet) subPlane.getRemainingRegion()).getVertices();
        shapeRenderer.begin();
        for (Vector2D[] loop : loops) {
            for (int i = 0; i < loop.length; i++) {
                Vector3D v1 = plane.toSpace(loop[i]);
                Vector3D v2 = plane.toSpace(loop[(i + 1) % loop.length]);
                shapeRenderer.line((float) v1.getX(), (float) v1.getY(), (float) v1.getZ(), (float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
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
