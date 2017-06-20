package net.masonapps.csgvr.modeling;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.HeadTransform;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrScreen;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;

import java.util.ArrayList;

/**
 * Created by Bob on 6/20/2017.
 */

public class SolidModelingScreen extends VrScreen {

    protected final ArrayList<Solid> solids = new ArrayList<>();
    protected Environment environment;
    private Array<Disposable> disposables = new Array<>();
    private Color backgroundColor = Color.BLACK.cpy();
    private VrUiContainer uiContainer;

    public SolidModelingScreen(VrGame game) {
        super(game);
        environment = createEnvironment();
        final Array<BaseLight> lights = new Array<>();
        addLights(lights);
        environment.add(lights);
        uiContainer = new VrUiContainer();
        GdxVr.input.setInputProcessor(uiContainer);
    }

    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
        return environment;
    }

    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.set(Color.DARK_GRAY, 0.0f, -1.0f, 0.0f);
        lights.add(light);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    @CallSuper
    public void update() {
        uiContainer.setPosition(getVrCamera().position);
        uiContainer.translate(getVrCamera().direction.x * 2f, getVrCamera().direction.y * 2f, getVrCamera().direction.z * 2f);
        uiContainer.lookAt(getVrCamera().position, Vector3.Y);
        uiContainer.act();
    }

    @Override
    @CallSuper
    public void render(Camera camera, int whichEye) {
        uiContainer.draw(camera);
        getModelBatch().begin(camera);
        for (Solid solid : solids) {
            getModelBatch().render(solid.getModelInstance(), environment);
        }
        getModelBatch().end();
    }

    @Nullable
    public Solid getClosestSolid(Ray ray) {
        float closestDst2 = Float.POSITIVE_INFINITY;
        final Vector3 hitPoint = new Vector3();
        Solid selected = null;
        for (Solid solid : solids) {
            if (solid.castRay(ray, hitPoint)) {
                final float dst2 = ray.origin.dst2(hitPoint);
                if (dst2 < closestDst2) {
                    closestDst2 = dst2;
                    selected = solid;
                }
            }
        }
        return selected;
    }

    @Override
    public void pause() {
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void resume() {
    }

    public void loadAsset(String filename, Class<?> type) {
        game.loadAsset(filename, type);
    }

    public void loadAsset(AssetDescriptor desc) {
        game.loadAsset(desc);
    }

    @Override
    @CallSuper
    public void dispose() {
        if (disposables != null) {
            for (Disposable d : disposables) {
                try {
                    if (d != null)
                        d.dispose();
                } catch (Exception e) {
                    Log.e(SolidModelingScreen.class.getSimpleName(), e.getMessage());
                }
            }
            disposables.clear();
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ModelBatch getModelBatch() {
        return game.getModelBatch();
    }

    public Array<Disposable> getDisposables() {
        return disposables;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        this.backgroundColor.set(r, g, b, a);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int rgba) {
        this.backgroundColor.set(rgba);
    }

    public void manageDisposable(Disposable... disposables) {
        for (Disposable d : disposables) {
            this.disposables.add(d);
        }
    }

    public void manageDisposable(Disposable disposable) {
        this.disposables.add(disposable);
    }

    public VrUiContainer getUiContainer() {
        return uiContainer;
    }

    public boolean isLoading() {
        return game.isLoading();
    }

    public Ray getControllerRay() {
        return game.getControllerRay();
    }
}