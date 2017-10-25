import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import lejos.geom.Line;
import lejos.geom.Rectangle;
import lejos.geom.Point;
import lejos.robotics.mapping.LineMap;

public class Projeto
{
	private float robotTheta;
	private float robotX;
	private float robotY;

	private float sameLineThreshold = 100.0;

	private List<Line> lines = new ArrayList<>();

	public Point polarToCartesian(float r, float thetaInRadians) {
		float x;
		float y;

		x = r * Math.cos(thetaInRadians);
		y = r * Math.sin(thetaInRadians);

		return new Point(x, y);
	}

	public Point changeBasis(float x, float y, float theta) {
		float gX;
		float gY;

		gX = (x * cos(theta))  - (y * sen(theta)) + robotX;
		gY = (x * sen(theta)) + (y * cos(theta)) + robotY;
		
		return new Point(gX, gY);		 
	}

	public void addLines(Point[] points, int start, int end) {
		float gX;
		float gY;
		
		if (end - start <= 1) return; 

		Point p1 = points[start];
		Point pn = points[end];	
		Line line = new Line(p1, pn);
		float maxDist = 0;
		
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
			addLines(points, i, end);
			addLines(points, start, i);
		}
	}

	public float distanceFromLine(Point point, Line line) {
		Point p1 = line.getP1();
		Point p2 = line.getP2();
		float x0, x1, x2;
		float y0, y1, y2;

		x0 = point.getX();
		y0 = point.getY();
		x1 = p1.getX();
		y1 = p1.getY();
		x2 = p2.getX();
		y2 = p2.getY();

		float dist;
		dist = Math.abs(((y2 - y1) * x0) - ((x2 - x1) * y0) + (x2 * y1) - (y2 * x1));
		dist = dist / Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));

		return dist;
	}

	public void readScan() {
		return; 
	}

	public float degreesToRadians(int degrees) {
		float radians;
		radians = degrees * Math.PI / 180.0;
		return radians; 
	}

 	public static void main(String[] args) {
        //abre arquivo
        //for linha in linhas do arquivo:
        	//readScan
 			//polarToCartesian
 			//changeBasis
 			//addLines
 		//lineMap.createSVGFile("lineMap.svg");
   
    }
}
