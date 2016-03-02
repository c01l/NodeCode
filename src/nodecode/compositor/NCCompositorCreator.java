package nodecode.compositor;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import nodecode.NodeDescription;
import nodecode.creator.NodeCreator;
import nodecode.drawer.NodeDrawer;
import nodecode.node.NCHighlightInfo;
import nodecode.node.NCNode;
import nodecode.signals.NCSyncronizer;
import nodecode.signals.SyncronizerDrawer;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;
import nodes.signals.Signal;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public class NCCompositorCreator extends NodeCreator {

	private String name;
	private NodeDescription desc;

	private LinkedList<CompositorNode> nodes;
	private LinkedList<CompositorIO> inputs, outputs;
	private LinkedList<CompositorEdge> edges;
	private LinkedList<CompositorSyncronizer> syncronizers;

	public NCCompositorCreator(String path, String name, NodeDescription desc) {
		super(path);

		this.name = name;
		this.desc = desc;

		this.nodes = new LinkedList<>();
		this.syncronizers = new LinkedList<>();
		this.inputs = new LinkedList<>();
		this.outputs = new LinkedList<>();
		this.edges = new LinkedList<>();
	}

	public String getName() {
		return this.name;
	}

	public LinkedList<CompositorIO> getInputs() {
		return this.inputs;
	}

	public LinkedList<CompositorIO> getOutputs() {
		return this.outputs;
	}

	public LinkedList<CompositorNode> getNodes() {
		return this.nodes;
	}

	public LinkedList<CompositorSyncronizer> getSyncronizers() {
		return this.syncronizers;
	}

	public LinkedList<CompositorEdge> getEdges() {
		return this.edges;
	}

	@Override
	public NCNode create() throws InstantiationException {
		NCCompositor comp = new NCCompositor(this.name, this.getPath());

		// add nodes
		HashMap<Long, NCNode> nodeList = new HashMap<>(this.nodes.size());
		for (CompositorNode n : this.nodes) {
			NCNode realNode = n.create();
			nodeList.put(n.getID(), realNode);
			comp.addNode(realNode);
		}

		// add inner inputs and output
		for (CompositorIO in : this.inputs) {
			comp.registerInput(in.getName(), new NodeInputInterface(in.getType(), in.getDefaultValue()));
		}

		for (CompositorIO out : this.outputs) {
			comp.registerOutput(out.getName(), new NodeOutputInterface(out.getType(), out.getDefaultValue()));
		}

		// syncronizer
		HashMap<Long, NCSyncronizer> syncList = new HashMap<>(this.syncronizers.size());
		for (CompositorSyncronizer sync : this.syncronizers) {
			NCSyncronizer s = sync.create();
			syncList.put(sync.getID(), s);
			comp.addSyncronizer(s);
		}

		// add edges
		for (CompositorEdge e : this.edges) {
			String start = e.getStart();
			String end = e.getEnd();

			if (!e.isSignal()) {
				String[] startSplit = start.split(":", 2);
				if (startSplit.length != 2) {
					throw new InstantiationException("Start layout wrong at '" + start + "'");
				}

				NodeOutputInterface outInterface = null;

				if (startSplit[0].equals("NODEIN")) {
					NCHighlightInfo<NodeOutputInterface> hi = comp.getInnerNCInput(startSplit[1]);
					if (hi == null)
						throw new InstantiationException("Cannot find start interface '" + startSplit[1] + "'");

					outInterface = hi.getReal();
				} else {
					long ID = Long.parseLong(startSplit[0]);
					NCNode startNode = null;
					for (Entry<Long, NCNode> entry : nodeList.entrySet()) {
						if (entry.getKey().equals(ID)) {
							startNode = entry.getValue();
							break;
						}
					}

					if (startNode == null)
						throw new InstantiationException("Cannot find start node '" + startSplit[0] + "'");

					outInterface = startNode.getOutput(startSplit[1]);
				}

				if (outInterface == null)
					throw new InstantiationException("Cannot find start interface for '" + startSplit[0] + "'");

				String[] endSplit = end.split(":", 2);

				if (endSplit.length != 2)
					throw new InstantiationException("End layout wrong at '" + end + "'");

				NodeInputInterface inInterface = null;

				if (endSplit[0].equals("NODEOUT")) {
					NCHighlightInfo<NodeInputInterface> hi = comp.getInnerNCOutput(endSplit[1]);
					if (hi == null)
						throw new InstantiationException("Cannot get NODEOUT interface '" + endSplit[1] + "'");

					inInterface = hi.getReal();
				} else {
					long ID = Long.parseLong(endSplit[0]);
					NCNode endNode = null;
					for (Entry<Long, NCNode> entry : nodeList.entrySet()) {
						if (entry.getKey().equals(ID)) {
							endNode = entry.getValue();
							break;
						}
					}

					if (endNode == null)
						throw new InstantiationException("Cannot find end node");

					inInterface = endNode.getInput(endSplit[1]);
				}

				if (inInterface == null)
					throw new InstantiationException("Cannot find end interface");

				comp.addEdge(outInterface, inInterface);
			} else {
				// signal edge
				SignalOutputInterface startInterface = null;

				if (start.equals("NODEIN") && start.equals("SIGIN")) {
					startInterface = comp.getSignalStart();
				} else {
					String[] startSplit = start.split(":", 2);

					if (startSplit.length != 2)
						throw new InstantiationException("Start layout wrong '" + start + "'");

					long ID;
					if (startSplit[0].startsWith("s")) {
						ID = Long.parseLong(startSplit[0].substring(1));
						NCSyncronizer sync = null;

						for (Entry<Long, NCSyncronizer> entry : syncList.entrySet()) {
							if (entry.getKey().equals(ID)) {
								sync = entry.getValue();
								break;
							}
						}

						if (sync == null)
							throw new InstantiationException("Cannot find syncronizer '" + ID + "'");

						int out = Integer.parseInt(startSplit[1]);
						startInterface = sync.getOutput(out);

						if (startInterface == null)
							throw new InstantiationException(
									"Cannot find interface '" + out + "' at syncronizer '" + ID + "'");
					} else {
						ID = Long.parseLong(startSplit[0]);
						NCNode startNode = null;
						for (Entry<Long, NCNode> entry : nodeList.entrySet()) {
							if (entry.getKey().equals(ID)) {
								startNode = entry.getValue();
								break;
							}
						}
						if (startNode == null)
							throw new InstantiationException("Start node cannot be found");

						if (startSplit[1].equals("SIGOUT")) {
							startInterface = startNode.getSignalOutput();
						} else if (startSplit[2].equals("SIGEX")) {
							startInterface = startNode.getExceptionOutput();
						}
					}

				}

				if (startInterface == null)
					throw new InstantiationException("Cannot find start interface");

				SignalInputInterface endInterface = null;

				if (start.equals("NODEEND")) {
					endInterface = comp.getSignalEnd();
				} else if (start.equals("NODEEX")) {
					endInterface = comp.getExceptionEnd();
				} else {
					String[] endSplit = end.split(":", 2);

					if (endSplit.length != 2)
						throw new InstantiationException("End layout wrong '" + end + "'");

					long ID = Long.parseLong(endSplit[0]);
					NCNode endNode = null;
					for (Entry<Long, NCNode> entry : nodeList.entrySet()) {
						if (entry.getKey().equals(ID)) {
							endNode = entry.getValue();
							break;
						}
					}

					if (endNode == null)
						throw new InstantiationException("Cannot find end node");

					if (endSplit[1].equals("SIGIN")) {
						endInterface = endNode.getSignalInput();
					}
				}

				if (endInterface == null)
					throw new InstantiationException("Cannot find end interface");

				Signal.route(startInterface, endInterface);
			}
		}

		return comp;
	}

	@Override
	public NodeDescription getDescription() {
		return this.desc;
	}

	public void addNode(long iD, NodeCreator creator, int x, int y) {
		this.nodes.add(new CompositorNode(iD, creator, x, y));
	}

	public void addSyncronizer(long ID, int inputSize, int outputSize, int x, int y) {
		this.syncronizers.add(new CompositorSyncronizer(ID, inputSize, outputSize, x, y));
	}

	public void addInput(String inputName, Class<?> type, Object defaultValue) {
		this.inputs.add(new CompositorIO(inputName, type, defaultValue));
	}

	public void addOutput(String inputName, Class<?> type, Object defaultValue) {
		this.outputs.add(new CompositorIO(inputName, type, defaultValue));
	}

	public void addDataEdge(String start, String end) {
		this.edges.add(new CompositorEdge(start, end, false));
	}

	public void addSignalEdge(String start, String end) {
		this.edges.add(new CompositorEdge(start, end, true));
	}

	public static class CompositorNode {
		private NodeCreator creator;
		private int x, y;
		private long ID;

		public CompositorNode(long ID, NodeCreator creator, int x, int y) {
			this.creator = creator;
			this.x = x;
			this.y = y;
			this.ID = ID;
		}

		public long getID() {
			return this.ID;
		}

		public NodeCreator getCreator() {
			return this.creator;
		}

		public NCNode create() throws InstantiationException {
			NCNode n = this.creator.create();
			n.getDrawerInstance().setPosition(this.x, this.y);
			return n;
		}
	}

	public static class CompositorIO {
		private String name;
		private Class<?> type;
		private Object defaultValue;

		public CompositorIO(String name, Class<?> type, Object defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public String getName() {
			return this.name;
		}

		public Class<?> getType() {
			return this.type;
		}

		public Object getDefaultValue() {
			return this.defaultValue;
		}

	}

	public static class CompositorEdge {
		private String start, end;
		private boolean signal;

		public CompositorEdge(String start, String end, boolean signal) {
			this.start = start;
			this.end = end;
			this.signal = signal;
		}

		public String getStart() {
			return this.start;
		}

		public String getEnd() {
			return this.end;
		}

		public boolean isSignal() {
			return this.signal;
		}
	}

	public static class CompositorSyncronizer {
		private int inAmount, outAmount;
		private int x, y;
		private long ID;

		public CompositorSyncronizer(long iD, int inAmount, int outAmount, int x, int y) {
			this.ID = iD;
			this.inAmount = inAmount;
			this.outAmount = outAmount;
			this.x = x;
			this.y = y;
		}

		public long getID() {
			return this.ID;
		}

		public int getInAmount() {
			return this.inAmount;
		}

		public int getOutAmount() {
			return this.outAmount;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

		public NCSyncronizer create() {
			NCSyncronizer sync = new NCSyncronizer(this.getInAmount(), this.getOutAmount());
			sync.getDrawerInstance().setPosition(this.getX(), this.getY());
			return sync;
		}
	}

	public static NCCompositorCreator newInstance(NCCompositor comp, String path, NodeDescription desc) {

		NCCompositorCreator creator = new NCCompositorCreator(path, comp.getName(), desc);

		// inner inputs
		HashMap<String, NodeInputInterface> inputs = comp.getInputs();
		for (Entry<String, NodeInputInterface> e : inputs.entrySet()) {
			NodeInputInterface nii = e.getValue();
			creator.addInput(e.getKey(), nii.getType(), nii.getDefaultValue());
		}

		// inner outputs
		HashMap<String, NodeOutputInterface> outputs = comp.getOutputs();
		for (Entry<String, NodeOutputInterface> e : outputs.entrySet()) {
			NodeOutputInterface nii = e.getValue();
			creator.addInput(e.getKey(), nii.getType(), nii.getStartValue());
		}

		// nodes
		List<NCNode> nodeList = comp.getNCNodes();
		for (NCNode n : nodeList) {
			NodeDrawer drawer = n.getDrawerInstance();
			Point pos = drawer.getPosition();
			creator.addNode(n.getID(), n.getCreator(), pos.x, pos.y);
		}

		// syncronizer
		List<NCSyncronizer> syncList = comp.getNCSyncronizers();
		for (NCSyncronizer s : syncList) {
			SyncronizerDrawer drawer = s.getDrawerInstance();
			Point pos = drawer.getPosition();
			creator.addSyncronizer(s.getID(), s.getInputSize(), s.getOutputSize(), pos.x, pos.y);
		}

		// data edges
		for (NCNode n : nodeList) {
			for (Entry<String, NodeInputInterface> in : n.getInputs().entrySet()) {
				NodeOutputInterface noi = in.getValue().getSource();
				if (noi != null) {
					if (!handleDataConnection(comp, noi, creator, Long.toString(n.getID()), in.getKey())) {
						// nix gut
					}
				}
			}
		}
		for (Entry<String, NCHighlightInfo<NodeInputInterface>> in : comp.getInnerNCOutputs().entrySet()) {
			NodeInputInterface nii = in.getValue().getReal();
			NodeOutputInterface noi = nii.getSource();
			if (noi != null) {
				if (!handleDataConnection(comp, noi, creator, "NODEOUT", in.getKey())) {
					// nix ok
				}
			}
		}

		// signal edges
		for (NCNode n : nodeList) {
			handleSignalConnection(comp, n.getSignalOutput(), creator, Long.toString(n.getID()), false);
			handleSignalConnection(comp, n.getExceptionOutput(), creator, Long.toString(n.getID()), true);
		}
		handleSignalConnection(comp, comp.getSignalStart(), creator, "NODEIN", false);

		return creator;
	}

	private static void handleSignalConnection(NCCompositor comp, SignalOutputInterface startInterface,
			NCCompositorCreator creator, String startID, boolean exception) {
		SignalInputInterface endInterface = startInterface.getTarget();
		if (endInterface != null) {
			String targetID = null;
			String targetInterfaceName = null;

			if (endInterface == comp.getSignalEnd()) {
				targetID = "NODEOUT";
				targetInterfaceName = "SIGIN";
			} else if (endInterface == comp.getExceptionEnd()) {
				targetID = "NODEOUT";
				targetInterfaceName = "SIGEX";
			} else {
				for (NCNode targetNode : comp.getNCNodes()) {
					if (endInterface == targetNode.getSignalInput()) {
						targetID = Long.toString(targetNode.getID());
						targetInterfaceName = "SIGIN";
						break;
					}
				}
				if (targetID != null) {
					for (NCSyncronizer syncNode : comp.getNCSyncronizers()) {
						if (syncNode.hasInterface(endInterface)) {
							targetID = "s" + Long.toString(syncNode.getID());
							targetInterfaceName = Integer.toString(syncNode.getInputs().indexOf(endInterface));
						}
					}
				}
			}

			String startInterfaceName = exception ? "SIGEX" : "SIGOUT";

			creator.addSignalEdge(startID + ":" + startInterfaceName, targetID + ":" + targetInterfaceName);
		}
	}

	private static boolean handleDataConnection(NCCompositor comp, NodeOutputInterface noi, NCCompositorCreator creator,
			String targetNodeID, String targetInterfaceName) {
		NCNode start = comp.getNode(noi);
		String startName = null;
		for (Entry<String, NodeOutputInterface> e : start.getOutputs().entrySet()) {
			if (e.getValue().equals(noi)) {
				startName = e.getKey();
			}
		}

		if (startName != null) {
			creator.addDataEdge(start.getID() + ":" + startName, targetNodeID + ":" + targetInterfaceName);
			return true;
		}

		for (Entry<String, NCHighlightInfo<NodeOutputInterface>> e : comp.getInnerNCInputs().entrySet()) {
			if (noi.equals(e.getValue().getReal())) {
				startName = e.getKey();
				break;
			}
		}

		if (startName != null) {
			creator.addDataEdge("NODEIN:" + startName, targetNodeID + ":" + targetInterfaceName);
			return true;
		}

		return false; // couldn't add edge
	}
}
