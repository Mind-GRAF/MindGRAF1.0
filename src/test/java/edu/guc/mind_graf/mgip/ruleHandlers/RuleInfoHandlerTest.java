package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class RuleInfoHandlerTest {

    @Test
    void insertRI_shouldReturnRuleInfoSetWithCombinedConstantRI_whenInputRIHasNoSubstitutions() throws InvalidRuleInfoException, DirectCycleException {
        // Arrange
        RuleInfoHandler handler = new Ptree(0, 0);
        RuleInfo inputRI = new RuleInfo("", 0, 1, 0, new Substitutions(), new FlagNodeSet(), new Support(-1));

        // Act
        RuleInfoSet result = handler.insertRI(inputRI);
        RuleInfo constantRI = handler.getConstantAntecedents("", 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, constantRI.getPcount());
    }

    @Test
    void getVariableAntecedents_shouldReturnPropositionNodeSetContainingOnlyAntecedentsWithFreeVariables() throws NoSuchTypeException {
        // Arrange
        Node G = Network.createVariableNode("G", "propositionnode");
        Node C = Network.createVariableNode("C", "propositionnode");

        // Create relations first
        Relation memberRelation = Network.createRelation("member", "", Adjustability.EXPAND, 2);
        Relation classRelation = Network.createRelation("class", "", Adjustability.EXPAND, 2);

        // Create nodes with proper classes
        Node government = Network.createNode("government", "propositionnode");
        Node civilian = Network.createNode("civilian", "propositionnode");
        Node nemo = Network.createNode("nemo", "propositionnode");
        Node fish = Network.createNode("fish", "propositionnode");

        // Create NodeSets for variables
        NodeSet gNodeSet = new NodeSet();
        gNodeSet.add(G);

        NodeSet cNodeSet = new NodeSet();
        cNodeSet.add(C);

        NodeSet govNodeSet = new NodeSet();
        govNodeSet.add(government);

        NodeSet civNodeSet = new NodeSet();
        civNodeSet.add(civilian);

        NodeSet nemoNodeSet = new NodeSet();
        nemoNodeSet.add(nemo);

        NodeSet fishNodeSet = new NodeSet();
        fishNodeSet.add(fish);

        // Create M0
        DownCable gMem = new DownCable(memberRelation, gNodeSet);
        DownCable gov = new DownCable(classRelation, govNodeSet);

        HashMap<String, DownCable> cablesM0 = new HashMap<>();
        cablesM0.put(gMem.getRelation().getName(), gMem);
        cablesM0.put(gov.getRelation().getName(), gov);

        DownCableSet downCableSetM0 = new DownCableSet(cablesM0);
        Node M0 = Network.createNode("propositionnode", downCableSetM0);

        // Create M1
        DownCable cMem = new DownCable(memberRelation, cNodeSet);
        DownCable civ = new DownCable(classRelation, civNodeSet);

        HashMap<String, DownCable> cablesM1 = new HashMap<>();
        cablesM1.put(cMem.getRelation().getName(), cMem);
        cablesM1.put(civ.getRelation().getName(), civ);

        DownCableSet downCableSetM1 = new DownCableSet(cablesM1);
        Node M1 = Network.createNode("propositionnode", downCableSetM1);

        // Create M2
        DownCable nMember = new DownCable(memberRelation, nemoNodeSet);
        DownCable fMember = new DownCable(classRelation, fishNodeSet);

        HashMap<String, DownCable> cablesM2 = new HashMap<>();
        cablesM2.put(nMember.getRelation().getName(), nMember);
        cablesM2.put(fMember.getRelation().getName(), fMember);

        DownCableSet downCableSetM2 = new DownCableSet(cablesM2);
        Node M2 = Network.createNode("propositionnode", downCableSetM2);

        // Act
        PropositionNodeSet result = RuleInfoHandler.getVariableAntecedents(new NodeSet(M0, M1, M2));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(M0));
        assertTrue(result.contains(M1));
        assertFalse(result.contains(M2));
    }
}