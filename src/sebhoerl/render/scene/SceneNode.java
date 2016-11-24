package sebhoerl.render.scene;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class SceneNode implements SceneObject {
    private AABB boundingBox;
    
    private SceneObject leafChild = null;
    private SceneNode children[] = null;
    
    public SceneNode(AABB boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public AABB getBoundingBox() {
        return boundingBox;
    }
    
    public boolean isLeaf() {
        return children == null;
    }
    
    public boolean isEmpty() {
        return isLeaf() && leafChild == null;
    }
    
    public void remove(SceneObject object) {
        if (leafChild == object) {
            leafChild = null;
        } else if (!isLeaf() && covers(object.getPosition())) {
            for (SceneNode child : children) child.remove(object);
        }
    }
    
    public boolean covers(Vector2D position) {
        return boundingBox.contains(position);
    }
    
    public boolean contains(SceneObject object) {
        if (leafChild == object) {
            return true;
        } else if (!isLeaf()) {
            for (SceneNode child : children) {
                if (child.contains(object)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void expand() {
        AABB childBoundingBoxes[] = boundingBox.split();
        children = new SceneNode[childBoundingBoxes.length];
        
        for (int i = 0; i < childBoundingBoxes.length; i++) {
            children[i] = new SceneNode(childBoundingBoxes[i]);
        }
        
        if (!isEmpty()) {
            addToChildren(leafChild);
        }
    }
    
    void addToChildren(SceneObject object) {
        for (SceneNode child : children) {
            if (child.add(object)) {
                return;
            }
        }
        
        throw new RuntimeException();
    }
    
    public boolean add(SceneObject object) {
        SceneNode current = this;
        
        while (!current.isLeaf()) { 
            SceneNode next = null;
            
            for (SceneNode child : current.getChildren()) {
                if (child.covers(object.getPosition())) {
                    next = child;
                }
            }
            
            if (next == null) {
                throw new RuntimeException();
            } else {
                current = next;
            }
        }
        
        current.add(object);
        
        
        
        
        if (covers(object.getPosition())) {
            if (isEmpty()) {
                leafChild = object;
                return true;
            } else if (isLeaf()) {
                expand();
                
                addToChildren(leafChild);
                leafChild = null;
            }
            
            addToChildren(object);
            return true;
        }
        
        return false;
    }

    @Override
    public Vector2D getPosition() {
        return Vector2D.ZERO;
    }
    
    public SceneObject getLeafChild() {
        return leafChild;
    }
    
    public SceneNode[] getChildren() {
        return children;
    }
    
    /*final static Vector2D UNIT_X = new Vector2D(1.0, 0.0);
    
    @Override
    public Vector2D getOrientation() {
        return UNIT_X;
    }
    
    @Override
    public Collection<SceneObject> getChildren() {
        if (isLeaf()) {
            return Arrays.asList(leafChild);
        } else {
            return Arrays.asList(children);
        }
    }*/
}
