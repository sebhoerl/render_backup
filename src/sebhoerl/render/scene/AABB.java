package sebhoerl.render.scene;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class AABB {
    private Vector2D lowerLeft = new Vector2D(0.0, 0.0);
    private Vector2D upperRight = new Vector2D(0.0, 0.0);
    
    public AABB(Vector2D lowerLeft, Vector2D upperRight) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
    }
    
    public Vector2D getCenter() {
        return lowerLeft.add(upperRight.subtract(lowerLeft).scalarMultiply(0.5));
    }
    
    public boolean contains(Vector2D position) {
        if (position.getX() >= lowerLeft.getX() && position.getX() <= upperRight.getX()) {
            if (position.getY() >= lowerLeft.getY() && position.getY() <= upperRight.getY()) {
                return true;
            }
        }
        
        return false;
    }
    
    public Vector2D getLowerLeft() {
        return lowerLeft;
    }
    
    public void setLowerLeft(Vector2D lowerLeft) {
        this.lowerLeft = lowerLeft;
    }
    
    public Vector2D getUpperRight() {
        return upperRight;
    }
    
    public void setUpperRight(Vector2D upperRight) {
        this.upperRight = upperRight;
    }
    
    public AABB[] split() {
        AABB[] children = new AABB[4];
        
        Vector2D half = upperRight.subtract(lowerLeft).scalarMultiply(0.5);
        
        children[0] = new AABB(new Vector2D(lowerLeft.getX(), lowerLeft.getY()), new Vector2D(lowerLeft.getX() + half.getX(), lowerLeft.getY() + half.getY()));
        children[1] = new AABB(new Vector2D(lowerLeft.getX(), lowerLeft.getY() + half.getY()), new Vector2D(lowerLeft.getX() + half.getX(), upperRight.getY()));
        children[2] = new AABB(new Vector2D(lowerLeft.getX() + half.getX(), lowerLeft.getY()), new Vector2D(upperRight.getX(), lowerLeft.getY() + half.getY()));
        children[3] = new AABB(new Vector2D(lowerLeft.getX() + half.getX(), lowerLeft.getY() + half.getY()), new Vector2D(upperRight.getX(), upperRight.getY()));
    
        return children;
    }
    
    public static AABB fromVertices(Vector2D[] vertices) {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        
        for (Vector2D vertex : vertices) {
            if (vertex.getX() < xmin) xmin = vertex.getX();
            if (vertex.getX() > xmax) xmax = vertex.getX();
            if (vertex.getY() < ymin) ymin = vertex.getY();
            if (vertex.getY() > ymax) ymax = vertex.getY();
        }
        
        return new AABB(new Vector2D(xmin, ymin), new Vector2D(xmax, ymax));
    }
    
    public Vector2D[] getCorners() {
        return new Vector2D[] {
            new Vector2D(lowerLeft.getX(), lowerLeft.getY()),
            new Vector2D(upperRight.getX(), lowerLeft.getY()),
            new Vector2D(upperRight.getX(), upperRight.getY()),
            new Vector2D(lowerLeft.getX(), upperRight.getY())
        };
    }
}
