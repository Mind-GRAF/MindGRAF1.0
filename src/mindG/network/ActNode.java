package mindG.network;

import mindG.network.cables.DownCableSet;

public class ActNode extends Node {

    public ActNode(String name, Boolean isVariable) {
        super(name, isVariable);
    }

    public ActNode(DownCableSet downCableSet) {
        super(downCableSet);
    }

}
