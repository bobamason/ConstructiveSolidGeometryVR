package net.masonapps.csgvr;

import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.csgvr.csg.CSG;
import net.masonapps.csgvr.csg.CsgTriangle;
import net.masonapps.csgvr.ui.Grid;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.SubPlane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

/**
 * Created by Bob on 5/19/2017.
 */
class PolyhedronsTest implements ApplicationListener {

    private final Array<ModelInstance> instances = new Array<>();
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private CameraInputController cameraController;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private DirectionalLight light;
    //    private PolyhedronsSet polyhedronsSet;
//    @Nullable
//    private SubPlane focusedSubPlane = null;
//    @Nullable
//    private com.badlogic.gdx.math.Plane focusedPlane = null;
//    private TranslationManipulator translationManipulator;
    private Grid grid;
    private boolean touched = false;
    private Vector3 hitPoint = new Vector3();

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

        grid = Grid.newInstance(5f);
        grid.setToPlane(new Plane(new Vector3D(0, 1, 0), 1e-10));
        testCut();
    }

    private void testCSG() {
        ModelBuilder mb = new ModelBuilder();
        final Model b1 = mb.createBox(1f, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        final Model b2 = mb.createBox(0.5f, 0.5f, 0.5f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        final List<CsgTriangle> aTriangles = CSG.trianglesFromMesh(b1.meshes.get(0), new Matrix4());
        final List<CsgTriangle> bTriangles = CSG.trianglesFromMesh(b2.meshes.get(0), new Matrix4().setToTranslation(0.5f, 0.5f, 0.5f));
        final Mesh mesh = CSG.toMesh(CSG.union(aTriangles, bTriangles));
        mb.begin();
        mb.part("u", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.SKY)));
        final Model model = mb.end();
        final ModelInstance csgTestInstance = new ModelInstance(model, -1, 0, -2);
        instances.add(csgTestInstance);
    }

    private void testCut() {
        ModelBuilder mb = new ModelBuilder();
        final Model s1 = mb.createBox(1f, 1f, 1f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        final Mesh sMesh = s1.meshes.get(0);
        final float[] sVerts = new float[sMesh.getNumVertices() * sMesh.getVertexSize() / 4];
        sMesh.getVertices(sVerts);
        final short[] sIndices = new short[sMesh.getNumIndices()];
        sMesh.getIndices(sIndices);
        final com.badlogic.gdx.math.Plane plane = new com.badlogic.gdx.math.Plane(new Vector3(1, 1, 1).nor(), 0);
        FloatArray vertArray = new FloatArray();
        ShortArray indArray = new ShortArray();
        final int vertexSize = 6;
        final Intersector.SplitTriangle split = new Intersector.SplitTriangle(vertexSize);
        final float[] tri = new float[vertexSize * 3];
        int index = 0;
        for (int i = 0; i < sIndices.length; i += 3) {
            int ia = sIndices[i];
            int ib = sIndices[i + 1];
            int ic = sIndices[i + 2];
            for (int j = 0; j < vertexSize; j++) {
                tri[j] = sVerts[ia + j];
            }
            for (int j = 0; j < vertexSize; j++) {
                tri[j + vertexSize] = sVerts[ib + j];
            }
            for (int j = 0; j < vertexSize; j++) {
                tri[j + vertexSize * 2] = sVerts[ic + j];
            }
            Intersector.splitTriangle(tri, plane, split);
            for (int j = 0; j < split.numFront; j++) {
                for (int v = 0; v < 3; v++) {
                    for (int k = 0; k < vertexSize; k++) {
                        vertArray.add(split.front[j * vertexSize * 3 + v * vertexSize + k]);
                    }
                    indArray.add(index++);
                }
            }
        }
        final Mesh mesh = new Mesh(false, vertArray.size / vertexSize, indArray.size, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal()));
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertArray.size; i += vertexSize) {
            sb.append(i / vertexSize);
            sb.append("|");
            for (int j = 0; j < vertexSize; j++) {
                sb.append(vertArray.get(i + j));
                if (j == vertexSize - 1)
                    sb.append("\n");
                else
                    sb.append(", ");
            }
        }
        Log.d("CSG", "vertices: " + sb.toString());
        Log.d("CSG", "indices: " + indArray.toString(","));
        mesh.setVertices(vertArray.toArray());
        mesh.setIndices(indArray.toArray());
        mb.begin();
        mb.part("u", mesh, GL20.GL_TRIANGLES, new Material(ColorAttribute.createDiffuse(Color.GREEN), IntAttribute.createCullFace(0)));
        final Model model = mb.end();
        final ModelInstance cutTestInstance = new ModelInstance(model, 0, 1, 0);
        instances.add(cutTestInstance);
    }

    @Override
    public void resize(int width, int height) {
        camera.position.set(2f, 1f, 2f);
        camera.near = 0.1f;
        camera.up.set(0, 1, 0);
        camera.lookAt(0, 0, 0);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void render() {
        update();

        Gdx.gl.glViewport(0, 0, (int) camera.viewportWidth, (int) camera.viewportHeight);
        Gdx.gl.glClearColor(0.15f, 0.25f, 0.35f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        light.setDirection(camera.direction);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        if (grid.isRenderingEnabled())
            modelBatch.render(grid.modelInstance);
//        translationManipulator.render(modelBatch);
        modelBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);

//        DebugUtils.renderPolygonTree(polyhedronsSet, shapeRenderer);

//        if (focusedSubPlane != null) {
//            shapeRenderer.setColor(Color.LIME);
//            renderSubPlane(focusedSubPlane);
//            grid.setRenderingEnabled(true);
//            grid.setToPlane((Plane) focusedSubPlane.getHyperplane());
//        } else {
//            grid.setRenderingEnabled(false);
//        }
    }

    private void update() {
//        if (Gdx.input.isTouched()) {
//            final Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
//            if (!touched) {
//                touched = true;
//            translationManipulator.inputDown(ray);
//                final Vector3D point = ConversionUtils.convertVector(ray.origin);
//                final Vector3D point2 = ConversionUtils.convertVector(ray.direction).add(point);
//                final SubPlane subPlane = (SubPlane) polyhedronsSet.firstIntersection(point, new Line(point, point2, polyhedronsSet.getTolerance()));
//                if (subPlane != null) {
//                    focusedSubPlane = subPlane;
//                    focusedPlane = ConversionUtils.convertPlane((Plane) focusedSubPlane.getHyperplane());
//                }
//            }
//
//            if (focusedPlane != null) {
//                if (Intersector.intersectRayPlane(ray, focusedPlane, hitPoint)) {
//
//                }
//            }
//            if (focusedSubPlane != null && doExtrude) {
//                instances.add(ConversionUtils.polyhedronsSetToModelInstance(new Extrusion(focusedSubPlane, 0.2f).getPolyhedronsSet(), new Material(ColorAttribute.createDiffuse(Color.SKY))));
//                doExtrude = false;
//            }
//        } else {
//            if (touched) {
//                if (focusedSubPlane != null) {
//
//                }
//                touched = false;
//            }
//        }
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
