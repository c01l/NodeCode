package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import gui.controlpane.ControlPane;
import gui.dialogs.SaveDialog;
import nodecode.EdgePainter;
import nodecode.NodeDescription;
import nodecode.NodeDescription.Author;
import nodecode.NodeDescription.Dependency;
import nodecode.Workspace;
import nodecode.compositor.CompositorDrawer;
import nodecode.compositor.NCCompositor;
import nodecode.compositor.NCCompositorCreator;
import nodecode.creator.HirachicalNodeCreatorFinder;
import nodecode.creator.NodeCreator;
import nodecode.drawer.NodeDrawer;
import nodecode.drawer.ObjectDrawer;
import nodecode.drawer.Positionable;
import nodecode.io.ConsoleIn;
import nodecode.io.ConsoleOut;
import nodecode.loader.CompositorLoader;
import nodecode.loader.LoaderException;
import nodecode.math.IntegerAddition;
import nodecode.math.IntegerParser;
import nodecode.node.Highlightable;
import nodecode.node.NCHighlightInfo;
import nodecode.node.NCNode;
import nodecode.signals.NCSyncronizer;
import nodecode.signals.SyncronizerDrawer;
import nodes.NodeInputInterface;
import nodes.NodeInterface;
import nodes.NodeOutputInterface;
import nodes.compositor.CompositorFinishedCallback;
import nodes.signals.Signal;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;

public class MainFrame extends JFrame {

	private File currentFile;
	private boolean changed = false;

	private NCCompositor comp;
	private HirachicalNodeCreatorFinder systemNodeCreator;

	/**
	 * Paths to recent nodes
	 */
	private SortedSet<NodeCreator> recentNodes;

	// GUI
	private JMenu m_recent_nodes;
	private CurrentCompositorDrawer drawingPane;
	private ControlPane controlPane;
	private JSplitPane splitPane;
	private JFrame self;

	// menu
	private JCheckBoxMenuItem mi_view_showData, mi_view_showSignals;

	public MainFrame() {
		super("NodeCode");
		this.self = this;
		this.setTitle(generateTitle());
		this.setSize(1024, 600);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quit();
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(getContentPane().getWidth() - ControlPane.WIDTH);
			}
		});

		// element initialization
		this.recentNodes = new TreeSet<>();

		this.comp = new NCCompositor("", "");
		this.comp.setEndCallback(new CompositorFinishedCallback() {
			@Override
			public void compositorRaisedAnException() {
				JOptionPane.showMessageDialog(self, "Comopsitor finished with an exception!", "Compositor Information",
						JOptionPane.ERROR_MESSAGE);
			}

			@Override
			public void compositorFinished() {
				JOptionPane.showMessageDialog(self, "Comopsitor finished!", "Compositor Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		this.drawingPane = new CurrentCompositorDrawer(this.comp.getDrawerInstance());

		this.systemNodeCreator = new HirachicalNodeCreatorFinder();
		this.systemNodeCreator.add(Workspace.getSystemNodeCreator());

		// menu bar
		this.setJMenuBar(createMenuBar());

		JToolBar tb_signals = createSignalsToolBar();
		this.getContentPane().add(tb_signals);

		// main window

		this.controlPane = new ControlPane();
		this.controlPane.setPreferredSize(new Dimension(200, this.getHeight()));

		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		this.getContentPane().add(this.splitPane);
		this.splitPane.setEnabled(false);
		this.splitPane.setDividerLocation((int) (this.getWidth() - ControlPane.WIDTH));
		this.splitPane.add(this.drawingPane);
		this.splitPane.add(this.controlPane);

		/* TESTING CODE START */

		ConsoleIn in1 = new ConsoleIn();

		IntegerParser p1 = new IntegerParser();
		IntegerParser p2 = new IntegerParser();

		IntegerAddition adder = new IntegerAddition();
		ConsoleOut out = new ConsoleOut();

		this.comp.addNode(adder);
		this.comp.addNode(in1);
		this.comp.addNode(out);
		this.comp.addNode(p1);
		this.comp.addNode(p2);

		/* TESTING CODE END */

	}

	private JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();

		// file - Tab
		JMenu m_file = new JMenu("File");
		mb.add(m_file);

		JMenuItem mi_file_newproject = new JMenuItem("New Project");
		m_file.add(mi_file_newproject);
		mi_file_newproject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		mi_file_newproject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO renew project
			}
		});

		m_file.addSeparator();

		JMenuItem mi_file_open = new JMenuItem("Open");
		m_file.add(mi_file_open);
		mi_file_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		mi_file_open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO load project
			}
		});

		JMenuItem mi_file_save = new JMenuItem("Save");
		m_file.add(mi_file_save);
		mi_file_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		mi_file_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		JMenuItem mi_file_save_as = new JMenuItem("Save As");
		m_file.add(mi_file_save_as);
		mi_file_save_as.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mi_file_save_as.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveas();
			}
		});

		m_file.addSeparator();

		JMenuItem mi_file_import_package = new JMenuItem("Import Package");
		m_file.add(mi_file_import_package);
		mi_file_import_package.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO load package
			}
		});

		JMenuItem mi_file_export_package = new JMenuItem("Export Package");
		m_file.add(mi_file_export_package);
		mi_file_export_package.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO export package
			}
		});

		m_file.addSeparator();

		JMenuItem mi_file_exit = new JMenuItem("Exit");
		m_file.add(mi_file_exit);
		mi_file_exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});

		// edit - Tab
		JMenu m_edit = new JMenu("Edit");
		mb.add(m_edit);

		// TODO strg+c, strg+v, strg+x

		JMenuItem mi_edit_duplicate = new JMenuItem("Duplicate");
		m_edit.add(mi_edit_duplicate);
		mi_edit_duplicate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		mi_edit_duplicate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO duplicate current object
			}
		});

		JMenuItem mi_edit_remove = new JMenuItem("Remove");
		m_edit.add(mi_edit_remove);
		mi_edit_remove.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
		mi_edit_remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Highlightable obj = drawingPane.getCurrentElement();
				if (obj != null) {
					if (obj instanceof NCNode) {
						comp.removeNode((NCNode) obj);
					} else if (obj instanceof SyncronizerDrawer) {
						comp.removeSyncronizer(((SyncronizerDrawer) obj).getReal());
					} else {
						return;
					}
					drawingPane.resetCurrentElement();
				}
			}
		});

		this.m_recent_nodes = new JMenu("Recent Nodes");

		m_edit.addSeparator();

		JMenuItem mi_edit_preferences = new JMenuItem("Preferences");
		m_edit.add(mi_edit_preferences);
		mi_edit_preferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.ALT_DOWN_MASK));
		mi_edit_preferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO show preferences window
			}
		});

		// Compositor
		JMenu m_compositor = new JMenu("Compositor");
		mb.add(m_compositor);

		JMenuItem mi_compositor_run = new JMenuItem("Run");
		m_compositor.add(mi_compositor_run);
		mi_compositor_run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
		mi_compositor_run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comp.start();
			}
		});

		m_compositor.addSeparator();

		JMenuItem mi_compositor_search = new JMenuItem("Search Node");
		m_compositor.add(mi_compositor_search);
		mi_compositor_search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));
		mi_compositor_search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SearchNodeCreator snc = new SearchNodeCreator(systemNodeCreator, comp);
				snc.setAutoRequestFocus(true);
				snc.setVisible(true);
			}
		});

		JMenuItem mi_compositor_addsyncronizer = new JMenuItem("Add Syncronizer");
		m_compositor.add(mi_compositor_addsyncronizer);
		mi_compositor_addsyncronizer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
		mi_compositor_addsyncronizer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				JPanel p_diag_main = new JPanel(new GridLayout(2, 2));

				JLabel l_diag_ins = new JLabel("Inputs: ");
				p_diag_main.add(l_diag_ins);
				JTextField tf_diag_ins = new JTextField(2);
				p_diag_main.add(tf_diag_ins);
				JLabel l_diag_outs = new JLabel("Outputs: ");
				p_diag_main.add(l_diag_outs);
				JTextField tf_diag_outs = new JTextField(2);
				p_diag_main.add(tf_diag_outs);

				int ret = JOptionPane.showConfirmDialog(self, p_diag_main, "New Snycronizer",
						JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.OK_OPTION) {
					try {
						int ins = Integer.parseInt(tf_diag_ins.getText());
						int outs = Integer.parseInt(tf_diag_outs.getText());

						comp.addSyncronizer(new NCSyncronizer(ins, outs));
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(self,
								"Failed to create Syncronizer.\n\nNumberFormatException: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

		// View
		JMenu m_view = new JMenu("View");
		mb.add(m_view);
		m_view.setMnemonic('V');

		this.mi_view_showData = new JCheckBoxMenuItem("Show Datalanes", true);
		m_view.add(this.mi_view_showData);
		this.mi_view_showData.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		this.mi_view_showData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Workspace.setShowData(mi_view_showData.getState());
				drawingPane.repaint();
			}
		});

		this.mi_view_showSignals = new JCheckBoxMenuItem("Show Signallanes", true);
		m_view.add(this.mi_view_showSignals);
		this.mi_view_showSignals.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		this.mi_view_showSignals.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Workspace.setShowSignals(mi_view_showSignals.getState());
				drawingPane.repaint();
			}
		});

		return mb;
	}

	private JToolBar createSignalsToolBar() {
		JToolBar tb = new JToolBar("Signals");
		tb.setPreferredSize(new Dimension(100, 35));

		JButton b_syncronizer = new JButton();
		b_syncronizer.setIcon(new ImageIcon("img/Syncronizer.png", "Syncronizer"));
		tb.add(b_syncronizer);

		return tb;
	}

	private String generateTitle() {
		String filename = currentFile != null ? currentFile.getAbsolutePath() : "";
		if (filename != "" && changed) {
			filename += "*";
		}
		return "NodeCode" + (filename != "" ? " - " + filename : "");
	}

	/**
	 * Closes the window and frees any allocated resources.
	 * 
	 * @return if the user deciedes to abort this process <code>false</code> is
	 *         returned, otherwise <code>true</code> is returned.
	 */
	private boolean quit() {

		if (this.changed) {
			// TODO do you want to save dialog
			return false;
		}

		this.comp.destroy();
		// TODO free resources

		System.exit(0);
		return true;
	}

	private void saveas() {

		SaveDialog diag = new SaveDialog();

		int ret = JOptionPane.showConfirmDialog(self, diag, "Save As...", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {

			// TODO change default author
			// TODO dependencies auto detection
			NodeDescription desc = new NodeDescription(new Author(0, "coil"), new Dependency[0], diag.getDescription());

			this.comp.setName(diag.getNodeName());
			this.comp.setDescription(desc);

			this.currentFile = new File(diag.getPath());

			save();
		}

	}

	private void save() {
		if (this.currentFile == null) {
			saveas();
			return;
		}

		// TODO update dependencies

		NCCompositorCreator creator = comp.getCreator();
		CompositorLoader loader = new CompositorLoader();

		try {
			loader.saveNodeCreator(creator, this.currentFile);
		} catch (LoaderException e1) {
			JOptionPane.showMessageDialog(self, "Failed to save node!\n\n" + e1.getMessage(), "Saving Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateRecentList() {
		this.m_recent_nodes.removeAll();

		if (this.recentNodes.isEmpty()) {
			JMenuItem tmp = new JMenuItem("no node found");
			tmp.setEnabled(false);
			this.m_recent_nodes.add(tmp);
		} else {
			for (NodeCreator nn : this.recentNodes) {
				JMenuItem tmp = new JMenuItem(nn.getPath());
				this.m_recent_nodes.add(tmp);
				tmp.addActionListener(new RecentlyUsedNodeMenuListener(nn));
			}
		}

	}

	private void tryToMakeEdge(NodeInterface start, NodeInterface end) {

		if (start.equals(end))
			return;

		Class<?> startType = start.getType();
		Class<?> endType = end.getType();

		if (startType == Signal.class && startType.equals(endType)) {
			// Signals
			if (start instanceof SignalOutputInterface && end instanceof SignalInputInterface) {
				((SignalOutputInterface) start).setConnection((SignalInputInterface) end);
				System.out.println("Established connection! (Sig 1)");
			} else if (end instanceof SignalOutputInterface && start instanceof SignalInputInterface) {
				((SignalOutputInterface) end).setConnection((SignalInputInterface) start);
				System.out.println("Established connection! (Sig 2)");
			} else {
				// other combination, not possible to make a signal connection
			}
		} else if (!startType.equals(Signal.class) && !endType.equals(Signal.class)) {
			// Data
			if ((start instanceof NodeOutputInterface) && (end instanceof NodeInputInterface)) {
				// everything is ok
			} else if ((end instanceof NodeOutputInterface) && (start instanceof NodeInputInterface)) {
				// just switch
				NodeInterface tmp = start;
				start = end;
				end = tmp;

				Class<?> tmpClazz = startType;
				startType = endType;
				endType = tmpClazz;
			} else {
				return; // no connection possible
			}

			if (endType.isAssignableFrom(startType)) {
				// possible
				((NodeInputInterface) end).setConnection((NodeOutputInterface) start);
			}
		} else {
			// "signal with data"-Mix
		}

	}

	private class RecentlyUsedNodeMenuListener implements ActionListener {
		private final NodeCreator creator;

		public RecentlyUsedNodeMenuListener(NodeCreator nodeName) {
			this.creator = nodeName;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			try {
				comp.addNode(this.creator.create());
			} catch (InstantiationException e) {
				JOptionPane.showMessageDialog(self, "Could not instantiate node.\n\n" + e.getMessage(), "Node Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class CurrentCompositorDrawer extends JComponent {
		private CompositorDrawer drawer;
		private ObjectDrawer currentElement;
		private NodeInterface dragStartInterface = null;
		private CompositorMouseListener mouseHandler;

		public CurrentCompositorDrawer(CompositorDrawer drawer) {
			this.drawer = drawer;

			this.currentElement = null;
			this.mouseHandler = new CompositorMouseListener();
			this.addMouseListener(this.mouseHandler);
			this.addMouseMotionListener(this.mouseHandler);
		}

		public void setDrawer(CompositorDrawer drawer) {
			this.drawer = drawer;
			this.currentElement = null;
		}

		public ObjectDrawer getCurrentElement() {
			return this.currentElement;
		}

		public void resetCurrentElement() {
			if (this.currentElement != null) {
				this.currentElement.setHighlighted(false);
				this.currentElement = null;

				controlPane.showNode(null);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			this.drawer.setContentDimension(this.getWidth(), this.getHeight());

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			this.drawer.paintContent(g2d);

			if (dragStartInterface != null) {
				Point p = this.drawer.getInCompositorInterfacePosition(dragStartInterface);
				Point m = this.mouseHandler.getMouseLocation();

				EdgePainter.paint(g2d, p, m);
			}
		}

		private class CompositorMouseListener extends MouseAdapter {
			private Point lastMousePosition;
			private boolean nodeDrag = false;
			private Highlightable lastHighlighted = null;

			private int nodeDragDx, nodeDragDy;

			public CompositorMouseListener() {
				this.lastMousePosition = new Point(0, 0);
			}

			public Point getMouseLocation() {
				return this.lastMousePosition;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO interface connections
				if (this.nodeDrag) {
					if (currentElement instanceof Positionable) {
						currentElement.setPosition(e.getX() - this.nodeDragDx, e.getY() - this.nodeDragDy);
						repaint();
					}
				}

				this.mouseMoved(e);

				if (dragStartInterface != null) {
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				this.lastMousePosition = new Point(e.getX(), e.getY());

				Highlightable nextHightlighted = drawer.getHighlightable(this.lastMousePosition.x,
						this.lastMousePosition.y);

				if (lastHighlighted != nextHightlighted) {
					System.out.println("Changed Highlight to: " + nextHightlighted);
					if (lastHighlighted != null && lastHighlighted != currentElement) {
						lastHighlighted.setHighlighted(false);
					}
					if (nextHightlighted != null) {
						nextHightlighted.setHighlighted(true);
					}
					lastHighlighted = nextHightlighted;
					repaint();
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// hit a node header?
				int mx = e.getX();
				int my = e.getY();

				System.out.println("Mouse: " + e.getX() + "/" + e.getY()); // KILLME
				for (NCNode n : comp.getNCNodes()) {
					NodeDrawer p = n.getDrawerInstance();

					if (p.isMouseOverMoveArea(mx, my)) {
						parseClick(p, mx, my);
						return;
					}
				}

				for (NCSyncronizer s : comp.getNCSyncronizers()) {
					SyncronizerDrawer p = s.getDrawerInstance();

					if (p.isMouseOverMoveArea(mx, my)) {
						parseClick(p, mx, my);
						return;
					}
				}

				// otherwise: remove current element
				resetCurrentElement();

				if (lastHighlighted != null) {
					if (lastHighlighted instanceof NCHighlightInfo<?>) {
						@SuppressWarnings("rawtypes")
						Object hightlighted = ((NCHighlightInfo) lastHighlighted).getReal();
						if (hightlighted instanceof NodeInterface) {
							dragStartInterface = (NodeInterface) hightlighted;
						}
					}
				}
			}

			private void parseClick(ObjectDrawer o, int mx, int my) {
				this.nodeDrag = true;
				Point pos = o.getPosition();
				this.nodeDragDx = mx - pos.x;
				this.nodeDragDy = my - pos.y;

				if (currentElement != null)
					currentElement.setHighlighted(false);

				currentElement = o;

				if (currentElement instanceof NodeDrawer)
					controlPane.showNode(((NodeDrawer) currentElement).getReal());
				// DOES NOT NEED TO BE SET TO HIGHLIGHTED BECAUSE OF
				// MOUSEOVER-HL
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				this.nodeDrag = false;

				if (dragStartInterface != null && lastHighlighted != null) {
					if (lastHighlighted instanceof NCHighlightInfo<?>) {
						@SuppressWarnings("rawtypes")
						Object hightlighted = ((NCHighlightInfo) lastHighlighted).getReal();
						if (hightlighted instanceof NodeInterface) {
							NodeInterface end = (NodeInterface) hightlighted;

							tryToMakeEdge(dragStartInterface, end);
							repaint();
						}
					}
				}
				dragStartInterface = null;
			}
		}
	}

}
