package sebhoerl.render.scene;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class Camera {
    final private GLU glu = new GLU();
    private Perspective perspective;
    
    private double[] position = {0.0, 0.0, 0.0};
    private double[] target = {1.0, 0.0, 0.0};
    private double[] up = {0.0, 0.0, 1.0};
    
    private double[] modelview = new double[16];
    
    public Camera(Perspective perspective) {
        this.perspective = perspective;
    }
    
    public void apply(GL2 gl) {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        
        glu.gluLookAt(
                position[0], position[1], position[2], 
                target[0], target[1], target[2], 
                up[0], up[1], up[2]);
    }
    
    public ScreenRay getScreenRay(int x, int y) {
        return perspective.getScreenRay(x, y, modelview);
    }
    
    public void setPosition(Vector3D position) {
        this.position = position.toArray();
    }
    
    public Vector3D getPosition() {
        return new Vector3D(position);
    }
    
    public void setTarget(Vector3D target) {
        this.target = target.normalize().toArray();
    }
    
    public Vector3D getTarget() {
        return new Vector3D(target);
    }
    
    public void setUp(Vector3D up) {
        this.up = up.normalize().toArray();
    }
    
    public Vector3D getUp() {
        return new Vector3D(up);
    }
}
