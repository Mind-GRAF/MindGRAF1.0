package inferenceRules;

import java.util.Hashtable;

public class SIndexHelper {

	public static Hashtable<Integer, RuisHandler> map = new Hashtable<Integer, RuisHandler>();
	
	
	public static int getSize() {
		return map.size();
	}
	
}