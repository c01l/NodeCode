package gui;

import java.io.File;
import java.io.IOException;

import nodecode.InterfaceColorSet;
import nodecode.Workspace;

public class MainStart {

	public static void main(String[] args) {
		// testing
		
		// loading
		
		// TODO preloading default classes
		//ClassPreLoader.preload(new File("/bin"));
		
		try {
			System.out.println(System.getProperty("user.dir"));
			
			// InterfaceColorSet.logWhileLoading = true;
			
			Workspace.setInterfaceColorSet(
					InterfaceColorSet.loadInterfaceColorSet(new File("config/defaultColorSet.ini")));
		} catch (IOException e) {
			System.err.println("Failed to load default InterfaceColorSet: " + e.getMessage());
		}
		
		// start gui
		MainFrame mf = new MainFrame();
		mf.setVisible(true);
	}

}
