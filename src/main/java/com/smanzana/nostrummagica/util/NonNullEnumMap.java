package com.smanzana.nostrummagica.util;

import java.util.EnumMap;

public class NonNullEnumMap<K extends Enum<K>, V> extends EnumMap<K,V> {

	private static final long serialVersionUID = 2249448301163273450L;
	
	protected final Class<K> keyClass;
	protected final V defaultValue;

	public NonNullEnumMap(Class<K> keyType, V fill) {
		super(keyType);
		
		this.defaultValue = fill;
		this.keyClass = keyType;
		clear();
	}
	
	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new IllegalArgumentException("Key to NonNullEnumMap cannot be null!");
		}
		if (value == null) {
			throw new IllegalArgumentException("Value to NonNullEnumMap cannot be null!");
		}
		
		return super.put(key, value);
	}
	
	@Override
	public void clear() {
		for (K type : this.keyClass.getEnumConstants()) {
			this.put(type, defaultValue);
		}
	}
	
}
