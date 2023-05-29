package inferenceRules;
import java.util.Enumeration;
import java.util.Hashtable;


	public class ContextRuisSet{
		private Hashtable<String, RuisHandler> ruisHandlers;

		public ContextRuisSet() {
			ruisHandlers = new Hashtable<String, RuisHandler>();
		}

		/**
		 * Adds the given RuisHandler by contextName into this.ruisHandlers
		 * @param contextName
		 * @param handler
		 * @return
		 */
		public RuisHandler addHandlerSet(String contextName, RuisHandler handler){
			return ruisHandlers.put(contextName, handler);
		}
		
		/**
		 * Checks whether this.ruisHandlers has an entry for the given contextName
		 * @param contextID
		 * @return
		 */
		public boolean hasContext(String contextName) {
			RuisHandler set = ruisHandlers.get(contextName);
			if(set == null)
				return false;
			return true;
		}

		/**
		 * Gets the stored entry for the given contextName 
		 * @param contextID
		 * @return
		 */
		public RuisHandler getByContext(String contextName) {
			return ruisHandlers.get(contextName);
		}
		
		/**
		 * Checks whether this.ruisHandlers contains the given handler
		 * @param handler
		 * @return
		 */
		public boolean contains(RuisHandler handler){
			return ruisHandlers.contains(handler);
		}
		public boolean containsKey(String handlerKey){
			return ruisHandlers.containsKey(handlerKey);
		}
		public RuisHandler getHandler(RuisHandler handler){
			return ruisHandlers.get(handler);
		}
		public Enumeration<String> getKeys(){
			return ruisHandlers.keys();
		}
		public int size(){
			return ruisHandlers.size();
		}
		public void remove(String contextName, RuisHandler handler){
			ruisHandlers.remove(handler, handler);
		}

		public void clear() {
			ruisHandlers.clear();
		}
	}
