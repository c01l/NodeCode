package nodecode.signals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import nodecode.InterfaceColorSet;
import nodecode.Workspace;
import nodecode.drawer.NodeDrawer;
import nodecode.drawer.ObjectDrawer;
import nodecode.node.Highlightable;
import nodecode.node.NCHighlightInfo;
import nodes.NodeInterface;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public class SyncronizerDrawer extends ObjectDrawer {

	public static final int INTERFACEMARGIN = 20;

	public static int DEFAULT_SPAWN_X = 0;
	public static int DEFAULT_SPAWN_Y = 0;

	public static final int WIDTH = 10;

	private ArrayList<NCHighlightInfo<SignalInputInterface>> inputs;
	private ArrayList<NCHighlightInfo<SignalOutputInterface>> outputs;

	private NCSyncronizer sync;

	public SyncronizerDrawer(NCSyncronizer sync) {
		this(sync, DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
	}

	public SyncronizerDrawer(NCSyncronizer sync, int x, int y) {
		super(x, y);
		this.sync = sync;

		System.out.println("Created Syncronizer with " + sync.getInputSize() + " / " + sync.getOutputSize());

		this.inputs = new ArrayList<>(this.sync.getInputSize());
		this.outputs = new ArrayList<>(this.sync.getOutputSize());

		for (int i = 0; i < sync.getInputSize(); ++i) {
			this.inputs.add(new NCHighlightInfo<SignalInputInterface>(this.sync.getInput(i)));
		}
		for (int i = 0; i < sync.getOutputSize(); ++i) {
			this.outputs.add(new NCHighlightInfo<SignalOutputInterface>(this.sync.getOutput(i)));
		}
	}

	@Override
	public boolean isMouseOverMoveArea(int mx, int my) {
		Point pos = this.getPosition();
		return pos.x < mx && mx < pos.x + WIDTH && pos.y < my && my < pos.y + this.getHeight();
	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return (int) ((Math.max(this.sync.getInputSize(), this.sync.getOutputSize()) + 0.7f) * INTERFACEMARGIN);
	}

	@Override
	public void paint(Graphics2D g) {
		Point pos = this.getPosition();

		if (this.isHighlighted()) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLACK);
		}

		// draw main body
		g.fillArc(pos.x, pos.y, WIDTH, WIDTH, 0, 180);
		g.fillRect(pos.x, pos.y + WIDTH / 2, WIDTH, this.getHeight() - WIDTH);
		g.fillArc(pos.x, pos.y + this.getHeight() - WIDTH, WIDTH, WIDTH, 180, 180);

		InterfaceColorSet colorSet = Workspace.getInterfaceColorSet();

		// draw interfaces
		int i = 0;
		for (NCHighlightInfo<SignalInputInterface> r : this.inputs) {
			Point p = getInterfacePosition(i, true);
			NodeDrawer.paintInterface(g, p, true, r, colorSet);
			++i;
		}

		i = 0;
		for (NCHighlightInfo<SignalOutputInterface> r : this.outputs) {
			Point p = getInterfacePosition(i, false);
			NodeDrawer.paintInterface(g, p, false, r, colorSet);
			++i;
		}
	}

	public Point getInterfacePosition(NodeInterface n) {
		if (n instanceof SignalInputInterface) {
			for (int i = 0; i < this.sync.getInputSize(); ++i) {
				if (this.sync.getInput(i) == n) {
					return getInterfacePosition(i, true);
				}
			}
		} else if (n instanceof SignalOutputInterface) {
			for (int i = 0; i < this.sync.getOutputSize(); ++i) {
				if (this.sync.getOutput(i) == n) {
					return getInterfacePosition(i, false);
				}
			}
		}
		return null;
	}

	private Point getInterfacePosition(int i, boolean input) {
		int mSize = input ? this.inputs.size() : this.outputs.size();
		Point pos = this.getPosition();
		return new Point(pos.x + (input ? 0 : WIDTH),
				(int) (pos.y + this.getHeight() / 2 + (i - (mSize - 1) / 2f) * INTERFACEMARGIN));
	}

	public NCSyncronizer getReal() {
		return this.sync;
	}

	public Highlightable getInterface(int x, int y) {
		Point pos = this.getPosition();

		if (y < pos.y || y > pos.y + this.getHeight())
			return null;

		if (pos.x - NodeDrawer.INTERFACESIZE < x && x < pos.x) {
			// maybe an input interface

			for (int i = 0; i < this.inputs.size(); ++i) {
				Point p = getInterfacePosition(i, true);

				if (Math.abs(p.y - y) <= NodeDrawer.INTERFACESIZE / 2) {
					return this.inputs.get(i);
				}
			}
		} else if (pos.x + this.getWidth() < x && x < pos.x + this.getWidth() + NodeDrawer.INTERFACESIZE) {
			// maybe an output interface

			for (int i = 0; i < this.outputs.size(); ++i) {
				Point p = getInterfacePosition(i, false);

				if (Math.abs(p.y - y) <= NodeDrawer.INTERFACESIZE / 2) {
					return this.outputs.get(i);
				}
			}
		}
		return null;
	}
}
