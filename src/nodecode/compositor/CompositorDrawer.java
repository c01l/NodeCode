package nodecode.compositor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import nodecode.EdgePainter;
import nodecode.InterfaceColorSet;
import nodecode.Workspace;
import nodecode.drawer.NodeDrawer;
import nodecode.node.Highlightable;
import nodecode.node.NCHighlightInfo;
import nodecode.node.NCNode;
import nodecode.signals.NCSyncronizer;
import nodecode.signals.SyncronizerDrawer;
import nodes.NodeInputInterface;
import nodes.NodeInterface;
import nodes.NodeOutputInterface;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public class CompositorDrawer extends NodeDrawer {

	public static final int INNERINTERFACESTART = 15;

	private NCCompositor comp;

	public CompositorDrawer(NCCompositor comp) {
		this(comp, DEFAULT_SPAWN_X, DEFAULT_SPAWN_Y);
	}

	public CompositorDrawer(NCCompositor comp, int x, int y) {
		super(comp, x, y);

		this.comp = (NCCompositor) comp;
	}

	@Override
	public void paintContent(Graphics2D g) {
		InterfaceColorSet colorSet = Workspace.getInterfaceColorSet();
		boolean drawSignals = Workspace.areSignalsShown();
		boolean drawData = Workspace.isDataShown();

		// draw inner edges
		if (drawData) {
			for (Entry<String, NCHighlightInfo<NodeInputInterface>> e : this.comp.getInnerNCOutputs().entrySet()) {
				NCHighlightInfo<NodeInputInterface> hi = e.getValue();

				NodeInputInterface nii = hi.getReal();
				NodeOutputInterface noi = nii.getSource();

				if (noi != null) {
					NodeDrawer drawer = this.comp.getNode(noi).getDrawerInstance();

					Point p1 = getInnerInterfacePosition(nii);
					Point p2 = drawer.getInterfacePosition(noi);

					EdgePainter.paint(g, p1, p2);
				}
			}

			for (NCNode n : this.comp.getNCNodes()) {
				NodeDrawer drawer = n.getDrawerInstance();
				HashMap<String, NCHighlightInfo<NodeInputInterface>> inputs = n.getNCInputs();
				for (NCHighlightInfo<NodeInputInterface> hi : inputs.values()) {
					NodeInputInterface ni = hi.getReal();
					NodeOutputInterface no = ni.getSource();

					if (no != null) {
						NodeDrawer drawer2 = this.comp.getNode(no).getDrawerInstance();

						Point p1 = drawer.getInterfacePosition(ni);
						Point p2 = drawer2.getInterfacePosition(no);

						EdgePainter.paint(g, p1, p2);
					}
				}
			}
		}

		if (drawSignals) {
			// draw signal start
			SignalOutputInterface startInterface = this.comp.getNCSignalStart().getReal();
			SignalInputInterface startTarget = startInterface.getTarget();
			if (startTarget != null) {
				Point p1 = getInnerInterfacePosition(startInterface);
				Point p2 = getInCompositorInterfacePosition(startTarget);

				EdgePainter.paint(g, p1, p2);
			}

			for (NCNode n : this.comp.getNCNodes()) {
				SignalOutputInterface outInterface = n.getSignalOutput();
				SignalInputInterface outTarget = outInterface.getTarget();
				if (outTarget != null) {
					Point p1 = getInCompositorInterfacePosition(outInterface);
					Point p2 = getInCompositorInterfacePosition(outTarget);

					EdgePainter.paint(g, p1, p2);
				}

				// exception
				SignalOutputInterface exInterface = n.getExceptionOutput();
				SignalInputInterface exTarget = exInterface.getTarget();
				if (exTarget != null) {
					Point p1 = getInCompositorInterfacePosition(exInterface);
					Point p2 = getInCompositorInterfacePosition(exTarget);

					EdgePainter.paint(g, p1, p2);
				}
			}

			for (NCSyncronizer s : this.comp.getNCSyncronizers()) {
				SyncronizerDrawer syncDrawer = s.getDrawerInstance();
				for (int i = 0; i < s.getOutputSize(); ++i) {

					SignalOutputInterface syncStartInterface = s.getOutput(i);
					SignalInputInterface syncTargetInterface = syncStartInterface.getTarget();
					if (syncTargetInterface != null) {

						Point p1 = syncDrawer.getInterfacePosition(syncStartInterface);
						Point p2 = getInCompositorInterfacePosition(syncTargetInterface);

						EdgePainter.paint(g, p1, p2);
					}
				}
			}
		}

		// draw inner interfaces
		if (drawData) {
			for (Entry<String, NCHighlightInfo<NodeInputInterface>> e : this.comp.getInnerNCOutputs().entrySet()) {
				NodeDrawer.paintInterface(g, getInnerInterfacePosition(e.getValue().getReal()), false, e.getValue(),
						colorSet);
			}

			for (Entry<String, NCHighlightInfo<NodeOutputInterface>> e : this.comp.getInnerNCInputs().entrySet()) {
				NodeDrawer.paintInterface(g, getInnerInterfacePosition(e.getValue().getReal()), true, e.getValue(),
						colorSet);
			}
		}
		if (drawSignals) {
			NodeDrawer.paintInterface(g, getInnerInterfacePosition(this.comp.getSignalStart()), false,
					this.comp.getNCSignalStart(), colorSet);
			NodeDrawer.paintInterface(g, getInnerInterfacePosition(this.comp.getSignalEnd()), true,
					this.comp.getNCSignalEnd(), colorSet);
			NodeDrawer.paintInterface(g, getInnerInterfacePosition(this.comp.getExceptionEnd()), true,
					this.comp.getNCExceptionEnd(), colorSet);
		}

		// draw inner nodes
		if (drawSignals) {
			for (NCSyncronizer s : this.comp.getNCSyncronizers()) {
				s.getDrawerInstance().paint(g);
			}
		}
		for (NCNode n : this.comp.getNCNodes()) {
			n.getDrawerInstance().paint(g);
		}

	}

	public Point getInnerInterfacePosition(NodeInterface ni) {
		if (ni == this.comp.getNCSignalStart().getReal()) {
			return new Point(0, this.getContentHeight() - getInnerInterfaceY(0));
		} else if (ni == this.comp.getNCExceptionEnd().getReal()) {
			return new Point(this.getContentWidth(), this.getContentHeight() - getInnerInterfaceY(0));
		} else if (ni == this.comp.getNCSignalEnd().getReal()) {
			return new Point(this.getContentWidth(), this.getContentHeight() - getInnerInterfaceY(1));
		}
		if (ni instanceof NodeInputInterface) {
			// inner output
			int i = 0;
			for (NCHighlightInfo<NodeInputInterface> nii : this.comp.getInnerNCOutputs().values()) {
				if (nii.getReal() == ni) {
					// CHECKME plus contentmargin?
					return new Point(this.getContentWidth(), getInnerInterfaceY(i));
				}
				++i;
			}
		} else if (ni instanceof NodeOutputInterface) {
			// inner input
			int i = 0;
			for (NCHighlightInfo<NodeOutputInterface> nii : this.comp.getInnerNCInputs().values()) {
				if (nii.getReal() == ni) {
					return new Point(0, getInnerInterfaceY(i));
				}
				++i;
			}
		}
		return null;
	}

	private int getInnerInterfaceY(int i) {
		return INNERINTERFACESTART + i * INTERFACEMARGIN;
	}

	@Override
	public Highlightable getHighlightable(int x, int y) {
		// inner interfaces
		if (0 < x && x < INTERFACESIZE / 2) {
			// signals
			Point p = getInnerInterfacePosition(this.comp.getSignalStart());
			if (Math.abs(p.y - y) < INTERFACESIZE / 2) {
				return this.comp.getNCSignalStart();
			}

			// data
			LinkedHashMap<String, NCHighlightInfo<NodeOutputInterface>> inputs = this.comp.getInnerNCInputs();
			int i = 0;
			for (NCHighlightInfo<NodeOutputInterface> hi : inputs.values()) {
				if (Math.abs(y - this.getInnerInterfaceY(i)) < INTERFACESIZE / 2) {
					return hi;
				}
				++i;
			}
		} else if (this.getContentWidth() - INTERFACESIZE / 2 < x && x < this.getContentWidth()) {
			// signals
			Point p = getInnerInterfacePosition(this.comp.getSignalEnd());
			if (Math.abs(p.y - y) < INTERFACESIZE / 2) {
				return this.comp.getNCSignalEnd();
			}

			Point p2 = getInnerInterfacePosition(this.comp.getExceptionEnd());
			if (Math.abs(p2.y - y) < INTERFACESIZE / 2) {
				return this.comp.getNCExceptionEnd();
			}

			// data
			LinkedHashMap<String, NCHighlightInfo<NodeInputInterface>> outputs = this.comp.getInnerNCOutputs();
			int i = 0;
			for (NCHighlightInfo<NodeInputInterface> hi : outputs.values()) {
				if (Math.abs(y - this.getInnerInterfaceY(i)) < INTERFACESIZE / 2) {
					return hi;
				}
				++i;
			}
		}

		// inner nodes
		for (NCNode n : this.comp.getNCNodes()) {
			NodeDrawer drawer = n.getDrawerInstance();
			Point p = drawer.getPosition();

			if (p.x < x && x < p.x + drawer.getWidth() && p.y < y && y < p.y + drawer.getHeight()) {
				return drawer.getHighlightable(x - p.x - NodeDrawer.CONTENTMARGIN,
						y - p.y - NodeDrawer.CONTENTMARGIN - NodeDrawer.HEADERHEIGHT);
			}

			Highlightable ret = drawer.getInterface(x, y);
			if (ret != null)
				return ret;

		}
		for (NCSyncronizer s : this.comp.getNCSyncronizers()) {
			SyncronizerDrawer drawer = s.getDrawerInstance();
			Point p = drawer.getPosition();

			if (p.x < x && x < p.x + drawer.getWidth() && p.y < y && y < p.y + drawer.getHeight()) {
				return drawer;
			}

			Highlightable ret = drawer.getInterface(x, y);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public Point getInCompositorInterfacePosition(NodeInterface i) {
		NCNode targetNode = this.comp.getNode(i);
		if (targetNode != null) {
			// connected to a node
			NodeDrawer targetDrawer = targetNode.getDrawerInstance();

			return targetDrawer.getInterfacePosition(i);
		}

		// connected to a syncronizer
		NCSyncronizer s = this.comp.getSyncronizer(i);
		if (s != null) {
			SyncronizerDrawer targetDrawer = s.getDrawerInstance();

			return targetDrawer.getInterfacePosition(i);
		}

		// compositor interface?
		Point pos = getInnerInterfacePosition(i);
		if (pos != null)
			return pos;

		return null;
	}
}
