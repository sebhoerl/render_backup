package sebhoerl.render.scene;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class Perspective {
    final private GLU glu = new GLU();
    
    private double width;
    private double height;
    private double fov;
    private double near;
    private double far;
    
    private int viewport[] = new int[4];
    private double projection[] = new double[16];
    
    public Perspective(double width, double height, double fov, double near, double far) {
        this.width = width;
        this.height = height;
        this.fov = fov;
        this.near = near;
        this.far = far;
    }
    
    public void update(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    public void apply(GL2 gl) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(fov, width / height, near, far);
        
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
    }
    
    public ScreenRay getScreenRay(int x, int y, double[] modelview) {
        double coordinates[] = new double[6];
        
        glu.gluUnProject(
                (double)x, (double)(height - y), 0.0,
                modelview, 0, projection, 0, viewport, 0, coordinates, 0);
        
        glu.gluUnProject(
                (double)x, (double)(height - y), 1.0,
                modelview, 0, projection, 0, viewport, 0, coordinates, 3);
        
        Vector3D position = new Vector3D(coordinates[0], coordinates[1], coordinates[2]);
        Vector3D direction = new Vector3D(coordinates[3] - coordinates[0], coordinates[4] - coordinates[1], coordinates[5] - coordinates[2]);
        
        return new ScreenRay(position, direction);
    }
}
