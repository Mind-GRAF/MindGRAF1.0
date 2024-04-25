package edu.guc.mind_graf.set;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleInfoSetTest {

    private RuleInfoSet ruleInfoSet;

    @BeforeEach
    public void setUp() {
        ruleInfoSet = new RuleInfoSet();
    }

    @AfterEach
    void tearDown() {
        ruleInfoSet.clear();
    }

    @Test
    public void testAddRuleInfo() {
        RuleInfo ruleInfo = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet());
        ruleInfoSet.addRuleInfo(ruleInfo);
        assertEquals(1, ruleInfoSet.size());
    }

    @Test
    public void testCombine() {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet());
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet());
        ruleInfoSet.addRuleInfo(ruleInfo1);
        ruleInfoSet.addRuleInfo(ruleInfo2);
        RuleInfo ruleInfo3 = new RuleInfo("", 0, 2, 2, new Substitutions(), new FlagNodeSet());
        ruleInfoSet.combine(ruleInfo3);
        assertEquals(2, ruleInfoSet.size());
        for(RuleInfo ruleInfo : ruleInfoSet) {
            assertNotEquals(ruleInfo, ruleInfo1);
        }
    }

    @Test
    public void testCombineAdd() {
        RuleInfoSet ruleInfoSet1 = new RuleInfoSet(new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet()));
        RuleInfo ruleInfo = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet());
        RuleInfoSet combined = ruleInfoSet1.combineAdd(ruleInfo);
        assertEquals(1, combined.size());
        assertEquals(3, ruleInfoSet1.size());
    }

    @Test
    public void testUnion() {
        RuleInfoSet ruleInfoSet1 = new RuleInfoSet(new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet()));
        RuleInfoSet ruleInfoSet2 = new RuleInfoSet(new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet()));
        RuleInfoSet union = ruleInfoSet1.union(ruleInfoSet2);
        assertEquals(2, union.size());
    }

    @Test
    public void testClear() {
        RuleInfo ruleInfo = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet());
        ruleInfoSet.addRuleInfo(ruleInfo);
        ruleInfoSet.clear();
        assertTrue(ruleInfoSet.isEmpty());
    }

    @Test
    public void testCombineDisjointSets() {
        RuleInfoSet ruleInfoSet1 = new RuleInfoSet(new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet()));
        RuleInfoSet ruleInfoSet2 = new RuleInfoSet(new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet()));
        RuleInfoSet combined = ruleInfoSet1.combineDisjointSets(ruleInfoSet2);
        assertEquals(1, combined.size());
    }

    @Test
    public void testRemoveRuleInfo() {
        RuleInfo ruleInfo = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet());
        ruleInfoSet.addRuleInfo(ruleInfo);
        ruleInfoSet.removeRuleInfo(ruleInfo);
        assertTrue(ruleInfoSet.isEmpty());
    }

    @Test
    void isEmpty() {
        assertTrue(ruleInfoSet.isEmpty());
    }

    @Test
    public void testAddRootRuleInfo() {
        RuleInfoSet ruleInfoSet1 = new RuleInfoSet(new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet()));
        RuleInfoSet ruleInfoSet2 = new RuleInfoSet(new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet()));
        ruleInfoSet1.addRootRuleInfo(ruleInfoSet2);
        assertEquals(3, ruleInfoSet1.size());
    }

}