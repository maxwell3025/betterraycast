package visuals;

public class Matrix2D {
point x;
point y;
	public Matrix2D(point x,point y) {
		this.x=x;
		this.y=y;
	}
	public point convert(point a){
		return point.add(point.multiply(x, a.x),point.multiply(y, a.y));
	}
	public line convert(line a){
		return new line(convert(a.a),convert(a.b));
	}
	public double determinant(){
		return x.x*y.y-x.y*y.x;
	}

}
