package edu.pdx.cs.multiview.swt.geometry;

import org.eclipse.swt.graphics.GC;


/**
 * I am an arc centered at 0,0
 * 
 * @author emerson
 */
public class Arc extends Angle {

	public final int radius;
	
	public Arc(double angle, double incrementAngle, int radius) {
		super(angle, incrementAngle);
		this.radius = radius;
	}

	public void fill(GC gc) {
		gc.fillArc(0,0,2*radius,2*radius,
			toDegrees(theta),toDegrees(increment));
	}

	private static int toDegrees(double anAngle) {
		return (int)Math.toDegrees(anAngle);
	}

	public void drawLine(GC gc) {
		Coordinate c = Coordinate.create(radius, theta).toJavaCoordinate(Coordinate.create(radius, radius)); 
		gc.drawLine(radius,radius,c.x(),c.y());
	}

	/**
	 * @returns the Point in the center of this arc
	 */
	public Coordinate center() {
		return Coordinate.create(radius,halfWayRadian());//TODO: maybe inline halfWayRadian()
	}
}
