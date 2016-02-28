package nodecode;

import java.awt.Graphics2D;
import java.awt.Point;

public class EdgePainter {

	public static void paint(Graphics2D g, Point start, Point end) {
		g.drawLine(start.x, start.y, end.x, end.y); // TODO bezier
	}

}
