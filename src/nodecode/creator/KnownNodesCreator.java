package nodecode.creator;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class KnownNodesCreator implements NodeCreatorFinder {

	private HashMap<String, NodeCreator> knownNodes;

	public KnownNodesCreator() {
		this.knownNodes = new HashMap<>();
	}

	public void add(String fullPath, NodeCreator c) {
		if (this.knownNodes.containsKey(fullPath)) {
			throw new IllegalArgumentException("Node already has a creator.");
		}
		this.knownNodes.put(fullPath, c);
	}

	public void remove(String fullPath) {
		this.knownNodes.remove(fullPath);
	}

	@Override
	public void findByName(String nodeNameStart, Set<NodeCreator> list) {
		for (Entry<String, NodeCreator> e : this.knownNodes.entrySet()) {
			String fullPath = e.getKey();
			String nodeName = fullPath.substring(fullPath.lastIndexOf(".") + 1);

			if (nodeName.startsWith(nodeNameStart)) {
				list.add(e.getValue());
			}
		}
	}

	@Override
	public void findByPath(String packageStart, Set<NodeCreator> packages) {
		for (Entry<String, NodeCreator> e : this.knownNodes.entrySet()) {
			String fullPath = e.getKey();

			if (fullPath.startsWith(packageStart)) {
				packages.add(e.getValue());
			}
		}
	}

}
