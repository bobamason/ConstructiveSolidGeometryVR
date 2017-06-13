package net.masonapps.csgvr.ui;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 6/12/2017.
 */

public class Rotator implements DaydreamControllerInputListener {
    private final Vector3 tempV = new Vector3();
    private final Quaternion rotation = new Quaternion();
    private final Quaternion rotX = new Quaternion();
    private final Quaternion rotY = new Quaternion();
    public float rotateRate = 90f;
    private float lastX, lastY;

    @Override
    public void onConnectionStateChange(int connectionState) {

    }

    @Override
    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {

    }

    @Override
    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
        switch (event.action) {
            case DaydreamTouchEvent.ACTION_DOWN:
                lastX = event.x;
                lastY = event.y;
                break;
            case DaydreamTouchEvent.ACTION_MOVE:
                final float deltaX = (event.x - lastX);
                final float deltaY = (event.y - lastY);
                lastX = event.x;
                lastY = event.y;
                tempV.set(1, 0, 0).mul(rotation).y = 0f;
                rotY.set(tempV.nor(), deltaY * rotateRate);
                rotX.set(Vector3.Y, deltaX * -rotateRate);
                rotation.mul(rotY).mul(rotX);
                break;
            default:
                break;
        }
    }

    public Quaternion getRotation() {
        return rotation;
    }
}
