package support;

import java.util.ArrayList;
import java.util.HashMap;

public class SupportTree {
	private int nodeID;
	private ArrayList<HashMap<Integer, ArrayList<SupportTree>>> children;
	
	public SupportTree(int nodeID) {
		this.nodeID = nodeID;
		this.children = new ArrayList<>();
	}

	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @return the children
	 */
	public ArrayList<HashMap<Integer, ArrayList<SupportTree>>> getChildren() {
		return this.children;
	}

	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(ArrayList<HashMap<Integer, ArrayList<SupportTree>>> children) {
		this.children = children;
	}
	
	
	
	
}
