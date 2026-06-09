package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SIndexDetailedTest {

    private static final String CONTEXT = "test";
    private static final int BELIEF = 0;

    private Node d;
    private Node p;
    private Node b;
    private Node s;
    private Node ahmed;
    private Node mona;
    private Node alice;
    private Node bob;
    private Node buildingA;
    private Node buildingB;
    private Node fire;

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        new Network();

        d = Network.createVariableNode("D", "individualnode");
        p = Network.createVariableNode("P", "individualnode");
        b = Network.createVariableNode("B", "individualnode");
        s = Network.createVariableNode("S", "individualnode");

        ahmed = Network.createNode("ahmed", "individualnode");
        mona = Network.createNode("mona", "individualnode");
        alice = Network.createNode("Alice", "individualnode");
        bob = Network.createNode("Bob", "individualnode");
        buildingA = Network.createNode("BuildingA", "individualnode");
        buildingB = Network.createNode("BuildingB", "individualnode");
        fire = Network.createNode("Fire", "individualnode");
    }

    @Test
    void singletonSIndex_groupsRuleInfosWithSameSubstitutionAndSeparatesDifferentSubstitutions()
            throws InvalidRuleInfoException, DirectCycleException {
        Singleton singleton = new Singleton(new NodeSet(d));

        RuleInfo doctorAhmed = ruleInfo(1, subs(d, ahmed));
        RuleInfo certifiedAhmed = ruleInfo(1, subs(d, ahmed));
        RuleInfo doctorMona = ruleInfo(1, subs(d, mona));

        RuleInfoSet firstInsert = singleton.insertVariableRI(doctorAhmed);
        assertEquals(1, firstInsert.size());
        assertEquals(1, singleton.getRuleInfoMap().size());

        RuleInfoSet sameBindingInsert = singleton.insertVariableRI(certifiedAhmed);
        assertEquals(
                1,
                sameBindingInsert.size(),
                "The second D=ahmed RuleInfo should combine with the existing D=ahmed entry."
        );
        RuleInfo combinedAhmed = onlyRuleInfo(sameBindingInsert);
        assertEquals(2, combinedAhmed.getPcount());
        assertEquals(ahmed, combinedAhmed.getSubs().get(d));
        assertEquals(
                1,
                singleton.getRuleInfoMap().size(),
                "Both D=ahmed RuleInfos should be stored under one Singleton SIndex key."
        );

        RuleInfoSet differentBindingInsert = singleton.insertVariableRI(doctorMona);
        RuleInfo monaEntry = onlyRuleInfo(differentBindingInsert);
        assertEquals(1, monaEntry.getPcount());
        assertEquals(mona, monaEntry.getSubs().get(d));
        assertEquals(
                2,
                singleton.getRuleInfoMap().size(),
                "D=mona should be stored separately from D=ahmed."
        );

        System.out.println("[TEST LOG][SINDEX SINGLETON]");
        System.out.println("  inserted: D=ahmed from first antecedent");
        System.out.println("  inserted: D=ahmed from second antecedent");
        System.out.println("  combined pcount for D=ahmed: " + combinedAhmed.getPcount());
        System.out.println("  inserted separately: D=mona");
        System.out.println("  singleton map size: " + singleton.getRuleInfoMap().size());
    }

    @Test
    void linearSIndex_respectsMinimumAndStoresOnlyCompleteRuleInfosBySharedVariable()
            throws InvalidRuleInfoException, DirectCycleException {
        Linear linear = new Linear(new NodeSet(b));
        linear.setMin(3);

        RuleInfo incompleteBob = ruleInfo(2, subs(p, bob, b, buildingB));
        RuleInfo completeAlice = ruleInfo(3, subs(p, alice, b, buildingA));

        RuleInfoSet rejected = linear.insertVariableRI(incompleteBob);
        assertNull(
                rejected,
                "A Linear SIndex node should reject a RuleInfo whose positive count is below min."
        );
        assertTrue(linear.getRuleInfoMap().isEmpty());

        RuleInfoSet inserted = linear.insertVariableRI(completeAlice);
        assertNotNull(inserted);
        RuleInfo aliceEntry = onlyRuleInfo(inserted);
        assertEquals(3, aliceEntry.getPcount());
        assertEquals(alice, aliceEntry.getSubs().get(p));
        assertEquals(buildingA, aliceEntry.getSubs().get(b));
        assertEquals(1, linear.getRuleInfoMap().size());

        System.out.println("[TEST LOG][SINDEX LINEAR MIN]");
        System.out.println("  rejected: P=Bob, B=BuildingB, pcount=2, min=3");
        System.out.println("  inserted: P=Alice, B=BuildingA, pcount=3");
        System.out.println("  linear map size: " + linear.getRuleInfoMap().size());
    }

    @Test
    void linearSIndex_combinesCompatibleEntriesAndDoesNotCombineIncompatibleEntries()
            throws InvalidRuleInfoException, DirectCycleException {
        Linear linear = new Linear(new NodeSet(b));
        linear.setMin(1);

        RuleInfo aliceAtBuildingA = ruleInfo(1, subs(p, alice, b, buildingA));
        RuleInfo buildingAHasFire = ruleInfo(1, subs(b, buildingA, s, fire));
        RuleInfo bobAtBuildingA = ruleInfo(1, subs(p, bob, b, buildingA));
        RuleInfo incompatibleAliceAtBuildingB = ruleInfo(1, subs(p, alice, b, buildingB));

        linear.insertVariableRI(aliceAtBuildingA);

        RuleInfoSet compatibleInsert = linear.insertVariableRI(buildingAHasFire);
        assertTrue(
                containsRuleInfo(compatibleInsert, p, alice, b, buildingA, s, fire),
                "Linear SIndex should combine compatible entries that share B=BuildingA."
        );

        RuleInfoSet sameSharedVariableDifferentPerson = linear.insertVariableRI(bobAtBuildingA);
        assertTrue(
                containsRuleInfo(sameSharedVariableDifferentPerson, p, bob, b, buildingA, s, fire),
                "A second compatible person at the same building should combine with the building fact."
        );

        int mapSizeBeforeIncompatibleInsert = linear.getRuleInfoMap().size();
        RuleInfoSet incompatibleInsert = linear.insertVariableRI(incompatibleAliceAtBuildingB);
        assertFalse(
                containsRuleInfo(incompatibleInsert, p, alice, b, buildingB, s, fire),
                "Alice at BuildingB must not combine with the Fire fact indexed under BuildingA."
        );
        assertEquals(
                mapSizeBeforeIncompatibleInsert + 1,
                linear.getRuleInfoMap().size(),
                "The incompatible BuildingB entry should be stored under a different shared-variable key."
        );

        System.out.println("[TEST LOG][SINDEX LINEAR COMPATIBILITY]");
        System.out.println("  inserted: P=Alice, B=BuildingA");
        System.out.println("  inserted: B=BuildingA, S=Fire");
        System.out.println("  combined: P=Alice, B=BuildingA, S=Fire");
        System.out.println("  inserted: P=Bob, B=BuildingA");
        System.out.println("  combined: P=Bob, B=BuildingA, S=Fire");
        System.out.println("  inserted separately: P=Alice, B=BuildingB");
        System.out.println("  no combination with S=Fire for BuildingB");
    }

    private RuleInfo ruleInfo(int pcount, Substitutions substitutions) {
        return new RuleInfo(
                CONTEXT,
                BELIEF,
                pcount,
                0,
                substitutions,
                new FlagNodeSet(),
                new Support(-1)
        );
    }

    private Substitutions subs(Node variable1, Node value1) {
        Substitutions substitutions = new Substitutions();
        substitutions.add(variable1, value1);
        return substitutions;
    }

    private Substitutions subs(Node variable1, Node value1, Node variable2, Node value2) {
        Substitutions substitutions = subs(variable1, value1);
        substitutions.add(variable2, value2);
        return substitutions;
    }

    private Substitutions subs(
            Node variable1,
            Node value1,
            Node variable2,
            Node value2,
            Node variable3,
            Node value3
    ) {
        Substitutions substitutions = subs(variable1, value1, variable2, value2);
        substitutions.add(variable3, value3);
        return substitutions;
    }

    private RuleInfo onlyRuleInfo(RuleInfoSet ruleInfos) {
        assertNotNull(ruleInfos);
        assertEquals(1, ruleInfos.size());
        return ruleInfos.iterator().next();
    }

    private boolean containsRuleInfo(RuleInfoSet ruleInfos, Node... variableValuePairs) {
        if (ruleInfos == null) {
            return false;
        }

        for (RuleInfo ruleInfo : ruleInfos) {
            boolean matches = true;
            for (int i = 0; i < variableValuePairs.length; i += 2) {
                Node variable = variableValuePairs[i];
                Node value = variableValuePairs[i + 1];
                Node actual = ruleInfo.getSubs().get(variable);
                if (actual == null || !value.equals(actual)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }

        return false;
    }
}
