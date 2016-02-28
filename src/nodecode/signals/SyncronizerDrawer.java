package nodecode.signals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import nodecode.InterfaceColorSet;
import nodecode.Workspace;
import nodecode.drawer.NodeDrawer;
import nodecode.drawer.ObjectDrawer;
import nodecode.node.NCHighlightInfo;
import nodes.NodeInterface;
import nodes.signals.Signal;
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
			g.setColor(Color.BLACK.brighter());
		} else {
			g.setColor(Color.BLACK);
		}

		g.fillArc(pos.x, pos.y, WIDTH, WIDTH, 0, 180);
		g.fillRect(pos.x, pos.y + WIDTH / 2, WIDTH, this.getHeight() - WIDTH);
		g.fillArc(pos.x, pos.y + this.getHeight() - WIDTH, WIDTH, WIDTH, 180, 180);

		InterfaceColorSet colorSet = Workspace.getInterfaceColorSet();
		Color signalColor = colorSet.getColor(Signal.class);

		int i = 0;
		for (NCHighlightInfo<SignalInputInterface> r : this.inputs) {
			Point p = getInterfacePosition(i, true);

			if (r.isHighlighted()) {
				g.setColor(signalColor.brighter());
			} else {
				g.setColor(signalColor);
			}

			g.fillArc(p.x - NodeDrawer.INTERFACESIZE / 2, p.y - NodeDrawer.INTERFACESIZE / 2, NodeDrawer.INTERFACESIZE,
					NodeDrawer.INTERFACESIZE, 90, 180);
			++i;
		}

		i = 0;
		for (NCHighlightInfo<SignalOutputInterface> r : this.outputs) {
			Point p = getInterfacePosition(i, false);
			if (r.isHighlighted()) {
				g.setColor(signalColor.brighter());
			} else {
				g.setColor(signalColor);
			}
			g.fillArc(p.x - NodeDrawer.INTERFACESIZE / 2, p.y - NodeDrawer.INTERFACESIZE / 2, NodeDrawer.INTERFACESIZE,
					NodeDrawer.INTERFACESIZE, 270, 180);
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
}
