package nodecode.io;

import nodecode.Workspace;
import nodecode.creator.NodeCreator;
import nodecode.defaultPackage.DefaultPackageNodeCreator;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.ReturnCode;

public class ConsoleOut extends NCNode {

	public static final String PATH = "io.ConsoleOut";

	private static final NodeCreator creator;

	static {
		creator = new DefaultPackageNodeCreator(PATH, "This node prints an input to stdout.") {
			@Override
			public NCNode create() {
				return new ConsoleOut();
			}
		};
		Workspace.getSystemNodeCreator().add(PATH, creator);
	}

	private NodeInputInterface in;

	public ConsoleOut() {
		super("Console Out");

		this.in = new NodeInputInterface(Object.class, null);
		this.registerInput("Input", this.in);

	}

	@Override
	public ReturnCode run() {

		Workspace.getConsole().println(this.in.getValue());

		return ReturnCode.SUCCESS;
	}

	@Override
	public NodeCreator getCreator() {
		return creator;
	}

}
