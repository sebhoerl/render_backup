package sebhoerl.render;

import java.util.PriorityQueue;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.glu.GLU;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class RunRenderer extends JFrame implements GLEventListener, MouseWheelListener, MouseMotionListener, MouseListener {
    private static final long serialVersionUID = 1L;
    private Network network = NetworkUtils.createNetwork();
    private double[] networkLinks;
    private GLU glu = new GLU();
    
    private PriorityQueue<Traversal> pendingQueue = new PriorityQueue<Traversal>(new Comparator<Traversal>() {
        @Override
        public int compare(Traversal o1, Traversal o2) {
            if (o1.start < o2.start) {
                return -1;
            } else {
                return 1;
            }
        }
    });
    
    private Queue<Traversal> activeQueue = new LinkedList<Traversal>();

    public static void main(String[] args) {
        (new RunRenderer()).run();
    }
    
    public RunRenderer() {
        super("Minimal OpenGL");
        this.setSize(800, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setSampleBuffers(true);
        
        previous = System.nanoTime();
        
        //canvas.getAnimator().start();
        
        (new MatsimNetworkReader(network)).readFile("/home/sebastian/thesis/sensitivity/data/network.xml");
        double[] bb = NetworkUtils.getBoundingBox(network.getNodes().values());
        
        networkLinks = new double[network.getLinks().size() * 4];
        int index = 0;
        
        for (Link link : network.getLinks().values()) {
            Coord start = link.getFromNode().getCoord();
            Coord end = link.getToNode().getCoord();
            
            networkLinks[index] = (start.getX() - bb[0]) / (bb[2] - bb[0]);
            networkLinks[index + 1] = (start.getY() - bb[1]) / (bb[3] - bb[1]);
            networkLinks[index + 2] = (end.getX() - bb[0]) / (bb[2] - bb[0]);
            networkLinks[index + 3] = (end.getY() - bb[1]) / (bb[3] - bb[1]);
            
            index += 4;
        }
        
        /*Traversal traversal = new Traversal();
        traversal.fromCoords[0] = 0.0f;
        traversal.fromCoords[1] = 0.0f;
        traversal.toCoords[0] = 1.0f;
        traversal.toCoords[1] = 1.0f;
        traversal.speed = 0.1;
        traversal.start = 9.0 * 3600.0;
        traversal.end = 10.0 * 3600.0;
        pendingQueue.add(traversal);*/
        
        EventsManager events = new EventsManagerImpl();
        
        final Map<Id<Vehicle>, LinkEnterEvent> enterLinkEvents = new HashMap<>();
        
        events.addHandler(new BasicEventHandler() {
            @Override
            public void reset(int iteration) {}
            
            void addTraversal(Id<Link> link, double startTime, double endTime, boolean av) {
                Coord fromCoords = network.getLinks().get(link).getFromNode().getCoord();
                Coord toCoords = network.getLinks().get(link).getToNode().getCoord();
                
                Traversal traversal = new Traversal();
                traversal.fromCoords[0] = (fromCoords.getX() - bb[0]) / (bb[2] - bb[0]);
                traversal.fromCoords[1] = (fromCoords.getY() - bb[1]) / (bb[3] - bb[1]);
                traversal.toCoords[0] = (toCoords.getX() - bb[0]) / (bb[2] - bb[0]);
                traversal.toCoords[1] = (toCoords.getY() - bb[1]) / (bb[3] - bb[1]);
                traversal.av = av;
                
                double dx = traversal.fromCoords[0] - traversal.toCoords[0];
                double dy = traversal.fromCoords[1] - traversal.toCoords[1];
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                traversal.speed = dist / (endTime - startTime);
                traversal.start = startTime;
                traversal.end = endTime;
                pendingQueue.add(traversal);
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
                        addTraversal(lle.getLinkId(), lee.getTime(), lle.getTime(), lee.getVehicleId().toString().contains("av"));
                    }
                }
            }
        });
        
        new MatsimEventsReader(events).readFile("/home/sebastian/thesis/av_baseline/baseline/output_events.xml.gz");
        
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        this.getContentPane().add(canvas);
        
        this.setVisible(true);
        this.setResizable(true);
        canvas.requestFocusInWindow();
        
        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();
        
        canvas.addMouseWheelListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(this);
    }
    
    GLCanvas canvas;
    
    public void run() {
        
    }
    
    private double value = 1.0;
    private long previous = 0;
    
    private double angle = 0.0;
    private double height = 0.2;
    double zoom = 0.2;
    private double radius = 1.0;
    
    private double tf = 10.0;
    private double time = 8.0 * 3600.0;
    
    private double centerX = 0.5;
    private double centerY = 0.5;
    
    private double dirX = 0.5;
    private double dirY = 0.5;
    private double dirZ = 0.5;

    public void compute(double delta, double time) {
        while (pendingQueue.size() > 0 && pendingQueue.peek().start <= time) {
            activeQueue.add(pendingQueue.poll());
        }
        
        Iterator<Traversal> iterator = activeQueue.iterator();
        
        while (iterator.hasNext()) {
            Traversal current = iterator.next();
            
            current.current[0] = current.fromCoords[0] + (current.toCoords[0] - current.fromCoords[0]) * (time - current.start) / (current.end - current.start);
            current.current[1] = current.fromCoords[1] + (current.toCoords[1] - current.fromCoords[1]) * (time - current.start) / (current.end - current.start);
            
            if (current.end < time) {
                iterator.remove();
            }
        }
    }
    
    void renderCar(GL2 gl, double x, double y, double dx, double dy, boolean av) {
        double carWidth = 0.001;
        double carLength = 0.002;
        double carHeight = 0.001;
        
        double[] topLeft = {carWidth * 0.5, 0.0};
        double[] topRight = {carWidth * 0.5, carLength};
        double[] bottomLeft = {-carWidth * 0.5, 0.0};
        double[] bottomRight = {-carWidth * 0.5, carLength};
        
        gl.glPushMatrix();
        
        gl.glTranslated(x, y, 0.0);
        gl.glRotated(90.0 + Math.atan2(dy, dx) / Math.PI * 180.0, 0.0, 0.0, 1.0);
        
        gl.glBegin(GL2.GL_LINES);
        
        if (av) {
            gl.glColor3d(1.0, 0.0, 0.0);
        } else {
            gl.glColor3d(0.0, 0.0, 1.0);
        }
        
        // Down
        
        gl.glVertex3d(topLeft[0], topLeft[1], 0.0);
        gl.glVertex3d(topRight[0], topRight[1], 0.0);
        
        gl.glVertex3d(topRight[0], topRight[1], 0.0);
        gl.glVertex3d(bottomRight[0], bottomRight[1], 0.0);
        
        gl.glVertex3d(bottomRight[0], bottomRight[1], 0.0);
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], 0.0);
        
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], 0.0);
        gl.glVertex3d(topLeft[0], topLeft[1], 0.0);
        
        // Roof
        
        gl.glVertex3d(topLeft[0], topLeft[1], carHeight);
        gl.glVertex3d(topRight[0], topRight[1], carHeight);
        
        gl.glVertex3d(topRight[0], topRight[1], carHeight);
        gl.glVertex3d(bottomRight[0], bottomRight[1], carHeight);
        
        gl.glVertex3d(bottomRight[0], bottomRight[1], carHeight);
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], carHeight);
        
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], carHeight);
        gl.glVertex3d(topLeft[0], topLeft[1], carHeight);
        
        // Corners
        
        gl.glVertex3d(topLeft[0], topLeft[1], 0.0);
        gl.glVertex3d(topLeft[0], topLeft[1], carHeight);
        
        gl.glVertex3d(topRight[0], topRight[1], 0.0);
        gl.glVertex3d(topRight[0], topRight[1], carHeight);
        
        gl.glVertex3d(bottomRight[0], bottomRight[1], 0.0);
        gl.glVertex3d(bottomRight[0], bottomRight[1], carHeight);
        
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], 0.0);
        gl.glVertex3d(bottomLeft[0], bottomLeft[1], carHeight);
        
        gl.glEnd();
        
        gl.glPopMatrix();
    }
    
    
    
    double eyeX = 0.0;
    double eyeY = 0.0;
    double eyeZ = 0.0;
    
    double rightX = 0.0;
    double rightY = 0.0;
    double rightZ = 0.0;
    
    @Override
    public void display(GLAutoDrawable drawable) {
        long current = System.nanoTime();
        double delta = ((double)(current - previous)) * 1e-9;
        
        //delta = 1.0 / 24.0;
        
        previous = current;
        value = -0.1 * delta;
        time += delta * tf;
        
        compute(delta, time);
        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        //gl.glMatrixMode(GL2.GL_PROJECTION);
        //gl.glLoadIdentity();
        
        //gl.glScaled(2.0, 2.0, 2.0);
        //gl.glTranslated(-0.5, -0.5, 0.0);
        
        //gl.glMatrixMode(GL2.GL_MODELVIEW);
        
        if (!translating) {
            //angle += value;
        }
        
        gl.glLoadIdentity();
        
        /*gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        
        if (gl.isExtensionAvailable("GL_ARB_multisample")) {
            gl.glEnable(GL2.GL_MULTISAMPLE);
        }*/
        
        eyeX = Math.cos(angle) * radius * zoom + centerX;
        eyeY = Math.sin(angle) * radius * zoom + centerY;
        eyeZ = height * zoom;
        
        dirX = centerX - eyeX;
        dirY = centerY - eyeY;
        dirZ = 0.0 - eyeZ;
        
        rightX = Math.cos(angle + Math.PI * 0.5);
        rightY = Math.sin(angle + Math.PI * 0.5);
        rightZ = 0.0;
        
        Vector3D right = new Vector3D(rightX, rightY, rightZ);
        Vector3D dir = new Vector3D(dirX, dirY, dirZ);
        
        Vector3D up = Vector3D.crossProduct(right, dir);
        
        glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, 0.0, up.getX(), up.getY(), up.getZ());
        gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, modelview, 0 );
        
        gl.glBegin(GL2.GL_LINES);
        gl.glColor3d(0.0, 0.0, 0.0);
        
        for (int index = 0; index < networkLinks.length; index += 4) {
            gl.glVertex2dv(networkLinks, index);
            gl.glVertex2dv(networkLinks, index + 2);
        }
        
        gl.glEnd();
        
        gl.glPointSize(3.0f);
        
        /*if (translating) {
            gl.glColor3d(1.0, 1.0, 0.0);
            gl.glBegin(GL2.GL_POINTS);
            gl.glVertex3d(dragCenter.getX(), dragCenter.getY(), 0.0);
            gl.glEnd();
            
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3d(dragEye.getX(), dragEye.getY(), dragEye.getZ());
            gl.glVertex3d(dragCenter.getX(), dragCenter.getY(), dragCenter.getZ());
            
            gl.glEnd();
        }*/
        
        //gl.glBegin(GL2.GL_POINTS);
        //gl.glColor3d(1.0, 0.0, 0.0);
        //for (Traversal traversal : activeQueue) {
        //   gl.glVertex2dv(traversal.current, 0);
        //}
        //gl.glEnd();

        for (Traversal traversal : activeQueue) {
            renderCar(gl, traversal.current[0], traversal.current[1], traversal.toCoords[0] - traversal.fromCoords[0], traversal.toCoords[1] - traversal.fromCoords[1], traversal.av);
        }
        
        gl.glFlush();
        
        /*BufferedImage screenshot = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = screenshot.getGraphics();
        ByteBuffer buffer = ByteBuffer.allocate((canvasWidth + 2) * canvasHeight * 3);
        gl.glReadPixels(0, 0, canvasWidth, canvasHeight, GL2.GL_RGB, GL2.GL_BYTE, buffer);
        
        for (int h = 0; h < canvasHeight; h++) {
            for (int w = 0; w < canvasWidth; w++) {
                // The color are the three consecutive bytes, it's like referencing
                // to the next consecutive array elements, so we got red, green, blue..
                // red, green, blue, and so on..
                graphics.setColor(new Color( buffer.get()*2, buffer.get()*2, buffer.get()*2 ));
                graphics.drawRect(w, (int)(canvasHeight - h), 1, 1); // height - h is for flipping the image
            }
        }
        
        imageNumber++;
        
        try {
            ImageIO.write(screenshot, "png", new File(String.format("/home/sebastian/movie/movie_%04d.png", imageNumber)));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
    
    int imageNumber = 0;

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    int canvasWidth = 0;
    int canvasHeight = 0;

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        
        canvasWidth = width;
        canvasHeight = height;
        
        final float h = ( float ) width / ( float ) height;
        gl.glViewport( 0, 0, width, height );
        gl.glGetIntegerv( GL2.GL_VIEWPORT, viewport, 0 );
        
        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity();
        glu.gluPerspective( 45.0f, h, 0.0001, 20.0 );
        gl.glGetDoublev( GL2.GL_PROJECTION_MATRIX, projection, 0 );
        
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoom = Math.max(0.0,  zoom + 0.1 * e.getWheelRotation());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (translating) {
            double fy = (e.getX() - transX) * 0.001;
            double fx = -(e.getY() - transY) * 0.001;
            
            centerX = dragStart.getX() + dragX.getX() * fx + dragY.getX() * fy;
            centerY = dragStart.getY() + dragX.getY() * fx + dragY.getY() * fy;
        } else if (rotating) {
            angle = rotStartAngle + (rotStartX - e.getX()) * 0.01;
            height = rotStartHeight + (rotStartY - e.getY()) * 0.01;
        } else if (zooming) {
            zoom = Math.max(0.0, zoomStart + (zoomStartX - e.getY()) * 0.001);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
    
    boolean zooming = false;
    int zoomStartX = 0;
    double zoomStart = 0.0;
    
    boolean translating = false;
    Vector3D dragCenter = new Vector3D(0.0, 0.0, 0.0);  
    
    Vector3D dragEye = new Vector3D(0.0, 0.0, 0.0);
    Vector3D dragStart = new Vector3D(0.0, 0.0, 0.0);
    
    int transX = 0;
    int transY = 0;
    
    Vector2D dragX = new Vector2D(0.0, 0.0);
    Vector2D dragY = new Vector2D(0.0, 0.0);

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }
    
    int viewport[] = new int[4];
    double modelview[] = new double[16];
    double projection[] = new double[16];

    boolean rotating = false;
    
    int rotStartX = 0;
    int rotStartY = 0;
    double rotStartAngle = 0.0;
    double rotStartHeight = 0.0;
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            translating = true;
            
            double wcoord1[] = new double[3];
            double wcoord2[] = new double[3];
            
            int winX = e.getX();
            int winY = canvasHeight - e.getY();
            
            glu.gluUnProject(
                    (double)winX, (double)winY, 0.0,
                    modelview, 0, projection, 0, viewport, 0, wcoord1, 0);
            
            glu.gluUnProject(
                    (double)winX, (double)winY, 1.0,
                    modelview, 0, projection, 0, viewport, 0, wcoord2, 0);
            
            double eyeX = wcoord1[0];
            double eyeY = wcoord1[1];
            double eyeZ = wcoord1[2];
            
            double dirX = wcoord2[0] - wcoord1[0];
            double dirY = wcoord2[1] - wcoord1[1];
            double dirZ = wcoord2[2] - wcoord1[2];
            
            Vector3D pickDir = new Vector3D(dirX, dirY, dirZ);
            pickDir = pickDir.normalize();
            
            //double dirX = centerX - eyeX;
            //double dirY = centerY - eyeY;
            //double dirZ = 0.0 - eyeZ;
            
            dragX = new Vector2D(this.dirX, this.dirY);
            dragY = new Vector2D(rightX, rightY);
            
            dragX = dragX.normalize().negate();
            dragY = dragY.normalize().negate();

            double s = -eyeZ / dirZ;
            dragCenter = new Vector3D(eyeX + s * dirX, eyeY + s * dirY, eyeZ + s * dirZ);
            
            dragEye = new Vector3D(eyeX, eyeY, eyeZ);
            
            transX = e.getX();
            transY = e.getY();
            
            dragStart = new Vector3D(centerX, centerY, 0.0);
        }  
        
        if (e.getButton() == MouseEvent.BUTTON3) {
            rotating = true;
            
            rotStartX = e.getX();
            rotStartY = e.getY();
            
            rotStartAngle = angle;
            rotStartHeight = height;
        }
        
        if (e.getButton() == MouseEvent.BUTTON2) {
            zooming = true;
            zoomStartX = e.getY();
            zoomStart = zoom;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            translating = false;
        }
        
        if (e.getButton() == MouseEvent.BUTTON3) {
            rotating = false;
        }
        
        if (e.getButton() == MouseEvent.BUTTON2) {
            zooming = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
}





