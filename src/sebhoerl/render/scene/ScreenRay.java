package sebhoerl.render.scene;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class ScreenRay {
    final private Vector3D position;
    final private Vector3D direction;
    
    public ScreenRay(Vector3D position, Vector3D direction) {
        this.position = position;
        this.direction = direction;
    }
    
    public Vector3D getPosition() {
        return position;
    }
    
    public Vector3D getDirection() {
        return direction;
    }
}
