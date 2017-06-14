package net.masonapps.csgvr;

import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.csgvr.primitives.Box;
import net.masonapps.csgvr.primitives.ConversionUtils;
import net.masonapps.csgvr.primitives.Cylinder;
import net.masonapps.csgvr.primitives.Solid;
import net.masonapps.csgvr.ui.Grid;
import net.masonapps.csgvr.ui.Rotator;

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
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 6/12/2017.
 */

public class CsgVrTestScreen extends VrWorldScreen {

    private final Rotator rotator;
    private final Solid solid;
    private final Matrix4 tempM = new Matrix4();
    private final Ray tempRay = new Ray();
    private final Entity wireFrame;
    private ModelBatch modelBatch;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    private PolyhedronsSet polyhedronsSet;
    @Nullable
    private SubPlane focusedPlane = null;
    //    private TransformManipulator transformManipulator;
    private Grid grid;
    @Nullable
    private SubPlane selectedPlane = null;
    private DaydreamControllerInputListener listener = new DaydreamControllerInputListener() {
        @Override
        public void onConnectionStateChange(int connectionState) {

        }

        @Override
        public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
            if (event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD && event.action == DaydreamButtonEvent.ACTION_DOWN) {
                selectedPlane = focusedPlane;
            }
        }

        @Override
        public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

        }
    };

    public CsgVrTestScreen(VrGame game) {
        super(game);
        setBackgroundColor(Color.NAVY);
        rotator = new Rotator();
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
        getWorld().add(grid);

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

        solid = new Solid();
        manageDisposable(solid);
        solid.setPolyhedronsSet(polyhedronsSet);
        solid.material = new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY), FloatAttribute.createShininess(50f));
        getWorld().add(new Entity(solid.getModelInstance(true)));
//        transformManipulator = new TransformManipulator(entity.transform);
        wireFrame = getWorld().add(new Entity(new ModelInstance(DebugUtils.createEdgeModel(solid.getModelInstance(false).model, Color.BLACK))));
        wireFrame.setLightingEnabled(false);
    }

    @Override
    public void show() {
        GdxVr.input.getDaydreamControllerHandler().addListener(rotator);
        GdxVr.input.getDaydreamControllerHandler().addListener(listener);
    }

    @Override
    public void hide() {
        GdxVr.input.getDaydreamControllerHandler().removeListener(rotator);
        GdxVr.input.getDaydreamControllerHandler().removeListener(listener);
    }

    @Override
    public void update() {
        super.update();
        if (Gdx.input.isTouched()) {
            tempRay.set(GdxVr.input.getInputRay());
//            transformManipulator.rayTest(ray);
            tempRay.mul(tempM.set(solid.getModelInstance(false).transform).inv());
            final Vector3D point = ConversionUtils.convertVector(tempRay.origin);
            final Vector3D point2 = ConversionUtils.convertVector(tempRay.direction).add(point);
            final SubPlane subPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
            if (subPlane != null) {
                focusedPlane = subPlane;
            }
        }
        solid.getModelInstance(false).transform.idt().translate(0, -0.5f, -3.0f).rotate(rotator.getRotation());
        wireFrame.transform.set(solid.getModelInstance(false).transform);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        Gdx.gl.glLineWidth(1f);
        super.render(camera, whichEye);
        light.setDirection(camera.direction);

//        modelBatch.begin(camera);
//        transformManipulator.render(modelBatch);
//        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);

        if (focusedPlane != null) {
            shapeRenderer.setColor(Color.CYAN);
            renderSubPlane(focusedPlane);
        }
        if (selectedPlane != null) {
            shapeRenderer.setColor(Color.LIME);
            renderSubPlane(selectedPlane);
            grid.setRenderingEnabled(true);
            grid.setToPlane((Plane) selectedPlane.getHyperplane());
        } else {
            grid.setRenderingEnabled(false);
        }
    }

    private void renderSubPlane(SubPlane subPlane) {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final Vector2D[][] loops = ((PolygonsSet) subPlane.getRemainingRegion()).getVertices();
        Gdx.gl.glLineWidth(2f);
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
