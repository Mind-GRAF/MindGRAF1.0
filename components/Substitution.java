package components;

import nodes.Node;

public class Substitution {
	private Node node ; 
	private Node variable ;
	public Substitution(Node node , Node variable) {
		// TODO Auto-generated constructor stub
		this.node = node ; 
		this.variable = variable ;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public Node getVariable() {
		return variable;
	}
	public void setVariable(Node variable) {
		this.variable = variable;
	}
	

}
