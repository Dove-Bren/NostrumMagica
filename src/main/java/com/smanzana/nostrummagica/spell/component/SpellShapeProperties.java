package com.smanzana.nostrummagica.spell.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;

public class SpellShapeProperties {

	protected final Map<SpellShapeProperty<?>, Object> properties;
	
	public SpellShapeProperties(Map<SpellShapeProperty<?>, Object> properties) {
		this.properties = properties;
	}
	
	public SpellShapeProperties() {
		this(new HashMap<>());
	}
	
	public <T> SpellShapeProperties addProperty(SpellShapeProperty<T> property) {
		return addProperty(property, property.getDefault());
	}
	
	public <T> SpellShapeProperties addProperty(SpellShapeProperty<T> property, T startingValue) {
		this.properties.put(property, startingValue);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(SpellShapeProperty<T> property) {
		return (T) this.properties.computeIfAbsent(property, p -> p.getDefault());
	}
	
	public <T> boolean hasValue(SpellShapeProperty<T> property) {
		return this.properties.containsKey(property);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T setValue(SpellShapeProperty<T> property, T value) {
		return (T) this.properties.put(property, value);
	}
	
	public Set<SpellShapeProperty<?>> getProperties() {
		return this.properties.keySet();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> void writeRow(CompoundNBT nbt, SpellShapeProperty<T> property, Object value) {
		nbt.put(property.getName(), property.writeValue((T)value));
	}
	
	public CompoundNBT toNBT() {
		CompoundNBT nbt = new CompoundNBT();
		for (Entry<SpellShapeProperty<?>, Object> row : this.properties.entrySet()) {
			writeRow(nbt, row.getKey(), row.getValue());
		}
		return nbt;
	}
	
	protected SpellShapeProperty<?> findProperty(String name) {
		for (SpellShapeProperty<?> property : this.properties.keySet()) {
			if (property.getName().equalsIgnoreCase(name)) {
				return property;
			}
		}
		return null;
	}
	
	public SpellShapeProperties fromNBT(CompoundNBT nbt) {
		this.properties.clear();
		for (String key : nbt.keySet()) {
			SpellShapeProperty<?> property = findProperty(key);
			if (property != null) {
				properties.put(property, property.readValue(nbt.get(key)));
			}
		}
		return this;
	}
	
	public SpellShapeProperties copy() {
		return new SpellShapeProperties(new HashMap<>(this.properties));
	}
}