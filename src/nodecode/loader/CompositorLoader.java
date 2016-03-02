package nodecode.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nodecode.NodeDescription;
import nodecode.NodeDescription.Author;
import nodecode.NodeDescription.Dependency;
import nodecode.compositor.NCCompositorCreator;
import nodecode.compositor.NCCompositorCreator.CompositorEdge;
import nodecode.compositor.NCCompositorCreator.CompositorIO;
import nodecode.compositor.NCCompositorCreator.CompositorNode;
import nodecode.compositor.NCCompositorCreator.CompositorSyncronizer;
import nodecode.creator.NodeCreator;

public class CompositorLoader {

	private static PrintStream logger = null;

	public void saveNodeCreator(NCCompositorCreator creator, File f) throws LoaderException {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("node");

			// Name
			Node nameNode = doc.createElement("name");
			nameNode.setNodeValue(creator.getName());
			root.appendChild(nameNode);

			// Path
			Node pathNode = doc.createElement("path");
			pathNode.setNodeValue(creator.getPath());
			root.appendChild(pathNode);

			NodeDescription desc = creator.getDescription();

			// Description
			Node descNode = doc.createElement("description");
			descNode.setNodeValue(desc.getDescription());
			root.appendChild(descNode);

			// Author
			Author author = desc.getAuthor();
			Element authorNode = doc.createElement("author");
			authorNode.setNodeValue(author.getName());
			authorNode.setAttribute("id", Long.toString(author.getID()));
			root.appendChild(authorNode);

			// Dependencies
			Element depNode = doc.createElement("dependencies");
			// TODO add dependecies
			root.appendChild(depNode);

			// Inputs
			for (CompositorIO i : creator.getInputs()) {
				Element inNode = doc.createElement("input");
				inNode.setAttribute("type", i.getType().getCanonicalName());
				inNode.setAttribute("name", i.getName());
				inNode.setAttribute("default", i.getDefaultValue() != null ? i.getDefaultValue().toString() : "null");
				root.appendChild(inNode);
			}

			// Outputs
			for (CompositorIO o : creator.getOutputs()) {
				Element outNode = doc.createElement("output");
				outNode.setAttribute("type", o.getType().getCanonicalName());
				outNode.setAttribute("name", o.getName());
				outNode.setAttribute("default", o.getDefaultValue() != null ? o.getDefaultValue().toString() : "null");
				root.appendChild(outNode);
			}

			// Code
			Element code = doc.createElement("code");
			createCodeNode(doc, code, creator);
			root.appendChild(code);

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			transformer.transform(source, result);

		} catch (DOMException e) {
			throw new LoaderException(e.getMessage());
		} catch (TransformerException e) {
			throw new LoaderException("Transformer: " + e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new LoaderException("Parser: " + e.getMessage());
		}
	}

	private static void createCodeNode(Document doc, Element code, NCCompositorCreator creator) {

		for (CompositorNode n : creator.getNodes()) {
			Element node = doc.createElement("node");
			node.setAttribute("id", Long.toString(n.getID()));
			node.setAttribute("type", n.getCreator().getPath());
			code.appendChild(node);
		}

		for (CompositorSyncronizer s : creator.getSyncronizers()) {
			Element sync = doc.createElement("sync");
			sync.setAttribute("id", "s" + Long.toString(s.getID()));
			sync.setAttribute("in", Integer.toString(s.getInAmount()));
			sync.setAttribute("out", Integer.toString(s.getOutAmount()));
			code.appendChild(sync);
		}

		Element dataflow = doc.createElement("dataflow");
		Element signalflow = doc.createElement("signalflow");

		for (CompositorEdge e : creator.getEdges()) {
			Element edge = doc.createElement("edge");

			edge.setAttribute("start", e.getStart());
			edge.setAttribute("end", e.getEnd());

			if (e.isSignal()) {
				signalflow.appendChild(edge);
			} else {
				dataflow.appendChild(edge);
			}
		}

		code.appendChild(dataflow);
		code.appendChild(signalflow);

	}

	public NodeCreator loadNodeCreator(File f) throws LoaderException {
		String name = null;
		String path = null;
		String description = null;
		Dependency[] dependencies = new Dependency[0];
		Author author = null;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.parse(new FileInputStream(f));

			Element root = doc.getDocumentElement();

			Node codeNode = null;

			LinkedList<Node> inputNodes = new LinkedList<>();
			LinkedList<Node> outputNodes = new LinkedList<>();

			// read params
			NodeList rootList = root.getChildNodes();
			for (int i = 0; i < rootList.getLength(); ++i) {
				Node n = rootList.item(i);

				switch (n.getNodeName().toLowerCase()) {
				case "name":
					name = n.getNodeValue();
					break;
				case "path":
					path = n.getNodeValue();
					break;
				case "description":
					description = n.getNodeValue();
					break;
				case "author":
					String authorName = n.getNodeValue();
					long authorID = -1;
					// check for id
					Node authorIdNode = n.getAttributes().getNamedItem("userid");
					if (authorIdNode != null)
						authorID = Long.parseLong(authorIdNode.getNodeValue());

					author = new Author(authorID, authorName);
					break;
				case "dependencies":
					NodeList depList = n.getChildNodes();
					dependencies = new Dependency[depList.getLength()];
					for (int j = 0; j < depList.getLength(); ++j) {
						Node depEntry = depList.item(j);

						String depPack = null;
						String depPath = null;

						// get package
						Node packageNode = depEntry.getAttributes().getNamedItem("package");
						if (packageNode != null) {
							depPack = packageNode.getNodeValue();
						}

						depPath = depEntry.getNodeValue();

						dependencies[j] = new Dependency(depPack, depPath);
					}
					break;
				case "code":
					// check if there is an implementation of this node
					Node impl = n.getAttributes().getNamedItem("implemented");
					if (impl != null) {
						return loadImplementation(impl.getNodeValue());
					}

					if (codeNode != null)
						throw new LoaderException("Multiple Code-Sections");

					codeNode = n;

					break;
				case "input":
					inputNodes.add(n);
					break;
				case "output":
					outputNodes.add(n);
					break;
				default:
					log("Unknown Node: " + n.getNodeName());
				}
			}

			if (name == null)
				throw new LoaderException("No Name");

			if (path == null)
				throw new LoaderException("No Path");

			if (author == null)
				throw new LoaderException("No Author");

			if (description == null)
				throw new LoaderException("No Description");

			if (codeNode == null)
				throw new LoaderException("No Code Section");

			NCCompositorCreator creator = new NCCompositorCreator(path, name,
					new NodeDescription(author, dependencies, description));

			// read comp in and outs
			for (Node n : inputNodes) {
				NamedNodeMap nnm = n.getAttributes();

				Node inputNameNode = nnm.getNamedItem("name");
				Node inputTypeNode = nnm.getNamedItem("type");
				Node inputDefaultNode = nnm.getNamedItem("default");

				if (inputNameNode == null)
					throw new LoaderException("Input hat no name");

				String inputName = inputNameNode.getNodeValue();

				if (inputTypeNode == null)
					throw new LoaderException("Input '" + inputName + "' has no type");

				String inputType = inputTypeNode.getNodeValue();
				try {
					Class<?> clazz = Class.forName(inputType);

					if (inputDefaultNode == null)
						throw new LoaderException("Default value is not given for '" + inputName + "'.");

					String inputDefault = inputDefaultNode.getNodeValue();
					Object defaultValue = null;

					if (clazz.equals(Integer.class)) {
						defaultValue = Integer.parseInt(inputDefault);
					} else if (clazz.equals(Double.class)) {
						defaultValue = Double.parseDouble(inputDefault);
					} else if (clazz.equals(String.class)) {
						defaultValue = inputDefault;
					}

					if (defaultValue == null)
						throw new LoaderException("Unsupported type: " + clazz.getCanonicalName());

					creator.addInput(inputName, clazz, defaultValue);

				} catch (ClassNotFoundException e) {
					throw new LoaderException("Input type '" + inputType + "' cannot be loaded");
				}

			}

			for (Node n : outputNodes) {
				NamedNodeMap nnm = n.getAttributes();

				Node outputNameNode = nnm.getNamedItem("name");
				Node outputTypeNode = nnm.getNamedItem("type");
				Node outputDefaultNode = nnm.getNamedItem("default");

				if (outputNameNode == null)
					throw new LoaderException("Output hat no name");

				String outputName = outputNameNode.getNodeValue();

				if (outputTypeNode == null)
					throw new LoaderException("Output '" + outputName + "' has no type");

				String outputType = outputTypeNode.getNodeValue();
				try {
					Class<?> clazz = Class.forName(outputType);

					if (outputDefaultNode == null)
						throw new LoaderException("Default value is not given for '" + outputName + "'.");

					String outputDefault = outputDefaultNode.getNodeValue();
					Object defaultValue = null;

					if (clazz.equals(Integer.class)) {
						defaultValue = Integer.parseInt(outputDefault);
					} else if (clazz.equals(Double.class)) {
						defaultValue = Double.parseDouble(outputDefault);
					} else if (clazz.equals(String.class)) {
						defaultValue = outputDefault;
					}

					if (defaultValue == null)
						throw new LoaderException("Unsupported type: " + clazz.getCanonicalName());

					creator.addOutput(outputName, clazz, defaultValue);

				} catch (ClassNotFoundException e) {
					throw new LoaderException("Output type '" + outputType + "' cannot be loaded");
				}
			}

			// read comp nodes
			LinkedList<Node> dataflowNodes = new LinkedList<Node>();
			LinkedList<Node> signalflowNodes = new LinkedList<Node>();

			NodeList codeNodeList = codeNode.getChildNodes();
			for (int j = 0; j < codeNodeList.getLength(); ++j) {
				Node cNode = codeNodeList.item(j);
				switch (cNode.getNodeName().toLowerCase()) {
				case "node":
					NamedNodeMap nnnm = cNode.getAttributes();

					Node idNode = nnnm.getNamedItem("id");
					Node typeNode = nnnm.getNamedItem("type");
					Node xNode = nnnm.getNamedItem("x");
					Node yNode = nnnm.getNamedItem("y");

					if (idNode == null)
						throw new LoaderException("Node has no id");

					long ID = Long.parseLong(idNode.getNodeValue());

					if (typeNode == null)
						throw new LoaderException("Node has no type (id = " + ID + ")");

					NodeCreator nc = getNodeCreator(typeNode.getNodeValue());
					if (nc == null)
						throw new LoaderException("Cant find loader for '" + typeNode.getNodeValue() + "'");

					int x = 0;
					int y = 0;

					if (xNode != null)
						x = Integer.parseInt(xNode.getNodeValue());

					if (yNode != null)
						y = Integer.parseInt(yNode.getNodeValue());

					creator.addNode(ID, nc, x, y);
					break;
				case "sync":
					NamedNodeMap snnm = cNode.getAttributes();

					Node sidNode = snnm.getNamedItem("id");
					Node sinNode = snnm.getNamedItem("in");
					Node soutNode = snnm.getNamedItem("out");
					Node sxNode = snnm.getNamedItem("x");
					Node syNode = snnm.getNamedItem("y");

					if (sidNode == null)
						throw new LoaderException("Node has no id");

					long sID = Long.parseLong(sidNode.getNodeValue());

					if (sinNode == null)
						throw new LoaderException("Node has no input count (id = " + sID + ")");

					int sin = Integer.parseInt(sinNode.getNodeValue());

					if (soutNode == null)
						throw new LoaderException("Node has no output count (id = " + sID + ")");

					int sout = Integer.parseInt(soutNode.getNodeValue());

					int sx = 0;
					int sy = 0;

					if (sxNode != null)
						sx = Integer.parseInt(sxNode.getNodeValue());

					if (syNode != null)
						sy = Integer.parseInt(syNode.getNodeValue());

					creator.addSyncronizer(sID, sin, sout, sx, sy);

					break;
				case "dataflow":
					dataflowNodes.add(cNode);
					break;
				case "signalflow":
					signalflowNodes.add(cNode);
					break;
				default:
					log("Unknown Code-Node: " + cNode.getNodeName());
					break;
				}
			}

			// read data flow
			for (Node n : dataflowNodes) {
				NamedNodeMap nnm = n.getAttributes();
				Node startNode = nnm.getNamedItem("start");
				Node endNode = nnm.getNamedItem("end");

				if (startNode == null)
					throw new LoaderException("Edge has no start");

				if (endNode == null)
					throw new LoaderException("Edge has no end");

				String start = startNode.getNodeValue();
				String end = endNode.getNodeValue();

				creator.addDataEdge(start, end);
			}

			// read signal flow
			for (Node n : signalflowNodes) {
				NamedNodeMap nnm = n.getAttributes();
				Node startNode = nnm.getNamedItem("start");
				Node endNode = nnm.getNamedItem("end");

				if (startNode == null)
					throw new LoaderException("Edge has no start");

				if (endNode == null)
					throw new LoaderException("Edge has no end");

				String start = startNode.getNodeValue();
				String end = endNode.getNodeValue();

				creator.addSignalEdge(start, end);
			}

			return creator;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return null;
	}

	private NodeCreator loadImplementation(String implPath) {
		// TODO loading from class
		return null;
	}

	private NodeCreator getNodeCreator(String path) {
		// TODO getNodeCreator()
		return null;
	}

	private void log(String msg) {
		if (logger != null)
			logger.println(msg);
	}

}
