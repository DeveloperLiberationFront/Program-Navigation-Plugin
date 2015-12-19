package edu.pdx.cs.multiview.swt.geometry;

import org.eclipse.swt.graphics.Point;

public abstract class Coordinate {

	public static Coordinate create(int x, int y) {
		return new XYCoordinate(x, y);
	}

	public static Coordinate create(double radius, double theta) {
		return new PolarCoordinate(radius,theta);
	}
	
	/**
	 * Creates a new Coordinate, where x and y are coordinates in an SWT control
	 * and originX and originY are the returned coordinate's origin on the SWT
	 * control.
	 */
	public static Coordinate create(int swtX, int swtY, int originX, int originY) {		
		return create(swtX - originX,originY - swtY);
	}
	
	public static Coordinate create(Point p, Point origin) {		
		return create(p.x,p.y,origin.x,origin.y);
	}
	
	/**
	 * Creates a new Coordinate on the Java origin, where the argument is my origin relative 
	 * to a java GUI component
	 * 
	 * @param originRelativeToJavaOrigin
	 * @return
	 */
	public Coordinate toJavaCoordinate(Coordinate originRelativeToJavaOrigin){
		return create(originRelativeToJavaOrigin.x()+x(), originRelativeToJavaOrigin.y()-y());
	}
	
	public abstract double theta();
	
	public abstract double radius();
	
	public abstract int x();
	
	public abstract int y();
	

	private static class XYCoordinate extends Coordinate{
		
		private int x,y;

		public XYCoordinate(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}
		
		@Override
		public double theta() {
			return Angle.normalize(Math.atan2( y , x ));
		}

		@Override
		public double radius() {
			return Math.sqrt(x*x+y*y);
		}
	}
	
	private static class PolarCoordinate extends Coordinate{
		
		private double radius;
		private double theta;
	     
		public PolarCoordinate(double radius, double theta) {
			super();
			this.radius = radius;
			this.theta = theta;
		}

		@Override
		public double theta() {
			return theta;
		}
		
		@Override
		public double radius() {
			return radius;
		}

		@Override
		public int x() {			
			return (int)(radius*Math.cos(theta));
		}

		@Override
		public int y() {
			return (int)(radius*Math.sin(theta));
		}
	}

	public Coordinate movedInOrOutBy(double scalingFactor) {
		return Coordinate.create(radius()*scalingFactor, theta());
	}
}
