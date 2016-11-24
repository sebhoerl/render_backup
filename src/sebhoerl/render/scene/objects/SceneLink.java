package sebhoerl.render.scene.objects;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import sebhoerl.render.scene.AABB;
import sebhoerl.render.scene.SceneObject;

public class SceneLink implements SceneObject {
    final private Vector2D from;
    final private Vector2D to;
    final private AABB boundingBox;
    final private double angle;
    
    public SceneLink(Vector2D from, Vector2D to) {
        this.from = from;
        this.to = to;
        
        Vector2D diff = to.subtract(from);
        angle = Math.atan2(diff.getY(), diff.getX()) * 180.0 / Math.PI;
        
        boundingBox = AABB.fromVertices(new Vector2D[]{ to, from });
    }
    
    public double getAngle() {
        return angle;
    }
    
    public Vector2D getFrom() {
        return from;
    }
    
    public Vector2D getTo() {
        return to;
    }
    
    @Override
    public AABB getBoundingBox() {
        return boundingBox;
    }

    @Override
    public Vector2D getPosition() {
        return from.add(to.subtract(from).scalarMultiply(0.5));
    }
}
