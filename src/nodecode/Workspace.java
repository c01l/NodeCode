package nodecode;

import java.io.InputStream;
import java.io.PrintStream;

import nodecode.creator.KnownNodesCreator;

public class Workspace {
	// /// Color Info
	private static InterfaceColorSet colorSet = null;

	public static InterfaceColorSet getInterfaceColorSet() {
		if (colorSet == null) {
			colorSet = InterfaceColorSet.getDefault();
		}
		return colorSet;
	}

	/**
	 * Sets a new {@link InterfaceColorSet} for the workspace
	 * 
	 * @param newColorSet
	 * @return the old {@link InterfaceColorSet}
	 */
	public static InterfaceColorSet setInterfaceColorSet(InterfaceColorSet newColorSet) {
		InterfaceColorSet ret = colorSet;
		colorSet = newColorSet;
		return ret;
	}

	// /// Signals Info
	private static boolean showSignals = true;
	private static boolean showData = true;

	public static boolean areSignalsShown() {
		return showSignals;
	}

	public static boolean isDataShown() {
		return showData;
	}

	public static void setShowSignals(boolean showSignals) {
		Workspace.showSignals = showSignals;
	}

	public static void setShowData(boolean showData) {
		Workspace.showData = showData;
	}

	///// Creator Info
	private static KnownNodesCreator systemNodeCreator = null;

	public static KnownNodesCreator getSystemNodeCreator() {
		if (systemNodeCreator == null) {
			systemNodeCreator = new KnownNodesCreator();
		}
		return systemNodeCreator;
	}

	///// Console
	private static PrintStream console = System.out;
	private static PrintStream consoleErr = System.err;
	private static InputStream consoleIn = System.in;

	public static PrintStream getConsole() {
		return console;
	}

	public static PrintStream getConsoleErr() {
		return consoleErr;
	}

	public static InputStream getConsoleIn() {
		return consoleIn;
	}

}
