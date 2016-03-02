package nodecode.compositor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import nodecode.NodeDescription;
import nodecode.node.NCHighlightInfo;
import nodecode.node.NCNode;
import nodecode.signals.NCSyncronizer;
import nodecode.signals.SyncronizerDrawer;
import nodes.Node;
import nodes.NodeInputInterface;
import nodes.NodeInterface;
import nodes.NodeOutputInterface;
import nodes.compositor.Compositor;
import nodes.compositor.CompositorFinishedCallback;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;
import nodes.signals.SignalSyncronizer;

public class NCCompositor extends NCNode {

	private Compositor real;
	private String fullPath;
	private NodeDescription description;

	private LinkedList<NCHighlightInfo<NodeOutputInterface>> innerIns;
	private LinkedList<NCHighlightInfo<NodeInputInterface>> innerOuts;

	private NCHighlightInfo<SignalOutputInterface> innerSigIn;
	private NCHighlightInfo<SignalInputInterface> innerSigOut, innerExOut;

	private CompositorDrawer drawer;

	private LinkedList<SyncronizerDrawer> syncDrawers;

	public NCCompositor(String name, String path) {
		super(name);
		this.fullPath = path + "." + name;
		this.description = null;

		this.real = new Compositor();
		this.drawer = new CompositorDrawer(this);

		this.innerIns = new LinkedList<>();
		this.innerOuts = new LinkedList<>();

		this.syncDrawers = new LinkedList<>();

		this.innerSigIn = new NCHighlightInfo<SignalOutputInterface>(this.real.getSignalStart());
		this.innerSigOut = new NCHighlightInfo<>(this.real.getSignalEnd());
		this.innerExOut = new NCHighlightInfo<>(this.real.getInnerExceptionReciever());
	}

	@Override
	public CompositorDrawer getDrawerInstance() {
		return this.drawer;
	}

	public NCHighlightInfo<NodeOutputInterface> getInnerNCInput(String name) {
		NodeOutputInterface input = this.real.getInnerInput(name);
		for (NCHighlightInfo<NodeOutputInterface> hi : this.innerIns) {
			if (hi.getReal() == input) {
				return hi;
			}
		}
		return null;
	}

	public NCHighlightInfo<NodeInputInterface> getInnerNCOutput(String name) {
		NodeInputInterface output = this.real.getInnerOutput(name);
		for (NCHighlightInfo<NodeInputInterface> hi : this.innerOuts) {
			if (hi.getReal() == output) {
				return hi;
			}
		}
		return null;
	}

	public LinkedHashMap<String, NCHighlightInfo<NodeOutputInterface>> getInnerNCInputs() {
		LinkedHashMap<String, NCHighlightInfo<NodeOutputInterface>> ret = new LinkedHashMap<>();
		HashMap<String, NodeOutputInterface> inputs = this.real.getInnerInputs();

		for (NCHighlightInfo<NodeOutputInterface> hi : this.innerIns) {
			// get real name
			String name = null;
			for (Entry<String, NodeOutputInterface> e : inputs.entrySet()) {
				if (e.getValue() == hi.getReal()) {
					name = e.getKey();
					break;
				}
			}

			// store
			ret.put(name, hi);
		}

		return ret;
	}

	public LinkedHashMap<String, NCHighlightInfo<NodeInputInterface>> getInnerNCOutputs() {
		LinkedHashMap<String, NCHighlightInfo<NodeInputInterface>> ret = new LinkedHashMap<>();
		HashMap<String, NodeInputInterface> outputs = this.real.getInnerOutputs();

		for (NCHighlightInfo<NodeInputInterface> hi : this.innerOuts) {
			// get real name
			String name = null;
			for (Entry<String, NodeInputInterface> e : outputs.entrySet()) {
				if (e.getValue() == hi.getReal()) {
					name = e.getKey();
					break;
				}
			}

			// store
			ret.put(name, hi);
		}

		return ret;
	}

	/**
	 * Returns a new NCCompositorCreator for this NCCompositor
	 * 
	 * @return a new NCCompositorCreator if everything is fine;
	 *         <code>null</code> if the description is missing
	 */
	@Override
	public NCCompositorCreator getCreator() {
		if (this.description == null)
			return null; // CHECKME exception?

		return NCCompositorCreator.newInstance(this, this.fullPath, this.description);
	}

	public List<NCNode> getNCNodes() {
		List<Node> l = this.real.getNodes();
		List<NCNode> ret = new ArrayList<>(l.size());

		for (Node n : l) {
			if (n instanceof NCNode) {
				ret.add((NCNode) n);
			}
		}
		return ret;
	}

	public List<NCSyncronizer> getNCSyncronizers() {
		List<SignalSyncronizer> l = this.real.getSyncronizers();
		List<NCSyncronizer> ret = new ArrayList<>(l.size());

		for (SignalSyncronizer s : l) {
			if (s instanceof NCSyncronizer) {
				ret.add((NCSyncronizer) s);
			}
		}

		return ret;
	}

	public NCNode getNode(NodeInterface i) {
		for (NCNode n : this.getNCNodes()) {
			if (n.hasInterface(i)) {
				return n;
			}
		}
		return null;
	}

	public NCSyncronizer getSyncronizer(NodeInterface i) {
		for (NCSyncronizer n : this.getNCSyncronizers()) {
			if (n.hasInterface(i)) {
				return n;
			}
		}
		return null;
	}

	public void addNode(NCNode n) {
		this.real.addNode(n);
	}

	public void removeNode(NCNode n) {
		this.real.removeNode(n);
	}

	public void addSyncronizer(SignalSyncronizer sync) {
		System.out.println("Normal Syncronizer added: " + sync.getInputSize() + "/" + sync.getOutputSize());

		this.addSyncronizer(new NCSyncronizer(sync));
	}

	public void addSyncronizer(NCSyncronizer sync) {
		this.real.addSyncronizer(sync);

		System.out.println("Syncronizer added: " + sync.getInputSize() + "/" + sync.getOutputSize());

		this.syncDrawers.add(sync.getDrawerInstance());
	}

	public void removeSyncronizer(SignalSyncronizer sync) {
		this.real.removeSyncronizer(sync);

		Iterator<SyncronizerDrawer> iter = this.syncDrawers.iterator();
		while (iter.hasNext()) {
			SyncronizerDrawer obj = iter.next();
			if (obj.getReal() == sync) {
				iter.remove();
			}
		}
	}

	public void removeSyncronizer(NCSyncronizer sync) {
		this.real.removeSyncronizer(sync);

		this.syncDrawers.remove(sync.getDrawerInstance());
	}

	public NCHighlightInfo<SignalInputInterface> getNCSignalEnd() {
		return this.innerSigOut;
	}

	public NCHighlightInfo<SignalInputInterface> getNCExceptionEnd() {
		return this.innerExOut;
	}

	public NCHighlightInfo<SignalOutputInterface> getNCSignalStart() {
		return this.innerSigIn;
	}

	public SignalInputInterface getSignalEnd() {
		return this.real.getSignalEnd();
	}

	public SignalInputInterface getExceptionEnd() {
		return this.real.getInnerExceptionReciever();
	}

	public SignalOutputInterface getSignalStart() {
		return this.real.getSignalStart();
	}

	public void start() {
		this.real.start();
	}

	public void addEdge(NCNode in1, String name1, NCNode in2, String name2) {
		this.real.addEdge(in1, name1, in2, name2);
	}

	public void addEdge(NodeOutputInterface start, NodeInputInterface end) {
		// TODO check if start and end are in this compositor

		end.setConnection(start);
	}

	public void setEndCallback(CompositorFinishedCallback finishCallback) {
		this.real.setEndCallback(finishCallback);
	}

	@Override
	public void registerInput(String name, NodeInputInterface i) {
		this.real.registerInput(name, i);
	}

	@Override
	protected void registerOutput(String name, NodeOutputInterface o) {
		this.real.registerOutput(name, o);
	}

	@Override
	protected NodeInputInterface removeInput(String name) {
		return this.real.removeInput(name);
	}

	@Override
	protected NodeOutputInterface removeOutput(String name) {
		return this.real.removeOutput(name);
	}

	public void setDescription(NodeDescription desc) {
		this.description = desc;
	}
}
