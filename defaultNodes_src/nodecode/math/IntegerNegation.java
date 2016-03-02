package nodecode.math;

import nodecode.Workspace;
import nodecode.creator.NodeCreator;
import nodecode.defaultPackage.DefaultPackageNodeCreator;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;
import nodes.ReturnCode;

public class IntegerNegation extends NCNode {

	public static final String PATH = "math.Negate";

	public static final NodeCreator creator;

	static {
		creator = new DefaultPackageNodeCreator(PATH, "This node produces the negation of an integer.") {
			@Override
			public NCNode create() {
				return new IntegerNegation();
			}
		};
		Workspace.getSystemNodeCreator().add(PATH, creator);
	}

	private NodeInputInterface in;
	private NodeOutputInterface out;

	public IntegerNegation() {
		super("Negate");

		this.in = new NodeInputInterface(Integer.class, 0);
		this.registerInput("Input", this.in);
		this.out = new NodeOutputInterface(Integer.class, 0);
		this.registerOutput("Output", this.out);
	}

	@Override
	public ReturnCode run() {

		try {
			this.out.setValue(-(Integer) this.in.getValue());
			return ReturnCode.SUCCESS;
		} catch (Exception e) { // Overflows,...
			return ReturnCode.EXCEPTION;
		}
	}

	@Override
	public NodeCreator getCreator() {
		return creator;
	}

}
