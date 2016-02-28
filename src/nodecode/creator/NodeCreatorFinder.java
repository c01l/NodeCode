package nodecode.creator;

import java.util.Set;

public interface NodeCreatorFinder {
	void findByName(String nodeNameStart, Set<NodeCreator> creatorList);

	void findByPath(String pathNameStart, Set<NodeCreator> creatorList);
}
