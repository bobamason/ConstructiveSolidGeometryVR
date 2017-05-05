package net.masonapps.csgvr;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class MainActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new CSGTest());
    }

    private class CSGTest implements ApplicationListener {
        @Override
        public void create() {
            
        }

        @Override
        public void resize(int width, int height) {

        }

        @Override
        public void render() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {

        }
    }
}
