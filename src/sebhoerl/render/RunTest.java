package sebhoerl.render;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import sebhoerl.render.logic.LinkPassage;
import sebhoerl.render.logic.NetworkLogic;
import sebhoerl.render.scene.AABB;
import sebhoerl.render.scene.Camera;
import sebhoerl.render.scene.Perspective;
import sebhoerl.render.scene.SceneGraph;
import sebhoerl.render.scene.objects.SceneLink;
import sebhoerl.render.scene.objects.SceneVehicle;
import sebhoerl.render.scene.renderers.SceneGraphRenderer;

public class RunTest extends JFrame implements GLEventListener {
    private static final long serialVersionUID = 1L;
    private Network network = NetworkUtils.createNetwork();
    
    final private GLCanvas canvas;
    
    final private SceneGraph sceneGraph;
    final private SceneGraphRenderer renderer;
    final private Camera camera;
    final private Perspective perspective;
    
    final private NetworkLogic networkLogic;
    private LinkPassage passage;
    
    
    public RunTest() {
        super("Minimal OpenGL");
        this.setSize(800, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        
        (new MatsimNetworkReader(network)).readFile("/home/sebastian/thesis/sensitivity/data/network.xml");
        double[] bb = NetworkUtils.getBoundingBox(network.getNodes().values());
        
        double minx = bb[0];
        double miny = bb[1];
        
        double dx = bb[2] - bb[0];
        double dy = bb[3] - bb[1];
        
        double fx = 1.0 / dx;
        double fy = 1.0 / dy;
        
        fy *= dy / dx;
        
        //if (dx < dy) {
        //    fy *= dy / dx;
        //} else {
        //    fx *= dx / dy;
        //}
        
        double f = fx;
        
        perspective = new Perspective(800.0, 800.0, 45.0, 0.01, 100.0);
        camera = new Camera(perspective);
        
        camera.setPosition(new Vector3D(0.5, 1.5, 1.0));
        camera.setTarget(new Vector3D(0.5, 0.5, 0.0));
        camera.setUp(new Vector3D(0.0, 0.0, 1.0));
        
        AABB worldBoundingBox = new AABB(new Vector2D(0.0, 0.0), new Vector2D(1.0, 1.0));
        sceneGraph = new SceneGraph(worldBoundingBox);
        
        renderer = new SceneGraphRenderer(camera);  
        networkLogic = new NetworkLogic();
        
        final Map<Id<Link>, SceneLink> links = new HashMap<>();
        
        for (Link link : network.getLinks().values()) {
            Coord start = link.getFromNode().getCoord();
            Coord end = link.getToNode().getCoord();
            
            Vector2D from = new Vector2D((start.getX() - minx) * fx, (start.getY() - miny) * fy);
            Vector2D to = new Vector2D((end.getX() - minx) * fx, (end.getY() - miny) * fy);
                    
            SceneLink sceneLink = new SceneLink(from, to);
            sceneGraph.add(sceneLink);
            
            links.put(link.getId(), sceneLink);
        }
        
        final Map<Id<Vehicle>, SceneVehicle> vehicles = new HashMap<>();
        EventsManager events = new EventsManagerImpl();
        
        final Map<Id<Vehicle>, LinkEnterEvent> enterLinkEvents = new HashMap<>();
        
        events.addHandler(new BasicEventHandler() {
            @Override
            public void reset(int iteration) {}
            
            void addTraversal(Id<Link> link, Id<Vehicle> vehicleId, double startTime, double endTime, boolean av) {
                SceneVehicle sceneVehicle = vehicles.get(vehicleId);
                
                if (sceneVehicle == null) {
                    sceneVehicle = new SceneVehicle(0.01, 0.02, 0.01);
                    vehicles.put(vehicleId, sceneVehicle);
                    sceneGraph.add(sceneVehicle);
                }
                
                LinkPassage passage = new LinkPassage(startTime, endTime, links.get(link), sceneVehicle);
                networkLogic.addPassage(passage);
            }

            @Override
            public void handleEvent(Event event) {
                if (event instanceof LinkEnterEvent) {
                    //if (!((LinkEnterEvent) event).getVehicleId().toString().contains("bus")) return;
                    
                    enterLinkEvents.put(((LinkEnterEvent) event).getVehicleId(), (LinkEnterEvent) event);
                } else if (event instanceof VehicleLeavesTrafficEvent) {
                    //if (!((VehicleLeavesTrafficEvent) event).getVehicleId().toString().contains("bus")) return;
                    
                    VehicleLeavesTrafficEvent levt = (VehicleLeavesTrafficEvent) event;
                    
                    if (enterLinkEvents.containsKey(levt.getVehicleId())) {
                        enterLinkEvents.remove(levt.getVehicleId());
                    }
                } else if (event instanceof LinkLeaveEvent) {
                    //if (!((LinkLeaveEvent) event).getVehicleId().toString().contains("bus")) return;
                    
                    LinkLeaveEvent lle = (LinkLeaveEvent) event;
                    
                    if (enterLinkEvents.containsKey(lle.getVehicleId())) {
                        LinkEnterEvent lee = enterLinkEvents.remove(lle.getVehicleId());
                        addTraversal(lle.getLinkId(), lle.getVehicleId(), lee.getTime(), lle.getTime(), lee.getVehicleId().toString().contains("av"));
                    }
                }
            }
        });
        
        new MatsimEventsReader(events).readFile("/home/sebastian/thesis/av_baseline/baseline/output_events.xml.gz");
        
        networkLogic.finish();
        
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        this.getContentPane().add(canvas);
        
        this.setVisible(true);
        this.setResizable(true);
        canvas.requestFocusInWindow();
        
        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();
        
        networkLogic.reset(3600.0 * 9.0);
    }
    
    public static void main(String[] args) {
        new RunTest();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        camera.apply(gl);
        networkLogic.advance(0.1);
        renderer.render(gl, sceneGraph);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void init(GLAutoDrawable drawable) {}

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        
        perspective.update(width, height);
        perspective.apply(gl);
    }
}
