package edu.pdx.cs.multiview.swt.geometry;

import junit.framework.TestCase;

public class CoordinateTest extends TestCase{

	public void testNWAngle(){
		for(double a1 : zeroRadian())
			for(double inc : quarterRadian())
				testXAngle(10,10,a1,inc);
	}
	
	public void testNEAngle(){
		for(double a1 : quarterRadian())
			for(double inc : quarterRadian())
				testXAngle(-10,10,a1,inc);
	}
	
	public void testSWAngle(){
		for(double a1 : halfRadian())
			for(double inc : quarterRadian())
				testXAngle(-10,-10,a1,inc);				
	}
	
	public void testSEAngle(){
		for(double a1 : threeQuarterRadian())
			for(double inc : quarterRadian())
				testXAngle(10,-10,a1,inc);				
	}


	

	
	private void testXAngle(int x, int y, double angle, double increment){
		Coordinate point = Coordinate.create(x, y);
		Angle arc1 = new Angle(angle,increment);
		assertTrue(arc1.contains(point));
	}
	
	private double pi = Math.PI;
	
	private double[] halfRadian() {		
		return new double[] {pi,-pi/*,3*pi,-3*pi*/};
	}
	
	private double[] quarterRadian() {
		return new double[] {pi/2,-pi*3/2};
	}
	
	private double[] zeroRadian() {
		return new double[] {0,2*pi};
	}
	
	private double[] threeQuarterRadian() {
		return new double[] {pi*3/2,-pi/2};
	}
	
}
