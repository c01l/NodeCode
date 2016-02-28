package nodecode.drawer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Map.Entry;

import nodecode.InterfaceColorSet;
import nodecode.Workspace;
import nodecode.node.Highlightable;
import nodecode.node.NCHighlightInfo;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeInterface;
import nodes.NodeOutputInterface;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public abstract class NodeDrawer extends ObjectDrawer {

	public static final int HEADERMARGIN = 10;
	public static final int HEADERHEIGHT = 20;
	public static final int INTERFACEMARGIN = 15;
	public static final int INTERFACESIZE = 10;
	public static final int INTERFACESTART = HEADERHEIGHT + INTERFACEMARGIN / 2;
	public static final int CONTENTMARGIN = 5;
	public static final int SIGNALINTERFACEY = HEADERHEIGHT / 2;

	public static final int DEFAULT_SPAWN_X = 100;
	public static final int DEFAULT_SPAWN_Y = 100;
	public static final int DEFAULT_CONTENT_WIDTH = 50;
	public static final int DEFAULT_CONTENT_HEIGHT = 50;

	private NCNode n = null;
	private int width = -1, height = -1;
	private int contentWidth, contentHeight;

	public NodeDrawer(NCNode n) {
		this(n, DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
	}

	public NodeDrawer(NCNode n, int x, int y) {
		super(x, y);
		this.n = n;

		this.contentWidth = DEFAULT_CONTENT_WIDTH;
		this.contentHeight = DEFAULT_CONTENT_HEIGHT;
	}

	@Override
	public void paint(Graphics2D g) {
		double width = this.getWidth();
		double height = this.getHeight();

		Point pos = this.getPosition();

		Font font = g.getFont();
		FontRenderContext context = g.getFontRenderContext();

		Rectangle2D titleBounds = font.getStringBounds(this.n.getName(), context);

		double titleWidth = titleBounds.getWidth();
		double titleHeight = titleBounds.getHeight();

		if (width < titleWidth + 2 * HEADERMARGIN)
			this.width = (int) (titleWidth + 2 * HEADERMARGIN);

		g.setColor(this.isHighlighted() ? Color.RED : Color.BLACK);

		g.drawRect(pos.x, pos.y, (int) width, HEADERHEIGHT);
		g.drawString(this.n.getName(), (int) (pos.x + width / 2d - titleWidth / 2d),
				(int) (pos.y + HEADERHEIGHT / 2d + titleHeight / 2d));
		g.drawRect(pos.x, pos.y + HEADERHEIGHT, (int) width, (int) height - HEADERHEIGHT);

		this.paintInterfaces(g);

		int dx = pos.x + CONTENTMARGIN;
		int dy = pos.y + HEADERHEIGHT + CONTENTMARGIN;
		g.translate(dx, dy);

		g.clipRect(0, 0, this.contentWidth, this.contentHeight);
		this.paintContent(g);

		g.translate(-dx, -dy);
		g.setClip(null);
	}

	protected void paintInterfaces(Graphics2D g) {
		InterfaceColorSet colorSet = Workspace.getInterfaceColorSet();
		if (Workspace.isDataShown()) {
			// input
			for (NCHighlightInfo<NodeInputInterface> in : this.n.getNCInputs().values()) {
				NodeInputInterface input = in.getReal();

				Color col = colorSet.getColor(input.getType());
				g.setColor(in.isHighlighted() ? col.brighter() : col);

				Point p = getInterfacePosition(input);

				g.fillArc(p.x - INTERFACESIZE / 2, p.y - INTERFACESIZE / 2, INTERFACESIZE, INTERFACESIZE, 90, 180);
			}

			// output
			for (NCHighlightInfo<NodeOutputInterface> out : this.n.getNCOutputs().values()) {
				NodeOutputInterface output = out.getReal();

				Color col = colorSet.getColor(output.getType());
				g.setColor(out.isHighlighted() ? col.brighter() : col);

				Point p = getInterfacePosition(output);

				g.fillArc(p.x - INTERFACESIZE / 2, p.y - INTERFACESIZE / 2, INTERFACESIZE, INTERFACESIZE, 270, 180);
			}
		}

		if (Workspace.areSignalsShown()) {
			SignalInputInterface input = this.n.getSignalInput();
			Point inPos = getInterfacePosition(input);

			g.setColor(colorSet.getColor(input.getType()));
			g.fillArc(inPos.x - INTERFACESIZE / 2, inPos.y - INTERFACESIZE / 2, INTERFACESIZE, INTERFACESIZE, 90, 180);

			SignalOutputInterface output = this.n.getSignalOutput();
			Point outPos = getInterfacePosition(output);

			g.setColor(colorSet.getColor(output.getType()));
			g.fillArc(outPos.x - INTERFACESIZE / 2, outPos.y - INTERFACESIZE / 2, INTERFACESIZE, INTERFACESIZE, 270,
					180);
		}
	}

	protected abstract void paintContent(Graphics2D g);

	@Override
	public boolean isMouseOverMoveArea(int mx, int my) {
		Point pos = this.getPosition();
		return pos.x < mx && mx < pos.x + this.getWidth() && pos.y < my && my < pos.y + HEADERHEIGHT;
	}

	/**
	 * Returns the width of the drawn node (interfaces excluded)
	 * 
	 * @return
	 */
	public int getWidth() {
		if (this.width == -1)
			this.width = this.contentWidth + CONTENTMARGIN * 2;

		return this.width;
	}

	public int getHeight() {
		if (this.height == -1)
			this.height = HEADERHEIGHT + this.getContentHeight() + 2 * CONTENTMARGIN;
		// CHECKME consider interface count?
		return this.height;
	}

	public void setContentDimension(int width, int height) {
		this.contentWidth = width;
		this.contentHeight = height;
	}

	public int getContentWidth() {
		return this.contentWidth;
	}

	public int getContentHeight() {
		return this.contentHeight;
	}

	public NCHighlightInfo<? extends NodeInterface> getInterface(int x, int y) {
		Point pos = this.getPosition();
		
		if (y < pos.y || y > pos.y + this.getHeight())
			return null;

		// is it an input interface?
		if (pos.x - INTERFACESIZE / 2 <= x && x <= pos.x) {
			// signal input
			if (Math.abs(pos.y + SIGNALINTERFACEY - y) <= INTERFACESIZE) {
				return this.n.getNCSignalInput();
			}

			// data input
			int i = 0;
			for (NCHighlightInfo<NodeInputInterface> iName : this.n.getNCInputs().values()) {
				int interfaceY = getRelativeInterfaceY(i);

				if (Math.abs(interfaceY - y) <= INTERFACESIZE) {
					return iName;
				}

				++i;
			}

		} else if (pos.x <= x && x <= pos.x + INTERFACESIZE / 2) {
			// signal output
			if (Math.abs(pos.y + SIGNALINTERFACEY - y) <= INTERFACESIZE) {
				return this.n.getNCSignalOutput();
			}

			// exception output
			if (Math.abs(pos.y + this.getHeight() - SIGNALINTERFACEY - y) <= INTERFACESIZE) {
				return this.n.getNCExceptionOutput();
			}

			// data output
			int i = 0;
			for (NCHighlightInfo<NodeOutputInterface> oName : this.n.getNCOutputs().values()) {
				int interfaceY = getRelativeInterfaceY(i);

				if (Math.abs(interfaceY - y) <= INTERFACESIZE) {
					return oName;
				}

				++i;
			}
		}

		// no interface was found
		return null;
	}

	private int getRelativeInterfaceY(int i) {
		return this.getPosition().y + INTERFACESTART + i * INTERFACEMARGIN;
	}

	public Point getInterfacePosition(NodeInterface i) {
		Point pos = this.getPosition();
		if (i == this.n.getSignalInput()) {
			return new Point(pos.x, pos.y + SIGNALINTERFACEY);
		} else if (i == this.n.getSignalOutput()) {
			return new Point(pos.x + this.getWidth(), pos.y + SIGNALINTERFACEY);
		} else if (i == this.n.getExceptionOutput()) {
			return new Point(pos.x + this.getWidth(), pos.y + getHeight() - SIGNALINTERFACEY);
		}

		if (i instanceof NodeInputInterface) {
			for (Entry<String, NodeInputInterface> e : this.n.getInputs().entrySet()) {
				if (e.getValue() == i) {
					int index = 0;
					for (String s : this.n.getInputs().keySet()) {
						if (s.equals(e.getKey())) {
							break;
						}
						index++;
					}

					return new Point(pos.x, getRelativeInterfaceY(index));
				}
			}
			return null;
		}

		if (i instanceof NodeOutputInterface) {
			for (Entry<String, NodeOutputInterface> e : this.n.getOutputs().entrySet()) {
				if (e.getValue() == i) {
					int index = 0;
					for (String s : this.n.getOutputs().keySet()) {
						if (s.equals(e.getKey())) {
							break;
						}
						index++;
					}

					return new Point(pos.x + this.getWidth(), getRelativeInterfaceY(index));
				}
			}
			return null;
		}

		return null;
	}

	public Highlightable getHighlightable(int x, int y) {
		return this;
	}

	public NCNode getReal() {
		return this.n;
	}

}
