package com.smanzana.nostrummagica.spells;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;

public class SpellShapePartProperties {
	 public final float level;
	 public final boolean flip;
	 
	 public SpellShapePartProperties(float level, boolean flip) {
		 this.level = level;
		 this.flip = flip;
	 }
	 
	 public SpellShapePartProperties() {
		 this(0f, false);
	 }
	 
	 private static final String NBT_LEVEL = "level";
	 private static final String NBT_FLIP = "flip";
	 
	 public CompoundNBT toNBT(@Nullable CompoundNBT tag) {
		 if (tag == null) {
			 tag = new CompoundNBT();
		 }
		 
		 tag.putFloat(NBT_LEVEL, level);
		 tag.putBoolean(NBT_FLIP, flip);
		 
		 return tag;
	 }
	 
	 public static SpellShapePartProperties FromNBT(CompoundNBT tag) {
		 return new SpellShapePartProperties(
				 tag.getFloat(NBT_LEVEL),
				 tag.getBoolean(NBT_FLIP)
				 );
	 }
}