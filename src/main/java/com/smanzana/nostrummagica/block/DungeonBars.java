package com.smanzana.nostrummagica.block;

import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class DungeonBars extends PaneBlock {

	public static final String ID = "dungeon_bars";
	
	public DungeonBars() {
		super(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.METAL)
				);
	}
	
}