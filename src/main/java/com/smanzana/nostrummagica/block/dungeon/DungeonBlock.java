package com.smanzana.nostrummagica.block.dungeon;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

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
	private static final String ID_STAIR_PREFIX = "dungeon_block_stair_";
	public static final String ID_LIGHT = ID_PREFIX + "light";
	public static final String ID_DARK = ID_PREFIX + "dark";
	public static final String ID_STAIR_LIGHT = ID_STAIR_PREFIX + "light";
	public static final String ID_STAIR_DARK = ID_STAIR_PREFIX + "dark";
	
	public final Type type;
	
	public DungeonBlock(Type type) {
		super(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE));
		this.type = type;
	}
}
