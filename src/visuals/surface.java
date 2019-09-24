package visuals;

import java.awt.Color;

public class surface {
line sur;
Color tex;
double z;
double height;
	public surface(line l, Color c, double zcoord, double wallheight) {
		sur=l;
		tex=c;
		height = wallheight;
		z=zcoord;
	}

}
