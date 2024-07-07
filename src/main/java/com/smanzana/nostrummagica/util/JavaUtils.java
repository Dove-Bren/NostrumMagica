package com.smanzana.nostrummagica.util;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;

/**
 * Utilities for java basic classes
 * @author Skyler
 *
 */
public class JavaUtils {

	public static final <T> Optional<T> GetRandom(Collection<T> collection) {
		return GetRandom(collection, new Random());
	}
	
	public static final <T> Optional<T> GetRandom(Collection<T> collection, Random rand) {
		return collection.stream().skip((long) (rand.nextDouble() * collection.size())).findFirst();
	}
	
}
