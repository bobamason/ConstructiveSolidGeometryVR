package net.masonapps.csgvr;

import android.support.annotation.Nullable;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import net.masonapps.csgvr.csg.CSG;
import net.masonapps.csgvr.primitives.Box;
import net.masonapps.csgvr.primitives.ConversionUtils;
import net.masonapps.csgvr.primitives.Extrusion;

import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSet;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.partitioning.RegionFactory;

/**
 * Created by Bob on 5/19/2017.
 */
class CSGTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private CameraInputController cameraController;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private CSG csg1;
    private DirectionalLight light;
    private PolyhedronsSet polyhedronsSet;
    @Nullable
    private SubPlane focusedPlane = null;
    private ModelBuilder modelBuilder;

    private static Model createModel(ModelBuilder modelBuilder, float radius) {
        modelBuilder.begin();
        final MeshPartBuilder part = modelBuilder.part("model", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, new Material(ColorAttribute.createDiffuse(Color.BLUE)));
        SphereShapeBuilder.build(part, radius * 2f, radius * 2f, radius * 2f, 24, 12);
        return modelBuilder.end();
    }

    @Override
    public void create() {
        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);
        environment = new Environment();
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        environment.set(new ColorAttribute(ColorAttribute.Ambient, Color.GRAY));
        light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        environment.add(light);

        modelBuilder = new ModelBuilder();

//            final ModelInstance s1 = new ModelInstance(createModel(modelBuilder, 1f));
//            final ModelInstance s2 = new ModelInstance(createModel(modelBuilder, 0.5f), 0.75f, 0.75f, 0.75f);
//            csg1 = CSG.fromMesh(s1.model.meshes.get(0), s1.transform);
//            instances.add(s1);
//            instances.add(s2);

        modelBuilder.begin();
//            final Mesh mesh1 = CSG.toMesh(csg1);
        final PolyhedronsSet ps1 = new Box().createPolyhedronsSet();
        final PolyhedronsSet ps2 = new Box(0.5, 1.2, 0.5).createPolyhedronsSet().translate(new Vector3D(0.25, 0, 0));
        polyhedronsSet = (PolyhedronsSet) new RegionFactory<Euclidean3D>().difference(ps1, ps2);
        final Mesh mesh1 = ConversionUtils.polyhedronsSetToMesh(polyhedronsSet);
        modelBuilder.part("mesh", mesh1, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.GRAY), ColorAttribute.createSpecular(Color.GRAY)));
        instances.add(new ModelInstance(modelBuilder.end()));

//            modelBuilder.begin();
//            final Mesh mesh2 = CSG.toMesh();
//            modelBuilder.part("mesh", mesh2, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.OLIVE)));
//            instances.add(new ModelInstance(modelBuilder.end()));
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
        light.setDirection(camera.direction);
        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
//            modelBatch.render(instances);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        if (Gdx.input.isTouched()) {
            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            final Vector3D point = ConversionUtils.convertVector3(ray.origin);
            final Vector3D point2 = ConversionUtils.convertVector3(ray.direction).add(point);
            focusedPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
            if (focusedPlane != null) {
                final Mesh mesh1 = ConversionUtils.polyhedronsSetToMesh(new Extrusion(focusedPlane, 0.2f).createPolyhedronsSet());
                modelBuilder.part("mesh", mesh1, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.SKY)));
                instances.add(new ModelInstance(modelBuilder.end()));
            }
        }
        if (focusedPlane != null) {
            shapeRenderer.setColor(Color.LIME);
            renderSubPlane(focusedPlane);
        }
    }

    private void renderSubPlane(SubPlane subPlane) {
        final Plane plane = (Plane) subPlane.getHyperplane();
        final Vector2D[][] loops = ((PolygonsSet) subPlane.getRemainingRegion()).getVertices();
        shapeRenderer.begin();
        for (Vector2D[] loop : loops) {
            for (int i = 0; i < loop.length; i++) {
                Vector3D v1 = plane.toSpace(loop[i]);
                Vector3D v2 = plane.toSpace(loop[(i + 1) % loop.length]);
                shapeRenderer.line((float) v1.getX(), (float) v1.getY(), (float) v1.getZ(), (float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
            }
        }
        shapeRenderer.end();
    }

//        private void renderCSGTree(CSG csg, Color color) {
//            final float s = 0.1f;
//            shapeRenderer.begin();
//            shapeRenderer.setColor(color);
//            polygons.clear();
//            csg.tree.getAllPolygons(polygons);
//            for (CSGPolygon polygon : polygons) {
//                for (Vertex vertex : polygon.vertices) {
//                    final Vector3 v = vertex.position;
//                    final Vector3 n;
//                    if (polygon.plane != null) {
//                        n = polygon.plane.normal;
//                        shapeRenderer.line(v.x, v.y, v.z, v.x + n.x * s, v.y + n.y * s, v.z + n.z * s);
//                    }
//                }
//            }
//            shapeRenderer.end();
//        }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        shapeRenderer.dispose();
    }
}