package sebhoerl.render.scene.renderers;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.jogamp.opengl.GL2;

import sebhoerl.render.scene.Camera;
import sebhoerl.render.scene.SceneGraph;
import sebhoerl.render.scene.SceneNode;
import sebhoerl.render.scene.SceneObject;
import sebhoerl.render.scene.objects.SceneLink;
import sebhoerl.render.scene.objects.SceneVehicle;

public class SceneGraphRenderer {
    final private Camera camera;
    final private SceneObjectRenderer objectRenderer;
    final private SceneLinkRenderer linkRenderer;
    final private SceneVehicleRenderer vehicleRenderer;
    
    public SceneGraphRenderer(Camera camera) {
        this.camera = camera;
        
        objectRenderer = new SceneObjectRenderer();
        linkRenderer = new SceneLinkRenderer(objectRenderer);
        vehicleRenderer = new SceneVehicleRenderer(objectRenderer);
    }
    
    public boolean isVisible(SceneObject object) {
        //AABB boundingBox = object.getBoundingBox();
       // boolean visible = false;
        
        return true;
    }
    
    public void render(GL2 gl, SceneGraph graph) {
        final Vector2D cameraPosition = new Vector2D(camera.getPosition().getX(), camera.getPosition().getY());
        
        // Can be changed to normal queue for better performance
        //Queue<SceneObject> pending = new LinkedList<SceneObject>();
        
        PriorityQueue<SceneObject> pending = new PriorityQueue<SceneObject>(new Comparator<SceneObject>() {
            @Override
            public int compare(SceneObject o1, SceneObject o2) {
                double d1 = cameraPosition.distanceSq(o1.getPosition());
                double d2 = cameraPosition.distanceSq(o2.getPosition());
                
                return (d1 < d2) ? -1 : 1;
            }
        });
        
        for (SceneObject object : graph.getChildren()) {
            pending.add(object);
        }
        
        while (!pending.isEmpty()) {
            SceneObject top = pending.poll();
            
            if (top instanceof SceneLink) {
                linkRenderer.renderLink(gl, (SceneLink) top);
            } else if (top instanceof SceneVehicle) {
                vehicleRenderer.renderVehicle(gl, (SceneVehicle) top);
            }
        }
        
        /*while (!pending.isEmpty()) {
            //System.err.println(pending.size());
            
            SceneObject top = pending.poll();
            
            // Traversing the scene graph and culling
            if (top instanceof SceneNode) {
                SceneNode node = (SceneNode) top;
                objectRenderer.renderObject(gl, top);
                
                if (!node.isEmpty()) {
                    if (node.isLeaf()) {
                        if (isVisible(node)) pending.add(node.getLeafChild());
                    } else {
                        for (SceneNode child : node.getChildren()) {
                            if (isVisible(node)) pending.add(child);
                        }
                    }
                }
            // From here rendering of the objects
            } else if (top instanceof SceneLink) {
                linkRenderer.renderLink(gl, (SceneLink) top);
            }
        }*/
        
        
    }
}
