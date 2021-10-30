package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class DungeonBars extends BlockPane {

	public static final String ID = "dungeon_bars";
	
	private static DungeonBars instance = null;
	public static DungeonBars instance() {
		if (instance == null)
			instance = new DungeonBars();
		
		return instance;
	}
	
	public DungeonBars() {
		super(Material.IRON, true);
		this.setSoundType(SoundType.METAL);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setUnlocalizedName(ID);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
}