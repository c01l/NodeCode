package nodecode.creator;

import java.util.LinkedList;
import java.util.Set;

public class HirachicalNodeCreatorFinder implements NodeCreatorFinder {

	private LinkedList<NodeCreatorFinder> creators;

	public HirachicalNodeCreatorFinder() {
		this.creators = new LinkedList<>();
	}

	public void add(NodeCreatorFinder ncf) {
		this.creators.add(ncf);
	}

	public void remove(NodeCreatorFinder ncf) {
		this.creators.remove(ncf);
	}

	@Override
	public void findByName(String nodeNameStart, Set<NodeCreator> list) {
		for (NodeCreatorFinder nc : this.creators) {
			nc.findByName(nodeNameStart, list);
		}
	}
	
	@Override
	public void findByPath(String packageStart, Set<NodeCreator> packages) {
		for(NodeCreatorFinder nc : this.creators) {
			nc.findByPath(packageStart, packages);
		}
	}
}
