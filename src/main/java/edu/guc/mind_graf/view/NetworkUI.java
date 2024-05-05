package edu.guc.mind_graf.view;

import javax.imageio.ImageIO;
import javax.swing.*;

import edu.guc.mind_graf.paths.AndPath;
import edu.guc.mind_graf.paths.BUnitPath;
import edu.guc.mind_graf.paths.ComposePath;
import edu.guc.mind_graf.paths.DomainRestrictPath;
import edu.guc.mind_graf.paths.FUnitPath;
import edu.guc.mind_graf.paths.IrreflexiveRestrictPath;
import edu.guc.mind_graf.paths.KPlusPath;
import edu.guc.mind_graf.paths.KStarPath;
import edu.guc.mind_graf.paths.Path;
import edu.guc.mind_graf.paths.PathTrace;
import edu.guc.mind_graf.paths.RangeRestrictPath;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;




import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.cables.UpCable;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
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
				// Node cs = Network.createNode("cs", "propositionnode");
				// Node fun = Network.createNode("fun", "propositionnode");
				// Node mary = Network.createNode("mary", "propositionnode");
				// Node believe = Network.createNode("believe", "propositionnode");
				// Node bob = Network.createNode("bob", "propositionnode");
				// Node know = Network.createNode("know", "propositionnode");
				//
				// Relation agent = Network.createRelation("agent", "", Adjustability.EXPAND,
				// 2);
				// Relation act = Network.createRelation ("act", "", Adjustability.EXPAND, 2);
				// Relation obj = Network.createRelation ("obj", "", Adjustability.EXPAND, 2);
				// Relation prop = Network.createRelation ("prop", "", Adjustability.EXPAND, 2);
				//
				// DownCable d1 = new DownCable(obj,new NodeSet(cs));
				// DownCable d2 = new DownCable(prop,new NodeSet(fun));
				//
				//
				//
				//
				// Node M0 = Network.createNode("propositionnode", new DownCableSet(d1,d2));
				//
				// DownCable d3 = new DownCable(obj,new NodeSet(M0));
				// DownCable d4 = new DownCable(act,new NodeSet(believe));
				// DownCable d5 = new DownCable(agent,new NodeSet(mary));
				//
				// Node M1 = Network.createNode("propositionnode", new DownCableSet(d3,d4,d5));
				//
				//
				// DownCable d6 = new DownCable(obj,new NodeSet(M1));
				// DownCable d7 = new DownCable(act,new NodeSet(know));
				// DownCable d8 = new DownCable(agent,new NodeSet(bob));
				//
				// Node M2 = Network.createNode("propositionnode", new DownCableSet(d6,d7,d8));
				//
				// FUnitPath p1 = new FUnitPath(agent);
				// FUnitPath p2 = new FUnitPath(act);
				// FUnitPath p3 = new FUnitPath(obj);
				//
				//
				// BUnitPath b1 = new BUnitPath(agent);
				// BUnitPath b2 = new BUnitPath(act);
				// BUnitPath b3 = new BUnitPath(obj);
				// ComposePath pCompose = new ComposePath(p2,b2);
				//
				// DomainRestrictPath dp1 = new DomainRestrictPath(p2, p1, believe);
				// RangeRestrictPath r1 = new RangeRestrictPath(p2, b1, believe);
				// IrreflexiveRestrictPath ir = new IrreflexiveRestrictPath(pCompose);
				// IrreflexiveRestrictPath ir2 = new IrreflexiveRestrictPath(p2);
				//
				//
				// LinkedList<Object[]> result1 = ir.follow(M1,new PathTrace(),new Context());
				// print(result1);
				// LinkedList<Object[]> result2 = pCompose.follow(M1,new PathTrace(),new
				// Context());
				// print(result2);
				// LinkedList<Object[]> result3 = ir2.follow(M1,new PathTrace(),new Context());
				// print(result3);
				//
				//// ComposePath pCompose2 = new ComposePath(pF5,pF4);
				// AndPath and = new AndPath(p3,p3);
				//
				//
				// LinkedList <Object[]> s = p3.follow(M2,new PathTrace(),new Context());
				// Path p4 = new KPlusPath(p3);
				// LinkedList <Object[]> s2 = p4.follow(M2,new PathTrace(),new Context());

				// for (Object[] object : s2) {
				// System.out.println("KStar " + object[0]);
				// }

				// AndPath and2 = new AndPath(pF6,pF5);
				// System.out.println(and.equals(and2));
				// Network.createNewSemanticType("FearNode","nodes.IndividualNode", null);
				// Node X = Network.createVariableNode("X", "FearNode");
				//
				// =====================================================================

				// build some variable nodes
				Node Z = Network.createVariableNode("Z", "propositionnode");
				Node Y = Network.createVariableNode("Y", "propositionnode");
				Node X = Network.createVariableNode("X", "propositionnode");

				// build some base nodes
				Node Base = Network.createNode("base", "propositionnode");

				Relation relation = Network.getRelations().get("forall");
				Relation relation2 = Network.createRelation("b", "propositionNode", Adjustability.EXPAND, 2);

				// create downCableSet and build M0
				DownCable d2 = new DownCable(relation2, new NodeSet(X, Z));
				DownCableSet downCableSet = new DownCableSet(d2);
				Node M0 = Network.createNode("propositionnode",
						downCableSet);

				// create downCableSet and build M1
				DownCable d = new DownCable(relation, new NodeSet(Z));
				DownCable d3 = new DownCable(relation2, new NodeSet(M0));
				DownCableSet downCableSet2 = new DownCableSet(d, d3);
				Node M1 = Network.createNode("propositionnode", downCableSet2);

				// create down cable set and build M2
				DownCable dM2 = new DownCable(relation2, new NodeSet(Y, M1));
				DownCableSet downCableSetM2 = new DownCableSet(dM2);
				Node M2 = Network.createNode("propositionnode",
						downCableSetM2);

				// create down cable set and build M3
				DownCable dM3 = new DownCable(relation2, new NodeSet(X, Y, Z));
				DownCableSet downCableSetM3 = new DownCableSet(dM3);
				Node M3 = Network.createNode("propositionnode",
						downCableSetM3);

				// create down cable set and build M4
				DownCable dM4 = new DownCable(relation, new NodeSet(X));
				DownCable dM4_2 = new DownCable(relation2, new NodeSet(M2, M3));
				DownCableSet downCableSetM4 = new DownCableSet(dM4, dM4_2);
				Node M4 = Network.createNode("propositionNode", downCableSetM4);

				/*
				 * System.out.println("-----------------------------------------------------");
				 * System.out.println("free vars" + M4.getFreeVariables());
				 */

				Substitutions s = new Substitutions();
				s.add(Y, Base);
				// ArrayList<Substitution> substitutionArr = new ArrayList<>();
				// substitutionArr.add(s);

				System.out.println("Hi" + M4.applySubstitution(s));

				// Network.RemoveNode(M4);

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
