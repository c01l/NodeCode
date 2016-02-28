package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import gui.controlpane.ControlPane;
import nodecode.Workspace;
import nodecode.compositor.CompositorDrawer;
import nodecode.compositor.NCCompositor;
import nodecode.creator.HirachicalNodeCreatorFinder;
import nodecode.creator.NodeCreator;
import nodecode.drawer.NodeDrawer;
import nodecode.drawer.ObjectDrawer;
import nodecode.drawer.Positionable;
import nodecode.node.Highlightable;
import nodecode.node.NCNode;
import nodecode.signals.NCSyncronizer;
import nodecode.signals.SyncronizerDrawer;
import nodes.Node;
import nodes.signals.Signal;
import nodes.signals.SignalInputInterface;
import nodes.signals.SignalOutputInterface;
import test.NumberAdderNode;
import test.NumberInputNode;
import test.NumberOutputNode;

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
	// menu
	private JCheckBoxMenuItem mi_view_showData, mi_view_showSignals;

	public MainFrame() {
		super("NodeCode");
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

		NumberInputNode in1 = new NumberInputNode();
		NumberInputNode in2 = new NumberInputNode();

		NumberAdderNode adder = new NumberAdderNode();

		NumberOutputNode out = new NumberOutputNode();

		this.comp.addNode(adder);
		this.comp.addNode(in1);
		this.comp.addNode(in2);
		this.comp.addNode(out);

		System.out.println("Added Nodes");

		this.comp.addEdge(in1, "Output", adder, "Input 1");
		this.comp.addEdge(in2, "Output", adder, "Input 2");

		System.out.println("1");

		this.comp.addSyncronizer(Signal.sync(new Node[] { in1, in2 }, new Node[] { adder }));

		System.out.println("2");

		this.comp.addSyncronizer(Signal.sync(new SignalOutputInterface[] { this.comp.getNCSignalStart().getReal() },
				new SignalInputInterface[] { in1.getSignalInput(), in2.getSignalInput() }));

		System.out.println("Do signals");

		Signal.route(adder, out);
		Signal.route(out.getSignalOutput(), this.comp.getNCSignalEnd().getReal());

		this.comp.addEdge(adder, "Output", out, "Input");

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
				// TODO save project
			}
		});

		JMenuItem mi_file_save_as = new JMenuItem("Save As");
		m_file.add(mi_file_save_as);
		mi_file_save_as.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		mi_file_save_as.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO save project
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

	private class RecentlyUsedNodeMenuListener implements ActionListener {
		private final NodeCreator creator;

		public RecentlyUsedNodeMenuListener(NodeCreator nodeName) {
			this.creator = nodeName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			comp.addNode(this.creator.create());
		}
	}

	private class CurrentCompositorDrawer extends JComponent {
		private CompositorDrawer drawer;
		private ObjectDrawer currentElement;

		public CurrentCompositorDrawer(CompositorDrawer drawer) {
			this.drawer = drawer;

			this.currentElement = null;
			MouseAdapter mAdapter = new CompositorMouseListener();
			this.addMouseListener(mAdapter);
			this.addMouseMotionListener(mAdapter);
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
		}

		private class CompositorMouseListener extends MouseAdapter {
			private boolean nodeDrag = false;
			private Highlightable lastHighlighted = null;

			private int nodeDragDx, nodeDragDy;

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO interface connections
				if (this.nodeDrag) {
					if (currentElement instanceof Positionable) {
						currentElement.setPosition(e.getX() - this.nodeDragDx, e.getY() - this.nodeDragDy);
					}
				}
				repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				Highlightable nextHightlighted = drawer.getHighlightable(e.getX(), e.getY());

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
			}
		}
	}

}
