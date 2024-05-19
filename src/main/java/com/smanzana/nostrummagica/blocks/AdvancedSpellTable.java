package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tiles.AdvancedSpellTableEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class AdvancedSpellTable extends BasicSpellTable {
	
	public static final String ID = "spelltable_advanced";
	
	public AdvancedSpellTable() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.notSolid()
				);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AdvancedSpellTableEntity();
	}
	
	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Blackstone Spell Table";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("A reinforced spell table with room or many runes.", "An experienced spell crafter can use this table to make powerful spells!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("A reinforced spell table with room or many runes.", "An experienced spell crafter can use this table to make powerful spells!");
	}
}
