package net.masonapps.csgvr.j3dbool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;

/**
 * Class representing a 3D solid.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Solid extends Mesh {
    /**
     * array of indices for the vertexArray from the 'vertexArray' attribute
     */
    protected short[] indices;
    /**
     * array of points defining the solid's vertexArray
     */
    protected Vertex[] vertexArray;

    //--------------------------------CONSTRUCTORS----------------------------------//

    /**
     * Construct a solid based on data arrays. An exception may occur in the case of
     * abnormal arrays (indices making references to inexistent vertexArray, there are less
     * colors than vertexArray...)
     *
     * @param vertexArray array of points defining the solid vertexArray
     * @param indices     array of indices for a array of vertexArray
     */
    public Solid(Vertex[] vertexArray, short[] indices) {
        super(false, vertexArray.length, indices.length, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
        setData(vertexArray, indices);
    }

    /**
     * Constructs a solid based on a coordinates file. It contains vertexArray and indices,
     * and its format is like this:
     * <p>
     * <br><br>4
     * <br>0 -5.00000000000000E-0001 -5.00000000000000E-0001 -5.00000000000000E-0001
     * <br>1  5.00000000000000E-0001 -5.00000000000000E-0001 -5.00000000000000E-0001
     * <br>2 -5.00000000000000E-0001  5.00000000000000E-0001 -5.00000000000000E-0001
     * <br>3  5.00000000000000E-0001  5.00000000000000E-0001 -5.00000000000000E-0001
     * <p>
     * <br><br>2
     * <br>0 0 2 3
     * <br>1 3 1 0
     *
     * @param solidFile file containing the solid coordinates
     * @param color     solid color
     */
//    public Solid(File solidFile, Color color) {
//        this();
//        loadCoordinateFile(solidFile, color);
//    }

    /**
     * Sets the initial features common to all constructors
     */
    protected void setInitialFeatures() {
        vertexArray = new Vertex[0];
        indices = new short[0];

//        setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
//        setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
//        setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    }

    //---------------------------------------GETS-----------------------------------//

    /**
     * Gets the solid vertexArray
     *
     * @return solid vertexArray
     */
    public Vertex[] getVertexArray() {
        Vertex[] newVertices = new Vertex[vertexArray.length];
        for (int i = 0; i < newVertices.length; i++) {
            newVertices[i] = (Vertex) vertexArray[i].clone();
        }
        return newVertices;
    }

    /**
     * Gets the solid indices for its vertexArray
     *
     * @return solid indices for its vertexArray
     */
    public int[] getIndices() {
        int[] newIndices = new int[indices.length];
        System.arraycopy(indices, 0, newIndices, 0, indices.length);
        return newIndices;
    }

    /**
     * Gets the vertexArray colors
     *
     * @return vertexArray colors
     */
    public Color[] getColors() {
        Color[] newColors = new Color[vertexArray.length];
        for (int i = 0; i < newColors.length; i++) {
            newColors[i] = vertexArray[i].getColor();
        }
        return newColors;
    }

    /**
     * Gets if the solid is empty (without any vertex)
     *
     * @return true if the solid is empty, false otherwise
     */
    public boolean isEmpty() {
        return indices.length == 0;
    }

    //---------------------------------------SETS-----------------------------------//

    /**
     * Sets the solid data. Each vertex may have a different color. An exception may
     * occur in the case of abnormal arrays (e.g., indices making references to
     * inexistent vertexArray, there are less colors than vertexArray...)
     *
     * @param vertices array of points defining the solid vertexArray
     * @param indices  array of indices for a array of vertexArray
     */
    public void setData(Vertex[] vertices, short[] indices) {
        this.vertexArray = new Vertex[vertices.length];
        this.indices = new short[indices.length];
        if (indices.length != 0) {
            for (int i = 0; i < vertices.length; i++) {
                this.vertexArray[i] = (Vertex) vertices[i].clone();
            }
            System.arraycopy(indices, 0, this.indices, 0, indices.length);

            defineGeometry();
        }
    }

    /**
     * Sets the solid data. Defines the same color to all the vertexArray. An exception may
     * may occur in the case of abnormal arrays (e.g., indices making references to
     * inexistent vertexArray...)
     *
     * @param vertices array of points defining the solid vertexArray
     * @param indices  array of indices for a array of vertexArray
     * @param color    the color of the vertexArray (the solid color)
     */
    public void setData(Vertex[] vertices, short[] indices, Color color) {
        Color[] colors = new Color[vertices.length];
        Arrays.fill(colors, color);
        setData(vertices, indices);
    }

    //-------------------------GEOMETRICAL_TRANSFORMATIONS-------------------------//

    /**
     * Applies a translation into a solid
     *
     * @param dx translation on the x axis
     * @param dy translation on the y axis
     * @param dz translation on the z axis
     */
    public void translate(float dx, float dy, float dz) {
        if (dx != 0 || dy != 0) {
            for (final Vertex v : vertexArray) {
                v.x += dx;
                v.y += dy;
                v.z += dz;
            }

            defineGeometry();
        }
    }

    /**
     * Applies a rotation into a solid
     *
     * @param rotation Quaternion rotation
     */
    public void rotate(Quaternion rotation) {

        if (!rotation.isIdentity()) {
            //get mean
            Vector3 mean = getMean();
            Vector3 tmp = new Vector3();

            for (final Vertex v : vertexArray) {
                v.x -= mean.x;
                v.y -= mean.y;
                v.z -= mean.z;

                v.normal.mul(rotation);
                tmp.set(v.x, v.y, v.z).mul(rotation);
                v.x = tmp.x;
                v.y = tmp.y;
                v.z = tmp.z;

                v.x += mean.x;
                v.y += mean.y;
                v.z += mean.z;
            }
        }

        defineGeometry();
    }

    /**
     * Applies a scale changing into the solid
     *
     * @param dx scale changing for the x axis
     * @param dy scale changing for the y axis
     * @param dz scale changing for the z axis
     */
    public void scale(float dx, float dy, float dz) {
        for (final Vertex v : vertexArray) {
            v.x *= dx;
            v.y *= dy;
            v.z *= dz;
        }

        defineGeometry();
    }

    //-----------------------------------PRIVATES--------------------------------//

    /**
     * Creates a geometry based on the indexes and vertexArray set for the solid
     */
    protected void defineGeometry() {
        setIndices(indices);
        final int vertexSize = 3 + 3 + 4;
        final float[] vertices = new float[vertexArray.length * vertexSize];
        for (int i = 0; i < vertexArray.length; i++) {
            final Vertex vertex = vertexArray[i];
            vertices[i * vertexSize] = vertex.x;
            vertices[i * vertexSize + 1] = vertex.y;
            vertices[i * vertexSize + 2] = vertex.z;

            final Vector3 normal = vertex.normal;
            vertices[i * vertexSize + 3] = normal.x;
            vertices[i * vertexSize + 4] = normal.y;
            vertices[i * vertexSize + 5] = normal.z;

            final Color color = vertex.getColor();
            vertices[i * vertexSize + 6] = color.r;
            vertices[i * vertexSize + 7] = color.g;
            vertices[i * vertexSize + 8] = color.b;
            vertices[i * vertexSize + 9] = color.a;
        }
        setVertices(vertices);
    }

    /**
     * Loads a coordinates file, setting vertexArray and indices 
     *
     * @param solidFile file used to create the solid
     * @param color solid color
     */
//	protected void loadCoordinateFile(File solidFile, Color color)
//	{
//		try
//		{
//			BufferedReader reader = new BufferedReader(new FileReader(solidFile));
//			
//			String line = reader.readLine();
//			int numVertices = Integer.parseInt(line);
//			vertexArray = new Vector3[numVertices];
//									
//			StringTokenizer tokens;
//			String token;
//						
//			for(int i=0;i<numVertices;i++)
//			{
//				line = reader.readLine();
//				tokens = new StringTokenizer(line);
//				tokens.nextToken();
//				vertexArray[i]= new Vector3(Double.parseDouble(tokens.nextToken()), Double.parseDouble(tokens.nextToken()), Double.parseDouble(tokens.nextToken()));
//			}
//			
//			reader.readLine();
//			
//			line = reader.readLine();
//			int numTriangles = Integer.parseInt(line);
//			indices = new int[numTriangles*3];
//						
//			for(int i=0,j=0;i<numTriangles*3;i=i+3,j++)
//			{
//				line = reader.readLine();
//				tokens = new StringTokenizer(line);
//				tokens.nextToken();
//				indices[i] = Integer.parseInt(tokens.nextToken());
//				indices[i+1] = Integer.parseInt(tokens.nextToken());
//				indices[i+2] = Integer.parseInt(tokens.nextToken());
//			}
//			
//			colors = new Color3f[vertexArray.length];
//			Arrays.fill(colors, color);
//			
//			defineGeometry();
//		}
//		
//		catch(IOException e)
//		{
//			System.out.println("invalid file!");
//			e.printStackTrace();
//		}
//	}

    /**
     * Gets the solid mean
     *
     * @return point representing the mean
     */
    protected Vector3 getMean() {
        Vector3 mean = new Vector3();
        for (int i = 0; i < vertexArray.length; i++) {
            mean.x += vertexArray[i].x;
            mean.y += vertexArray[i].y;
            mean.z += vertexArray[i].z;
        }
        mean.x /= vertexArray.length;
        mean.y /= vertexArray.length;
        mean.z /= vertexArray.length;

        return mean;
    }
}