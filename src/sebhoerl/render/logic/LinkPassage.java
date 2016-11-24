package sebhoerl.render.logic;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import sebhoerl.render.scene.objects.SceneLink;
import sebhoerl.render.scene.objects.SceneVehicle;

public class LinkPassage {
    SceneVehicle vehicle;
    SceneLink link;
    
    double startTime;
    double endTime;
    
    public LinkPassage(double startTime, double endTime, SceneLink link, SceneVehicle vehicle) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.link = link;
        this.vehicle = vehicle;
    }
    
    public SceneVehicle getVehicle() {
        return vehicle;
    }
    
    public void setVehicle(SceneVehicle vehicle) {
        this.vehicle = vehicle;
    }
    
    public double getStartTime() {
        return startTime;
    }
    
    public double getEndTime() {
        return endTime;
    }
    
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }
    
    public void advance(double time) {
        Vector2D diff = link.getTo().subtract(link.getFrom());
        Vector2D position = link.getFrom().add(diff.scalarMultiply((time - startTime) / (endTime - startTime)));
        
        vehicle.setPosition(position.getX(), position.getY());
        vehicle.setAngle(link.getAngle());
        vehicle.setActive(true);
    }
}
