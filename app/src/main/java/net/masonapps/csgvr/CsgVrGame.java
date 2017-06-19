package net.masonapps.csgvr;

import com.badlogic.gdx.graphics.g3d.ModelBatch;

import org.masonapps.libgdxgooglevr.gfx.VrGame;

/**
 * Created by Bob on 5/26/2017.
 */

class CsgVrGame extends VrGame {

    @Override
    public void create() {
        super.create();
        setScreen(new CsgVrTestScreen(this));
    }

    @Override
    public ModelBatch getModelBatch() {
        return new ModelBatch();
    }
}
