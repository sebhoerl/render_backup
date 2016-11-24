package sebhoerl.render.scene.renderers;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jogamp.opengl.GL2;
import sebhoerl.render.scene.SceneObject;
import sebhoerl.render.scene.objects.SceneLink;
import sebhoerl.render.scene.objects.SceneVehicle;

public class SceneVehicleRenderer {
    final SceneObjectRenderer objectRenderer;
    
    public SceneVehicleRenderer(SceneObjectRenderer objectRenderer) {
        this.objectRenderer = objectRenderer;
    }
    
    public void renderVehicle(GL2 gl, SceneVehicle vehicle) {
        objectRenderer.renderObject(gl, vehicle);
        
        if (vehicle.isActive()) {
        
        Vector2D lowerLeft = new Vector2D(-vehicle.getLength() * 0.5, -vehicle.getWidth() * 0.5);
        Vector2D upperLeft = new Vector2D(-vehicle.getLength() * 0.5, vehicle.getWidth() * 0.5);
        Vector2D upperRight = new Vector2D(vehicle.getLength() * 0.5, vehicle.getWidth() * 0.5);
        Vector2D lowerRight = new Vector2D(vehicle.getLength() * 0.5, -vehicle.getWidth() * 0.5);
        
        double height = vehicle.getHeight();
            gl.glPushMatrix();
            gl.glTranslated(vehicle.getPosition().getX(), vehicle.getPosition().getY(), 0.0);
            gl.glRotated(vehicle.getAngle(), 0.0, 0.0, 1.0);
            
            gl.glBegin(GL2.GL_LINES);
            
            gl.glColor3d(1.0, 0.0, 0.0);
            
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), 0.0);
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), 0.0);
            
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), 0.0);
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), 0.0);
            
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), 0.0);
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), 0.0);
            
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), 0.0);
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), 0.0);
            
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), height);
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), height);
            
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), height);
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), height);
            
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), height);
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), height);
            
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), height);
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), height);
            
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), 0.0);
            gl.glVertex3d(lowerLeft.getX(), lowerLeft.getY(), height);
            
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), 0.0);
            gl.glVertex3d(upperLeft.getX(), upperLeft.getY(), height);
            
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), 0.0);
            gl.glVertex3d(upperRight.getX(), upperRight.getY(), height);
            
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), 0.0);
            gl.glVertex3d(lowerRight.getX(), lowerRight.getY(), height);
            
            gl.glEnd();
            
            gl.glPopMatrix();
        }
    }
}
