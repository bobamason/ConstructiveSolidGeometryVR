package net.masonapps.csgvr.csg;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Bob on 5/5/2017.
 */

public class Vertex {
    
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3(0, 1, 0);
    public final Vector2 uv = new Vector2();

    public Vertex(Vertex vertex) {
        set(vertex);
    }

    private void set(Vertex vertex) {
        this.position.set(vertex.position);
        this.normal.set(vertex.normal);
        this.uv.set(vertex.uv);
    }

    public void flip() {
        normal.scl(-1);
    }

    public Vertex interpolate(Vertex other, float t) {
        position.lerp(other.position, t);
        normal.lerp(other.normal, t);
        uv.lerp(other.uv, t);
        return this;
    }
}
