package net.masonapps.csgvr.csg;

/**
 * Created by Bob on 5/8/2017.
 */

public class CSG {

    private BSPTreeNode tree;

    public CSG(BSPTreeNode tree) {
        this.tree = tree;
    }

    public CSG union(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        a.build(b.getAllPolygons());
        return new CSG(a);
    }

    public CSG subtract(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.getAllPolygons());
        a.invert();
        return new CSG(a);
    }

    public CSG intersect(CSG other) {
        BSPTreeNode a = tree.copy();
        BSPTreeNode b = other.tree.copy();
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.getAllPolygons());
        return new CSG(a);
    }
}
