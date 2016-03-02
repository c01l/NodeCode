package gui.controlpane;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nodecode.NodeDescription;
import nodecode.NodeDescription.Dependency;
import nodecode.node.NCNode;
import nodes.NodeInputInterface;
import nodes.NodeOutputInterface;

public class NodeControlPane extends JPanel {

	private static final long serialVersionUID = 3643287836637381925L;

	private NCNode n;

	// gui
	private JLabel l_name;

	public NodeControlPane(NCNode n) {
		this.n = n;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// gui
		this.l_name = new JLabel(n.getName());
		this.add(l_name);
		this.l_name.setFont(new Font("Arial", Font.BOLD, 34));

		// infos
		NodeDescription desc = n.getCreator().getDescription();

		JPanel p_description = new JPanel();
		this.add(p_description);
		p_description.setLayout(new BoxLayout(p_description, BoxLayout.Y_AXIS));

		p_description.add(new JLabel("Author: " + desc.getAuthor()));
		p_description.add(new JLabel("Path: " + this.n.getCreator().getPath()));
		p_description.add(new JLabel("Description: " + desc.getDescription()));
		Dependency[] dependencies = desc.getDependencies();
		p_description.add(
				new JLabel("Dependencies: " + (dependencies.length == 0 ? "None" : Arrays.toString(dependencies))));

		// inputs
		JPanel p_inputs = new JPanel();
		p_inputs.setLayout(new BoxLayout(p_inputs, BoxLayout.Y_AXIS));
		this.add(p_inputs);

		JLabel l_inHeader = new JLabel("Inputs");
		p_inputs.add(l_inHeader, BorderLayout.CENTER);
		l_inHeader.setFont(new Font("Arial", Font.BOLD, 25));

		if (this.n.getInputs().size() == 0) {
			JPanel p_spare = new JPanel();
			p_inputs.add(p_spare);
			p_spare.add(new JLabel("None"), BorderLayout.CENTER);
		}

		for (Entry<String, NodeInputInterface> e : this.n.getInputs().entrySet()) {
			JPanel p_input_entry = new JPanel();
			p_inputs.add(p_input_entry);
			p_input_entry.add(new JLabel(e.getKey() + ": "), BorderLayout.WEST);
			Object val = e.getValue().getValue();
			p_input_entry.add(new JLabel(val != null ? val.toString() : "null"), BorderLayout.EAST); // TODO
																										// images?
		}

		// outputs
		JPanel p_outputs = new JPanel();
		p_outputs.setLayout(new BoxLayout(p_outputs, BoxLayout.Y_AXIS));
		this.add(p_outputs);

		JLabel l_outHeader = new JLabel("Outputs");
		p_outputs.add(l_outHeader);
		l_outHeader.setFont(new Font("Arial", Font.BOLD, 25));

		for (Entry<String, NodeOutputInterface> e : this.n.getOutputs().entrySet()) {
			JPanel p_output_entry = new JPanel();
			p_outputs.add(p_output_entry);
			p_output_entry.add(new JLabel(e.getKey() + ": "), BorderLayout.WEST);
			p_output_entry.add(new JLabel(e.getValue().getValue().toString()), BorderLayout.EAST); // TODO
																									// images?
		}

	}

}
