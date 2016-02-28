package test;

import nodecode.NodeDescription;
import nodecode.creator.NodeCreator;
import nodecode.node.NCNode;
import nodes.NodeOutputInterface;

public class NumberInputNode extends NCNode {

	public static final String path = "math.NumberInput";
	
	private NodeOutputInterface output;

	public NumberInputNode() {
		super("Number Input");

		this.output = new NodeOutputInterface(Number.class, 0);
		this.registerOutput("Output", this.output);
	}

	@Override
	public NodeCreator getCreator() {
		return new NodeCreator(path) {
			@Override
			public NodeDescription getDescription() {
				return new NodeDescription("coil", new String[0],
						"A number input node");
			}

			@Override
			public NCNode create() {
				return new NumberInputNode();
			}
		};
	}
}
