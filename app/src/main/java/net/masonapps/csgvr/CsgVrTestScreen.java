package net.masonapps.csgvr;

import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.csgvr.modeling.Solid;
import net.masonapps.csgvr.modeling.SolidModelingScreen;
import net.masonapps.csgvr.modeling.SolidWorld;
import net.masonapps.csgvr.primitives.Box;
import net.masonapps.csgvr.primitives.Cylinder;
import net.masonapps.csgvr.ui.DaydreamCameraController;
import net.masonapps.csgvr.ui.Grid;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;
import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.TextButtonVR;

/**
 * Created by Bob on 6/12/2017.
 */

public class CsgVrTestScreen extends SolidModelingScreen {

    private final DaydreamCameraController cameraController;
    private final Matrix4 tempM = new Matrix4();
    private final Ray tempRay = new Ray();
    private final Vector3 tempV = new Vector3();
    private SpriteBatch spriteBatch;
    //    private final Entity wireFrame;
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
    @Nullable
    private Solid selectedSolid = null;
    private DaydreamControllerInputListener listener = new DaydreamControllerInputListener() {
        @Override
        public void onConnectionStateChange(int connectionState) {

        }

        @Override
        public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
            if (event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD) {
                if (event.action == DaydreamButtonEvent.ACTION_DOWN && selectedSolid != null) {
                    selectedSolid = getSolidWorld().getClosestSolid(GdxVr.input.getInputRay());
                } else if (event.action == DaydreamButtonEvent.ACTION_UP) {
                    selectedSolid = null;
                }
            }
        }

        @Override
        public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

        }
    };

    public CsgVrTestScreen(SolidModelingVrGame game) {
        super(game);
        setBackgroundColor(Color.DARK_GRAY);
        cameraController = new DaydreamCameraController(getVrCamera());
        environment = new Environment();
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        environment.set(new ColorAttribute(ColorAttribute.Ambient, Color.GRAY));
        light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        environment.add(light);
        getVrCamera().near = 0.1f;
        getVrCamera().position.set(0, 1f, 4f);

        grid = Grid.newInstance(5f);

        polyhedronsSet = new Box(2, 0.25f, 2).createSolid().getPolyhedronsSet();
        for (int i = 1; i < 3; i++) {
            final Box box = new Box(2, 0.25f, 2);
            box.transform.rotate(Vector3.Y, 30 * i);
            polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, box.createSolid().getPolyhedronsSet());
        }

        final Cylinder cylinder = new Cylinder(0.5f, 0.5f);
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().union(polyhedronsSet, cylinder.createSolid().getPolyhedronsSet());

        final Cylinder hole = new Cylinder(0.25f, 1f);
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().difference(polyhedronsSet, hole.createSolid().getPolyhedronsSet());
        final Cylinder rounded = new Cylinder((float) (Math.sqrt(2) * 0.95), 0.5f);
        rounded.divisions = 24;
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().intersection(polyhedronsSet, rounded.createSolid().getPolyhedronsSet());

//        instances.add(PolyhedronsetToLineModel.convert(polyhedronsSet));

        final Solid solid = new Solid(polyhedronsSet);
        getWorld().add(solid);
//        transformManipulator = new TransformManipulator(entity.transform);
//        wireFrame = getWorld().add(new Entity(new ModelInstance(DebugUtils.createEdgeModel(solid.getModelInstance(false).model, Color.BLACK))));
//        wireFrame.setLightingEnabled(false);

        spriteBatch = new SpriteBatch();
        final TextButtonVR addCubeButton = new TextButtonVR(spriteBatch, "Add Cube", game.getSkin());
        addCubeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getSolidWorld().add(new Box(1f, 1f, 1f).createSolid());
            }
        });

        getUiContainer().addProcessor(addCubeButton);
    }

    private SolidWorld getSolidWorld() {
        return (SolidWorld) getWorld();
    }

    @Override
    public void show() {
//        GdxVr.input.getDaydreamControllerHandler().addListener(cameraController);
        GdxVr.input.getDaydreamControllerHandler().addListener(listener);
    }

    @Override
    public void hide() {
//        GdxVr.input.getDaydreamControllerHandler().removeListener(cameraController);
        GdxVr.input.getDaydreamControllerHandler().removeListener(listener);
    }

    @Override
    public void update() {
        super.update();
        if (selectedSolid != null) {
            selectedSolid.setPosition(GdxVr.input.getInputRay().origin);
            selectedSolid.translate(GdxVr.input.getInputRay().direction);
            selectedSolid.translate(GdxVr.input.getInputRay().direction);
        }

        getUiContainer().setPosition(getVrCamera().position);
        getUiContainer().lookAt(tempV.set(getVrCamera().position).add(getVrCamera().direction), Vector3.Y);
//        wireFrame.transform.set(solid.getModelInstance(false).transform);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        Gdx.gl.glLineWidth(1f);
        super.render(camera, whichEye);
        light.setDirection(camera.direction);

        shapeRenderer.setProjectionMatrix(camera.combined);

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);

//        if (focusedPlane != null) {
//            shapeRenderer.setColor(Color.CYAN);
//            renderSubPlane(focusedPlane);
//        }
//
//        if (selectedPlane != null) {
//            shapeRenderer.setColor(Color.LIME);
//            renderSubPlane(selectedPlane);
//            grid.setRenderingEnabled(true);
//            grid.setToPlane((Plane) selectedPlane.getHyperplane());
//        } else {
//            grid.setRenderingEnabled(false);
//        }

        if (grid.isRenderingEnabled()) {
            modelBatch.begin(camera);
            modelBatch.render(grid.modelInstance, environment);
            modelBatch.end();
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
        if (spriteBatch != null)
            spriteBatch.dispose();
        spriteBatch = null;
    }
}
