package net.masonapps.csgvr.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 6/12/2017.
 */

public class DaydreamCameraController implements DaydreamControllerInputListener {
    private final Vector3 tempV = new Vector3();
    private final Camera camera;
    public float rotateRate = 90f;
    public Vector3 target = new Vector3();
    private float lastX, lastY;

    public DaydreamCameraController(Camera camera) {
        this.camera = camera;
    }

//    @Override
//    public void onConnectionStateChange(int connectionState) {
//
//    }
//
//    @Override
//    public void onButtonEvent(Controller controller, DaydreamButtonEvent event) {
//
//    }
//
//    @Override
//    public void onTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
//        switch (event.action) {
//            case DaydreamTouchEvent.ACTION_DOWN:
//                lastX = event.x;
//                lastY = event.y;
//                break;
//            case DaydreamTouchEvent.ACTION_MOVE:
//                final float deltaX = (event.x - lastX);
//                final float deltaY = (event.y - lastY);
//                lastX = event.x;
//                lastY = event.y;
//                tempV.set(camera.direction).crs(camera.up).y = 0f;
//                camera.rotateAround(target, tempV.nor(), deltaY * rotateRate);
//                camera.rotateAround(target, Vector3.Y, deltaX * -rotateRate);
//                break;
//            default:
//                break;
//        }
//    }

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
