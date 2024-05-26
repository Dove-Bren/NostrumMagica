package com.smanzana.nostrummagica.spells.components;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

import net.minecraft.nbt.CompoundNBT;

/**
 * A shape part of a spell, including any properties this instance may have.
 * @author Skyler
 *
 */
public class SpellShapePart {

	protected final SpellShape shape;
	protected final SpellShapePartProperties properties;
	
	public SpellShapePart(SpellShape shape, SpellShapePartProperties properties) {
		this.shape = shape;
		this.properties = properties;
	}

	public SpellShape getShape() {
		return shape;
	}

	public SpellShapePartProperties getProperties() {
		return properties;
	}
	
	private static final String NBT_SHAPE = "shape";
	private static final String NBT_PROPS = "properties";
	
	public CompoundNBT toNBT(@Nullable CompoundNBT tag) {
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		tag.putString(NBT_SHAPE, this.getShape().getShapeKey());
		tag.put(NBT_PROPS, properties.toNBT(null));
		
		return tag;
	}
	
	public static SpellShapePart FromNBT(CompoundNBT tag) {
		SpellShape shape = SpellShape.get(tag.getString(NBT_SHAPE));
		if (shape == null) {
			shape = SpellShape.getAllShapes().iterator().next();
		}
		
		return new SpellShapePart(shape, SpellShapePartProperties.FromNBT(tag.getCompound(NBT_PROPS)));
	}
	
}
