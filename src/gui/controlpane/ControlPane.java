package gui.controlpane;

import javax.swing.JPanel;

import nodecode.node.NCNode;

public class ControlPane extends JPanel {
	public static final int WIDTH = 200;

	public void showNode(NCNode n) {
		this.removeAll();

		if (n != null) {
			System.out.println("Show " + n.getName());

			NodeControlPane ncp = new NodeControlPane(n);
			this.add(ncp);
			ncp.setPreferredSize(this.getPreferredSize());
		}

		this.validate();
	}
}
