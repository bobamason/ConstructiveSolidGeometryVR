package net.masonapps.csgvr.csg;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Bob on 5/5/2017.
 */

public class BSPTree{
    
    public Array<CSGPolygon> polygons;
    public CSGPlane divider = new CSGPlane();
    public BSPTree front = null;
    public BSPTree back = null;

    public BSPTree() {
        this.polygons = new Array<>();
    }
    
    public void build(Array<CSGPolygon> polygons){
        if(polygons.size == 0) return;
        Array<CSGPolygon> frontPolygons = new Array<>();
        Array<CSGPolygon> backPolygons = new Array<>();
        for (int i = 0; i < polygons.size; i++) {
            divider.splitPolygon(polygons.get(i), polygons, polygons, frontPolygons, backPolygons);
        }
    }

    public void add(CSGPolygon polygon){
        
    }

    public void clip(BSPTree other){
        
    }
    
    public void invert() {
        
    }
    
    public Array<CSGPolygon> getAllPolygons(Array<CSGPolygon> out){
        return out;
    }
    
    public BSPTree copy(){
        return new BSPTree();
    }
}
