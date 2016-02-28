package test;

import java.awt.Color;
import java.awt.Graphics2D;

import nodecode.NodeDescription;
import nodecode.creator.NodeCreator;
import nodecode.drawer.DefaultNodeDrawer;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;

public class NumberAdderNode extends NCNode {

	private NodeInputInterface in1, in2;
	private NodeOutputInterface out;

	public NumberAdderNode() {
		super("Adder");

		this.in1 = new NodeInputInterface(Number.class, 0);
		this.in2 = new NodeInputInterface(Number.class, 0);
		this.out = new NodeOutputInterface(Number.class, 0);

		this.registerInput("Input 1", this.in1);
		this.registerInput("Input 2", this.in2);
		this.registerOutput("Output", this.out);

		this.setDrawerInstance(new DefaultNodeDrawer(this) {
			@Override
			protected void paintContent(Graphics2D g) {
				g.setColor(Color.RED);
				g.drawRect(5, 5, 10, 10);
			}
		});
	}

	@Override
	public NodeCreator getCreator() {
		return new NodeCreator("math.Adder") {
			@Override
			public NodeDescription getDescription() {
				return new NodeDescription("coil", new String[0], "A number adder node");
			}

			@Override
			public NCNode create() {
				return new NumberAdderNode();
			}
		};
	}

}
