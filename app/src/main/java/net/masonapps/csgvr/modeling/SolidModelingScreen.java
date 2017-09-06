package net.masonapps.csgvr.modeling;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.gfx.World;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.input.VrUiContainer;

/**
 * Created by Bob on 6/20/2017.
 */

public class SolidModelingScreen extends VrWorldScreen {
    
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

    @Override
    protected World createWorld() {
        return new SolidWorld();
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.set(Color.DARK_GRAY, 0.0f, -1.0f, 0.0f);
        lights.add(light);
    }

    @Override
    @CallSuper
    public void update() {
        super.update();
        uiContainer.setPosition(getVrCamera().position);
        uiContainer.translate(getVrCamera().direction.x * 2f, getVrCamera().direction.y * 2f, getVrCamera().direction.z * 2f);
        uiContainer.lookAt(getVrCamera().position, Vector3.Y);
        uiContainer.act();
    }

    @Override
    @CallSuper
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        uiContainer.draw(camera);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public VrUiContainer getUiContainer() {
        return uiContainer;
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

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
}