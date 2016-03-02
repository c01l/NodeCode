package nodecode.math;

import nodecode.Workspace;
import nodecode.creator.NodeCreator;
import nodecode.defaultPackage.DefaultPackageNodeCreator;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;
import nodes.ReturnCode;

public class IntegerAddition extends NCNode {

	public static final String PATH = "math.Add";

	public static final NodeCreator creator;

	static {
		creator = new DefaultPackageNodeCreator(PATH, "This Node adds two integer values.") {
			@Override
			public NCNode create() {
				return new IntegerAddition();
			}
		};
		Workspace.getSystemNodeCreator().add(PATH, creator);
	}

	private NodeInputInterface in1, in2;
	private NodeOutputInterface sum;

	public IntegerAddition() {
		super("Add");

		this.in1 = new NodeInputInterface(Integer.class, 0);
		this.registerInput("Addend 1", this.in1);
		this.in2 = new NodeInputInterface(Integer.class, 0);
		this.registerInput("Addend 2", this.in2);
		this.sum = new NodeOutputInterface(Integer.class, 0);
		this.registerOutput("Sum", this.sum);
	}

	@Override
	public ReturnCode run() {

		try {
			this.sum.setValue((Integer) this.in1.getValue() + (Integer) this.in2.getValue());
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
