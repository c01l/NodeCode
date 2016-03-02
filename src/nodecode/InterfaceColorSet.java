package nodecode;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import nodes.signals.Signal;

/**
 * A {@link InterfaceColorSet} stores a {@link Color} for each data-type an
 * interface can have.
 * 
 * @author Roland Wallner
 *
 */
public class InterfaceColorSet {
	public static boolean logWhileLoading = false;

	private HashMap<Class<?>, Color> colorMap;
	private Color defaultColor;
	private Color highlightColor;
	
	public InterfaceColorSet(Color defaultColor, Color highlightColor) {
		this.colorMap = new HashMap<>();

		this.defaultColor = defaultColor;
		this.highlightColor = highlightColor;
	}

	/**
	 * Adds or replaces the {@link Color} for a data-type
	 * 
	 * @param type
	 * @param color
	 */
	public void addColorCode(Class<?> type, Color color) {
		this.colorMap.put(type, color);
	}

	/**
	 * Returns the {@link Color} stored for a data-type, if no color is found
	 * the default color is returned.
	 * 
	 * @param type
	 * @return
	 */
	public Color getColor(Class<?> type) {
		Color ret = this.colorMap.get(type);
		if (ret == null)
			ret = this.defaultColor;
		return ret;
	}

	/**
	 * Returns the default {@link InterfaceColorSet}
	 * 
	 * @return
	 */
	public static InterfaceColorSet getDefault() {
		InterfaceColorSet s = new InterfaceColorSet(Color.BLUE, Color.PINK); // TODO more?
		s.addColorCode(Signal.class, Color.BLACK);
		return s;
	}

	public static InterfaceColorSet loadInterfaceColorSet(File f) throws IOException {
		FileReader fReader = null;
		BufferedReader bReader = null;

		try {
			fReader = new FileReader(f);
			bReader = new BufferedReader(fReader);

			HashMap<Class<?>, Color> map = new HashMap<>();
			Color defaultColor = null, highlightColor = null;

			String line;
			while ((line = bReader.readLine()) != null) {
				line = line.trim();

				if (logWhileLoading) {
					System.out.println("Line loaded: " + line);
				}

				if (line.startsWith("#") || line.isEmpty()) {
					// comment
				} else if (line.startsWith("DEFAULT")) {
					String[] l = line.split("=");
					String[] rgb = l[1].split(",");

					if (rgb.length != 3 || defaultColor != null) {
						throw new IOException("Line pattern mismatch");
					} else {
						defaultColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]),
								Integer.parseInt(rgb[2]));
						if (logWhileLoading) {
							System.out.println("Set default color to " + defaultColor.toString());
						}
					}
				} else if (line.startsWith("HIGHLIGHT")) {
					String[] l = line.split("=");
					String[] rgb = l[1].split(",");

					if (rgb.length != 3 || highlightColor != null) {
						throw new IOException("Line pattern mismatch");
					} else {
						highlightColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]),
								Integer.parseInt(rgb[2]));
						if (logWhileLoading) {
							System.out.println("Set highlight color to " + highlightColor.toString());
						}
					}
				} else {
					int pos = line.indexOf("=");
					if (pos == -1) {
						throw new IOException("Line pattern mismatch");
					} else {
						String clazzName = line.substring(0, pos).trim();
						String rgbCode = line.substring(pos + 1).trim();
						String[] rgb = rgbCode.split(",");
						int r = Integer.parseInt(rgb[0]);
						int g = Integer.parseInt(rgb[1]);
						int b = Integer.parseInt(rgb[2]);

						System.out.println("Search for: " + clazzName);

						try {
							Class<?> c;
							if (clazzName.equals("SIGNALS")) {
								c = Signal.class;
							} else {
								c = Class.forName(clazzName, false, null);
							}
							map.put(c, new Color(r, g, b));

							if (logWhileLoading)
								System.out.println("Set " + r + "," + g + "," + b + " for " + c.getCanonicalName());
						} catch (ClassNotFoundException e) {
							// class not found -> do nothing
							if (logWhileLoading)
								System.out.println("Class not found");

							System.out.println(e);
						}
					}
				}
			}

			// store
			InterfaceColorSet retSet = new InterfaceColorSet(defaultColor, highlightColor);
			for (Entry<Class<?>, Color> e : map.entrySet()) {
				retSet.addColorCode(e.getKey(), e.getValue());
			}

			return retSet;

		} catch (IOException e) {
			throw e;
		} finally {
			if (bReader != null) {
				fReader.close();
			}
			if (fReader != null) {
				bReader.close();
			}
		}

	}

	public Color getInterfaceHighlightColor() {
		return this.highlightColor;
	}
}
