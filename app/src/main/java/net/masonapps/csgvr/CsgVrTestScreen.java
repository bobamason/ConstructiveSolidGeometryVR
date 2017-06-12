package net.masonapps.csgvr;

import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.primitives.Box;
import net.masonapps.csgvr.primitives.ConversionUtils;
import net.masonapps.csgvr.primitives.Cylinder;
import net.masonapps.csgvr.ui.Grid;
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
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;

/**
 * Created by Bob on 6/12/2017.
 */

public class CsgVrTestScreen extends VrWorldScreen {

    private final Array<ModelInstance> instances = new Array<>();
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    private PolyhedronsSet polyhedronsSet;
    @Nullable
    private SubPlane focusedPlane = null;
    private TransformManipulator transformManipulator;
    private Grid grid;

    public CsgVrTestScreen(VrGame game) {
        super(game);
        environment = new Environment();
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        environment.set(new ColorAttribute(ColorAttribute.Ambient, Color.GRAY));
        light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        environment.add(light);

        grid = Grid.newInstance();

        polyhedronsSet = new Box(2, 0.25f, 2).getPolyhedronsSet();
        for (int i = 1; i < 3; i++) {
            final Box box = new Box(2, 0.25f, 2);
            box.rotateY(30 * i);
            polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, box.getPolyhedronsSet());
        }

        final Cylinder cylinder = new Cylinder(0.5f, 0.5f);
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, cylinder.getPolyhedronsSet());

        final Cylinder hole = new Cylinder(0.25f, 1f);
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().difference(polyhedronsSet, hole.getPolyhedronsSet());
        final Cylinder rounded = new Cylinder((float) (Math.sqrt(2) * 0.95), 0.5f);
        rounded.divisions = 24;
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().intersection(polyhedronsSet, rounded.getPolyhedronsSet());

//        instances.add(PolyhedronsetToLineModel.convert(polyhedronsSet));

        final Material material = new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(50f));
        final ModelInstance modelInstance = ConversionUtils.polyhedronsSetToModelInstance(polyhedronsSet, material);
        instances.add(modelInstance);
        transformManipulator = new TransformManipulator(modelInstance.transform);
        instances.add(new ModelInstance(DebugUtils.createEdgeModel(modelInstance.model, Color.BLACK)));
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        update();

        Gdx.gl.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glClearColor(0.15f, 0.25f, 0.35f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        light.setDirection(camera.direction);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        if (grid.isRenderingEnabled())
            modelBatch.render(grid.modelInstance);
        transformManipulator.render(modelBatch);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);

        if (focusedPlane != null) {
            shapeRenderer.setColor(Color.LIME);
            renderSubPlane(focusedPlane);
            grid.setRenderingEnabled(true);
            grid.setToPlane((Plane) focusedPlane.getHyperplane());
        } else {
            grid.setRenderingEnabled(false);
        }
    }

    @Override
    public void update() {
        super.update();
        if (Gdx.input.isTouched()) {
            final Ray ray = GdxVr.input.getInputRay().cpy();
            transformManipulator.rayTest(ray);
            final Vector3D point = ConversionUtils.convertVector(ray.origin);
            final Vector3D point2 = ConversionUtils.convertVector(ray.direction).add(point);
            final SubPlane subPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
            if (subPlane != null) {
                focusedPlane = subPlane;
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
    public void dispose() {
        super.dispose();
        modelBatch.dispose();
        shapeRenderer.dispose();
    }
}
