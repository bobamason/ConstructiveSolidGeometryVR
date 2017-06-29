package net.masonapps.csgvr;

import android.os.Bundle;

import org.masonapps.libgdxgooglevr.vr.VrActivity;

public class MainActivity extends VrActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new SolidModelingVrGame());
    }

}
