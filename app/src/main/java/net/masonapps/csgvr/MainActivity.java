package net.masonapps.csgvr;

import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.csg.CSG;

public class MainActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new CSGTest());
    }

    private static class CSGTest implements ApplicationListener {

        private final Array<ModelInstance> instances = new Array<>();
        private final PerspectiveCamera camera = new PerspectiveCamera();
        private ModelBatch modelBatch;
        private Environment environment;

        private static Model createSphere(ModelBuilder modelBuilder, float radius) {
            modelBuilder.begin();
            final MeshPartBuilder part = modelBuilder.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, new Material(ColorAttribute.createDiffuse(Color.RED)));
            SphereShapeBuilder.build(part, radius * 2f, radius * 2f, radius * 2f, 24, 24);
            return modelBuilder.end();
        }
        
        @Override
        public void create() {
            environment = new Environment();
            modelBatch = new ModelBatch();
            environment.set(new ColorAttribute(ColorAttribute.Ambient, Color.GRAY));
            final DirectionalLight light = new DirectionalLight();
            light.setDirection(new Vector3(1, -1, -1).nor());
            environment.add(light);

            final ModelBuilder modelBuilder = new ModelBuilder();

            final ModelInstance s1 = new ModelInstance(createSphere(modelBuilder, 1f));
            final ModelInstance s2 = new ModelInstance(createSphere(modelBuilder, 0.6f), 0.5f, 0.5f, 0.5f);
            final CSG csg1 = CSG.fromMesh(s1.model.meshes.get(0), s1.transform);
            final CSG csg2 = CSG.fromMesh(s2.model.meshes.get(0), s2.transform);

//            modelBuilder.begin();
//            final Mesh mesh = CSG.toMesh(csg1);
//            modelBuilder.part("mesh", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.SKY)));
//            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh mesh = CSG.toPoints(csg1);
            modelBuilder.part("mesh", mesh, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.SKY)));
            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh lineMesh = CSG.toPoints(csg2);
            modelBuilder.part("lines", lineMesh, GL20.GL_POINTS, new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
            instances.add(new ModelInstance(modelBuilder.end()));

            modelBuilder.begin();
            final Mesh uMesh = CSG.toLineMesh(csg1.union(csg2));
            modelBuilder.part("lines", uMesh, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.GREEN)));
            instances.add(new ModelInstance(modelBuilder.end(), 0, 0.01f, 0));

//            instances.add(s1);
//            instances.add(s2);
        }

        @Override
        public void resize(int width, int height) {
            camera.position.set(0, 0.5f, 4f);
            camera.up.set(0, 1, 0);
            camera.lookAt(0, 0, 0);
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }

        @Override
        public void render() {
            Gdx.gl.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
            Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            modelBatch.begin(camera);
//            modelBatch.render(instances, environment);
            modelBatch.render(instances);
            modelBatch.end();
        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {
            modelBatch.dispose();
        }
    }
}
