package com.smanzana.nostrummagica.block.dungeon;

import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class DungeonBarsBlock extends PaneBlock {

	public static final String ID = "dungeon_bars";
	
	public DungeonBarsBlock() {
		super(Block.Properties.of(Material.METAL)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.METAL)
				);
	}
	
}