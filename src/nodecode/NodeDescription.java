package nodecode;

import nodecode.node.NCNode;

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
	private Author author;

	/**
	 * Path to other {@link NCNode}s that are used by this {@link NCNode}
	 */
	private Dependency[] dependencies;

	/**
	 * Description of the Node-Action
	 */
	private String description;

	public NodeDescription(Author author, Dependency[] dependencies, String description) {
		this.author = author;
		this.dependencies = dependencies;
		this.description = description;
	}

	public Author getAuthor() {
		return this.author;
	}

	public Dependency[] getDependencies() {
		return this.dependencies;
	}

	public String getDescription() {
		return this.description;
	}

	public static class Dependency {
		private String pack;
		private String nodePath;

		public Dependency(String pack, String nodePath) {
			this.pack = pack;
			this.nodePath = nodePath;
		}

		public String getPackage() {
			return this.pack;
		}

		public String getNodePath() {
			return this.nodePath;
		}

		@Override
		public String toString() {
			return this.pack + ":" + this.nodePath;
		}
	}

	public static class Author {
		private final long ID;
		private String name;

		public Author(long iD, String name) {
			ID = iD;
			this.name = name;
		}

		public long getID() {
			return this.ID;
		}

		public String getName() {
			return this.name;
		}

	}

}
