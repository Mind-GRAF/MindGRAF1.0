package set;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Set<K,V> {
    private HashMap<K,V> set;
    
    public Set() {
        set = new HashMap<K,V>();
    }

    public HashMap<K,V> getSet() {
		return set;
	}

	public void setSet(HashMap<K,V> set) {
		this.set = set;
	}
	
	// Implementation of Set interface methods
    public int size() {
        return set.size();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public boolean contains(Object o) {
        return set.containsKey(o);
    }


    public void add(K k , V v) {
         set.put(k,v);
    }
    public void add(Map.Entry<K, V> e){
    	set.put(e.getKey(), e.getValue());
    }
    public boolean remove(Object o) {
       try{
    	   set.remove(o);
    	  
    	   return true;
       }catch(Exception e){
    	  return false;
       }
       
    }

    public boolean containsAll(Collection<K> keys) {
        return set.keySet().containsAll(keys);
    }


    public void addAll(Map<? extends K, ? extends V> m) {
        set.putAll(m);
    }

    public void removeAll(Collection<K> keys) {
        set.keySet().removeAll(keys);
    }

    public void clear() {
        set.clear();
    }

    // Set operation methods
    public Set<K,V> union(Set<K,V> otherSet) {
        Set<K,V> result = new Set<>();
        result.addAll(this.set);
        result.addAll(otherSet.set);
        return result;
    }

    public Set<K,V> intersection(Set<K,V> otherSet) {
        Set<K,V> result = new Set<>();
        for (Map.Entry<K,V> entry : set.entrySet()) {
        	if(otherSet.contains(entry)){
        		result.add(entry);
        	}
        }
        
        return result;
    }

    public Set<K,V> difference(Set<K,V> otherSet) {
        Set<K,V> result = new Set<>();
        result.addAll(this.set);
        result.removeAll(otherSet.set.keySet());
        return result;
    }

    public boolean isSubsetOf(Set<K,V> otherSet) {
        return otherSet.containsAll(this.set.keySet());
    }
}