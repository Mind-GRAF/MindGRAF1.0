package edu.guc.mind_graf.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
	
	@BeforeEach
	void setUp() {
		Hashtable<Integer,String> attitudeNames = new Hashtable<>();
		attitudeNames.put(0, "guc");
		Controller.setUp(attitudeNames, false);
	}
	
	@Test
	void test1() {
		System.out.println("Test 1 | uvbr false");
		Controller.setCurrContext("guc");
	}
}