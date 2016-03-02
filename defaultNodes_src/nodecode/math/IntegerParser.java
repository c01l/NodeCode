package nodecode.math;

import nodecode.Workspace;
import nodecode.creator.NodeCreator;
import nodecode.defaultPackage.DefaultPackageNodeCreator;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;
import nodes.ReturnCode;

public class IntegerParser extends NCNode {

	public static final String PATH = "math.IntParser";

	private static final NodeCreator creator;

	static {
		creator = new DefaultPackageNodeCreator(PATH, "This node parses an integer out of a string.") {
			@Override
			public NCNode create() {
				return new IntegerParser();
			}
		};
		Workspace.getSystemNodeCreator().add(PATH, creator);
	}

	private NodeInputInterface in;
	private NodeOutputInterface out;

	public IntegerParser() {
		super("Integer Parser");

		this.in = new NodeInputInterface(String.class, "");
		this.registerInput("Input", this.in);

		this.out = new NodeOutputInterface(Integer.class, 0);
		this.registerOutput("Output", this.out);
	}

	@Override
	public ReturnCode run() {
		try {
			int i = Integer.parseInt((String) this.in.getValue());

			this.out.setValue(i);

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
