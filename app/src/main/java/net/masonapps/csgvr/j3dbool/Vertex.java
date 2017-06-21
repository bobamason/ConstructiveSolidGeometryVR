package net.masonapps.csgvr.j3dbool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

/**
 * Represents of a 3d face vertex.
 * <p>
 * <br><br>See:
 * D. H. Laidlaw, W. B. Trumbore, and J. F. Hughes.
 * "Constructive Solid Geometry for Polyhedral Objects"
 * SIGGRAPH Proceedings, 1986, p.161.
 *
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 */
public class Vertex implements Cloneable {
    /**
     * vertex status if it is still unknown
     */
    public static final int UNKNOWN = 1;
    /**
     * vertex status if it is inside a solid
     */
    public static final int INSIDE = 2;
    /**
     * vertex status if it is outside a solid
     */
    public static final int OUTSIDE = 3;
    /**
     * vertex status if it on the boundary of a solid
     */
    public static final int BOUNDARY = 4;
    /**
     * tolerance value to test equalities
     */
    private static final float TOL = 1e-5f;
    /**
     * vertex coordinate in X
     */
    public float x;
    /**
     * vertex coordinate in Y
     */
    public float y;
    /**
     * vertex coordinate in Z
     */
    public float z;
    /**
     * vertex normal
     */
    public Vector3 normal = new Vector3();
    /**
     * references to vertexArray conected to it by an edge
     */
    private ArrayList<Vertex> adjacentVertices;
    /**
     * vertex status relative to other object
     */
    private int status;
    /**
     * vertex color
     */
    private Color color;

    //----------------------------------CONSTRUCTORS--------------------------------//

    /**
     * Constructs a vertex with unknown status
     *
     * @param position vertex position
     * @param color    vertex color
     */
    public Vertex(Vector3 position, Color color) {
        this.color = color.cpy();

        x = position.x;
        y = position.y;
        z = position.z;

        adjacentVertices = new ArrayList();
        status = UNKNOWN;
    }

    /**
     * Constructs a vertex with unknown status
     *
     * @param x     coordinate on the x axis
     * @param y     coordinate on the y axis
     * @param z     coordinate on the z axis
     * @param color vertex color
     */
    public Vertex(float x, float y, float z, Color color) {
        this.color = color.cpy();

        this.x = x;
        this.y = y;
        this.z = z;

        adjacentVertices = new ArrayList();
        status = UNKNOWN;
    }

    /**
     * Constructs a vertex with definite status
     *
     * @param position vertex position
     * @param color    vertex color
     * @param status   vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public Vertex(Vector3 position, Color color, int status) {
        this.color = color.cpy();

        x = position.x;
        y = position.y;
        z = position.z;

        adjacentVertices = new ArrayList();
        this.status = status;
    }

    /**
     * Constructs a vertex with a definite status
     *
     * @param x      coordinate on the x axis
     * @param y      coordinate on the y axis
     * @param z      coordinate on the z axis
     * @param color  vertex color
     * @param status vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public Vertex(float x, float y, float z, Color color, int status) {
        this.color = color.cpy();

        this.x = x;
        this.y = y;
        this.z = z;

        adjacentVertices = new ArrayList();
        this.status = status;
    }

    //-----------------------------------OVERRIDES----------------------------------//

    /**
     * Clones the vertex object
     *
     * @return cloned vertex object
     */
    public Object clone() {
        try {
            Vertex clone = (Vertex) super.clone();
            clone.x = x;
            clone.y = y;
            clone.z = z;
            clone.color = color.cpy();
            clone.status = status;
            clone.adjacentVertices = new ArrayList();
            for (int i = 0; i < adjacentVertices.size(); i++) {
                clone.adjacentVertices.add((Vertex) adjacentVertices.get(i).clone());
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Makes a string definition for the Vertex object
     *
     * @return the string definition
     */
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    /**
     * Checks if an vertex is equal to another. To be equal, they have to have the same
     * coordinates(with some tolerance) and color
     *
     * @param anObject the other vertex to be tested
     * @return true if they are equal, false otherwise.
     */
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Vertex)) {
            return false;
        } else {
            Vertex vertex = (Vertex) anObject;
            return Math.abs(x - vertex.x) < TOL && Math.abs(y - vertex.y) < TOL
                    && Math.abs(z - vertex.z) < TOL && color.equals(vertex.color);
        }
    }

    //--------------------------------------SETS------------------------------------//

    /**
     * Gets the vertex position
     *
     * @return vertex position
     */
    public Vector3 getPosition() {
        return new Vector3(x, y, z);
    }

    //--------------------------------------GETS------------------------------------//

    /**
     * Gets an array with the adjacent vertexArray
     *
     * @return array of the adjacent vertexArray
     */
    public Vertex[] getAdjacentVertices() {
        Vertex[] vertices = new Vertex[adjacentVertices.size()];
        for (int i = 0; i < adjacentVertices.size(); i++) {
            vertices[i] = adjacentVertices.get(i);
        }
        return vertices;
    }

    /**
     * Gets the vertex status
     *
     * @return vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the vertex status
     *
     * @param status vertex status - UNKNOWN, BOUNDARY, INSIDE or OUTSIDE
     */
    public void setStatus(int status) {
        if (status >= UNKNOWN && status <= BOUNDARY) {
            this.status = status;
        }
    }

    /**
     * Gets the vertex color
     *
     * @return vertex color
     */
    public Color getColor() {
        return color.cpy();
    }

    //----------------------------------OTHERS--------------------------------------//

    /**
     * Sets a vertex as being adjacent to it
     *
     * @param adjacentVertex an adjacent vertex
     */
    public void addAdjacentVertex(Vertex adjacentVertex) {
        if (!adjacentVertices.contains(adjacentVertex)) {
            adjacentVertices.add(adjacentVertex);
        }
    }

    /**
     * Sets the vertex status, setting equally the adjacent ones
     *
     * @param status new status to be set
     */
    public void mark(int status) {
        //mark vertex
        this.status = status;

        //mark adjacent vertexArray
        Vertex[] adjacentVerts = getAdjacentVertices();
        for (int i = 0; i < adjacentVerts.length; i++) {
            if (adjacentVerts[i].getStatus() == Vertex.UNKNOWN) {
                adjacentVerts[i].mark(status);
            }
        }
    }
}