package gui.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SaveDialog extends JPanel {

	private JTextField tf_name, tf_path, tf_file;
	private JTextArea ta_description;

	// TODO hight calculated the right way
	
	public SaveDialog() {
		this.setLayout(new GridLayout(0, 1));

		JPanel p_file = new JPanel();
		this.add(p_file);
		p_file.add(new JLabel("File: "), BorderLayout.WEST);
		this.tf_file = new JTextField(20);
		p_file.add(this.tf_file, BorderLayout.CENTER);
		JButton b_fileChooser = new JButton("Browse...");
		p_file.add(b_fileChooser, BorderLayout.EAST);
		b_fileChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO filechooser
			}
		});

		this.add(new JSeparator(JSeparator.HORIZONTAL));

		JPanel p_name = new JPanel();
		this.add(p_name);
		JLabel l_name = new JLabel("Name: ");
		p_name.add(l_name, BorderLayout.WEST);
		this.tf_name = new JTextField(30);
		p_name.add(this.tf_name, BorderLayout.CENTER);

		JPanel p_path = new JPanel();
		this.add(p_path);
		JLabel l_path = new JLabel("Path: ");
		p_path.add(l_path, BorderLayout.WEST);
		this.tf_path = new JTextField(30);
		p_path.add(this.tf_path, BorderLayout.CENTER);

		this.add(new JSeparator(JSeparator.HORIZONTAL));

		JPanel p_description = new JPanel();
		this.add(p_description);
		JLabel l_description = new JLabel("Description: ");
		p_description.add(l_description, BorderLayout.WEST);
		this.ta_description = new JTextArea(5, 40);
		p_description.add(this.ta_description, BorderLayout.CENTER);

	}

	public String getNodeName() {
		return this.tf_name.getText();
	}

	public String getPath() {
		return this.tf_path.getText();
	}

	public String getDescription() {
		return this.ta_description.getText();
	}

}
