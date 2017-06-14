package net.masonapps.csgvr;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class MainActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new PolyhedronsTest());
    }

}
