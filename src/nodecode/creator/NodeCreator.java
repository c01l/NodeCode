package nodecode.creator;

import nodecode.NodeDescription;
import nodecode.node.NCNode;

public abstract class NodeCreator implements Comparable<NodeCreator> {

	private final String fullPath;

	public NodeCreator(String fullPath) {
		this.fullPath = fullPath;
	}

	/**
	 * This method returns the path to the node this class is creating.
	 * 
	 * @return path
	 */
	public String getPath() {
		return this.fullPath;
	}

	public abstract NCNode create();
	public abstract NodeDescription getDescription();

	@Override
	public int compareTo(NodeCreator o) {
		if (o == null)
			throw new NullPointerException();

		if (o == this)
			return 0;

		return this.getPath().compareTo(o.getPath());
	}

}
