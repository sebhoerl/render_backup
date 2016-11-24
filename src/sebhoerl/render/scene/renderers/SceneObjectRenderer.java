package sebhoerl.render.scene.renderers;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jogamp.opengl.GL2;

import sebhoerl.render.scene.AABB;
import sebhoerl.render.scene.SceneObject;

public class SceneObjectRenderer {
    private boolean drawBoundingBoxes = false;
    private double boundingBoxColor[] = { 0.0, 1.0, 0.0 };
    
    public void renderObject(GL2 gl, SceneObject object) {
        if (drawBoundingBoxes) {
            AABB aabb = object.getBoundingBox();
            
            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glColor3dv(boundingBoxColor, 0);
            
            for (Vector2D vertex : aabb.getCorners()) {
                gl.glVertex3d(vertex.getX(), vertex.getY(), 0.0);
            }
            
            gl.glEnd();
            
            gl.glBegin(GL2.GL_POINTS);
            gl.glColor3d(1.0, 0.0, 0.0);
            gl.glVertex3d(aabb.getCenter().getX(), aabb.getCenter().getY(), 0.0);
            gl.glEnd();
        }
    }
}
