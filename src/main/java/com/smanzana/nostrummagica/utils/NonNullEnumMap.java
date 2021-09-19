package com.smanzana.nostrummagica.utils;

import java.util.EnumMap;

public class NonNullEnumMap<K extends Enum<K>, V> extends EnumMap<K,V> {

	private static final long serialVersionUID = 2249448301163273450L;

	public NonNullEnumMap(Class<K> keyType, V fill) {
		super(keyType);
		for (K type : keyType.getEnumConstants()) {
			this.put(type, fill);
		}
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
	
}
