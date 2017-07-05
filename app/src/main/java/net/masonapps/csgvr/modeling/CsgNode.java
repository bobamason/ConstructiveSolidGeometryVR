package net.masonapps.csgvr.modeling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.masonapps.csgvr.primitives.Primitive;

/**
 * Created by Bob on 7/5/2017.
 */

public class CsgNode {

    boolean isLeaf;
    @Nullable
    private CsgNode parent = null;
    @Nullable
    private CsgNode left = null;
    @Nullable
    private CsgNode right = null;
    @Nullable
    private Operator operator = null;
    @Nullable
    private Primitive primitive = null;

    public CsgNode(@Nullable CsgNode parent, @NonNull CsgNode left, @NonNull CsgNode right, @NonNull Operator operator) {
        this.parent = parent;
        this.left = left;
        this.right = right;
        this.operator = operator;
        isLeaf = false;
    }

    public CsgNode(@Nullable CsgNode parent, @NonNull Primitive primitive) {
        this.parent = parent;
        this.primitive = primitive;
        isLeaf = false;
    }

    public Operator getOperator() {
        return operator;
    }

    public CsgNode getLeft() {
        return left;
    }

    public CsgNode getRight() {
        return right;
    }

    public enum Operator {
        UNION, DIFFERENCE, INTERSECTION
    }
}
