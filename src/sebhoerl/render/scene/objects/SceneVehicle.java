package sebhoerl.render.scene.objects;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import sebhoerl.render.scene.AABB;
import sebhoerl.render.scene.SceneObject;

public class SceneVehicle implements SceneObject {
    final private double width;
    final private double length;
    final private double height;
    
    private Vector2D position = new Vector2D(0.0, 0.0);
    private double angle = 0.0;
    private boolean active = false;
    
    public SceneVehicle(double width, double length, double height) {
        this.width = width;
        this.length = length;
        this.height = height;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setPosition(double x, double y) {
        this.position = new Vector2D(x, y);
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    @Override
    public AABB getBoundingBox() {
        return null;
    }

    @Override
    public Vector2D getPosition() {
        return position;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getLength() {
        return length;
    }
    
    public double getHeight() {
        return height;
    }
}
