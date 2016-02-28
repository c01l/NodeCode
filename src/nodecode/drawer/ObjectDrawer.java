package nodecode.drawer;

import java.awt.Point;

import nodecode.node.Highlightable;

public abstract class ObjectDrawer implements Drawable, Positionable, Highlightable {

	private Point pos;
	private boolean isHighlighted = false;

	public ObjectDrawer() {
		this(0, 0);
	}

	public ObjectDrawer(int x, int y) {
		this.pos = new Point(x, y);
	}

	@Override
	public void setPosition(int x, int y) {
		this.pos = new Point(x, y);
	}

	@Override
	public Point getPosition() {
		return this.pos;
	}

	@Override
	public boolean isHighlighted() {
		return this.isHighlighted;
	}

	@Override
	public void setHighlighted(boolean h) {
		this.isHighlighted = h;
	}

}
