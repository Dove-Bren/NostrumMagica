package com.smanzana.nostrummagica.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MemoryPool<T> {
	
	public static interface Allocator<T> {
		public @Nonnull T construct();
	}
	
	protected final Allocator<T> allocator;
	protected final List<T> pool;
	protected final List<T> loaned;
	
	public MemoryPool(Allocator<T> allocator) {
		this(allocator, 16);
	}
	
	public MemoryPool(Allocator<T> allocator, int capacity) {
		this.allocator = allocator;
		loaned = new ArrayList<>(capacity);
		pool = new ArrayList<>(capacity);
		this.grow(capacity);
	}
	
	protected void grow() {
		grow(Math.max(1, loaned.size()));
	}
	
	protected void grow(int amount) {
		// Add `amount` new items to the pool
		for (int i = 0; i < amount; i++) {
			pool.add(allocator.construct());
		}
	}
	
	public T claim() {
		if (pool.isEmpty()) {
			grow();
		}
		
		T obj = pool.get(0);
		loaned.add(obj);
		return obj;
	}
	
	public void release(T obj) {
		if (!loaned.remove(obj)) {
			throw new RuntimeException("Attempted to release a non-pooled object!");
		}
		
		pool.add(obj);
	}
}
