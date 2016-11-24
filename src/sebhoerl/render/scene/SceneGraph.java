package sebhoerl.render.scene;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class SceneGraph {
    private AABB boundingBox;
    private LinkedList<SceneObject> children = new LinkedList<>();
    
    public SceneGraph(AABB boundingBox) {
        this.boundingBox = boundingBox;
    }
    
    public AABB getBoundingBox() {
        return boundingBox;
    }
    
    public void add(SceneObject object) {
        children.add(object);
    }
    
    public void remove(SceneObject object) {
        children.remove(object);
    }
    
    public Collection<SceneObject> getChildren() {
        return children;
    }
}
