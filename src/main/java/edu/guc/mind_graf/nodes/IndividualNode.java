package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCableSet;

public class IndividualNode extends Node {

	public IndividualNode(String name, Boolean isVariable) {
		super(name, isVariable);
	}

	public IndividualNode(DownCableSet downCableSet) {
		super(downCableSet);
	}

}
