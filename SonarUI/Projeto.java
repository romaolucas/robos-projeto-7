import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import lejos.geom.Line;
import lejos.geom.Rectangle;
import lejos.geom.Point;
import lejos.robotics.mapping.LineMap;

public class Projeto
{
	private static float robotTheta;
	private static float robotX;
	private static float robotY;

	private static float sameLineThreshold = 100f;
	private static float isObjectThreshold = 248f;

	private static List<Line> lines = new ArrayList<>();

	public static Point polarToCartesian(float r, float thetaInRadians) {
		float x;
		float y;

		x = (float) (r * Math.cos(thetaInRadians));
		y = (float) (r * Math.sin(thetaInRadians));

		return new Point(x, y);
	}

	public static Point changeBasis(float x, float y, float theta) {
		float gX;
		float gY;

		gX = (float) ((x * Math.cos(theta))  - (y * Math.sin(theta)) + robotX);
		gY = (float) ((x * Math.sin(theta)) + (y * Math.cos(theta)) + robotY);
		
		return new Point(gX, gY);		 
	}

	public static void addLines(Point[] points, int start, int end) {
		if (end - start <= 1) return; 

		Point p1 = points[start];
		Point pn = points[end];	
		Line line = new Line((float) p1.getX(), (float) p1.getY(), (float) pn.getX(), (float) pn.getY());
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
		} else {
			Projeto.addLines(points, maxDistIndex, end);
			Projeto.addLines(points, start, maxDistIndex);
		}
	}

	public static float distanceFromLine(Point point, Line line) {
		Point p1 = line.getP1();
		Point p2 = line.getP2();
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

		    	List<Point> points = new ArrayList<>();
		    	for (int i = 0; i < scanFloat.size(); i++) {
		    		float theta = Projeto.degreesToRadians((i + 1) * 2);
		    		if (scanFloat.get(i) < isObjectThreshold) continue;
		    		Point point = Projeto.polarToCartesian(scanFloat.get(i), theta);
		    		point = Projeto.changeBasis((float) point.getX(), (float) point.getY(), theta);
		    		points.add(point);
		    	}
		    	Projeto.addLines(points.toArray(new Point[points.size()]), 0, points.size() - 1);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		LineMap lineMap = new LineMap(lines.toArray(new Line[lines.size()]), new Rectangle(0, 0, 4000, 4000));
 		try {
 			lineMap.createSVGFile("lineMap.svg");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
    }
}
