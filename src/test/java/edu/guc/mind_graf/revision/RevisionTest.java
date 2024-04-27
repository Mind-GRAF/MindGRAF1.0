package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RevisionTest {
    Network n;

    @BeforeEach
    void setUp(){
        n = new Network();
        System.out.println("Testing Revesion");
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

        ContextController.setUp(attitudeNames,consistentAttitudes ,false);
        ContextController.createNewContext("guc");
    }

    @Test
    void filterAttitudes() {

        ArrayList<ArrayList<Integer>> output = Revision.filterAttitudes(ContextController.getConsistentAttitudes(),0);
        ArrayList< ArrayList< Integer>> expected = new ArrayList<>();
        expected.add(new ArrayList<>(List.of(0)));
        expected.add(new ArrayList<>(List.of(0,1)));
        expected.add(new ArrayList<>(List.of(0,2)));
        expected.add(new ArrayList<>(List.of(0,2,3)));

        Assertions.assertEquals(expected,output);
    }
}