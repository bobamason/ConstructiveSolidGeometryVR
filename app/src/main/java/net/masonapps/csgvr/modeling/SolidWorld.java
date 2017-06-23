package net.masonapps.csgvr.modeling;

import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.World;

/**
 * Created by Bob on 6/23/2017.
 */

public class SolidWorld extends World {

    public SolidWorld() {
    }

    @Nullable
    public Solid getClosestSolid(Ray ray) {
        float closestDst2 = Float.POSITIVE_INFINITY;
        final Vector3 hitPoint = new Vector3();
        Solid selected = null;
        for (Entity entity : entities) {
            if (entity instanceof Solid) {
                final Solid solid = (Solid) entity;
                if (solid.castRay(ray, hitPoint)) {
                    final float dst2 = ray.origin.dst2(hitPoint);
                    if (dst2 < closestDst2) {
                        closestDst2 = dst2;
                        selected = solid;
                    }
                }
            }
        }
        return selected;
    }

    @Override
    public void update() {
        super.update();
        for (Entity solid : entities) {
            if (solid instanceof Solid) {
                ((Solid) solid).updateTransform();
            }
        }
    }
}
