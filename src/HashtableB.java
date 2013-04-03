/*
 * HashtableB.java
 * 
 * Version 1.0
 * 
 * 2013-3-19
 * 
 * Copyright (c) 2013 Jackson Scholl. 
 */

import java.util.*;

/**
 * A linear-probing hash table implementation.
 * 
 * @version 2013-03-19
 * @author Jackson Scholl
 *
 * @param <K> The key type
 * @param <V> The value type
 */
public class HashtableB<K extends Comparable<K>, V> implements ST<K, V> {
    private static final int MIN_CAPACITY = 11; // The minimum size of the array; when smaller than this, no down-sizing will occur.
    
    private Pair<K, V>[] array; // The array holding all the key/value pairs
    private int size; // The current number of elements.
    private int capacity; // Current capacity of the array.
    
    private double maxFullness; // determines how full the array can get before resizing occurs; default 1/2
    private double minFullness; // determines how empty the array can get before resizing occurs; default 3/4
    private double setFullness; // determines how full the array should be made when resizing; default 1/4
    
    /**
     * Constructs an empty {@code HashtableB} with the specified {@code maximum}, {@code minimum}, and {@code set} fullness ratios
     *
     * @param  maximum	the maximum fullness
     * @param  minimum	the minimum fullness
     * @param  set		the fullness when the array is resized.
     * @throws IllegalArgumentException if {@code minimum} is less than or equal to zero
     * 				or {@code set} is less or equal to than {@code minimum}
     * 				or {@code maximum} is less than or equal to {@code set}
     * 				or {@code maximum} is greater than one.
     */
    @SuppressWarnings("unchecked")
	public HashtableB(double maximum, double minimum, double set) throws IllegalArgumentException {
        if (0 >= minimum)
            throw new IllegalArgumentException("Illegal minimum fullness: " + minimum);
        if (minimum >= set)
            throw new IllegalArgumentException("Minimum fullness is greater than or equal to set.");
        if (set >= maximum)
            throw new IllegalArgumentException("Set fullness is greater than or equal to maximum.");
        if (maximum >= 1)
        	throw new IllegalArgumentException("Illegal maximum fullness: " + maximum);
        
        size = 0;
        capacity = MIN_CAPACITY;
        maxFullness = maximum;
        minFullness = minimum;
        this.setFullness = set;
        
        array = (Pair<K, V>[]) new Pair[capacity];
    }
    
    /**
     * Constructs an empty {@code HashtableB} with the specified {@code maximum} and {@code minimum} fullness ratios,
     * and a default {@code set} ratio.
     *
     * @param  maximum	the maximum fullness
     * @param  minimum	the minimum fullness
     * @throws IllegalArgumentException if {@code minimum} is less than or equal to zero
     * 				or {@code minimum} is greater than the default {@code set}
     * 				or {@code maximum} is less than or equal to the default {@code set}
     * 				or {@code maximum} is greater than one.
     */
    public HashtableB(double maximum, double minimum) throws IllegalArgumentException {
        this(maximum, minimum, 0.5);
    }
    
    /**
     * Constructs an empty {@code HashtableB} with the default {@code maximum}, {@code minimum}, and {@code set} fullness ratios.
     * 
     */
    public HashtableB() {
        this(0.75, 0.25);
    }
    
	/**
	 * Returns the current number of keys with associated values.
	 * 
	 * @return the number of currently-mapped keys
	 */
	public int size() {
	    return size;
	}

	/**
	 * Returns {@code true} if this map contains no key-value mappings.
	 *
	 * @return {@code true} if this map contains no key-value mappings
	 */
	public boolean isEmpty() {
	    return size == 0;
	}
	
	// A hash of the key. I used the absolute value of the key's hashcode so that I didn't get weird negative indices.
	private int hash(K key) throws NullPointerException {
		if(key == null)
			throw new NullPointerException("Key is not allowed to be null");
	    return Math.abs(key.hashCode());
	}
	
	private int getIndex(K key) {
		int i = hash(key) % capacity;
	    while (array[i] != null && !key.equals(array[i].k)) {
	        i = (i+1) % capacity;
	    }
	    return i;
	}

	/**
	 * Returns the value that is mapped to the given key.
	 *
	 * @param key the key to locate
	 * @return the value mapped to {@code key} or {@code null} if not found
	 * @throws NullPointerException if the specified key is null
	 */
	public V get(K key) throws NullPointerException {
		if (key == null)
			throw new NullPointerException("Key is not allowed to be null");
	    
	    int i = getIndex(key);
	    return array[i]==null? null : array[i].v;
	}

	/**
     * Returns {@code true} if this hash table contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this hash table is to be tested
     * @return {@code true} if this hash table contains a mapping for the specified
     *         key
     * @throws NullPointerException if the specified key is null
     */
    public boolean containsKey(K key) throws NullPointerException {
    	if (key == null)
    		throw new NullPointerException("Key is not allowed to be null");
    	
    	int i = getIndex(key);
	    return array[i] != null;
    }
    
    /**
     * Returns {@code true} if this map maps one or more keys to the specified value. Takes {@code O(n)} time.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the
     *         specified value
     * @throws NullPointerException if the specified value is null
     */
    public boolean containsValue(V value) throws NullPointerException {
    	if (value == null)
			throw new NullPointerException("Value is not allowed to be null");
    	
    	for (Pair<K, V> p : array) {
    		if(p!=null && value.equals(p.v))
    			return true;
    	}
    	
    	return false;
    }

    /**
	 * Returns the set of all the keys contained in this hash table.
	 * 
	 * @return the set of all the keys contained in this hash table
	 */
	public Set<K> getAllKeys() {
	    Set<K> set = new HashSet<K>(size);
	    for (Pair<K, V> p : array)
	        if(p != null)
	        	set.add(p.k);
	    return set;
	}

	/**
     * Associates the specified value with the specified key in this hash table.  If the hash table previously
     * contained a mapping for the key, the old value is replaced by the specified value.
     *
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     * 
     * @throws NullPointerException if the specified key or value is null
     */
	public V put(K key, V val) throws NullPointerException {
		if (key == null)
			throw new NullPointerException("Key is not allowed to be null");
		if (val == null)
			throw new NullPointerException("Value is not allowed to be null");
        
		int i = getIndex(key);
        
        if(array[i] == null){ // If we are putting a new key in, increase the size.
            size++;
            array[i] = new Pair<K, V>(key, val);
            resizeIfNeeded(); // If we need to resize, do so.
            return null;
        } else {
        	assert key.equals(array[i].k);
        	V previousValue = array[i].v;
        	array[i].v = val;
        	return previousValue;
        }
        
    }
    
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) throws NullPointerException {
    	for(java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet())
    		put(e.getKey(), e.getValue());
    }
    
    /**
	 * Removes the mapping for a key from the hash table if it is present.
	 *
	 * <p>The hash table will not contain a mapping for the specified key once the call returns.
	 *
	 * @param key key whose mapping is to be removed from the hash table
	 * @throws NullPointerException if the specified key is null
	 */ 
	public V remove(K key) throws NullPointerException{
		if(key == null)
			throw new NullPointerException("Key is not allowed to be null");
	    List<Pair<K, V>> pairs = new ArrayList<Pair<K, V>>();
	    
	    
	    // Find our key.
	    int i = getIndex(key);
	    
	    if(array[i] == null)
            return null;
	    
	    // Remove all the keys that could have been "forced over" by this key.
	    while(array[i] != null){
	        pairs.add(array[i]);
	        array[i] = null;
	        size--;
	        i = (i+1) % capacity;
	    }
	    
	    V value = pairs.remove(0).v; // Remove the key we're deleting.
	    
	    for(Pair<K, V> p : pairs)
	        this.put(p.k, p.v); // Put the rest back in the hashtable.
	    
	    return value;
	}

	/**
     * Resizes the array and copies over the elements if the size is out of bounds.
     * 
     */
    private void resizeIfNeeded(){
        if(!((size<capacity*minFullness && capacity>MIN_CAPACITY) || size>capacity*maxFullness)){
            return;
        }
        int newCapacity = (int) (size/setFullness); // The size of the new array
        
        @SuppressWarnings("unchecked")
		Pair<K, V>[] newArray = (Pair<K, V>[]) new Pair[newCapacity];
        
        for(int j=0; j<capacity; j++){
            Pair<K, V> q = array[j];
            if(q==null)
                continue;
            
            int i = hash(q.k) % newCapacity;
            while (newArray[i]!=null && !q.k.equals(newArray[i].k)) {
                i = (i+1) % newCapacity; // get next index
            }
            newArray[i] = q;
        }
        this.array = newArray;
        this.capacity = newCapacity;
    }
    
	public String toString(){
        return String.format("Hashtable(%.2f, %.2f, %.2f)", maxFullness, minFullness, setFullness);
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.array);
		result = prime * result + this.capacity;
		long temp;
		temp = Double.doubleToLongBits(this.maxFullness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.minFullness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.setFullness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + this.size;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HashtableB))
			return false;
		HashtableB<?, ?> other = (HashtableB<?, ?>) obj;
		if (!Arrays.equals(this.array, other.array))
			return false;
		if (this.capacity != other.capacity)
			return false;
		if (Double.doubleToLongBits(this.maxFullness) != Double
				.doubleToLongBits(other.maxFullness))
			return false;
		if (Double.doubleToLongBits(this.minFullness) != Double
				.doubleToLongBits(other.minFullness))
			return false;
		if (Double.doubleToLongBits(this.setFullness) != Double
				.doubleToLongBits(other.setFullness))
			return false;
		if (this.size != other.size)
			return false;
		return true;
	}
}

// A key-value pair.
class Pair<K, V>{
    K k;
    V v;
    
    public Pair(K key, V val) {
        k = key;
        v = val;
    }

	/* (non-Javadoc) 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.k == null) ? 0 : this.k.hashCode());
		result = prime * result + ((this.v == null) ? 0 : this.v.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Pair))
			return false;
		@SuppressWarnings("unchecked")
		Pair<K, V> other = (Pair<K, V>) obj;
		if (this.k == null) {
			if (other.k != null)
				return false;
		} else if (!this.k.equals(other.k))
			return false;
		if (this.v == null) {
			if (other.v != null)
				return false;
		} else if (!this.v.equals(other.v))
			return false;
		return true;
	}
}

class HashtableBSupplier implements STSupplier {
	private double max; // determines how full the array can get before resizing occurs; default 1/2
    private double min; // determines how empty the array can get before resizing occurs; default 3/4
    private double set; // determines how full the array should be made when resizing; default 1/4
    
    /**
     * Constructs empty {@code HashtableB}'s with the specified {@code maximum}, {@code minimum}, and {@code set} fullness ratios
     *
     * @param  maximum	the maximum fullness
     * @param  minimum	the minimum fullness
     * @param  set		the fullness when the arrays are resized
     * 
     * @see HashtableB
     */
	public HashtableBSupplier(double maximum, double minimum, double set){        
        max = maximum;
        min = minimum;
        this.set = set;
    }
	
	public HashtableBSupplier(double maximum, double minimum){
		this(maximum, minimum, 0.5);
	}
	
	public HashtableBSupplier(){
		this(0.75, 0.30);
	}
	
	public <K extends Comparable<K>, V> ST<K, V> getNew() {
		return new HashtableB<K, V>(max, min, set);
	}
	
	public String toString() {
		if(set == 0.5)
			return String.format("HT-B(%d/%d)", (int) (max*100), (int) (min*100));
		else
			return String.format("HT-B(%d/%d/%d)", (int) (max*100), (int) (min*100), (int) (set*100));
	}
}


/*
 public boolean equals(Objec){
 return k.equals(key);
 }*/

/*
 public HashtableB(double factor, double margin){
 this(factor*(1.0+margin), factor/(1.0+margin), factor);
 }*/

/*
 public static long hash(String key){
 long x = 0;
 for(int i=0; i<key.length(); i++){
 //x = x*257 + key.charAt(0);
 x = x*257 + key.charAt(i);
 }
 return x;
 }*/




/*public boolean checkSize(){
    int x = 0;
    for(int i=0; i<capacity; i++){
        if(array[i] != null)
            x++;
    }
    return x == size;
}*/