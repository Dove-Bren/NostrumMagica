package com.smanzana.nostrummagica.spell.component;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.nbt.CompoundTag;

/**
 * A shape part of a spell, including any properties this instance may have.
 * @author Skyler
 *
 */
public class SpellShapePart {

	protected final SpellShape shape;
	protected final SpellShapeProperties properties;
	
	public SpellShapePart(SpellShape shape, SpellShapeProperties properties) {
		this.shape = shape;
		this.properties = properties;
	}
	
	public SpellShapePart(SpellShape shape) {
		this(shape, shape.getDefaultProperties());
	}

	public SpellShape getShape() {
		return shape;
	}

	public SpellShapeProperties getProperties() {
		return properties;
	}
	
	private static final String NBT_SHAPE = "shape";
	private static final String NBT_PROPS = "properties";
	
	public CompoundTag toNBT(@Nullable CompoundTag tag) {
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		tag.putString(NBT_SHAPE, this.getShape().getShapeKey());
		tag.put(NBT_PROPS, properties.toNBT());
		
		return tag;
	}
	
	public static SpellShapePart FromNBT(CompoundTag tag) {
		SpellShape shape = SpellShape.get(tag.getString(NBT_SHAPE));
		if (shape == null) {
			shape = SpellShape.getAllShapes().iterator().next();
		}
		
		return new SpellShapePart(shape, shape.getDefaultProperties().fromNBT(tag.getCompound(NBT_PROPS)));
	}
	
}
