package nodecode.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class NodePackageLoader {

	public static PrintStream logger = null;

	public NodePackageLoader() {
	}

	public void preload(File path) throws IOException {
		JarFile jarFile = new JarFile(path);
		Enumeration<JarEntry> e = jarFile.entries();

		URL[] urls = { new URL("jar:file:" + path + "!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);

		while (e.hasMoreElements()) {
			JarEntry je = (JarEntry) e.nextElement();
			if (je.isDirectory() || !je.getName().endsWith(".class")) {
				continue;
			}
			// -6 because of .class
			String className = je.getName().substring(0, je.getName().length() - 6);
			className = className.replace('/', '.');
			try {
				Class.forName(className, true, cl);
				if (logger != null)
					logger.println(className + " loaded.");
			} catch (ClassNotFoundException e1) {
				if (logger != null)
					logger.println(e1.getMessage());
			}
		}

		jarFile.close();
	}

}
