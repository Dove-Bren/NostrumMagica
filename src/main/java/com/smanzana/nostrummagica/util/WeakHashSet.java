package com.smanzana.nostrummagica.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet<T> implements Set<T> {
	
	private final WeakHashMap<T, Boolean> table;
	
	public WeakHashSet() {
		table = new WeakHashMap<>();
	}
	
	public WeakHashSet(int capacity) {
		table = new WeakHashMap<>(capacity);
	}

	@Override
	public boolean add(T e) {
		return table.putIfAbsent(e, true) == null;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean modified = false;
		for (T elem : c) {
			modified |= add(elem);
		}
		return modified;
	}

	@Override
	public void clear() {
		table.clear();
	}

	@Override
	public boolean contains(Object o) {
		return table.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return table.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return table.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		return table.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Object o : c) {
			modified |= remove(o);
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<T> it = table.keySet().iterator();
		while (it.hasNext()) {
			T elem = it.next();
			if (!c.contains(elem)) {
				it.discard();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public int size() {
		return table.size();
	}

	@Override
	public Object[] toArray() {
		return table.keySet().toArray();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		return table.keySet().toArray(a);
	}

}
