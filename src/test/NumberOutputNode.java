package test;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import nodecode.NodeDescription;
import nodecode.creator.NodeCreator;
import nodecode.drawer.NodeDrawer;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;

public class NumberOutputNode extends NCNode {

	private NodeInputInterface in;
	private Number oldInput = 0;

	public NumberOutputNode() {
		super("Number Output");

		this.in = new NodeInputInterface(Number.class, 0);
		this.registerInput("Input", this.in);

		this.setDrawerInstance(new NodeDrawer(this) {
			@Override
			protected void paintContent(Graphics2D g) {
				Number n = (Number) in.getValue();

				if (!n.equals(oldInput)) {
					// recalculate width

					Font font = g.getFont();
					FontRenderContext context = g.getFontRenderContext();

					Rectangle2D titleBounds = font.getStringBounds(n.toString(), context);

					double titleWidth = titleBounds.getWidth();
					double titleHeight = titleBounds.getHeight();

					setContentDimension((int) titleWidth, (int) titleHeight);
				}

				g.drawString(n.toString(), 0, this.getContentHeight());

				oldInput = n;
			}
		});
	}

	@Override
	public NodeCreator getCreator() {
		return new NodeCreator("math.Output") {
			@Override
			public NodeDescription getDescription() {
				return new NodeDescription("coil", new String[0], "A number output node");
			}

			@Override
			public NCNode create() {
				return new NumberOutputNode();
			}
		};
	}
}
