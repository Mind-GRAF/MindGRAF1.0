package edu.guc.mind_graf.view;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;


import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;

public class NetworkUI extends JFrame {
	private JPanel networkPanel;
	private HashMap<String, String> createdNodes;
	BufferedImage background = null;
	BufferedImage nodeImg = null;

	public NetworkUI() throws IOException {
		background = ImageIO.read(new File("src/main/resources/images/background.jpg"));
		nodeImg = ImageIO.read(new File("src/main/resources/images/node.png"));
		createdNodes = new HashMap<String, String>();
		// Set up the UI
		setTitle("Network UI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(1800, 900));
		setLayout(new BorderLayout());

		// Create and add UI components
		networkPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// g.drawImage(background, 0, 0, this.getWidth(),
				// this.getHeight(),this);
			}
		};
		networkPanel.setLayout(null); // Use absolute layout for custom node
										// positioning
		networkPanel.setOpaque(false);

		// Display nodes and cables recursively
		int initialX = 250; // Initial X position for the root node
		int initialY = 50; // Initial Y position for the root node

		int levelHeight = 100; // Vertical spacing between levels of nodes

		// ArrayList<Integer> MolIds = new ArrayList<Integer>();
		// for (Node node : Network.getNodes().values()) {
		// if (node.isMolecular()) {
		// MolIds.add(node.getId());
		// }
		// }
		//
		// // Sort the MolIds ArrayList in ascending order
		// Collections.sort(MolIds);
		HashMap<String, Node> hashedMoleculars = new HashMap<String, Node>();

		for (HashMap<String, Node> Set : Network.getMolecularNodes().values()) {
			for (Node node : Set.values()) {
				hashedMoleculars.put(node.getName(), node);
			}
		}

		for (int i = 0; i < hashedMoleculars.values().size(); i++) {
			String key = "";
			if (hashedMoleculars.containsKey("P" + i))
				key = "P" + i;
			else
				key = "M" + i;
			if (hashedMoleculars.containsKey(key)) {

				if (hashedMoleculars.get(key).getUpCableSet().isEmpty()) {
					int x = createNodeUI(hashedMoleculars.get(key), networkPanel, initialX,
							initialY, 1, levelHeight, 0);
					initialX += x + 150;
				}
			}
		}

		HashMap<String, Node> hashednodes = new HashMap<String, Node>();

		for (Node node : Network.getNodes().values()) {
			if (node.isBase() || node.isVariable())
				hashednodes.put(node.getName(), node);
		}

		for (Node node : hashednodes.values()) {

			if (node.getUpCableSet().isEmpty()) {
				int x = createNodeUI(node, networkPanel, initialX,
						initialY, 1, levelHeight, 0);
				initialX += x + 50;

			}
		}

		add(networkPanel, BorderLayout.CENTER);

		// Pack and display the UI
		pack();
		setVisible(true);
	}

	private int createNodeUI(Node node, JPanel parentPanel, int x, int y,
			int currentLevel, int levelHeight, int i) {
		if (createdNodes.containsValue(node.getName())) {
			createdNodes.put(x + "," + y, node.getName() + "temp" + i);
			i++;
		}

		createdNodes.put(x + "," + y, node.getName());

		// Create node panel (circle representation)
		JPanel nodePanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(new Color(100, 100, 100));
				g.fillOval(0, 0, getWidth(), getHeight());
				// g.drawImage(nodeImg, 0, 0, 50, 50,this);

			}
		};
		nodePanel.setPreferredSize(new Dimension(50, 50));
		nodePanel.setOpaque(false);
		nodePanel.setBounds(x, y, 55, 55);
		JLabel nameLabel = new JLabel(node.getName());
		nameLabel.setBounds(x + 50, y + 50, 100, 100);

		nameLabel.setForeground(Color.WHITE);
		nodePanel.add(nameLabel);
		parentPanel.add(nodePanel);
		// Create cable panels and child nodes recursively
		if (node.isMolecular()) {
			DownCableSet downCableSet = node.getDownCableSet();
			if (downCableSet != null) {

				int childCount = downCableSet.size();
				int childSpacing = 100; // Horizontal spacing between child
										// nodes

				// Calculate the total width of child nodes
				int totalWidth = childCount * childSpacing;

				// Calculate the starting X position for child nodes
				int startX = x - totalWidth / 2;
				// Calculate the Y position for child nodes
				int startY = y + levelHeight;

				int childY = startY;

				// Create cable panels and child nodes recursively
				int currentX = startX;
				int currentY = startY;

				for (DownCable downCable : downCableSet.getValues()) {
					for (Node connectedNode : downCable.getNodeSet()
							.getValues()) {

						int parentX = x;
						int parentY = y;

						int childX = currentX + childSpacing / 2 - 25;
						if (createdNodes.containsKey(childX + "," + childY)) {
							childX += 100;
							currentX += 100;
						}
						int currX = currentX;
						int currY = currentY;

						JPanel cablePanel = new JPanel() {
							@Override
							protected void paintComponent(Graphics g) {
								super.paintComponent(g);

								g.setColor(Color.BLACK);
								g.drawLine(parentX + 25, parentY + 25,
										currX + 50, currY);

							}
						};
						cablePanel.setPreferredSize(new Dimension(childSpacing,
								1));
						cablePanel.setOpaque(false);
						// cablePanel.setBackground(Color.black);
						cablePanel.setBounds(0, 0, 1000, 1000);
						JLabel relationLabel = new JLabel(downCable
								.getRelation().getName());
						relationLabel.setBounds(currX + 50, y + 20, 100, 100);
						relationLabel.setForeground(Color.RED);
						relationLabel.setOpaque(false);
						parentPanel.add(relationLabel);

						parentPanel.add(cablePanel);

						// Calculate the child node's X position and center it
						// horizontally
						createNodeUI(connectedNode, parentPanel, childX,
								childY, currentLevel + 1, levelHeight, i);

						currentX += childSpacing;
					}
				}
				return childCount * childSpacing;
			}
		}
		return 100;
	}

	public static void print(LinkedList<Object[]> l) {
		int i = 0;
		String result = "[";
		for (Object[] objects : l) {
			result += (objects[0] + (i == l.size() - 1 ? "]" : ", "));
		}
		System.out.println(result);
	}

	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeLater(() -> {
			try {
				Network network = new Network();
			Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node actionN = Network.createNode("pickup", "individualnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 2);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE, 2);
            Relation qualifiers = Network.createRelation("qualifiers", "", Adjustability.NONE, 2);

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(var1));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));

            
            DownCable d4 = new DownCable(action, new NodeSet(actionN));
            
            Node actNode = Network.createNode("actnode", new DownCableSet(d4, d3));

            DownCable d5 = new DownCable(qualifiers, new NodeSet(M0,M1));
            DownCable d7 = new DownCable(obj, new NodeSet(actNode));

            Node M2 = Network.createNode("propositionnode", new DownCableSet(d5,d7));

				System.out.println(Network.getNodes());
				// ======================================================

				// 1. create the base nodes
				// // 1. create the base nodes
				// Node nemo = Network.createNode("Nemo", "propositionNode");
				// Node clown = Network.createNode("Fish", "propositionNode");
				// Node X = Network.createVariableNode("X", "propositionNode");
				// Node aquatic = Network.createNode("aquatic", "propositionNode");
				//
				// // 2. create the needed relations
				// Relation object = Network.createRelation("object", "",
				// Adjustability.EXPAND, 2);
				// Relation member = Network.createRelation("member", "",
				// Adjustability.EXPAND, 2);
				// Relation Class = Network.createRelation("Class", "",
				// Adjustability.EXPAND, 2);
				// Relation property = Network.createRelation("property", "",
				// Adjustability.EXPAND, 2);
				// Relation antecedent = Network.createRelation("ant", "",
				// Adjustability.EXPAND, 2);
				// Relation cons = Network.createRelation("cq", "",
				// Adjustability.EXPAND, 2);
				// Network.quantifiers.put("forall", "forall");
				//
				// Relation forAll = Network.createRelation("forall", "",
				// Adjustability.EXPAND, 2);
				// // 3. create downcables for each molecularNode
				//
				// DownCable d1 = new DownCable(object, new NodeSet(X));
				// DownCable d2 = new DownCable(member, new NodeSet(nemo));
				// DownCable d5 = new DownCable(member, new NodeSet(X));
				// DownCable d3 = new DownCable(Class, new NodeSet(clown));
				// DownCable d4 = new DownCable(property, new NodeSet(aquatic));
				//
				// // 4.create molecular nodes
				// Node M1 = Network.createNode("propositionnode",
				// new DownCableSet(d2, d3));
				//
				// Node M2 = Network.createNode("propositionnode",
				// new DownCableSet(d3, d5));
				// Node M4 = Network.createNode("propositionnode",
				// new DownCableSet(d1, d4));
				// DownCable d6 = new DownCable(antecedent, new NodeSet(M1));
				// DownCable d7 = new DownCable(cons, new NodeSet(M4));
				// DownCable d8 = new DownCable(forAll, new NodeSet(X));
				// Node M3 = Network.createNode("propositionnode",
				// new DownCableSet(d6, d8, d7));
				//
				//

				NetworkUI m = new NetworkUI();

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
