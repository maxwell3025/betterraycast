package visuals;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

public class renderer extends JFrame implements KeyListener, Runnable, MouseMotionListener {
	BufferedImage screen = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
	BufferedImage finishedscreen = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics = screen.createGraphics();
	boolean[] ispressed = new boolean[450];
	ArrayList<surface> walls = new ArrayList<surface>();
	surface[] wallstart = { new surface(new line(new point(3.5, 5.2), new point(2.5, 2.8)), randomcolor(), 0.25, 1),
			new surface(new line(new point(6.7, 2.8), new point(6.2, 9.5)), randomcolor(), 0, 3),
			new surface(new line(new point(-5.0, -5.2), new point(-3.4, -2.8)), randomcolor(), -2, 0.0625) };
	double angle;
	double perspectiveerror = Math.PI * 0;
	Matrix2D perspectivematrix = new Matrix2D(
			new point(Math.cos(angle + perspectiveerror), -Math.sin(angle + perspectiveerror)),
			new point(Math.sin(angle + perspectiveerror), Math.cos(angle + perspectiveerror)));
	point playerpos = new point(0, 0);
	boolean finishedwithframe = true;
	double FOV = 1;
	double[] zbuffer = new double[2000];
	int[] waits = new int[1000];
	int threadnum = -1;
	int fps = 60;
	double speed = 0.01;
	BufferedImage truescreen;

	public renderer() {
		// initializaton
		for (surface l : wallstart) {

			walls.add(l);
			System.out.print("yay");
		}
		double gap = Math.PI / 128;
		for (double deg = 0; deg < Math.PI * 2; deg += gap) {

			System.out.println(deg);
			walls.add(new surface(
					new line(new point(Math.sin(deg), Math.cos(deg)),
							new point(Math.sin(deg + gap), Math.cos(deg + gap))),
					Color.getHSBColor((float) (deg / Math.PI * 0.5), 1, 1), (deg / Math.PI * 0.5) - 0.5, 0.5));
			walls.add(new surface(
					new line(new point(Math.sin(deg + gap), Math.cos(deg + gap)),
							new point(Math.sin(deg), Math.cos(deg))),
					Color.getHSBColor((float) (deg / Math.PI * 1), 1, 1), (deg / Math.PI * 0.5) - 0.5, 0.5));
			// TODO
		}
		walls.add(new surface(new line(new point(0, 1), new point(0, 2)), Color.BLUE, 0, 1));
		setSize(1000, 1000);
		addKeyListener(this);
		addMouseMotionListener(this);
		setVisible(true);

	}

	public void update() {
		finishedwithframe = false;
		graphics.clearRect(0, 0, 2000, 1000);
		for (surface l : walls) {
			renderwall(l.sur, l.tex, l.height, l.z);
		}
		Arrays.fill(zbuffer, 0);
		for (int i = walls.size() - 1; i >= 0; i--) {
			surface l = walls.get(i);
			renderwall(l.sur, l.tex, l.height, l.z);
		}
		Arrays.fill(zbuffer, 0);
		finishedscreen.createGraphics().drawImage(screen, 0, 0, null);
		repaint();

	}

	public static Color randomcolor() {
		return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
	}
	@Deprecated
	public void mapwall(line l, Color c) {
		graphics.setColor(c);
		graphics.drawLine(1000 + (int) (l.a.x * 20), 500 + (int) (l.a.y * 10), 1000 + (int) (l.b.x * 20),
				500 + (int) (l.b.y * 10));
	}

	public void drawwall(line l, Color c, double height, double z) {

		line cur = perspectiveconverted(l);
		int bx = 1000 + (int) (2000 * cur.b.x);
		int ax = 1000 + (int) (2000 * cur.a.x);
		for (int i = bx; i < ax; i++) {
			if (i < 0) {
				i = 0;
			}
			if (i >= 2000) {
				break;
			}
			double interpolant = (double) (i - bx) / (ax - bx);
			double wallheight = interpolate(cur.b.y, cur.a.y, interpolant);
			if (wallheight > zbuffer[i]) {
				zbuffer[i] = wallheight;
				double lighting = wallheight * 4;
				if (lighting < 1) {
					graphics.setColor(new Color((int) (c.getRed() * lighting), (int) (c.getGreen() * lighting),
							(int) (c.getBlue() * lighting)));

				} else {
					graphics.setColor(c);
				}
				// TODO
				graphics.drawRect(i, (int) (500 - (500 * (wallheight * height))) - (int) (z * 500 * wallheight), 0,
						(int) (1000 * (wallheight * height)));
			}
		}

	}

	public static double interpolate(double start, double end, double interpolant) {
		return (1 - interpolant) * start + interpolant * end;
	}

	public void renderwall(line l, Color c, double height, double z) {
		drawwall(clip(perspectivematrix.convert(relative(l))), c, height, z);
		// mapwall(perspectivematrix.convert(relative(l)), c);

	}

	public static line clip(line l) {
		double x1 = l.a.x;
		double y1 = l.a.y;
		double x2 = l.b.x;
		double y2 = l.b.y;
		if (y1 < 0 && y2 < 0) {
			return new line(new point(100, 0.01), new point(100, 0.01));
		} else if (y1 < 0) {
			double scale = y2 / (y2 - y1);
			return new line(new point(x2 + (x1 - x2) * scale, 0.01), l.b);
		} else if (y2 < 0) {
			double scale = y1 / (y1 - y2);
			return new line(l.a, new point(x1 + (x2 - x1) * scale, 0.01));

		} else {
			return l;
		}
	}

	public static int clip255(int in) {
		if (in < 0) {
			return 0;
		} else if (in > 255) {
			return 255;
		}
		return in;

	}

	public point perspectiveconverted(point a) {
		return new point(a.x / (a.y * FOV * ((double) getWidth() / (double) getHeight())), 1 / (FOV * a.y));
	}

	public line relative(line a) {
		return new line(point.subtract(a.a, playerpos), point.subtract(a.b, playerpos));
	}

	public line perspectiveconverted(line a) {
		return new line(perspectiveconverted(a.a), perspectiveconverted(a.b));
	}

	public static void main(String[] args) {
		renderer r = new renderer();
		new Thread(r).start();
		new Thread(r).start();
		new Thread(r).start();

	}

	public synchronized void paint(Graphics g) {
		g.drawImage(finishedscreen, 0, 0, getWidth(), getHeight(), null);
		finishedwithframe = true;

	}

	public void keyTyped(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		ispressed[e.getKeyCode()] = true;
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(ABORT);
		}

	}

	public void keyReleased(KeyEvent e) {
		ispressed[e.getKeyCode()] = false;

	}

	public void playerUpdate() {
		if (ispressed[KeyEvent.VK_G]) {
			angle -= 0.001;
		}
		if (ispressed[KeyEvent.VK_H]) {
			angle += 0.001;
		}
		if (ispressed[KeyEvent.VK_D]) {
			playerpos = point.add(playerpos,
					new point(Math.sin(-angle + Math.PI * 0.5) * speed, Math.cos(-angle + Math.PI * 0.5) * speed));
		}
		if (ispressed[KeyEvent.VK_A]) {
			playerpos = point.add(playerpos,
					new point(-Math.sin(-angle + Math.PI * 0.5) * speed, -Math.cos(-angle + Math.PI * 0.5) * speed));
		}
		if (ispressed[KeyEvent.VK_W]) {
			playerpos = point.add(playerpos, new point(Math.sin(-angle) * speed, Math.cos(-angle) * speed));
		}
		if (ispressed[KeyEvent.VK_S]) {
			playerpos = point.add(playerpos, new point(-Math.sin(-angle) * speed, -Math.cos(-angle) * speed));
		}
	}

	public void run() {
		threadnum++;
		if (threadnum == 0) {
			while (true) {
				perspectivematrix = new Matrix2D(
						new point(Math.cos(angle + perspectiveerror), -Math.sin(angle + perspectiveerror)),
						new point(Math.sin(angle + perspectiveerror), Math.cos(angle + perspectiveerror)));
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (finishedwithframe) {
					update();
				}

				for (int i = 0; i < 1000; i++) {
					waits[i]++;
				}
			}

		}
		if (threadnum == 1) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				for (int i = 1; i < 1000; i++) {
					waits[i - 1] = waits[i];
				}
				waits[999] = 0;
				fps = waits[0];
			}
		}
		if (threadnum == 2) {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				playerUpdate();
			}
		}

	}

	public void mouseDragged(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		angle += (double) (((int) (getWidth() * 0.5)) - e.getX()) * 0.001;
		try {
			new Robot().mouseMove(getX() + (int) (getWidth() * 0.5), getY() + (int) (getHeight() * 0.5));
		} catch (AWTException e1) {
			e1.printStackTrace();
		}

	}

}
