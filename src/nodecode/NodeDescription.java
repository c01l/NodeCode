package nodecode;

/**
 * This set of information stores data about a {@link NCNode}.
 * 
 * @author Roland Wallner
 *
 */
public class NodeDescription {

	/**
	 * Author of the {@link NCNode}
	 */
	private String author;

	/**
	 * Path to other {@link NCNode}s that are used by this {@link NCNode}
	 */
	private String[] dependencies;
	
	/**
	 * Description of the Node-Action
	 */
	private String description;

	public NodeDescription(String author, String[] dependencies, String description) {
		this.author = author;
		this.dependencies = dependencies;
		this.description = description;
	}

	public String getAuthor() {
		return this.author;
	}

	public String[] getDependencies() {
		return this.dependencies;
	}

	public String getDescription() {
		return this.description;
	}

}
