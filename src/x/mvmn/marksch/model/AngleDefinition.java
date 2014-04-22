package x.mvmn.marksch.model;

public class AngleDefinition implements AngleDefinitionReadOnly {

	private double radians = 0.0;
	private int degrees = 0;
	private int minutes = 0;
	private int seconds = 0;

	public AngleDefinition() {
	}

	public AngleDefinition(int degrees, int minutes, int seconds) {
		setAngle(degrees, minutes, seconds);
	}

	public AngleDefinition(double radians) {
		setRadians(radians);
	}

	public void setRadians(final double radians) {
		this.radians = radians % (Math.PI * 2);
		if (this.radians < 0) {
			this.radians += Math.PI * 2;
		}
		double anglesDiv = this.radians * 180.0d / Math.PI;
		this.degrees = (int) (anglesDiv);
		this.minutes = (int) ((anglesDiv - degrees) * 60.0d);
		this.seconds = (int) (((anglesDiv - degrees) * 60.0d - minutes) * 60.0d);
	}

	public void setAngle(final int degrees, int minutes, final int seconds) {
		this.seconds = seconds % 60;
		minutes = minutes + seconds / 60;
		this.minutes = minutes % 60;
		this.degrees = degrees % 360 + minutes / 60;
		radians = ((double) this.degrees * Math.PI + ((double) this.minutes * Math.PI) / 60.0d + ((double) this.seconds * Math.PI) / 3600.0d) / 180.0d;
	}

	public String toString() {
		return String.format("Angle %s (%s %s' %s\")", radians, degrees, minutes, seconds);
	}

	public double getRadians() {
		return radians;
	}

	public int getDegrees() {
		return degrees;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getSeconds() {
		return seconds;
	}
}
