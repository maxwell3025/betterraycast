package visuals;

public class point {
	double x;
	double y;

	public point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void moveto(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public static point add(point a,point b){
		return new point(a.x+b.x,a.y+b.y);
	}
	public static point subtract(point a,point b){
		return new point(a.x-b.x,a.y-b.y);
	}
	public static point multiply(point a,double scale){
		return new point(a.x*scale,a.y*scale);
	}
}
