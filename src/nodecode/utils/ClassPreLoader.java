package nodecode.utils;

import java.io.File;

public class ClassPreLoader {

	public static boolean logging = false;

	public static void preload(File path) {
		preload(path, "");
	}

	public static void preload(File path, String pack) {
		for (File f : path.listFiles()) {
			if (logging) {
				System.out.println("Preloading: " + f.getAbsolutePath());
			}

			if (f.isFile() && f.getName().endsWith(".class")) {
				try {
					String clazzPath = pack + "." + f.getName().subSequence(0, f.getName().lastIndexOf('.'));
					// System.out.println("Trying: " + clazzPath);

					Class.forName(clazzPath);

					if (logging) {
						System.out.println("Successfully loaded");
					}

				} catch (ClassNotFoundException e) {
					if (logging) {
						System.err.println("Couldn't find class: " + e.getMessage());
					}
				}
			} else if (f.isDirectory()) {
				if (logging) {
					System.out.println("Opening Directory");
				}
				preload(f, (pack.isEmpty() ? "" : (pack + ".")) + f.getName());
			}
		}

	}

}
