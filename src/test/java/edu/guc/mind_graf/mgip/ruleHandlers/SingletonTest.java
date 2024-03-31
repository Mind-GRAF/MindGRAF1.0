package edu.guc.mind_graf.mgip.ruleHandlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.Singleton;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SingletonTest {

    private Singleton singleton;

    @BeforeEach
    void setUp() {
        singleton = new Singleton();
    }

    @Test
    void testInsertIntoMap() {
        RuleInfo ri = new RuleInfo();
        int hash = 1;

        singleton.insertIntoMap(ri, hash);

        RuleInfo result = singleton.getRuleInfoMap().get(hash);

        assertNotNull(result);
        assertEquals(ri, result);
        assertEquals(1, singleton.getRuleInfoMap().size());
        assertEquals(ri, singleton.getRuleInfoMap().get(hash));
    }
}