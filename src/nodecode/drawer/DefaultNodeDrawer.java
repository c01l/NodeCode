package nodecode.drawer;

import java.awt.Graphics2D;

import nodecode.node.Highlightable;
import nodecode.node.NCNode;

public class DefaultNodeDrawer extends NodeDrawer {

	public DefaultNodeDrawer(NCNode n) {
		this(n, DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
	}

	public DefaultNodeDrawer(NCNode n, int x, int y) {
		super(n, x, y);
	}

	@Override
	protected void paintContent(Graphics2D g) {
		// do nothing
	}
	
	@Override
	public Highlightable getHighlightable(int x, int y) {
		return this;
	}
}
