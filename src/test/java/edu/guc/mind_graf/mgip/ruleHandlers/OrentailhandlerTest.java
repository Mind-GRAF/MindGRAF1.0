package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrEntailhandlerTest {

    @Test
    void insertRI_shouldReturnNullWhenPcountIsZero() {
        // Arrange
        OrEntailhandler handler = new OrEntailhandler();
        RuleInfo ri = new RuleInfo("", 0, 0, 0, null, null, new Support(-1));

        // Act
        RuleInfoSet result = handler.insertRI(ri);

        // Assert
        assertNull(result);
    }

    @Test
    void insertRI_shouldReturnRuleInfoSetWhenPcountIsGreaterThanZero() {
        // Arrange
        OrEntailhandler handler = new OrEntailhandler();
        RuleInfo ri = new RuleInfo("", 0, 1, 0, null, null, new Support(-1));

        // Act
        RuleInfoSet result = handler.insertRI(ri);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(ri));
    }

    @Test
    void getUsedToInfer_shouldReturnCorrectRuleInfo() {
        // Arrange
        OrEntailhandler handler = new OrEntailhandler();
        RuleInfo expected = new RuleInfo("", 0, 1, 0, null, null, new Support(-1));

        // Act
        handler.insertRI(expected);
        RuleInfo actual = handler.getUsedToInfer();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void setUsedToInfer_shouldSetCorrectRuleInfo() {
        // Arrange
        OrEntailhandler handler = new OrEntailhandler();
        RuleInfo expected = new RuleInfo("", 0, 1, 0, null, null, new Support(-1));

        // Act
        handler.setUsedToInfer(expected);
        RuleInfo actual = handler.getUsedToInfer();

        // Assert
        assertEquals(expected, actual);
    }

}