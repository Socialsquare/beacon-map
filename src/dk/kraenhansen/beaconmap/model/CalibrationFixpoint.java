package dk.kraenhansen.beaconmap.model;

public class CalibrationFixpoint {
	
	private String title;
	private double x;
	private double y;
	
	public CalibrationFixpoint(String title, double d, double e) {
		this.title = title;
		this.x = d;
		this.y = e;
	}

	public String getTitle() {
		return title;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
}
