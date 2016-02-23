package edu.pdx.cs.multiview.swt.geometry;


public class Angle {

	public final double theta, increment;

	public Angle(double theta, double increment) {
		this.theta = normalize(theta);
		this.increment = increment;
	}
	
	public boolean contains(Coordinate c) {
		
		double otherTheta = c.theta();
		return theta < otherTheta && otherTheta < (theta+increment) || 
				(theta - radian()) < otherTheta && otherTheta < (theta+increment-radian());
	}
	
	public double halfWayRadian() {
		return theta + 0.5*increment;
	}

	public int degAngle() {
		return (int)Math.toDegrees(theta);
	}

	public int degIncrement() {
		return (int)Math.toDegrees(increment);
	}

	public static double normalize(double angle) {
		
		//change to if/elseif
		while(angle<0)
			angle += radian();
		
		if(angle>=radian())
			angle = angle % radian();
		
		return angle;
	}

	public static double radian() {
		return Math.PI*2;
	}
}
