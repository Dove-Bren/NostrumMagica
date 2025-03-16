package com.smanzana.nostrummagica.block.dungeon;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class DungeonBarsBlock extends IronBarsBlock {

	public static final String ID = "dungeon_bars";
	
	public DungeonBarsBlock() {
		super(Block.Properties.of(Material.METAL)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.METAL)
				);
	}
	
}