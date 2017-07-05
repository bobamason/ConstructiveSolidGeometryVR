package net.masonapps.csgvr;

import android.support.annotation.Nullable;

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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.ui.Grid;
import net.masonapps.csgvr.ui.TranslationManipulator;
import net.masonapps.csgvr.utils.ConversionUtils;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Created by Bob on 5/19/2017.
 */
class PolyhedronsTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private CameraInputController cameraController;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    private PolyhedronsSet polyhedronsSet;
    @Nullable
    private SubPlane focusedSubPlane = null;
    @Nullable
    private com.badlogic.gdx.math.Plane focusedPlane = null;
    private TranslationManipulator translationManipulator;
    private Grid grid;
    private boolean touched = false;
    private Vector3 hitPoint = new Vector3();

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

        grid = Grid.newInstance(5f);

        final ModelBuilder modelBuilder = new ModelBuilder();
        final Model model = modelBuilder.createBox(1f, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        polyhedronsSet = ConversionUtils.meshToPolyhedronSet(model.meshes.get(0));

//        polyhedronsSet = new Box(2, 0.25f, 2).getPolyhedronsSet();
//        for (int i = 1; i < 3; i++) {
//            final Box box = new Box(2, 0.25f, 2);
//            box.rotateY(30 * i);
//            polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, box.getPolyhedronsSet());
//        }
//
//        final Cylinder cylinder = new Cylinder(0.5f, 0.5f);
//        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, cylinder.getPolyhedronsSet());
//
//        final Cylinder hole = new Cylinder(0.25f, 1f);
//        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().difference(polyhedronsSet, hole.getPolyhedronsSet());
//        final Cylinder rounded = new Cylinder((float) (Math.sqrt(2) * 0.95), 0.5f);
//        rounded.divisions = 24;
//        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().intersection(polyhedronsSet, rounded.getPolyhedronsSet());

//        instances.add(PolyhedronsetToLineModel.convert(polyhedronsSet));

        final Material material = new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(50f));
        final ModelInstance modelInstance = ConversionUtils.polyhedronsSetToModelInstance(polyhedronsSet, material);
        instances.add(modelInstance);
        translationManipulator = new TranslationManipulator();
//        instances.add(new ModelInstance(DebugUtils.createEdgeModel(modelInstance.model, Color.BLACK)));
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
        translationManipulator.render(modelBatch);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);

        if (focusedSubPlane != null) {
            shapeRenderer.setColor(Color.LIME);
            renderSubPlane(focusedSubPlane);
            grid.setRenderingEnabled(true);
            grid.setToPlane((Plane) focusedSubPlane.getHyperplane());
        } else {
            grid.setRenderingEnabled(false);
        }
    }

    private void update() {
        if (Gdx.input.isTouched()) {
            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            if (!touched) {
                touched = true;
//            translationManipulator.inputDown(ray);
                final Vector3D point = ConversionUtils.convertVector(ray.origin);
                final Vector3D point2 = ConversionUtils.convertVector(ray.direction).add(point);
                final SubPlane subPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
                if (subPlane != null) {
                    focusedSubPlane = subPlane;
                    focusedPlane = ConversionUtils.convertPlane((Plane) focusedSubPlane.getHyperplane());
                }
            }

            if (focusedPlane != null) {
                if (Intersector.intersectRayPlane(ray, focusedPlane, hitPoint)) {

                }
            }
//            if (focusedSubPlane != null && doExtrude) {
//                instances.add(ConversionUtils.polyhedronsSetToModelInstance(new Extrusion(focusedSubPlane, 0.2f).getPolyhedronsSet(), new Material(ColorAttribute.createDiffuse(Color.SKY))));
//                doExtrude = false;
//            }
        } else {
            if (touched) {
                if (focusedSubPlane != null) {

                }
                touched = false;
            }
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
