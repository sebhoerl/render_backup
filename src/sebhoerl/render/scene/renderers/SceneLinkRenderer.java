package sebhoerl.render.scene.renderers;

import com.jogamp.opengl.GL2;
import sebhoerl.render.scene.SceneObject;
import sebhoerl.render.scene.objects.SceneLink;

public class SceneLinkRenderer {
    final SceneObjectRenderer objectRenderer;
    
    public SceneLinkRenderer(SceneObjectRenderer objectRenderer) {
        this.objectRenderer = objectRenderer;
    }
    
    public void renderLink(GL2 gl, SceneLink link) {
        objectRenderer.renderObject(gl, link);
        
        gl.glBegin(GL2.GL_LINES);
        
        gl.glColor3d(1.0, 1.0, 1.0);
        gl.glVertex3d(link.getFrom().getX(), link.getFrom().getY(), 0.0);
        gl.glVertex3d(link.getTo().getX(), link.getTo().getY(), 0.0);
        
        gl.glEnd();
    }
}
