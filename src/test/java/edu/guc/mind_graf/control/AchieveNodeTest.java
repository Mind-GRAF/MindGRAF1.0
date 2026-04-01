package edu.guc.mind_graf.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.AchieveNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

/**
 *
 * @author marwannelsayed
 */
public class AchieveNodeTest {
    Network n;
    @BeforeEach
    void setUp() {
        n = new Network();
        System.out.println("Testing Attitude Node");
        Set<String,Integer> attitudeNames = new Set<>();
        attitudeNames.add( "beliefs",0);
        attitudeNames.add("obligations",1);
        attitudeNames.add("fears",2);
        attitudeNames.add("hate",3);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,2)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,2,3)));

        NetworkController.setUp(attitudeNames,consistentAttitudes ,false,false,false,1);
        ContextController.createNewContext("guc");
    }

    @Test
    void test1(){
        try {
            System.out.println("Testing example 1");
            ContextController.setCurrContext("guc");
            Context c = ContextController.getContext("guc");
            Node prop = Network.createNode("fun", "propositionnode");
            Node achieve = Network.createNode("achieve", "actnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE,2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE,2);

            DownCable d1 = new DownCable(obj, new NodeSet(prop));
            DownCable d2 = new DownCable(action, new NodeSet(achieve));

            DownCableSet downCableSetM0 = new DownCableSet(d1,d2);

            AchieveNode M0 = new AchieveNode(downCableSetM0);

            Scheduler.initiate();

            Scheduler.addToActQueue(M0);

            Scheduler.schedule();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
