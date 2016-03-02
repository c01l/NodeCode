package nodecode.node;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import nodecode.creator.NodeCreator;
import nodecode.drawer.DefaultNodeDrawer;
import nodecode.drawer.NodeDrawer;
import nodes.Node;
import nodes.NodeInputInterface;
import nodes.NodeInterface;
import nodes.NodeOutputInterface;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public abstract class NCNode extends Node {

	private static long nodeIDCounter = 0;
	private final long NODEID;

	private String name;

	private LinkedList<NCHighlightInfo<NodeInputInterface>> inputNCInfo;
	private LinkedList<NCHighlightInfo<NodeOutputInterface>> outputNCInfo;

	private NCHighlightInfo<SignalInputInterface> ncsigin;
	private NCHighlightInfo<SignalOutputInterface> ncsigout;
	private NCHighlightInfo<SignalOutputInterface> ncsigex;

	protected NodeDrawer drawer;

	public NCNode(String name) {
		this.name = name;

		this.NODEID = nodeIDCounter++;

		this.inputNCInfo = new LinkedList<>();
		this.outputNCInfo = new LinkedList<>();

		this.ncsigin = new NCHighlightInfo<SignalInputInterface>(this.getSignalInput());
		this.ncsigout = new NCHighlightInfo<SignalOutputInterface>(this.getSignalOutput());
		this.ncsigex = new NCHighlightInfo<SignalOutputInterface>(this.getExceptionOutput());

		this.drawer = new DefaultNodeDrawer(this);
	}

	/**
	 * Returns the name of this {@link NCNode}.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the {@link NCHighlightInfo} with its {@link NodeInputInterface}.
	 * 
	 * @param name
	 *            the name of the interface
	 * @return {@link NCHighlightInfo} if the {@link NodeInputInterface} was
	 *         found; <code>null</code> otherwise.
	 */
	public NCHighlightInfo<NodeInputInterface> getNCInput(String name) {
		NodeInputInterface input = super.getInput(name);
		for (NCHighlightInfo<NodeInputInterface> hi : this.inputNCInfo) {
			if (hi.getReal() == input) {
				return hi;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link NCHighlightInfo} with its {@link NodeOutputInterface}.
	 * 
	 * @param name
	 *            the name of the interface
	 * @return {@link NCHighlightInfo} if the {@link NodeOutputInterface} was
	 *         found; <code>null</code> otherwise.
	 */
	public NCHighlightInfo<NodeOutputInterface> getNCOutput(String name) {
		NodeOutputInterface output = super.getOutput(name);
		for (NCHighlightInfo<NodeOutputInterface> hi : this.outputNCInfo) {
			if (hi.getReal() == output) {
				return hi;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link NCHighlightInfo} with the {@link SignalInputInterface}
	 * of this {@link NCNode}.
	 * 
	 * @return
	 */
	public NCHighlightInfo<SignalInputInterface> getNCSignalInput() {
		return this.ncsigin;
	}

	/**
	 * Returns the {@link NCHighlightInfo} with the
	 * {@link SignalOutputInterface} of this {@link NCNode}.
	 * 
	 * @return
	 */
	public NCHighlightInfo<SignalOutputInterface> getNCSignalOutput() {
		return this.ncsigout;
	}

	/**
	 * Returns this {@link NCHighlightInfo} with the Exception Output of this
	 * {@link NCNode}.
	 * 
	 * @return
	 */
	public NCHighlightInfo<SignalOutputInterface> getNCExceptionOutput() {
		return this.ncsigex;
	}

	public LinkedHashMap<String, NCHighlightInfo<NodeInputInterface>> getNCInputs() {
		LinkedHashMap<String, NCHighlightInfo<NodeInputInterface>> ret = new LinkedHashMap<>();

		for (Entry<String, NodeInputInterface> e : this.getInputs().entrySet()) {
			ret.put(e.getKey(), this.getInput(e.getValue()));
		}

		return ret;
	}

	public LinkedHashMap<String, NCHighlightInfo<NodeOutputInterface>> getNCOutputs() {
		LinkedHashMap<String, NCHighlightInfo<NodeOutputInterface>> ret = new LinkedHashMap<>();

		for (Entry<String, NodeOutputInterface> e : this.getOutputs().entrySet()) {
			ret.put(e.getKey(), this.getOutput(e.getValue()));
		}

		return ret;
	}

	@Override
	protected void registerInput(String name, NodeInputInterface i) {
		super.registerInput(name, i);

		this.inputNCInfo.add(new NCHighlightInfo<NodeInputInterface>(i));
	}

	@Override
	protected void registerOutput(String name, NodeOutputInterface o) {
		super.registerOutput(name, o);

		this.outputNCInfo.add(new NCHighlightInfo<NodeOutputInterface>(o));
	}

	@Override
	protected NodeInputInterface removeInput(String name) {
		NodeInputInterface ret = super.removeInput(name);

		Iterator<NCHighlightInfo<NodeInputInterface>> iter = this.inputNCInfo.iterator();
		while (iter.hasNext()) {
			NCHighlightInfo<NodeInputInterface> obj = iter.next();

			if (obj.getReal() == ret)
				iter.remove();
		}

		return ret;
	}

	@Override
	protected NodeOutputInterface removeOutput(String name) {
		NodeOutputInterface ret = super.removeOutput(name);

		Iterator<NCHighlightInfo<NodeOutputInterface>> iter = this.outputNCInfo.iterator();
		while (iter.hasNext()) {
			NCHighlightInfo<NodeOutputInterface> obj = iter.next();

			if (obj.getReal() == ret)
				iter.remove();
		}

		return ret;
	}

	protected void setDrawerInstance(NodeDrawer drawer) {
		this.drawer = drawer;
	}

	public NodeDrawer getDrawerInstance() {
		return this.drawer;
	}

	public abstract NodeCreator getCreator();

	public boolean hasInterface(NodeInterface i) {
		if (this.ncsigin.getReal() == i) {
			return true;
		} else if (this.ncsigout.getReal() == i) {
			return true;
		} else if (this.ncsigex.getReal() == i) {
			return true;
		}

		if (i instanceof NodeInputInterface) {
			if (this.getInputs().containsValue(i)) {
				return true;
			}
		}

		if (i instanceof NodeOutputInterface) {
			if (this.getOutputs().containsValue(i)) {
				return true;
			}
		}

		return false;
	}

	public NCHighlightInfo<NodeInputInterface> getInput(NodeInputInterface i) {
		for (NCHighlightInfo<NodeInputInterface> hi : this.inputNCInfo) {
			if (hi.getReal() == i) {
				return hi;
			}
		}
		return null;
	}

	public NCHighlightInfo<NodeOutputInterface> getOutput(NodeOutputInterface i) {
		for (NCHighlightInfo<NodeOutputInterface> hi : this.outputNCInfo) {
			if (hi.getReal() == i) {
				return hi;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NCNode) {
			return this.NODEID == ((NCNode) obj).NODEID;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) this.NODEID;
	}

	@Override
	public String toString() {
		return this.getCreator().getPath() + "[ID=" + this.NODEID + "]";
	}

	public long getID() {
		return this.NODEID;
	}
	
}
