package edu.guc.mind_graf.Context;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.function.IntBinaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class ContextControllerTest {

	private Set<String, Integer> attitudes;
	private ArrayList<ArrayList<Integer>> consistentAttitudes;

	@BeforeEach
	void setUp() {
		// Setup attitudes
		attitudes = new Set<>();
		attitudes.add("belief", 0);
		attitudes.add("desire", 1);
		attitudes.add("intention", 2);

		// Setup consistent attitudes
		consistentAttitudes = new ArrayList<>();
		ArrayList<Integer> group1 = new ArrayList<>();
		group1.add(0);
		group1.add(1);
		consistentAttitudes.add(group1);

		ArrayList<Integer> group2 = new ArrayList<>();
		group2.add(2);
		consistentAttitudes.add(group2);

		// Initialize ContextController
		ContextController.setup(attitudes, consistentAttitudes, true, true, 1);
	}

	@AfterEach
	void tearDown() {
		// Clean up any created contexts
		Context.deleteAssumedContexts();
		// Reset ContextController by setting up fresh
		ContextController.setup(new Set<>(), new ArrayList<>(), false, false, 1);
	}

	@Test
	void testSetup() {
		// Test with different configurations
		ContextController.setup(attitudes, consistentAttitudes, false, false, 2);

		assertFalse(ContextController.automaticHandlingEnabled());
		assertFalse(ContextController.isCacheEnabled());
		assertEquals(attitudes, ContextController.getAttitudes());
		assertEquals(consistentAttitudes, ContextController.getConsistentAttitudes());
	}

	@Test
	void testGetAttitudeName() {
		assertEquals("belief", ContextController.getAttitudeName(0));
		assertEquals("desire", ContextController.getAttitudeName(1));
		assertEquals("intention", ContextController.getAttitudeName(2));
		assertNull(ContextController.getAttitudeName(99)); // Non-existent attitude
	}

	@Test
	void testGetAttitudeNumber() {
		assertEquals(0, ContextController.getAttitudeNumber("belief"));
		assertEquals(1, ContextController.getAttitudeNumber("desire"));
		assertEquals(2, ContextController.getAttitudeNumber("intention"));
	}

	@Test
	void testCreateNewContext() {
		// Create a new context
		ContextController.createNewContext("TestContext");

		// Verify context was created
		Context context = ContextController.getContext("TestContext");
		assertNotNull(context);
		assertEquals("TestContext", context.getName());

		// Test creating duplicate context
		assertThrows(RuntimeException.class, () ->
				ContextController.createNewContext("TestContext")
		);
	}

	@Test
	void testGetContextNonExistent() {
		assertThrows(RuntimeException.class, () ->
				ContextController.getContext("NonExistentContext")
		);
	}

	@Test
	void testSetAndGetCurrentContext() {
		// Create and set current context
		ContextController.createNewContext("CurrentContext");
		ContextController.setCurrContext("CurrentContext");

		assertEquals("CurrentContext", ContextController.getCurrContextName());

		// Test setting non-existent context
		assertThrows(RuntimeException.class, () ->
				ContextController.setCurrContext("NonExistentContext")
		);
	}

	@Test
	void testGetContextSet() {
		ContextSet contextSet = ContextController.getContextSet();
		assertNotNull(contextSet);

		// Create a context and verify it's in the set
		ContextController.createNewContext("TestContext");
		assertTrue(contextSet.contains("TestContext"));
	}

	@Test
	void testAutomaticHandlingEnabled() {
		// Test with true (set in setUp)
		assertTrue(ContextController.automaticHandlingEnabled());

		// Test with false
		ContextController.setup(attitudes, consistentAttitudes, false, true, 1);
		assertFalse(ContextController.automaticHandlingEnabled());
	}

	@Test
	void testIsCacheEnabled() {
		// Test with true (set in setUp)
		assertTrue(ContextController.isCacheEnabled());

		// Test with false
		ContextController.setup(attitudes, consistentAttitudes, true, false, 1);
		assertFalse(ContextController.isCacheEnabled());
	}

	@Test
	void testGetMergeFunctionMax() {
		// Default (1) should be Math::max
		ContextController.setup(attitudes, consistentAttitudes, true, true, 1);
		IntBinaryOperator mergeFunc = ContextController.getMergeFunction();

		assertEquals(10, mergeFunc.applyAsInt(5, 10));
		assertEquals(10, mergeFunc.applyAsInt(10, 5));
	}

	@Test
	void testGetMergeFunctionMin() {
		// 2 should be Math::min
		ContextController.setup(attitudes, consistentAttitudes, true, true, 2);
		IntBinaryOperator mergeFunc = ContextController.getMergeFunction();

		assertEquals(5, mergeFunc.applyAsInt(5, 10));
		assertEquals(5, mergeFunc.applyAsInt(10, 5));
	}

	@Test
	void testGetMergeFunctionAverage() {
		// 3 should be average
		ContextController.setup(attitudes, consistentAttitudes, true, true, 3);
		IntBinaryOperator mergeFunc = ContextController.getMergeFunction();

		assertEquals(7, mergeFunc.applyAsInt(5, 10)); // (5 + 10) / 2 = 7
		assertEquals(15, mergeFunc.applyAsInt(10, 20)); // (10 + 20) / 2 = 15
	}

	@Test
	void testGetMergeFunctionDefaultCase() {
		// Any other number should default to Math::max
		ContextController.setup(attitudes, consistentAttitudes, true, true, 99);
		IntBinaryOperator mergeFunc = ContextController.getMergeFunction();

		assertEquals(10, mergeFunc.applyAsInt(5, 10));
		assertEquals(10, mergeFunc.applyAsInt(10, 5));
	}

	@Test
	void testMultipleContextCreation() {
		// Create multiple contexts
		ContextController.createNewContext("Context1");
		ContextController.createNewContext("Context2");
		ContextController.createNewContext("Context3");

		// Verify all contexts exist
		assertNotNull(ContextController.getContext("Context1"));
		assertNotNull(ContextController.getContext("Context2"));
		assertNotNull(ContextController.getContext("Context3"));

		// Verify they are different objects
		assertNotEquals(
				ContextController.getContext("Context1"),
				ContextController.getContext("Context2")
		);
	}

	@Test
	void testConsistentAttitudesRetrieval() {
		ArrayList<ArrayList<Integer>> retrieved = ContextController.getConsistentAttitudes();

		assertEquals(2, retrieved.size());
		assertEquals(2, retrieved.get(0).size());
		assertEquals(1, retrieved.get(1).size());

		assertTrue(retrieved.get(0).contains(0));
		assertTrue(retrieved.get(0).contains(1));
		assertTrue(retrieved.get(1).contains(2));
	}

	@Test
	void testNullCurrentContextOperations() {
		// In your test method, before assertThrows:
		try {
			// Using reflection to force currContext to null
			java.lang.reflect.Field field = ContextController.class.getDeclaredField("currContext");
			field.setAccessible(true);
			field.set(null, null); // Setting static field to null
		} catch (Exception e) {
			fail("Could not set currContext to null: " + e.getMessage());
		}

// Now test the exception
		assertThrows(NullPointerException.class, () ->
				ContextController.getCurrContextName()
		);
	}

	@Test
	void testContextPersistenceAcrossOperations() {
		// Create context
		ContextController.createNewContext("PersistentContext");

		// Get reference to context
		Context context1 = ContextController.getContext("PersistentContext");

		// Switch current context and back
		ContextController.createNewContext("TempContext");
		ContextController.setCurrContext("TempContext");
		ContextController.setCurrContext("PersistentContext");

		// Verify it's still the same context object
		Context context2 = ContextController.getContext("PersistentContext");
		assertEquals(context1, context2);
	}

	@Test
	void testAttitudeOperations() {
		// Test all attitude-related operations
		Set<String, Integer> retrievedAttitudes = ContextController.getAttitudes();

		assertEquals(3, retrievedAttitudes.size());
		assertEquals(0, retrievedAttitudes.get("belief"));
		assertEquals(1, retrievedAttitudes.get("desire"));
		assertEquals(2, retrievedAttitudes.get("intention"));

		// Test reverse lookup
		assertEquals("belief", ContextController.getAttitudeName(0));
		assertEquals(0, ContextController.getAttitudeNumber("belief"));
	}

	@Test
	void testContextCreationWithCurrentContext() {
		// Create and set current context
		ContextController.createNewContext("MainContext");
		ContextController.setCurrContext("MainContext");

		// Create another context while one is current
		ContextController.createNewContext("SecondContext");

		// Current context should still be MainContext
		assertEquals("MainContext", ContextController.getCurrContextName());

		// Both contexts should exist
		assertNotNull(ContextController.getContext("MainContext"));
		assertNotNull(ContextController.getContext("SecondContext"));
	}

	@Test
	void testEmptyAttitudesSetup() {
		// Test with empty attitudes
		Set<String, Integer> emptyAttitudes = new Set<>();
		ArrayList<ArrayList<Integer>> emptyConsistent = new ArrayList<>();

		ContextController.setup(emptyAttitudes, emptyConsistent, true, true, 1);

		assertEquals(0, ContextController.getAttitudes().size());
		assertEquals(0, ContextController.getConsistentAttitudes().size());

		// Create context with empty attitudes
		ContextController.createNewContext("EmptyContext");
		Context context = ContextController.getContext("EmptyContext");
		assertNotNull(context);
		assertEquals(0, context.getHypotheses().get(0).length);
	}

	// Note about methods that require PropositionNode
	@Test
	void testMethodsRequiringPropositionNode() {
		System.out.println("=== Methods Requiring PropositionNode ===");
		System.out.println("The following methods cannot be fully tested without PropositionNode instances:");
		System.out.println("- addHypothesisToContext(String, int, int, int)");
		System.out.println("- addHypothesisToContext(int, int, int)");
		System.out.println("- removeHypothesisFromContext(String, int, int, int)");
		System.out.println("- removeHypothesisFromContext(int, int, int)");
		System.out.println();
		System.out.println("To test these, you need:");
		System.out.println("1. Create PropositionNode instances with the actual constructor");
		System.out.println("2. Initialize Network.getNodeById() to return these nodes");
		System.out.println("3. Then call the hypothesis methods with valid node IDs");
		System.out.println("=========================================");
	}
}