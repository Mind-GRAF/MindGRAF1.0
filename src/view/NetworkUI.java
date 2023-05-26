package view;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cables.DownCable;
import cables.DownCableSet;
import cables.UpCable;
import components.Substitutions;
import caseFrames.Adjustability;
import network.Network;
import nodes.Node;
import relations.Relation;
import set.NodeSet;

public class NetworkUI extends JFrame {
	private JPanel networkPanel;
	private HashMap<String, String> createdNodes;
	BufferedImage background = null;
	BufferedImage nodeImg = null;

	public NetworkUI() throws IOException {
		background = ImageIO.read(new File("images\\background.jpg"));
		nodeImg = ImageIO.read(new File("images\\node.png"));
		createdNodes = new HashMap<String, String>();
		// Set up the UI
		setTitle("Network UI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(1200, 800));
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
		int initialX = 200; // Initial X position for the root node
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
		ArrayList<Node> moleculars = new ArrayList<Node>();
		HashMap<String, Node> hashedMoleculars = new HashMap<String, Node>();

		for (HashMap<String, Node> Set : Network.getMolecularNodes().values()) {
			for (Node node : Set.values()) {
				hashedMoleculars.put(node.getName(), node);
			}
		}

		for (int i = 0; i < hashedMoleculars.values().size(); i++) {
			
				if (hashedMoleculars.get("M"+i).getUpCableSet().isEmpty()) {
					int x = createNodeUI(hashedMoleculars.get("M"+i), networkPanel, initialX,
							initialY, 1, levelHeight, 0);
					initialX += x+350;
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

	public static void main(String[] args) throws Exception {

		SwingUtilities.invokeLater(() -> {
			try {
				Network network = new Network();
				// Node cs = Network.createNode("cs", "propositionnode");
				// Node fun = Network.createNode("fun", "propositionnode");
				// Node mary = Network.createNode("mary", "propositionnode");
				// Node believe = Network.createNode("believe",
				// "propositionnode");
				// Node bob = Network.createNode("bob", "propositionnode");
				// Node know = Network.createNode("know", "propositionnode");
				//
				// Relation agent = Network.createRelation("agent", "",
				// Adjustability.EXPAND, 2);
				// Relation act = Network.createRelation("act", "",
				// Adjustability.EXPAND, 2);
				// Relation obj = Network.createRelation("obj", "",
				// Adjustability.EXPAND, 2);
				// Relation prop = Network.createRelation("prop", "",
				// Adjustability.EXPAND, 2);
				//
				// DownCable d1 = new DownCable(obj, new NodeSet(cs));
				// DownCable d2 = new DownCable(prop, new NodeSet(fun));
				//
				// Node M1 = Network.createNode("propositionnode", new
				// DownCableSet(d1, d2));
				//
				// DownCable d3 = new DownCable(obj, new NodeSet(M1));
				// DownCable d4 = new DownCable(act, new NodeSet(believe));
				// DownCable d5 = new DownCable(agent, new NodeSet(mary));
				//
				// Node M2 = Network.createNode("propositionnode", new
				// DownCableSet(d3, d4, d5));
				//
				// DownCable d6 = new DownCable(obj, new NodeSet(M2));
				// DownCable d7 = new DownCable(act, new NodeSet(know));
				// DownCable d8 = new DownCable(agent, new NodeSet(bob));
				//
				// Node M3 = Network.createNode("propositionnode", new
				// DownCableSet(d6, d7, d8));
				// =====================================================================
				 Node Z = Network.createVariableNode("Z", "propositionnode");
				 Node Y = Network.createVariableNode("Y", "propositionnode");
				 Network.createNewSemanticType("FearNode",
				 "nodes.IndividualNode", null);
				 Node X = Network.createVariableNode("X", "FearNode");
				 Node Base = Network.createNode("base", "propositionnode");
				 Network.quantifiers.put("forall","forall");
				
				 Relation relation = Network.createRelation ("forall", "",
				 Adjustability.EXPAND, 2);
				 Relation relation2 = Network.createRelation("b", "",
				 Adjustability.EXPAND, 2);
				
				 NodeSet nodeSetX = new NodeSet(X);
				 NodeSet nodeSetZ = new NodeSet(Z);
				 NodeSet nodeSetXZ = new NodeSet(X,Z);
				 NodeSet nodeSetY = new NodeSet(Y);
				
				 // M0
				 DownCable d2 = new DownCable(relation2, nodeSetXZ);
				 DownCableSet downCableSet = new DownCableSet(d2);
				 Node M0 = Network.createNode("propositionnode",
				 downCableSet);
				
				 //M1
				 NodeSet nodeSetM0 = new NodeSet(M0);
				 DownCable d = new DownCable(relation, nodeSetZ);
				 DownCable d3 = new DownCable(relation2,nodeSetM0);
				 DownCableSet downCableSet2 = new DownCableSet(d,d3);
				 Node M1 = Network.createNode("propositionnode",
				 downCableSet2);
				
				 NodeSet nodeSetM1 = new NodeSet();
				 nodeSetM1.add(M1);
				
				 //M2
				 DownCable dM2 = new DownCable(relation2,
				 nodeSetY.union(nodeSetM1));
				
				 DownCableSet downCableSetM2 = new DownCableSet(dM2);
				 Node M2 = Network.createNode("propositionnode",
				 downCableSetM2);
				
				 //M3
				 DownCable dM3 = new DownCable(relation2, new NodeSet(X,Y,Z));
				
				 DownCableSet downCableSetM3 = new DownCableSet(dM3);
				 Node M3 = Network.createNode("propositionnode",
				 downCableSetM3);
				
				 //M4
				 NodeSet nodeSetM23 = new NodeSet(M2,M3);
				
				
				 DownCable dM4 = new DownCable(relation, nodeSetX);
				 DownCable dM4_2 = new DownCable(relation2,nodeSetM23);
				
				 DownCableSet downCableSetM4 = new DownCableSet(dM4,dM4_2);
				 Node M4 = Network.createNode("propositionnode",
				 downCableSetM4);
				
				 /*
				 System.out.println("-----------------------------------------------------");
				 System.out.println("free vars" + M4.getFreeVariables());
				 */
				
				 Substitutions s = new Substitutions();
				 s.add(Y,Base);
//				  ArrayList<Substitution> substitutionArr = new ArrayList<>();
				 // substitutionArr.add(s);
				 System.out.println(M4);
				
				 System.out.println("Hi" + M4.applySubstitution(s));
				 // network.printNodes();
				 for (UpCable up : X.getUpCableSet().getValues()) {
				 System.out.println(up);
				 }
				// ======================================================

//				// 1. create the base nodes
//				Node nemo = Network.createNode("Nemo", "propositionNode");
//				Node clown = Network.createNode("clownFish", "propositionNode");
//				// 2. create the needed relations
//				Relation object = Network.createRelation("object", "",
//						Adjustability.EXPAND, 2);
//				Relation member = Network.createRelation("member", "",
//						Adjustability.EXPAND, 2);
//				Relation Class = Network.createRelation("Class", "",
//						Adjustability.EXPAND, 2);
//				Relation subClass = Network.createRelation("subClass", "",
//						Adjustability.EXPAND, 2);
//				// 3. create downcables for each molecularNode
//
//				DownCable d1 = new DownCable(object, new NodeSet(nemo));
//				DownCable d2 = new DownCable(member, new NodeSet(nemo));
//				DownCable d3 = new DownCable(Class, new NodeSet(clown));
//				DownCable d4 = new DownCable(subClass, new NodeSet(clown));
//
//				// 4.create molecular nodes
//
//				Node M1 = Network.createNode("propositionnode",
//						new DownCableSet(d1));
//				Node M2 = Network.createNode("propositionnode",
//						new DownCableSet(d2, d3));
//				Node M3 = Network.createNode("propositionnode",
//						new DownCableSet(d4));

				new NetworkUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
