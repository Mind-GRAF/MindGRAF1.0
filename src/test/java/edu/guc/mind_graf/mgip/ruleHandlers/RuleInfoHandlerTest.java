package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleInfoHandlerTest {

    @Test
    void insertRI_shouldReturnRuleInfoSetWithCombinedConstantRI_whenInputRIHasNoSubstitutions() throws InvalidRuleInfoException, InvalidRuleInfoException {
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

        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(G));
        Node government = Network.createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(C));
        Node civilian = Network.createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMem, civ));

        Node nemo = Network.createNode("nemo", "propositionnode");
        Node fish = Network.createNode("fish", "propositionnode");
        DownCable nMember = new DownCable(Network.getRelations().get("member"), new NodeSet(nemo));
        DownCable fMember = new DownCable(Network.getRelations().get("class"), new NodeSet(fish));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(nMember, fMember));

        // Act
        PropositionNodeSet result = RuleInfoHandler.getVariableAntecedents(new NodeSet(M0, M1, M2));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(M0));
        assertFalse(result.contains(M2));
    }
}