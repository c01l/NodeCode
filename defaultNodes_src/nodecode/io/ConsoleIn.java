package nodecode.io;

import java.io.InputStream;
import java.util.Scanner;

import nodecode.Workspace;
import nodecode.creator.NodeCreator;
import nodecode.defaultPackage.DefaultPackageNodeCreator;
import nodecode.node.NCNode;
import nodes.NodeOutputInterface;
import nodes.ReturnCode;

public class ConsoleIn extends NCNode {

	public static final String PATH = "io.ConsoleIn";

	public static final NodeCreator creator;

	static {
		creator = new DefaultPackageNodeCreator(PATH, "This node reads a line from stdin.") {
			@Override
			public NCNode create() {
				return new ConsoleIn();
			}
		};
		Workspace.getSystemNodeCreator().add(PATH, creator);
	}

	private NodeOutputInterface out;

	public ConsoleIn() {
		super("Console In");

		this.out = new NodeOutputInterface(String.class, "");
		this.registerOutput("Output", this.out);
	}

	@Override
	public ReturnCode run() {

		InputStream iStream = Workspace.getConsoleIn();

		// will not be closed because its not my stream
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(iStream);
		try {
			String line = scanner.nextLine();

			this.out.setValue(line);

			return ReturnCode.SUCCESS;
		} catch (Exception e) {
			return ReturnCode.EXCEPTION;
		}
	}

	@Override
	public NodeCreator getCreator() {
		return creator;
	}

}
