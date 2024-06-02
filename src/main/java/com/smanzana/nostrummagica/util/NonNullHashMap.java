package com.smanzana.nostrummagica.util;

import java.util.HashMap;

public class NonNullHashMap<K, V> extends HashMap<K,V> {

	private static final long serialVersionUID = -5150038759997545128L;

	public NonNullHashMap() {
		super();
	}
	
	public NonNullHashMap(int capacity) {
		new HashMap<>(capacity);
	}
	
	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new IllegalArgumentException("Key to NonNullHashMap cannot be null!");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value to NonNullHashMap cannot be null!");
		}
		
		return super.put(key, value);
	}
	
}
