package view;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cables.DownCable;
import cables.DownCableSet;
import components.Substitution;
import caseFrames.Adjustability;
import network.Network;
import nodes.Node;
import relations.Relation;
import set.NodeSet;

public class NetworkUI extends JFrame {
    private JPanel networkPanel;
    private HashMap<String,String> createdNodes; 
    public NetworkUI() {
        createdNodes=new HashMap<String,String>();
        // Set up the UI
        setTitle("Network UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 800));
        setLayout(new BorderLayout());

        // Create and add UI components
        networkPanel = new JPanel();
        networkPanel.setLayout(null); // Use absolute layout for custom node positioning
        networkPanel.setOpaque(false);
        // Display nodes and cables recursively
        int initialX = 100; // Initial X position for the root node
        int initialY = 50; // Initial Y position for the root node

        int levelHeight = 100; // Vertical spacing between levels of nodes

//        ArrayList<Integer> MolIds = new ArrayList<Integer>();
//        for (Node node : Network.getNodes().values()) {
//            if (node.isMolecular()) {
//                MolIds.add(node.getId());
//            }
//        }
//
//        // Sort the MolIds ArrayList in ascending order
//        Collections.sort(MolIds);
         		
        for (Node node : Network.getMolecularNodes().values()) {
			if(node.getUpCableSet().isEmpty())
       createNodeUI(node, networkPanel, initialX, initialY,1, levelHeight,0);
			initialX+=200;
			
        }

        add(networkPanel, BorderLayout.CENTER);

        // Pack and display the UI
        pack();
        setVisible(true);
    }

    private void createNodeUI(Node node, JPanel parentPanel, int x, int y, int currentLevel, int levelHeight,int i) {
    	if(createdNodes.containsValue(node.getName())){
    		createdNodes.put(x+","+y,node.getName()+"temp"+i);
    		i++;
    	}

    	createdNodes.put(x+","+y,node.getName());
    		
        // Create node panel (circle representation)
        JPanel nodePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.GRAY);
                g.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        nodePanel.setPreferredSize(new Dimension(50, 50));
        
        nodePanel.setBounds(x, y, 50, 50);
        JLabel nameLabel = new JLabel(node.getName());
        nodePanel.add(nameLabel);
        parentPanel.add(nodePanel);
        // Create cable panels and child nodes recursively
        if (node.isMolecular()) {
            DownCableSet downCableSet = node.getDownCableSet();
            if (downCableSet != null) {
              
                int childCount = downCableSet.size();
                int childSpacing = 100; // Horizontal spacing between child nodes

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
                    for (Node connectedNode : downCable.getNodeSet().getValues()) {
                    
                     	int parentX = x ; 
                    	int parentY = y ;
                    	
                    	int childX = currentX + childSpacing / 2 - 25;
                    	if(createdNodes.containsKey(childX+","+childY)){
                    		childX  += 100;
                    		currentX+=100;
                    	}
                    	int currX = currentX;
                		int currY = currentY;

                    	JPanel cablePanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            
                            g.setColor(Color.BLACK);
                            g.drawLine(parentX+25, parentY+25, currX+50 ,currY );
                            
                        }
                    };
                    cablePanel.setPreferredSize(new Dimension(childSpacing, 1));
                    cablePanel.setOpaque(false);
//                    cablePanel.setBackground(Color.black);
                    cablePanel.setBounds(0, 0, 1000 , 1000);
                    JLabel relationLabel = new JLabel(downCable.getRelation().getName());
                    relationLabel.setBounds(currX+50, y+20, 100 ,100 );
                    relationLabel.setForeground(Color.RED);
                    relationLabel.setOpaque(false);
                    parentPanel.add(relationLabel);
                    
                    parentPanel.add(cablePanel);

                    // Calculate the child node's X position and center it horizontally
                    createNodeUI(connectedNode, parentPanel, childX, childY, currentLevel+1,levelHeight,i);

                    currentX += childSpacing;
                }
                }
            }
        }
    }

            public static void main(String[] args) {
                SwingUtilities.invokeLater(() -> {
                    Network network = new Network();
//                    Node cs = Network.createNode("cs", "propositionnode");
//                    Node fun = Network.createNode("fun", "propositionnode");
//                    Node mary = Network.createNode("mary", "propositionnode");
//                    Node believe = Network.createNode("believe", "propositionnode");
//                    Node bob = Network.createNode("bob", "propositionnode");
//                    Node know = Network.createNode("know", "propositionnode");
//
//                    Relation agent = new Relation("agent", "", Adjustability.EXPAND, 2);
//                    Relation act = new Relation("act", "", Adjustability.EXPAND, 2);
//                    Relation obj = new Relation("obj", "", Adjustability.EXPAND, 2);
//                    Relation prop = new Relation("prop", "", Adjustability.EXPAND, 2);
//
//                    DownCable d1 = new DownCable(obj, new NodeSet(cs));
//                    DownCable d2 = new DownCable(prop, new NodeSet(fun));
//
//                    Node M1 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
//
//                    DownCable d3 = new DownCable(obj, new NodeSet(M1));
//                    DownCable d4 = new DownCable(act, new NodeSet(believe));
//                    DownCable d5 = new DownCable(agent, new NodeSet(mary));
//
//                    Node M2 = Network.createNode("propositionnode", new DownCableSet(d3, d4, d5));
//
//                    DownCable d6 = new DownCable(obj, new NodeSet(M2));
//                    DownCable d7 = new DownCable(act, new NodeSet(know));
//                    DownCable d8 = new DownCable(agent, new NodeSet(bob));
//
//                    Node M3 = Network.createNode("propositionnode", new DownCableSet(d6, d7, d8));
                	
            		Node Z = Network.createVariableNode("Z", "propositionnode");
            		Node Y = Network.createVariableNode("Y", "propositionnode");
            		Node X = Network.createVariableNode("X", "propositionnode");
            		Node Base = Network.createNode("base", "propositionnode");
            		Network.quantifiers.put("forall","forall");
            		
            		Relation relation = new Relation ("forall", "", Adjustability.EXPAND, 2);
            		Relation relation2 = new Relation("b", "", Adjustability.EXPAND, 2);
            		
            		NodeSet nodeSetX = new NodeSet();
            		NodeSet nodeSetZ = new NodeSet();
            		NodeSet nodeSetXZ = new NodeSet();
            		NodeSet nodeSetY = new NodeSet();
            
            		nodeSetZ.add(Z);
            		nodeSetX.add(X);
            		nodeSetXZ.add(X);
            		nodeSetXZ.add(Z);
            		nodeSetY.add(Y);
            		
            		// M0
            		DownCable d2 = new DownCable(relation2, nodeSetXZ);
            		HashMap<String, DownCable> Cables = new HashMap<>();
            		Cables.put(d2.getRelation().getName(), d2);
            		DownCableSet downCableSet = new DownCableSet(Cables);
            		Node M0 = Network.createNode("propositionnode", downCableSet);
            
            		//M1 
            		NodeSet nodeSetM0 = new NodeSet();
            			nodeSetM0.add(M0);
            		DownCable d = new DownCable(relation, nodeSetZ);
            		DownCable d3 = new DownCable(relation2,nodeSetM0);
            		HashMap<String, DownCable> Cables2 = new HashMap<>();
            			Cables2.put(d3.getRelation().getName(), d3);
            			Cables2.put(d.getRelation().getName(), d);
            		DownCableSet downCableSet2 = new DownCableSet(Cables2);		
            		Node M1 = Network.createNode("propositionnode", downCableSet2);
            		
            		NodeSet nodeSetM1 = new NodeSet();
            		nodeSetM1.add(M1);
            		
            		//M2 
            		DownCable dM2 = new DownCable(relation2, nodeSetY.union(nodeSetM1));
            			HashMap<String, DownCable> CablesM2 = new HashMap<>();
            				CablesM2.put(dM2.getRelation().getName(), dM2);
            			DownCableSet downCableSetM2 = new DownCableSet(CablesM2);		
            			Node M2 = Network.createNode("propositionnode", downCableSetM2);
            	
            			//M3 
            			DownCable dM3 = new DownCable(relation2, nodeSetX.union(nodeSetZ.union(nodeSetY)));
            			
            				HashMap<String, DownCable> CablesM3 = new HashMap<>();
            					CablesM3.put(dM3.getRelation().getName(), dM3);
            					
            				DownCableSet downCableSetM3 = new DownCableSet(CablesM3);		
            				Node M3 = Network.createNode("propositionnode", downCableSetM3);
            		
            				//M4 
            				NodeSet nodeSetM3 = new NodeSet();
            				nodeSetM3.add(M3);
            				NodeSet nodeSetM2 = new NodeSet();
            				nodeSetM3.add(M2);
            				
            				NodeSet nodeSetM23 = new NodeSet();
            				nodeSetM23.add(M2);
            				nodeSetM23.add(M3);
            				
            				DownCable dM4 = new DownCable(relation, nodeSetX);
            				DownCable dM4_2 = new DownCable(relation2,nodeSetM23);
//            				DownCable dM4_3 = new DownCable(relation2,nodeSetM3);
            				
            					HashMap<String, DownCable> CablesM4 = new HashMap<>();
            						CablesM4.put(dM4.getRelation().getName(), dM4);
            						CablesM4.put(dM4_2.getRelation().getName()+1, dM4_2);
            						
            					DownCableSet downCableSetM4 = new DownCableSet(CablesM4);		
            					Node M4 = Network.createNode("propositionnode", downCableSetM4);
            					
                				DownCableSet downCableSetM5 = new DownCableSet(dM4);		
                				Node M5 = Network.createNode("propositionnode", downCableSetM5);

            		System.out.println("-----------------------------------------------------");
            		System.out.println("free vars" + M4.getFreeVariables());
            		
            		
//            		Substitution s = new Substitution(Base, Z);
//            		ArrayList<Substitution> substitutionArr = new ArrayList<>();
//            		substitutionArr.add(s);
//            		System.out.println(M4);
////            		System.out.println(M4.clone());
//            		
//            		System.out.println(M4.substitute(substitutionArr));
//            		network.printNodes();

                    new NetworkUI();
                });
            }
        }
