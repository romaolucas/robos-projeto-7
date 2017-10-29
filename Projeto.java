import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.awt.geom.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

// import lejos.geom.Line;
// import lejos.geom.Rectangle;
// import lejos.geom.Point;
// import lejos.robotics.mapping.LineMap;

public class Projeto
{
	private static float robotTheta;
	private static float robotX;
	private static float robotY;

	private static float sameLineThreshold = 100f;
	private static float isObjectThreshold = 248f;

	private static List<Line2D> lines = new ArrayList<>();

	public static Point2D polarToCartesian(float r, float thetaInRadians) {
		float x;
		float y;

		x = (float) (r * Math.cos(thetaInRadians));
		y = (float) (r * Math.sin(thetaInRadians));

		if (y < -1) System.out.println(x + " e " + y);
		return new Point2D.Float(x, y);
	}

	public static Point2D changeBasis(float x, float y, float theta) {
		float gX;
		float gY;

		gX = (float) ((x * Math.cos(theta))  - (y * Math.sin(theta)) + robotX);
		gY = (float) ((x * Math.sin(theta)) + (y * Math.cos(theta)) + robotY);
		

		// if (gY < 0) System.out.println(gX + " e " + gY);
		return new Point2D.Float(gX, gY);		 
	}

	public static void addLines(Point2D[] points, int start, int end) {
		if (end - start <= 1) return; 

		Point2D p1 = points[start];
		Point2D pn = points[end];	
		Line2D line = new Line2D.Float((float) p1.getX(), (float) p1.getY(), (float) pn.getX(), (float) pn.getY());
		float maxDist = 0f;
		int maxDistIndex = 0;
		
		for (int i = start + 1; i < end; i++) {
			float dist = distanceFromLine(points[i], line);
			if (dist > maxDist) {
				maxDist = dist;
				maxDistIndex = i;
			}
		}

		if (maxDist < sameLineThreshold) {
			lines.add(line);
			StdDraw.setPenRadius(0.01);
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.line(line.getP1().getX(), line.getP1().getY(), line.getP2().getX(), line.getP2().getY());        
			// System.out.println("linha: " + line.getP1().toString() + " e " + line.getP2().toString());
		} else {
			Projeto.addLines(points, maxDistIndex, end);
			Projeto.addLines(points, start, maxDistIndex);
		}
	}

	public static float distanceFromLine(Point2D point, Line2D line) {
		Point2D p1 = line.getP1();
		Point2D p2 = line.getP2();
		float x0, x1, x2;
		float y0, y1, y2;

		x0 = (float) point.getX();
		y0 = (float) point.getY();
		x1 = (float) p1.getX();
		y1 = (float) p1.getY();
		x2 = (float) p2.getX();
		y2 = (float) p2.getY();

		float dist;
		dist = (float) (Math.abs(((y2 - y1) * x0) - ((x2 - x1) * y0) + (x2 * y1) - (y2 * x1)));
		dist = (float) (dist / Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2)));

		return dist;
	}

	public static float degreesToRadians(int degrees) {
		float radians;
		radians = (float) (degrees * Math.PI / 180f);
		return radians; 
	}

 	public static void main(String[] args) {
		File file = new File("data.txt");
		BufferedReader reader = null;
		StdDraw.setScale(-800, 800);

		try {
		    reader = new BufferedReader(new FileReader(file));
		    String text = null;
		    while ((text = reader.readLine()) != null) {
		    	List<String> scan = Arrays.asList(text.split(","));
		    	List<Float> scanFloat = new ArrayList<>();
		    	for (int i = 0; i < scan.size(); i++) {
		    		scanFloat.add(Float.parseFloat(scan.get(i)));
		    	}
		    	robotX = scanFloat.get(0);
		    	robotY = scanFloat.get(1);
		    	robotTheta = scanFloat.get(2);

		    	scanFloat.remove(0);
		    	scanFloat.remove(1);
		    	scanFloat.remove(2);

		    	List<Point2D> points = new ArrayList<>();
		    	for (int i = 0; i < scanFloat.size(); i++) {
		    		float theta = Projeto.degreesToRadians(i * 2);
		    		if (scanFloat.get(i) < isObjectThreshold) continue;
		    		Point2D point = Projeto.polarToCartesian(scanFloat.get(i), theta);
		    		point = Projeto.changeBasis((float) point.getX(), (float) point.getY(), theta);
		    		points.add(point);
		    	}
		    	Projeto.addLines(points.toArray(new Point2D[points.size()]), 0, points.size() - 1);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}		
    }
}
