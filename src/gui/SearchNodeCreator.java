package gui;

import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import gui.utils.ListEntryDrawer;
import gui.utils.ListEntryDrawerCreator;
import nodecode.compositor.NCCompositor;
import nodecode.creator.NodeCreator;
import nodecode.creator.NodeCreatorFinder;

public class SearchNodeCreator extends JFrame {

	private JTextField tf_input;
	private ResultListModel<NodeCreator> resultModel;
	private JList<ListEntryDrawer<NodeCreator>> p_list;
	private NodeCreatorFinder globalNodeCreatorFinder;
	private NCCompositor compositor;

	public SearchNodeCreator(NodeCreatorFinder globalNodeCreator, NCCompositor compositor) {
		super("Search Node Dialog");
		this.globalNodeCreatorFinder = globalNodeCreator;
		this.compositor = compositor;

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setUndecorated(true);
		this.setSize(400, 300);
		this.setLocationRelativeTo(null);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				close();
			}
		});

		// gui
		this.tf_input = new JTextField();
		this.getContentPane().add(tf_input, BorderLayout.NORTH);
		this.tf_input.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("Key pressed: " + e.getKeyCode());
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					close();
					break;
				case KeyEvent.VK_ENTER:
					createNode();
					close();
					break;
				case KeyEvent.VK_UP:
					int index = p_list.getSelectedIndex();
					if (index == -1) {
						index = 0;
					} else if (index == 0) {
						index = resultModel.getSize() - 1;
					} else {
						index--;
					}
					p_list.setSelectedIndex(index);
					break;
				case KeyEvent.VK_DOWN:
					int index2 = p_list.getSelectedIndex();
					if (index2 == -1 || index2 == resultModel.getSize() - 1) {
						index2 = 0;
					} else {
						index2++;
					}
					p_list.setSelectedIndex(index2);
					break;
				default:
					// do nothing
				}
				super.keyPressed(e);
			}
		});

		this.tf_input.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateList();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateList();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateList();
			}
		});

		ScrollPane scroll = new ScrollPane();
		this.getContentPane().add(scroll, BorderLayout.CENTER);

		this.resultModel = new ResultListModel<>(new ListEntryDrawerCreator<NodeCreator>() {
			@Override
			public ListEntryDrawer<NodeCreator> create(NodeCreator x) {
				return new ListEntryDrawer<NodeCreator>(x) {
					@Override
					public String toString() {
						return this.elem.getPath();
					}
				};
			}
		});
		this.p_list = new JList<>(this.resultModel);
		scroll.add(this.p_list);
		this.p_list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					close();
				}
			}
		});
		this.p_list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					createNode();
					close();
				}
			}
		});
		// TODO test double click listener -> createNode();
	}

	private void updateList() {
		String updatingFor = this.tf_input.getText();
		System.out.println("Updating for: '" + updatingFor + "'"); // KILLME

		Set<NodeCreator> list = new TreeSet<>();
		this.globalNodeCreatorFinder.findByName(updatingFor, list);
		this.globalNodeCreatorFinder.findByPath(updatingFor, list);

		this.resultModel.setList(new ArrayList<>(list));

		this.p_list.validate();
	}

	private void close() {
		this.dispose();
	}

	private void createNode() {
		if (this.p_list.getSelectedIndex() == -1)
			return;

		NodeCreator creator = this.p_list.getSelectedValue().getReal();
		System.out.println("Using " + creator.getPath());
		try {
			this.compositor.addNode(creator.create());
		} catch (InstantiationException e) {
			JOptionPane.showMessageDialog(this, "Could not instanciate node!\n\n" + e.getMessage(), "Node Error",
					JOptionPane.ERROR_MESSAGE);
		}
		// TODO repaint compositor

		// TODO creating the wrong node (Target: compositor.Start -> Real:
		// output.Debug)
	}

	private class ResultListModel<T> implements ListModel<ListEntryDrawer<T>> {
		private List<T> list;
		private LinkedList<ListDataListener> listeners;
		private ListEntryDrawerCreator<T> creator;

		public ResultListModel(ListEntryDrawerCreator<T> creator) {
			this.list = new LinkedList<>();
			this.listeners = new LinkedList<>();
			this.creator = creator;
		}

		public void setList(List<T> l) {
			this.list = l;
			for (ListDataListener listener : this.listeners) {
				listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.list.size()));
			}
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			this.listeners.add(l);
		}

		@Override
		public ListEntryDrawer<T> getElementAt(int index) {
			return this.creator.create(this.list.get(index));
		}

		@Override
		public int getSize() {
			return this.list.size();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			this.listeners.remove(l);
		}
	}

}
