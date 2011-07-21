package geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.strtree.STRtree;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ConcaveHullBuilder {

    private static GeometryFactory gf = new GeometryFactory();
    private static Random rand = new Random();

    private static double alpha = 50d;

    public static void main(String[] args) {
        ConcaveHullBuilder hull = new ConcaveHullBuilder();
        hull.demo();
    }

    private void demo() {

        /**
         * Make an irregular polygon and generate random points within it
         * to test the concave hull algorithm
         */
        final int numPoints = 200;

        Coordinate[] vertices = {
            new Coordinate(0, 0),
            new Coordinate(100, 100),
            new Coordinate(0, 250),
            new Coordinate(200, 300),
            new Coordinate(200, 450),
            new Coordinate(350, 450),
            new Coordinate(350, 200),
            new Coordinate(250, 200),
            new Coordinate(350, 100),
            new Coordinate(0, 0)
        };

        Polygon poly = gf.createPolygon(gf.createLinearRing(vertices), null);
        Envelope env = poly.getEnvelopeInternal();

        int n = 0;
        Point[] points = new Point[numPoints];
        while (n < numPoints) {
            Coordinate c = new Coordinate();
            c.x = rand.nextDouble() * env.getWidth() + env.getMinX();
            c.y = rand.nextDouble() * env.getHeight() + env.getMinY();
            Point p = gf.createPoint(c);
            if (poly.contains(p)) {
                points[n++] = p;
            }
        }

        List<LineString> edges = getConcaveHull(points, alpha);

        display((int)(env.getWidth()*1.1), (int)(env.getHeight()*1.1),
                poly, Color.GRAY,
                points, Color.RED,
                edges, Color.RED);
    }

    public static Geometry getConcaveHullGeo(Point[] points, double alpha) throws Exception {
    	if (points.length<=2) return new GeometryFactory().createMultiPoint(points);
    	List<Point> l = new ArrayList<Point>();
    	for ( Point p : points ) l.add(p.getCentroid());
    	List<LineString> edges = getConcaveHull(l.toArray(new Point[0]), alpha);
    	List<Coordinate> cl = new ArrayList<Coordinate>();
    	for (LineString ls : edges) {
            Point p0 = ls.getStartPoint();
            Point p1 = ls.getEndPoint();
            cl.add(p0.getCoordinate());
            cl.add(p1.getCoordinate());
    	}
    	try {
    		if(edges.size()>0) cl.add(edges.get(0).getStartPoint().getCoordinate());
    		GeometryFactory fact = new GeometryFactory();
    		LinearRing ring = fact.createLinearRing(cl.toArray(new Coordinate[0]));
    		return fact.createPolygon(ring, null);
    	} catch ( Exception e ) {
    		return new GeometryFactory().createMultiPoint(points);
    	}
    }
    
    /**
     * Identify a concave hull using the simple alpha-shape algorithm described in:
     * <blockquote>
     * Shen Wei (2003) Building boundary extraction based on LIDAR point clouds data.
     * The International Archives of the Photogrammetry, Remote Sensing and Spatial
     * Information Sciences. Vol. XXXVII. Part B3b. Beijing 2008
     * </blockquote>
     * @param points the point cloud
     * @param alpha the single parameter for the algorithm
     * @return a List of LineString boundary segments for the concave hull
     */
    public static List<LineString> getConcaveHull(Point[] points, double alpha) {
        double alpha2 = 2 * alpha;
        STRtree index = new STRtree();
        for (Point p : points) index.insert(p.getEnvelopeInternal(), p);
        index.build();
        List<LineString> edges = new ArrayList<LineString>();
        List<Point> candidates = new ArrayList<Point>();
        candidates.addAll(Arrays.asList(points));
        while (!candidates.isEmpty()) {
            Point p1 = candidates.remove(0);
            Envelope qEnv = new Envelope(p1.getX() - alpha2, p1.getX() + alpha2, p1.getY() - alpha2, p1.getY() + alpha2);
            PointVisitor visitor = new PointVisitor(p1, alpha2);
            index.query(qEnv, visitor);
            if (visitor.plist.size() < 2) { break; }
            visitor.plist.remove(p1);
            boolean[] used = new boolean[visitor.plist.size()];
            Arrays.fill(used, false);
            int numPts = visitor.plist.size();
            while (numPts > 0) {
                Point p2;
                while (true) {
                    int pindex = rand.nextInt(visitor.plist.size());
                    if (!used[pindex]) {
                        p2 = visitor.plist.get(pindex);
                        used[pindex] = true;
                        numPts--;
                        break;
                    }
                }
                Point pcentre = createCircle(p1, p2, alpha);
                boolean onBoundary = true;
                for (Point vp : visitor.plist) {
                    if (vp != p2) {
                        if (pcentre.distance(vp) <= alpha) {
                            onBoundary = false;
                            break;
                        }
                    }
                }
                if (onBoundary) {
                    edges.add(gf.createLineString(new Coordinate[]{p1.getCoordinate(), p2.getCoordinate()}));
                }
            }
        }
        return edges;
    }

    /**
     * Calculate the centre coordinates of a circle of radius alpha that has
     * point coordinates c1 and c2 on its circumference.
     *
     * @param c1 first circumference point
     * @param c2 second circumference point
     * @param alpha radius
     * @return a Coordinate representing the circle centre
     */
    private static Point createCircle(Point p1, Point p2, double alpha) {
        Coordinate centre = new Coordinate();

        double dx = (p2.getX() - p1.getX());
        double dy = (p2.getY() - p1.getY());
        double s2 = dx * dx + dy * dy;

        double h = Math.sqrt(alpha * alpha / s2 - 0.25d);

        centre.x = p1.getX() + dx / 2 + h * dy;
        centre.y = p1.getY() + dy / 2 + h * (p1.getX() - p2.getX());

        return gf.createPoint(centre);
    }

    /**
     * Display demo results
     * @param poly the polygon used to generate the point cloud
     * @param polyCol display colour for the polygon
     * @param points the point cloud
     * @param pointCol display colour for the points
     * @param edges concave hull boundary segments as a List of LineStrings
     * @param edgeCol display colour for the segments
     */
    private void display(int w, int h,
            final Polygon poly, final Color polyCol,
            final Point[] points, final Color pointCol,
            final List<LineString> edges, final Color edgeCol) {

        JFrame frame = new JFrame("Concave hull demo");

        JPanel panel = new JPanel() {

            final int ow = 2;
            final int ow2 = 4;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                g2.setColor(polyCol);
                LineString ring = poly.getExteriorRing();
                Coordinate clast = ring.getCoordinateN(0);
                for (int i = 1; i < ring.getNumPoints(); i++) {
                    Coordinate c = ring.getCoordinateN(i);
                    g2.drawLine((int) clast.x, (int) clast.y, (int)
c.x, (int) c.y);
                    clast = c;
                }

                g2.setColor(pointCol);
                for (int i = 0; i < points.length; i++) {
                    int x = (int) Math.round(points[i].getX());
                    int y = (int) Math.round(points[i].getY());
                    g2.fillOval(x - ow, y - ow, ow2, ow2);
                }

                g2.setColor(edgeCol);
                Geometry polyEnv = poly.getEnvelope();
                for (LineString l : edges) {
                    Point p0 = l.getStartPoint();
                    Point p1 = l.getEndPoint();
                    int x0 = (int) l.getStartPoint().getX();
                    int y0 = (int) l.getStartPoint().getY();
                    int x1 = (int) l.getEndPoint().getX();
                    int y1 = (int) l.getEndPoint().getY();

                    g2.drawLine(x0, y0, x1, y1);
                }
            }
        };

        panel.setBackground(Color.WHITE);

        frame.getContentPane().add(panel);
        Insets insets = frame.getInsets();
        frame.setSize(w + insets.left + insets.right, h + insets.bottom + insets.top);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class PointVisitor implements ItemVisitor {

    public List<Point> plist = new ArrayList<Point>();
    private double maxDist;
    private Point refP;

    PointVisitor(Point refP, double maxDist) {
        this.refP = refP;
        this.maxDist = maxDist;
    }

    public void visitItem(Object o) {
        if (refP.isWithinDistance((Point) o, maxDist)) {
            plist.add((Point) o);
        }
    }
}

