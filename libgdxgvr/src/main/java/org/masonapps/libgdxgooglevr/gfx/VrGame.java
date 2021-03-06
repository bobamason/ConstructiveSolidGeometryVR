package org.masonapps.libgdxgooglevr.gfx;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrCursor;
import org.masonapps.libgdxgooglevr.input.VrInputProcessor;
import org.masonapps.libgdxgooglevr.vr.VrApplicationAdapter;

/**
 * Created by Bob on 12/22/2016.
 */

public class VrGame extends VrApplicationAdapter implements DaydreamControllerInputListener {
    private static final String CONTROLLER_FILENAME = "ddcontroller.g3db";
    private final Vector3 controllerScale = new Vector3(10f, 10f, 10f);
    protected VrScreen screen;
    protected Ray ray = new Ray();
    protected boolean isInputVisible = true;
    protected VrCursor cursor;
    protected AssetManager assets;
    private boolean loading = true;
    private ShapeRenderer shapeRenderer;
    private Color cursorColor1 = new Color(1f, 1f, 1f, 1f);
    private Color cursorColor2 = new Color(1f, 1f, 1f, 0f);
    @Nullable
    private ModelInstance controllerInstance = null;
    private ModelBatch modelBatch;

    @Override
    public void create() {
        super.create();
        modelBatch = createModelBatch();
        assets = new AssetManager();
        cursor = new VrCursor();
        cursor.setDeactivatedDiameter(0.02f);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        loadAsset(CONTROLLER_FILENAME, Model.class);
    }

    protected ModelBatch createModelBatch() {
        return new ModelBatch(new PhongShaderProvider());
    }

    @Override
    public void pause() {
        if (screen != null) screen.pause();
        GdxVr.input.removeDaydreamControllerListener(this);
    }

    @Override
    public void resume() {
        if (screen != null) screen.resume();
        GdxVr.input.addDaydreamControllerListener(this);
    }

    @Override
    @CallSuper
    public void update() {
        if (loading) {
            if (assets.update()) {
                doneLoading(assets);
                loading = false;
            }
        }
        final VrInputProcessor vrInputProcessor = GdxVr.input.getVrInputProcessor();
        if (vrInputProcessor != null && vrInputProcessor.isCursorOver()) {
            cursor.position.set(vrInputProcessor.getHitPoint3D());
            cursor.lookAtTarget(ray.origin, Vector3.Y);
            cursor.setVisible(true);
        } else {
            cursor.position.set(ray.direction.x + ray.origin.x, ray.direction.y + ray.origin.y, ray.direction.z + ray.origin.z);
            cursor.lookAtTarget(ray.origin, Vector3.Y);
            cursor.setVisible(!GdxVr.input.isControllerConnected());
        }
        if (screen != null) screen.update();
    }

    @CallSuper
    protected void doneLoading(AssetManager assets) {
        if (controllerInstance == null)
            controllerInstance = new ModelInstance(assets.get(CONTROLLER_FILENAME, Model.class));
        if (screen != null) screen.doneLoading(assets);
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        if (screen != null) screen.onDrawFrame(headTransform, leftEye, rightEye);
        super.onDrawFrame(headTransform, leftEye, rightEye);
    }

    @Override
    public void render(Camera camera, int whichEye) {
        if (screen != null) screen.render(camera, whichEye);
        if (controllerInstance != null && GdxVr.input.isControllerConnected() && isInputVisible) {
            modelBatch.begin(camera);
            modelBatch.render(controllerInstance);
            modelBatch.end();
        }
        if (isInputVisible)
            renderCursor(camera);
    }

    protected void renderCursor(Camera camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (GdxVr.input.isControllerConnected()) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin();
            Gdx.gl.glLineWidth(2f);
            shapeRenderer.line(ray.origin.x, ray.origin.y, ray.origin.z, cursor.position.x, cursor.position.y, cursor.position.z, cursorColor1, cursorColor2);
            shapeRenderer.end();
        }
        cursor.render(camera);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if (screen != null) screen.renderAfterCursor(camera);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        if (screen != null) screen.onNewFrame(headTransform);
    }

    @Override
    public void onDrawEye(Eye eye) {
        super.onDrawEye(eye);
        if (screen != null) screen.onDrawEye(eye);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        super.onFinishFrame(viewport);
        if (screen != null) screen.onFinishFrame(viewport);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
        if (GdxVr.input.isControllerConnected()) {
            ray.set(GdxVr.input.getInputRay());
            if (controllerInstance != null) {
                controllerInstance.transform.set(GdxVr.input.getControllerPosition(), GdxVr.input.getControllerOrientation(), controllerScale);
            }
        }
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {

    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
        if (screen != null) screen.onCardboardTrigger();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        shapeRenderer = null;
        if (assets != null)
            assets.dispose();
        assets = null;
        if (modelBatch != null) {
            modelBatch.dispose();
        }
        modelBatch = null;
        if (screen != null) {
            try {
                screen.hide();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                screen.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadAsset(String filename, Class<?> type) {
        assets.load(filename, type);
        loading = true;
    }

    public void loadAsset(AssetDescriptor desc) {
        assets.load(desc);
        loading = true;
    }

    public ModelBatch getModelBatch() {
        return modelBatch;
    }

    public VrScreen getScreen() {
        return screen;
    }

    public void setScreen(VrScreen screen) {
        try {
            if (this.screen != null)
                this.screen.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.screen = screen;
        try {
            if (this.screen != null)
                this.screen.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssetManager getAssets() {
        return assets;
    }

    public boolean isLoading() {
        return loading;
    }

    public Ray getControllerRay() {
        return ray;
    }

    public boolean isInputVisible() {
        return isInputVisible;
    }

    public void setInputVisible(boolean visible) {
        isInputVisible = visible;
    }

    public VrCursor getCursor() {
        return cursor;
    }
}