package sebhoerl.render.scene;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public interface SceneObject {
    AABB getBoundingBox();
    
    Vector2D getPosition();
}
