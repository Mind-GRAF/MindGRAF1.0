package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.set.RuleInfoSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrentailhandlerTest {

    @Test
    void insertRI_shouldReturnNullWhenPcountIsZero() {
        // Arrange
        Orentailhandler handler = new Orentailhandler();
        RuleInfo ri = new RuleInfo("", 0, 0, 0, null, null);

        // Act
        RuleInfoSet result = handler.insertRI(ri);

        // Assert
        assertNull(result);
    }

    @Test
    void insertRI_shouldReturnRuleInfoSetWhenPcountIsGreaterThanZero() {
        // Arrange
        Orentailhandler handler = new Orentailhandler();
        RuleInfo ri = new RuleInfo("", 0, 1, 0, null, null);

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
        Orentailhandler handler = new Orentailhandler();
        RuleInfo expected = new RuleInfo("", 0, 1, 0, null, null);

        // Act
        handler.insertRI(expected);
        RuleInfo actual = handler.getUsedToInfer();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void setUsedToInfer_shouldSetCorrectRuleInfo() {
        // Arrange
        Orentailhandler handler = new Orentailhandler();
        RuleInfo expected = new RuleInfo("", 0, 1, 0, null, null);

        // Act
        handler.setUsedToInfer(expected);
        RuleInfo actual = handler.getUsedToInfer();

        // Assert
        assertEquals(expected, actual);
    }

}