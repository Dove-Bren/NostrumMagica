package com.smanzana.nostrummagica.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class DungeonBlock extends Block {
	
	public static enum Type {
		LIGHT,
		DARK;

		public String getName() {
			return name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	private static final String ID_PREFIX = "dungeon_block_";
	public static final String ID_LIGHT = ID_PREFIX + "light";
	public static final String ID_DARK = ID_PREFIX + "dark";
	
	public final Type type;
	
	public DungeonBlock(Type type) {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				);
		this.type = type;
	}
}
