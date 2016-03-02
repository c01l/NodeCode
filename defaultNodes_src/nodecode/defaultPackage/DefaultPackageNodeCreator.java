package nodecode.defaultPackage;
import nodecode.NodeDescription;
import nodecode.NodeDescription.Author;
import nodecode.NodeDescription.Dependency;
import nodecode.creator.NodeCreator;

public abstract class DefaultPackageNodeCreator extends NodeCreator {

	public static final Author mainAuthor = new Author(0, "coil");

	private NodeDescription desc;

	public DefaultPackageNodeCreator(String path, String description) {
		super(path);

		this.desc = new NodeDescription(mainAuthor, new Dependency[0], description);
	}

	@Override
	public NodeDescription getDescription() {
		return this.desc;
	}

}
